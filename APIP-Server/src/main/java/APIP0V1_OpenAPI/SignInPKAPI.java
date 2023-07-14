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
        import java.util.HashMap;
        import java.util.Map;


        import static constants.ApiNames.SignInAPI;


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

//@WebServlet(ApiNames.APIP0V1Path + SignInAPI)
public class SignInPKAPI extends HttpServlet {
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

        fid = signInCheckResult.getFid();
//        pubKey = signInCheckResult.getPubKey();

        SignInReplyData signInReplyData;

        try {
            signInReplyData = makeSession();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        replier.setGot(1);
        replier.setTotal(1);
        replier.setData(signInReplyData);
        writer.write(replier.reply0Success(fid));
    }

    private SignInReplyData makeSession() throws Exception {

        String sessionKey = genSessionKey();
        String sessionKeyEncrypted = encryptSessionKey(sessionKey,pubKey);
        String sessionName = makeSessionName(sessionKey);
        SignInReplyData data = new SignInReplyData();

        Map<String,String> sessionMap = new HashMap<>();
        sessionMap.put("sessionKey",sessionKey);
        sessionMap.put("fid", fid);

        //Delete the old session of the requester.
        String name = jedis0.hget(Strings.FID_SESSION_NAME,fid);
        if(name!=null)jedis1.del(name);

        //Set the new session
        jedis1.hmset(sessionName,sessionMap);
        Params params = service.getParams();
        jedis1.expire(sessionName,Long.parseLong(params.getSessionDays())*86400);

        data.setSessionKey(sessionKeyEncrypted);


        String oldSessionName = jedis0.hget(Strings.FID_SESSION_NAME, fid);
        if(oldSessionName!=null) {
            jedis1.del(oldSessionName);
        }
        jedis0.hset(Strings.FID_SESSION_NAME, fid,sessionName);

        return data;
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
