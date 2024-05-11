package APIP0V1_OpenAPI;

import apipRequest.SessionData;
import constants.Strings;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesType;
import initial.Initiator;
import javaTools.BytesTools;
import redis.clients.jedis.Jedis;
import service.Params;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Session {
    private String sessionName;
    private String sessionKey;
    private String fid;

    public SessionData makeSession(Jedis jedis, String fid) {
        String sessionKey;
        String sessionName;
        SessionData data;

        jedis.select(1);
        do {
            sessionKey = genSessionKey();
            sessionName = makeSessionName(sessionKey);
        } while (jedis.exists(sessionName));
        data = new SessionData();
        Map<String,String> sessionMap = new HashMap<>();
        sessionMap.put("sessionKey",sessionKey);
        sessionMap.put("fid", fid);

    //Delete the old session of the requester.
        jedis.select(0);
        String oldSessionName = jedis.hget(Initiator.serviceName+"_"+ Strings.FID_SESSION_NAME, fid);

        jedis.select(1);
        if (oldSessionName != null) jedis.del(oldSessionName);

        //Set the new session
        jedis.hmset(sessionName, sessionMap);
        Params params = Initiator.service.getParams();

        long lifeSeconds = Long.parseLong(params.getSessionDays()) * 86400;

        jedis.expire(sessionName, lifeSeconds);

        data.setSessionKey(sessionKey);
        long expireTime = System.currentTimeMillis() + (lifeSeconds * 1000);

        data.setExpireTime(expireTime);

        jedis.select(0);
        jedis.hset(Initiator.serviceName+"_"+Strings.FID_SESSION_NAME, fid, sessionName);
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

    public static String encryptSessionKey(String sessionKey, String pubKey, String sign) throws Exception {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesData eccAesData= new EccAesData(EccAesType.AsyOneWay, sessionKey,pubKey);
        ecc.encrypt(eccAesData);
        if(eccAesData.getError()!=null){
            return "Error:"+eccAesData.getError();
        }
        return eccAesData.toJson();
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }
}
