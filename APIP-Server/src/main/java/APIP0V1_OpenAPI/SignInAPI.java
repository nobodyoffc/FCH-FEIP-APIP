package APIP0V1_OpenAPI;

import EccAes256K1P7.EccAes256K1P7;
import constants.ApiNames;
import constants.ReplyInfo;
import service.ApipService;
import javaTools.BytesTools;
import initial.Initiator;
import redis.clients.jedis.Jedis;
import service.Params;
import constants.Strings;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


import static constants.ApiNames.SignInAPI;
import static constants.Strings.*;


/**
 * 1. 验证请求
 *    1）公钥地址是否有余额
 *    2）验证url是否当前请求url
 *    3）时间戳是否在窗口期
 *    4）验证签名
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

/**
 * 简化登录
 * 0. 采用https传输，无需加密直接返回sessionKey，请求时不再提供公钥，改为FID。
 * 1. FID = 用户签名中的fid、addr或address项的值
 *    Sign = 用户签名中的sign或signature项的值
 * 2. Request body：严格等于展示给用户签名的信息
 * 3. 加入mode项，值为renew时，用新sessionKey取代旧的，否则返回sessionKey。
 */

@WebServlet(ApiNames.APIP0V1Path + SignInAPI)
public class SignInAPI extends HttpServlet {
    private static final Jedis jedis1 = Initiator.jedis1Session;
    private static final Jedis jedis0 = Initiator.jedis0Common;
    private static final ApipService service = Initiator.service;
    private String fid = null;
    private String pubKey = null;
    private final Replier replier = new Replier();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response,replier);

        SignInCheckResult signInCheckResult;
        try {
            signInCheckResult = requestChecker.checkSignInRequest();
        } catch (SignatureException e) {
            e.printStackTrace();
            return;
        }

        if(signInCheckResult==null) return;

        fid = signInCheckResult.getFid();

        SignInReplyData signInReplyData = new SignInReplyData();

        String mode = signInCheckResult.getSignInRequestBody().getMode();

        if((!jedis0.hexists(FID_SESSION_NAME, fid)) || "renew".equals(mode)){
            try {
                signInReplyData = makeSession();
            } catch (Exception e) {
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Some thing wrong when making sessionKey.\n"+e.getMessage());
                writer.write(replier.reply1020OtherError(fid));
                return;
            }
        }else{
            String sessionName = jedis0.hget(FID_SESSION_NAME, fid);
            signInReplyData.setSessionKey(jedis1.hget(sessionName, SESSION_KEY));

            long expireMilliSed = jedis1.ttl(sessionName);
            if(expireMilliSed<0){
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Expire time is wrong.");
                writer.write(replier.reply1020OtherError(fid));
                return;
            }
            long expireTime =System.currentTimeMillis()+expireMilliSed*1000;
            signInReplyData.setExpireTime(expireTime);
        }

        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        replier.setGot(1);
        replier.setTotal(1);
        replier.setData(signInReplyData);
        writer.write(replier.reply0Success(fid));
        replier.setData(null);
    }


    private SignInReplyData makeSession() throws Exception {

        String sessionKey = genSessionKey();
        String sessionName = makeSessionName(sessionKey);
        SignInReplyData data = new SignInReplyData();

        Map<String,String> sessionMap = new HashMap<>();
        sessionMap.put("sessionKey",sessionKey);
        sessionMap.put("fid", fid);

        //Delete the old session of the requester.
        String oldSessionName = jedis0.hget(Strings.FID_SESSION_NAME,fid);
        if(oldSessionName!=null)jedis1.del(oldSessionName);

        //Set the new session
        jedis1.hmset(sessionName,sessionMap);
        Params params = service.getParams();
        long lifeSeconds = Long.parseLong(params.getSessionDays()) * 86400;
        jedis1.expire(sessionName,lifeSeconds);

        data.setSessionKey(sessionKey);
        long expireTime = System.currentTimeMillis()+(lifeSeconds*1000);
        data.setExpireTime(expireTime);

        jedis0.hset(Strings.FID_SESSION_NAME, fid,sessionName);

        return data;
    }

    public static String millisecondToDataTime(long milliTime) {

        Instant instant = Instant.ofEpochMilli(milliTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }

    private String genSessionKey() {
        SecureRandom random = new SecureRandom();

        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return BytesTools.bytesToHexStringBE(keyBytes);
    }
    private String makeSessionName(String sessionKey) {
        return sessionKey.substring(0,12);
    }
    private String encryptSessionKey(String sessionKey, String pubKey) throws Exception {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        return ecc.encrypt(sessionKey,pubKey);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("This API accepts only POST request.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("This API accepts only POST request.");
    }
}
