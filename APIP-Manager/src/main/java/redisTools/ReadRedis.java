package redisTools;

import redis.clients.jedis.Jedis;

public class ReadRedis {

    public static long readHashLong(Jedis jedis, String key, String filed){

        long var =0;
        String varStr = jedis.hget(key, filed);
        if(varStr!= null){
            var = Long.parseLong(varStr);
        }
        return var;
    }

    public static long readLong(Jedis jedis, String key){

        long var =0;
        String varStr = jedis.get(key);
        if(varStr!= null){
            var = Long.parseLong(varStr);
        }
        return var;
    }
}
