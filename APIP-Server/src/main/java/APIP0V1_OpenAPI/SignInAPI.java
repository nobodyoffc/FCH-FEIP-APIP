package APIP0V1_OpenAPI;

import AesEcc.ECIES;
import AesEcc.EXPList;
import service.ApipService;
import javaTools.BytesTools;
import keyTools.KeyTools;
import initial.Initiator;
import redis.clients.jedis.Jedis;
import service.Params;
import startAPIP.RedisKeys;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


import static api.Constant.SignInAPI;
import static api.Constant.*;


/**
 * 1. 验证请求
 *    1）公钥地址是否有余额：不在返回1000
 *    2）验证url是否当前请求url，不在返回1001
 *    3）时间戳是否在窗口期：不在返回1002
 *    4）验证签名，失败返回1003.
 * 2. 分配session
 *    1) sessionKey: 32字节随机数
 *    2) sessionName: sessionKey的两次sha256值的hex字符串的后12个字符
 *    3）startTime: 当前时间戳，到毫秒。
 *    4) fid: 请求者的FCH地址。
 * 3. 替代session
 *    1) 在redis0由fid找到旧的sessionName，
 *    2) 在redis1将旧session删除
 *    3）将新session写入redis1
 *    4) 将新sessionName写入redis0对应fid
 * 4. 响应
 *    1）用请求者pubKey加密sessionKey得到sessionKeyEncrypted。
 *    2）将sessionKeyEncrypted、sessionDays和startTime响应给请求者。
 * 5. 扣费
 *    所有响应均按照service公布的pricePerRequest扣除fid所对应的balance。
 * */

@WebServlet(APIP0V1Path + SignInAPI)
public class SignInAPI extends HttpServlet {
    private static Jedis jedis1 = Initiator.jedis1Session;
    private static Jedis jedis0 = Initiator.jedis0Common;
    private static ApipService service = Initiator.service;
    private static Params params = new Params();
    private static long price = Initiator.price;
    private String fid = null;
    private String pubKey = null;
    private Replier replier = new Replier();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response);

        SignInCheckResult signInCheckResult;
        try {
            signInCheckResult = requestChecker.checkSignInRequest(replier);
        } catch (SignatureException e) {
            e.printStackTrace();
            return;
        }

        fid = signInCheckResult.getFid();
        pubKey = signInCheckResult.getPubKey();

        SignInReplyData signInReplyData = null;

        try {
            signInReplyData = makeSession();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        response.setHeader(CodeInHeader,String.valueOf(Code0Success));
        replier.setGot(1);
        replier.setTotal(1);
        replier.setData(signInReplyData);
        writer.write(replier.reply0Success(fid));
    }

    private SignInReplyData makeSession() throws NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        String sessionKey = genSessionKey();
        String sessionKeyEncrypted = encryptSessionKey(sessionKey,pubKey);
        String sessionName = makeSessionName(sessionKey);
        SignInReplyData data = new SignInReplyData();

        Map<String,String> sessionMap = new HashMap<>();
        sessionMap.put("sessionKey",sessionKey);
        sessionMap.put("fid", fid);

        //Delete the old session of the requester.
        String name = jedis0.hget(RedisKeys.AddrSessionName,fid);
        if(name!=null)jedis1.del(name);

        //Set the new session
        jedis1.hmset(sessionName,sessionMap);
        params = (Params) service.getParams();
        jedis1.expire(sessionName,Long.parseLong(params.getSessionDays())*86400);

        data.setSessionKeyEncrypted(sessionKeyEncrypted);
        data.setSessionDays(Integer.parseInt(params.getSessionDays()));
        data.setStartTime(System.currentTimeMillis());

        String oldSessionName = jedis0.hget(RedisKeys.AddrSessionName, fid);
        if(oldSessionName!=null) {
            jedis1.del(oldSessionName);
        }
        jedis0.hset(RedisKeys.AddrSessionName, fid,sessionName);

        return data;
    }
    private String genSessionKey() {
        SecureRandom random = new SecureRandom();

        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        String sessionKey = BytesTools.bytesToHexStringBE(keyBytes);
        return sessionKey;
    }
    private String makeSessionName(String sessionKey) {
        return sessionKey.substring(0,12);
    }
    private String encryptSessionKey(String sessionKey, String pubKey) throws NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        String pubKey65 = KeyTools.recoverPK33ToPK65(pubKey);
        byte[] pubKeyBytes = BytesTools.hexToByteArray(pubKey65.substring(2));

        EXPList.set_EXP_List();
        ECIES ecies = new ECIES();
        ecies.getPair(pubKeyBytes);
        byte[] cypherBytes = ecies.encrypt(sessionKey);
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedSessionKey = encoder.encodeToString(cypherBytes);//AES256.byteToHexString(cypherBytes);
        return encryptedSessionKey;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("This API accepts only POST request. 抱歉！");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("This API accepts only POST request. 抱歉！");
    }

}
