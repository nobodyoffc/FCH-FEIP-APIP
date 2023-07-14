package APIP0V1_OpenAPI;

import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import javaTools.BytesTools;
import cryptoTools.SHA;
import keyTools.KeyTools;
import initial.Initiator;
import order.Order;
import org.bitcoinj.core.ECKey;
import redis.clients.jedis.Jedis;
import constants.Strings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static constants.ApiNames.APIP0V1Path;
import static constants.Strings.*;
import static initial.Initiator.serviceName;
import static redisTools.ReadRedis.readHashLong;

public class RequestChecker {
    private static final Jedis jedis1 = Initiator.jedis1Session;
    private static final Jedis jedis0 = Initiator.jedis0Common;
    private static final Jedis jedis2 = Initiator.jedis2Nonce;
    private long price;
    private final PrintWriter writer;
    private final HttpServletResponse response;
    private final HttpServletRequest request;
    private final Gson gson = Initiator.gson;

    private long windowTime;
    private String fid = null;
    private String pubKey = null;
    private String sessionKey;
    private final Replier replier;
    private byte[] requestBodyBytes;


    public RequestChecker(HttpServletRequest request, HttpServletResponse response, Replier replier) throws IOException {

        try {
            windowTime = Long.parseLong(jedis0.hget(CONFIG, WINDOW_TIME));
        }catch (Exception e){
            windowTime = 5000;
        }
        response.reset();
        response.setContentType("application/json;charset=utf-8");

        this.replier = replier;
        this.request = request;
        this.response = response;
        this.writer = response.getWriter();
        this.response.setContentType("application/json;charset=utf-8");
        this.replier.setData(null);
    }

    public static boolean checkPublicSessionKey(HttpServletResponse response, Replier replier, PrintWriter writer, String addr) {
        if(PUBLIC.equals(addr)){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("This session key isn't allowed to request this API.");
            writer.write(replier.reply1020OtherError(addr));
            return true;
        }
        return false;
    }

    public SignInCheckResult checkSignInRequest() throws IOException, SignatureException {

        SignInCheckResult signInCheckResult = new SignInCheckResult();
        price = Initiator.readPrice();

        String sign = request.getHeader(ReplyInfo.SignInHeader);
        if(sign==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1000SignMissed));
            String urlHead = jedis0.hget(serviceName+PARAMS,URL_HEAD);
            String data = "FID and Sign are required in request header.";
            SecureRandom secureRandom = new SecureRandom();
            byte[] bytes = new byte[4];
            secureRandom.nextBytes(bytes);
            int nonce = ByteBuffer.wrap(bytes).getInt();
            if(nonce<0)nonce=(-nonce);
            long timestamp = System.currentTimeMillis();
            if(urlHead!=null) {
                data = """
                        A signature are requested:
                        \tRequest header:
                        \t\tFID = <Freecash address of the requester>
                        \t\tSign = <The signature of request body signed by the private key of the FID.>
                        \tRequest body:{"url":"%s","nonce":"%d","time":"%d"}""".formatted(urlHead+APIP0V1Path.substring(1) + ApiNames.SignInAPI,nonce,timestamp);
            }
            replier.setData(data);
            writer.write(this.replier.reply1000MissSign());
            replier.setData(null);
            return null;
        }

        String fid = request.getHeader(ReplyInfo.FidInHeader);
        if(fid==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1015FidMissed));
            writer.write(this.replier.reply1015FidMissed());
            return null;
        }
        signInCheckResult.setFid(fid);
        this.fid = fid;

        requestBodyBytes = getRequestBodyBytes(request);
        if(requestBodyBytes==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1003BodyMissed));
            writer.write(this.replier.reply1003MissBody(fid));
            return null;
        }

        SignInRequestBody signInRequestBody = getSignInRequestBody(requestBodyBytes);
        if(signInRequestBody==null)return null;

        if(!isGoodAsySign(sign)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1008BadSign));
            writer.write(this.replier.reply1008BadSign(fid));
            return null;
        }

        if(isBadBalance()){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1004InsufficientBalance));

            String buyJson = gson.toJson(Order.getJsonBuyOrder(jedis0.get(serviceName+Strings.SID)));
            replyInsufficientBalance(buyJson, fid);
            return null;
        }

        if(isBadNonce(signInRequestBody.getNonce())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1007UsedNonce));
            writer.write(this.replier.reply1007UsedNonce(fid));
            return null;
        }
        this.replier.setNonce(signInRequestBody.getNonce());

        if(isBadUrl(signInRequestBody.getUrl())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1005UrlUnequal));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("requestedURL",request.getRequestURL().toString());
            dataMap.put("signedURL",signInRequestBody.getUrl());
            this.replier.setData(dataMap);
            writer.write(this.replier.reply1005UrlUnequal(fid));
            return null;
        }

        if(isBadTime(signInRequestBody.getTime())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1006RequestTimeExpired));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("windowTime", String.valueOf(windowTime));
            this.replier.setData(dataMap);
            writer.write(this.replier.reply1006RequestTimeExpired(fid));
            return null;
        }

        signInCheckResult.setSignInRequestBody(signInRequestBody);
        return signInCheckResult;
    }

    private boolean isGoodAsySign(String sign) throws SignatureException {
        String message = new String(requestBodyBytes);

        sign = sign.replace("\\u003d", "=");

        String signPubKey = ECKey.signedMessageToKey(message, sign).getPublicKeyAsHex();

        String signFid = KeyTools.pubKeyToFchAddr(signPubKey);

        return signFid.equals(fid);
    }
    private SignInRequestBody getSignInRequestBody(byte[] requestBodyBytes) {
        String requestDataJson = new String(requestBodyBytes);
        SignInRequestBody connectRequestBody;
        try {
            connectRequestBody = gson.fromJson(requestDataJson, SignInRequestBody.class);
        }catch(Exception e){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1013BadRequest));
            writer.write(replier.reply1013BadRequst(fid));
            return null;
        }
        return connectRequestBody;
    }

    public DataCheckResult checkDataRequest() throws IOException {

        DataCheckResult dataCheckResult = new DataCheckResult();

        String sign = request.getHeader(ReplyInfo.SignInHeader);

        if(sign==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1000SignMissed));
            writer.write(replier.reply1000MissSign());
            return null;
        }

        String sessionName = request.getHeader(ReplyInfo.SessionNameInHeader);
        if(sessionName==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1002SessionNameMissed));
            writer.write(replier.reply1002MissSessionName());
            return null;
        }

        Session session = getSession(sessionName);
        if(session ==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1009SessionTimeExpired));
            writer.write(replier.reply1009SessionTimeExpired());
            return null;
        }

        fid = session.getFid();
        sessionKey = session.getSessionKey();

        dataCheckResult.setSessionName(sessionName);
        dataCheckResult.setSessionKey(sessionKey);
        dataCheckResult.setAddr(fid);

        if(isBadBalance()){

            String buyJson = gson.toJson(Order.getJsonBuyOrder(jedis0.get(Strings.SID)));
            replyInsufficientBalance(buyJson, fid);

            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1004InsufficientBalance));
            return null;
        }

        requestBodyBytes = getRequestBodyBytes(request);
        if(requestBodyBytes==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1003BodyMissed));
            writer.write(replier.reply1003MissBody(fid));
            return null;
        }

        if(!isGoodSymSign(sign)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1008BadSign));
            writer.write(replier.reply1008BadSign(fid));
            return null;
        }

        DataRequestBody dataRequestBody = getDataRequestBody(requestBodyBytes);

        assert dataRequestBody != null;

        replier.setNonce(dataRequestBody.getNonce());
        if(dataRequestBody.getVia()!=null)replier.setVia(dataRequestBody.getVia());

        if(isBadNonce(dataRequestBody.getNonce())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1007UsedNonce));
            writer.write(replier.reply1007UsedNonce(fid));
            return null;
        }

        if(isBadUrl(dataRequestBody.getUrl())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1005UrlUnequal));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("requestedURL",request.getRequestURL().toString());
            dataMap.put("signedURL",dataRequestBody.getUrl());
            replier.setData(dataMap);
            writer.write(replier.reply1005UrlUnequal(fid));
            return null;
        }

        if(isBadTime(dataRequestBody.getTime())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1006RequestTimeExpired));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("windowTime", String.valueOf(windowTime));
            replier.setData(dataMap);
            writer.write(replier.reply1006RequestTimeExpired(fid));
            return null;
        }

        dataCheckResult.setDataRequestBody(dataRequestBody);
        return dataCheckResult;
    }

    private void replyInsufficientBalance(String buyJson, String fid) {
        String account = Initiator.service.getParams().getAccount();
        Map<String,String> dataMap = new HashMap<>();
        dataMap.put("account",account);
        dataMap.put("WriteInOpReturn",buyJson);
        this.replier.setData(dataMap);
        writer.write(this.replier.reply1004InsufficientBalance(fid));
    }

    public Session getSession(String sessionName) {

        String fid = jedis1.hget(sessionName,"fid");
        String sessionKey = jedis1.hget(sessionName,"sessionKey");
        if(fid==null || sessionKey ==null){
            return null;
        }
        Session session = new Session();
        session.setFid(fid);
        session.setSessionKey(sessionKey);
        session.setSessionName(sessionName);
        return session;
    }
    private boolean isGoodSymSign(String sign) {
        if(sign==null)return false;
        byte[] signBytes = BytesTools.bytesMerger(requestBodyBytes, BytesTools.hexToByteArray(sessionKey));
        String doubleSha256Hash = HexFormat.of().formatHex(SHA.Sha256x2(signBytes));

        if(!sign.equals(doubleSha256Hash)){
            replier.setData(doubleSha256Hash);
            return false;
        }
        return true;
    }
    private byte[] getRequestBodyBytes(HttpServletRequest request) throws IOException {
        return request.getInputStream().readAllBytes();
    }
    private DataRequestBody getDataRequestBody(byte[] requestBodyBytes) {

        String requestDataJson = new String(requestBodyBytes);

        DataRequestBody dataRequestBody;
        try {
            dataRequestBody = gson.fromJson(requestDataJson, DataRequestBody.class);
        }catch(Exception e){
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1013BadRequest));
            replier.setData(e);
            writer.write(replier.reply1013BadRequst(fid));
            return null;
        }

        return dataRequestBody;
    }
    public boolean isBadNonce(long nonce){
        if(nonce == 0)return true;
        String nonceStr = String.valueOf(nonce);
        if(jedis2.get(nonceStr)!=null)
            return true;
        jedis2.set(nonceStr,"");
        jedis2.expire(nonceStr,windowTime);/**/
        return false;
    }
    public boolean isBadBalance(){
        long balance = readHashLong(jedis0, Strings.USER, fid);

        return balance < price;
    }
    public boolean isBadUrl(String signedUrl){
        return !request.getRequestURL().toString().equals(signedUrl);
    }
    public boolean isBadTime(long time){
        windowTime = Long.parseLong(jedis0.hget(CONFIG,WINDOW_TIME));
        return Math.abs(System.currentTimeMillis() - time) > windowTime;
    }


    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }
}
