package API;

import SignIn.SignInCheckResult;
import SignIn.SignInReplyData;
import Tools.EccAes256K1P7;
import RequestCheck.Replier;
import RequestCheck.RequestChecker;
import Tools.SymSign;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;


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
 * 5. 用户访问
 *    用户浏览器本地保存sessionKey，每次访问用sessionKey加在body后sha256x2哈希获得Sign放在请求头部，供验证。
 * */

@WebServlet("/Path/" + "SignIn")
public class SignInAPI extends HttpServlet {
    private static final Jedis jedis0 = new Jedis();
    static {
        jedis0.select(1);
    }
    private static final Jedis jedis1 = new Jedis();
    static {
        jedis1.select(1);
    }
    private String fid = null;
    private String pubKey = null;
    private final Replier replier = new Replier();
    private final int SessionDays = 100;
    private final String FID = "fid";
    private final String SESSION_NAME = "sessionName";
    private final String SESSION_KEY = "sessionKey";
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

        SignInReplyData signInReplyData;

        try {
            signInReplyData = makeSession();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        response.setHeader("Code",String.valueOf(0));
        replier.setData(signInReplyData);
        writer.write(replier.reply0Success(fid));
    }

    private SignInReplyData makeSession() throws Exception {

        String sessionKey = genSessionKey();
        String sessionKeyEncrypted = encryptSessionKey(sessionKey,pubKey);
        String sessionName = makeSessionName(sessionKey);
        SignInReplyData data = new SignInReplyData();

        Map<String,String> sessionMap = new HashMap<>();
        sessionMap.put(SESSION_KEY,sessionKey);
        sessionMap.put(FID, fid);

        //Delete the old session of the requester.
        String name = jedis0.hget(SESSION_NAME,fid);
        if(name!=null)jedis1.del(name);

        //Set the new session
        jedis1.hmset(sessionName,sessionMap);

        jedis1.expire(sessionName,SessionDays*86400);

        data.setSessionKeyEncrypted(sessionKeyEncrypted);
        data.setSessionDays(SessionDays);
        data.setStartTime(System.currentTimeMillis());

        String oldSessionName = jedis0.hget(SESSION_NAME, fid);
        if(oldSessionName!=null) {
            jedis1.del(oldSessionName);
        }
        jedis0.hset(SESSION_NAME, fid,sessionName);

        return data;
    }
    private String genSessionKey() {
        SecureRandom random = new SecureRandom();

        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return SymSign.bytesToHex(keyBytes);
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
