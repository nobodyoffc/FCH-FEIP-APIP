package redisTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class GetJedis {
    private static final Logger log = LoggerFactory.getLogger(GetJedis.class);

    public static Jedis getJedis(String ip, int port)  {

        if (port == 0 || ip== null) return  null;

        Jedis jedis = new Jedis(ip, port);
        try {
            String ping = jedis.ping();
            if (ping.equals("PONG")) {
                return jedis;
            } else {
                log.error("Failed to startup redis.");
                return null;
            }
        }catch (Exception e){
            log.error("Failed to startup redis.");
            return null;
        }
    }
}
