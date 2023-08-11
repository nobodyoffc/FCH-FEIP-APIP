package main;

import constants.ReplyInfo;
import redis.clients.jedis.Jedis;
import tools.ApipTools;

import java.net.URI;

import static constants.Strings.SID;

public class SimpleTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        System.out.println(jedis.ping());
        jedis.close();
        System.out.println(jedis.ping());
    }
}
