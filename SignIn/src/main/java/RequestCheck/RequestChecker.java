package RequestCheck;

import SignIn.Session;
import SignIn.SignInCheckResult;
import SignIn.SignInRequestBody;
import com.google.gson.Gson;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static RequestCheck.Constant.*;
import static Tools.KeyTools.*;

public class RequestChecker {
    private static Jedis jedis0 = new Jedis();
    private static Jedis jedis1 = new Jedis();
    static {
        jedis1.select(1);
    }
    private static Jedis jedis2 = new Jedis();
    static {
        jedis2.select(2);
    }
    private final PrintWriter writer;
    private final HttpServletResponse response;
    private final String WINDOW_TIME = "windowTime";
    private long windowTime = Long.parseLong(jedis0.get(WINDOW_TIME));
    private HttpServletRequest request;
    private String fid = null;
    private String pubKey = null;
    private String sessionKey;
    private Replier replier = new Replier();
    private byte[] requestBodyBytes;


    public RequestChecker(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.reset();
        response.setContentType("application/json;charset=utf-8");

        this.request = request;
        this.response = response;
        this.writer = response.getWriter();
        this.response.setContentType("application/json;charset=utf-8");
    }

    public SignInCheckResult checkSignInRequest(Replier replier) throws IOException, SignatureException {
        this.replier=replier;
        SignInCheckResult signInCheckResult = new SignInCheckResult();

        String sign = request.getHeader(SignInHeader);

        if(sign==null){
            response.setHeader(CodeInHeader,String.valueOf(1000));
            writer.write(this.replier.reply1000MissSign());
            return null;
        }

        requestBodyBytes = getRequestBodyBytes(request);
        if(requestBodyBytes==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1003MissBody));
            writer.write(this.replier.reply1003MissBody(fid));
            return null;
        }

        SignInRequestBody signInRequestBody = getSignInRequestBody(requestBodyBytes);
        if(signInRequestBody==null)return null;

        pubKey = signInRequestBody.getPubKey();
        signInCheckResult.setPubKey(pubKey);

        if(pubKey==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1001MissPubKey));
            writer.write(this.replier.reply1001MissPubKey());
            return null;
        }

        fid = pubKeyToFchAddr(pubKey);
        signInCheckResult.setFid(fid);

        if(!isGoodNonce(signInRequestBody.getNonce())){
            response.setHeader(CodeInHeader,String.valueOf(Code1007UsedNonce));
            writer.write(this.replier.reply1007UsedNonce(fid));
            return null;
        }
        this.replier.setNonce(signInRequestBody.getNonce());

        if(!isGoodUrl(signInRequestBody.getUrl())){
            response.setHeader(CodeInHeader,String.valueOf(Code1005UrlUnequal));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("requestedURL",request.getRequestURL().toString());
            dataMap.put("signedURL",signInRequestBody.getUrl());
            this.replier.setData(dataMap);
            writer.write(this.replier.reply1005UrlUnequal(fid));
            return null;
        }

        if(!isGoodTime(signInRequestBody.getTime())){
            response.setHeader(CodeInHeader,String.valueOf(Code1006RequestTimeExpired));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("windowTime", String.valueOf(windowTime));
            this.replier.setData(dataMap);
            writer.write(this.replier.reply1006RequestTimeExpired(fid));
            return null;
        }

        if(!isGoodAsySign(sign)){
            response.setHeader(CodeInHeader,String.valueOf(Code1008BadSign));
            writer.write(this.replier.reply1008BadSign(fid));
            return null;
        }

        signInCheckResult.setSignInRequestBody(signInRequestBody);
        return signInCheckResult;
    }
    private boolean isGoodAsySign(String sign) throws SignatureException {
        String message = new String(requestBodyBytes);

        sign = sign.replace("\\u003d", "=");

        String signPubKey = ECKey.signedMessageToKey(message, sign).getPublicKeyAsHex();

        return signPubKey.equals(pubKey);
    }
    private SignInRequestBody getSignInRequestBody(byte[] requestBodyBytes) {
        Gson gson = new Gson();
        String requestDataJson = new String(requestBodyBytes);
        SignInRequestBody connectRequestBody;
        try {
            connectRequestBody = gson.fromJson(requestDataJson, SignInRequestBody.class);
        }catch(Exception e){
            response.setHeader(CodeInHeader,String.valueOf(Code1013BadRequest));
            writer.write(replier.reply1013BadRequst(fid));
            return null;
        }
        return connectRequestBody;
    }

    public DataCheckResult checkDataRequest() throws IOException {

        DataCheckResult dataCheckResult = new DataCheckResult();

        String sign = request.getHeader(SignInHeader);

        if(sign==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1000MissSign));
            writer.write(replier.reply1000MissSign());
            return null;
        }

        String sessionName = request.getHeader(SessionNameInHeader);
        if(sessionName==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1002MissSessionName));
            writer.write(replier.reply1002MissSessionName());
            return null;
        }

        Session session = getSession(sessionName);
        if(session ==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1009SessionTimeExpired));
            writer.write(replier.reply1009SessionTimeExpired());
            return null;
        }

        fid = session.getFid();
        sessionKey = session.getSessionKey();

        dataCheckResult.setSessionName(sessionName);
        dataCheckResult.setSessionKey(sessionKey);
        dataCheckResult.setAddr(fid);

        requestBodyBytes = getRequestBodyBytes(request);
        if(requestBodyBytes==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1003MissBody));
            writer.write(replier.reply1003MissBody(fid));
            return null;
        }

        DataRequestBody dataRequestBody = getDataRequestBody(requestBodyBytes);
        //TODO
        if(!isGoodNonce(dataRequestBody.getNonce())){
        //if(false){
            response.setHeader(CodeInHeader,String.valueOf(Code1007UsedNonce));
            writer.write(replier.reply1007UsedNonce(fid));
            return null;
        }

        if(!isGoodUrl(dataRequestBody.getUrl())){
            response.setHeader(CodeInHeader,String.valueOf(Code1005UrlUnequal));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("requestedURL",request.getRequestURL().toString());
            dataMap.put("signedURL",dataRequestBody.getUrl());
            replier.setData(dataMap);
            writer.write(replier.reply1005UrlUnequal(fid));
            return null;
        }

        if(!isGoodTime(dataRequestBody.getTime())){
            response.setHeader(CodeInHeader,String.valueOf(Code1006RequestTimeExpired));
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("windowTime", String.valueOf(windowTime));
            replier.setData(dataMap);
            writer.write(replier.reply1006RequestTimeExpired(fid));
            return null;
        }
//TODO
        if(!isGoodSymSign(sign)){
        //if(false){
            response.setHeader(CodeInHeader,String.valueOf(Code1008BadSign));
            writer.write(replier.reply1008BadSign(fid));
            return null;
        }

        dataCheckResult.setDataRequestBody(dataRequestBody);
        return dataCheckResult;
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
        byte[] signBytes = bytesMerger(requestBodyBytes, hexToByteArray(sessionKey));
        String doubleSha256Hash = HexFormat.of().formatHex(Sha256Hash.hashTwice(signBytes));

        if(!sign.equals(doubleSha256Hash)){
            replier.setData(doubleSha256Hash);
            return false;
        }
        return true;
    }
    private byte[] getRequestBodyBytes(HttpServletRequest request) throws IOException {
        byte[] requestBodyBytes = request.getInputStream().readAllBytes();
        return requestBodyBytes;
    }
    private DataRequestBody getDataRequestBody(byte[] requestBodyBytes) throws IOException {
        Gson gson = new Gson();
        String requestDataJson = new String(requestBodyBytes);

        DataRequestBody dataRequestBody;
        try {
            dataRequestBody = gson.fromJson(requestDataJson, DataRequestBody.class);
        }catch(Exception e){
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1013BadRequest));
            replier.setData(e);
            writer.write(replier.reply1013BadRequst(fid));
            return null;
        }

        return dataRequestBody;
    }
    public boolean isGoodNonce(long nonce){
        if(nonce == 0)return false;
        String nonceStr = String.valueOf(nonce);
        if(jedis2.get(nonceStr)!=null)
            return false;
        jedis2.set(nonceStr,"");
        jedis2.expire(nonceStr,windowTime);/**/
        return true;
    }
    public boolean isGoodUrl(String signedUrl){
        if(!request.getRequestURL().toString().equals(signedUrl)){
            return false;
        }
        return true;
    }
    public boolean isGoodTime(long time){
        windowTime = Long.parseLong(jedis0.get("windowTime"));
        if(Math.abs(System.currentTimeMillis()- time)> windowTime){
            return false;
        }
        return true;
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
