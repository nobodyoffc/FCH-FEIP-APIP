package APIP0V1_OpenAPI;

import apipClass.DataRequestBody;
import apipClass.SignInRequestBody;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import cryptoTools.SHA;
import initial.Initiator;
import initial.ServerParamsInRedis;
import javaTools.BytesTools;
import keyTools.KeyTools;
import order.Order;
import org.bitcoinj.core.ECKey;
import redis.clients.jedis.Jedis;
import tools.ApipTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static constants.ApiNames.APIP0V1Path;
import static constants.Strings.PUBLIC;

public class RequestChecker {
//    private final Jedis jedis0;

    private final PrintWriter writer;
    private final HttpServletResponse response;
    private final HttpServletRequest request;
    private final Gson gson;
    private final String url;

//    private long windowTime;
//    private long price;
//    private long balance;

    private final String apiName;
    private String fid = null;
    private String pubKey = null;
    private String sessionKey;
    private final Replier replier;
    private byte[] requestBodyBytes;
    private transient ServerParamsInRedis paramsInRedis;


    public RequestChecker(HttpServletRequest request, HttpServletResponse response, Replier replier) throws IOException {

        this.replier = replier;
        this.request = request;
        this.response = response;
        this.writer = response.getWriter();
        this.url = request.getRequestURL().toString();
        this.apiName = ApipTools.getApiNameFromUrl(url);
        this.replier.setData(null);
        this.gson = new Gson();
    }

    public SignInCheckResult checkSignInRequest() throws IOException, SignatureException {

        SignInCheckResult signInCheckResult = new SignInCheckResult();

        String url = request.getRequestURL().toString();
        if(illegalUrl(url)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1016IllegalUrl));
            writer.write(this.replier.reply1016IllegalUrl());
            return null;
        }

        String fid = request.getHeader(ReplyInfo.FidInHeader);
        if(fid==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1015FidMissed));
            String data = "A FID is required in request header.";
            replier.setData(data);
            writer.write(this.replier.reply1015FidMissed());
            return null;
        }
        signInCheckResult.setFid(fid);
        this.fid = fid;



        String sign = request.getHeader(ReplyInfo.SignInHeader);
        if(sign==null||"".equals(sign)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1000SignMissed));
            String urlHead = Initiator.params.getUrlHead();
            String data = "A Sign is required in request header.";
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

        requestBodyBytes = getRequestBodyBytes(request);
        if(requestBodyBytes==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1003BodyMissed));
            writer.write(this.replier.reply1003MissBody(fid));
            return null;
        }

        SignInRequestBody signInRequestBody = getSignInRequestBody(requestBodyBytes);
        if(signInRequestBody==null)return null;

        if(isBadNonce(signInRequestBody.getNonce())){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1007UsedNonce));
            this.replier.setNonce(signInRequestBody.getNonce());
            writer.write(this.replier.reply1007UsedNonce(fid));
            return null;
        }
        this.replier.setNonce(signInRequestBody.getNonce());

        if(!isGoodAsySign(sign)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1008BadSign));
            writer.write(this.replier.reply1008BadSign(fid));
            return null;
        }

        this.paramsInRedis = new ServerParamsInRedis(fid, apiName);
        replier.setParamsInRedis(paramsInRedis);


        if(isBadBalance()){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1004InsufficientBalance));

            String buyJson = gson.toJson(Order.getJsonBuyOrder(Initiator.service.getSid()));
            replyInsufficientBalance(buyJson, fid);
            return null;
        }

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
            dataMap.put("windowTime", String.valueOf(paramsInRedis.getWindowTime()));
            this.replier.setData(dataMap);
            writer.write(this.replier.reply1006RequestTimeExpired(fid));
            return null;
        }

        signInCheckResult.setSignInRequestBody(signInRequestBody);
        return signInCheckResult;
    }

    public static boolean isPublicSessionKey(HttpServletResponse response, Replier replier, PrintWriter writer, String addr) {
        if(PUBLIC.equals(addr)){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("This session key isn't allowed to request this API.");
            writer.write(replier.reply1020OtherError(addr));
            return true;
        }
        return false;
    }

    public boolean illegalUrl(String url){
        try {
            URI uri = new URI(url);
            uri.toURL();
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
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

        if(illegalUrl(url)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1016IllegalUrl));
            writer.write(this.replier.reply1016IllegalUrl());
            return null;
        }
        if("".equals(apiName)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("No such API. Check the API name in request url.");
            writer.write(replier.reply1020OtherError());
            return null;
        }

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
        this.paramsInRedis = new ServerParamsInRedis(fid, apiName);
        replier.setParamsInRedis(paramsInRedis);
        sessionKey = session.getSessionKey();

        dataCheckResult.setSessionName(sessionName);
        dataCheckResult.setSessionKey(sessionKey);
        dataCheckResult.setAddr(fid);

        if(isBadBalance()){

            String buyJson = gson.toJson(Order.getJsonBuyOrder(Initiator.service.getSid()));
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
        DataRequestBody dataRequestBody = getDataRequestBody(requestBodyBytes);

        assert dataRequestBody != null;

        replier.setNonce(dataRequestBody.getNonce());

        if(!isGoodSymSign(sign)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1008BadSign));
            writer.write(replier.reply1008BadSign(fid));
            return null;
        }

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
            dataMap.put("windowTime", String.valueOf(paramsInRedis.getWindowTime()));
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

        Jedis jedis1 = new Jedis();
        jedis1.select(1);

        String fid = jedis1.hget(sessionName,"fid");
        String sessionKey = jedis1.hget(sessionName,"sessionKey");
        if(fid==null || sessionKey ==null){
            return null;
        }
        Session session = new Session();
        session.setFid(fid);
        session.setSessionKey(sessionKey);
        session.setSessionName(sessionName);
        jedis1.close();
        return session;
    }
    private boolean isGoodSymSign(String sign) {
        if(sign==null)return false;
        byte[] signBytes = BytesTools.bytesMerger(requestBodyBytes, BytesTools.hexToByteArray(sessionKey));
        String doubleSha256Hash = HexFormat.of().formatHex(SHA.Sha256x2(signBytes));

        if(!sign.equals(doubleSha256Hash)){
            replier.setData("The sign of the request body should be: "+doubleSha256Hash);
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
        Jedis jedis2 = new Jedis();
        jedis2.select(2);
        if(nonce == 0)return true;
        String nonceStr = String.valueOf(nonce);
        if(jedis2.get(nonceStr)!=null)
            return true;
        jedis2.set(nonceStr,"");
        jedis2.expire(nonceStr,paramsInRedis.getWindowTime());
        jedis2.close();
        return false;
    }
    public boolean isBadBalance(){
            return paramsInRedis.getBalance() < paramsInRedis.getPrice();
    }
    public boolean isBadUrl(String signedUrl){
        return !request.getRequestURL().toString().equals(signedUrl);
    }
    public boolean isBadTime(long time){
        return Math.abs(System.currentTimeMillis() - time) > paramsInRedis.getWindowTime();
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
