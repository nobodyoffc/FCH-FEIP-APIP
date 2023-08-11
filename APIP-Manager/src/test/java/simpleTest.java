import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

import static constants.Constants.FchToSatoshi;

public class simpleTest {
    public static void main(String[] args) {
        try {
            jedisRename();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void jedisRename() throws InterruptedException {
        Jedis jedis = new Jedis();

//        jedis.set("old","o");
//        TimeUnit.SECONDS.sleep(10);
        jedis.rename("old","new");
    }

    private static double round(double raw){
        long i = 0;
        i = (long) (raw*FchToSatoshi);
        System.out.println(i);
        return (double) i;
    }
}
