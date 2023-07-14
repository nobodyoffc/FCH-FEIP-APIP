package redisTools;

import keyTools.KeyTools;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;

public class WriteRedis {

    public static void setFid(String key, String field,BufferedReader br, Jedis jedis) throws IOException {
        while(true) {
            String value = jedis.hget(key, field);
            System.out.println("The "+field+" is "+value+". Input to set it. Enter to ignore it:");
            String input = br.readLine();
            if("".equals(input))return;
            if(KeyTools.isValidFchAddr(input)) {
                jedis.hset(key, field, input);
                return;
            }else {
                System.out.println("It's not a FCH address.Input again. Enter to ignore");
            }
        }
    }

    public static void setNumber(String key, String field, BufferedReader br,Jedis jedis) throws IOException {
        while(true) {
            String value = jedis.hget(key, field);
            System.out.println("The "+field+" is "+value+". Input to set it. Enter to ignore it:");
            String input = br.readLine();
            if("".equals(input))return;
            try{
                Double.parseDouble(input);
            }catch (Exception e){
                System.out.println("Input a number please. Try again. Enter to ignore.");
                continue;
            }
            jedis.hset(key, field, input);
            return;
        }

    }
}
