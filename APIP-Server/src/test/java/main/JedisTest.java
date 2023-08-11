package main;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisTest {
    public static void main(String[] args) {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(128);
            poolConfig.setMaxIdle(128);

            JedisPool jedisPool = new JedisPool(poolConfig , "localhost",6379);
            Jedis jedisTest = jedisPool.getResource();
            System.out.println("jedisTest:" + jedisTest.ping());
            jedisTest.close();
            jedisPool.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
