package main;

import redis.clients.jedis.Jedis;

public class SimpleTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        System.out.println(jedis.ping());
        jedis.close();
        System.out.println(jedis.ping());
    }
}
