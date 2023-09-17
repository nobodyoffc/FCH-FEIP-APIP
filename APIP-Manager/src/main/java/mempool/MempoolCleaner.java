package mempool;

import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicBoolean;

public class MempoolCleaner implements Runnable {
    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private Jedis jedis = new Jedis();
    private String blockFilePath;
    private static final Logger log = LoggerFactory.getLogger(MempoolCleaner.class);
    public MempoolCleaner(String blockFilePath) {
        jedis.select(3);
        this.blockFilePath =blockFilePath;
    }

    public void run() {
        System.out.println("MempoolCleaner running...");
        try {
            while (running.get()) {
                ParseTools.waitForChangeInDirectory(blockFilePath,running);
                jedis.flushDB();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shutdown() {
        jedis.close();
        running.set(false);
    }

    public void restart(){
        jedis = new Jedis();
        running.set(true);
    }
}
