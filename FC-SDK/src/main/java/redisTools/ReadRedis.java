package redisTools;

import redis.clients.jedis.Jedis;

public class ReadRedis {

    public static long readHashLong(Jedis jedis, String key, String filed){

        long var =0;
        String varStr;
        try {
           varStr = jedis.hget(key, filed);
        }catch (Exception e){
            varStr=null;
        }
        if(varStr!= null){
            var = Long.parseLong(varStr);
        }
        return var;
    }


    public static long readLong(Jedis jedis, String key){

        long var =0;
        String varStr;
        try {
            varStr = jedis.get(key);
        }catch (Exception e){
            varStr=null;
        }
        if(varStr!= null){
            var = Long.parseLong(varStr);
        }
        return var;
    }

}
