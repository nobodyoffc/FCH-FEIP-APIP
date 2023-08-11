package mempool;

import config.ConfigAPIP;
import fcTools.ParseTools;
import fileTools.BlockFileTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;

import static constants.Strings.CONFIG;
import static constants.Strings.CONFIG_FILE_PATH;

public class MempoolCleaner implements Runnable {
    private volatile boolean running = true;
    private Jedis jedis = new Jedis();
    private String blockFilePath;
    private static final Logger log = LoggerFactory.getLogger(MempoolCleaner.class);
    public MempoolCleaner(String blockFilePath) {

        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis.hget(CONFIG,CONFIG_FILE_PATH));
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0) {
                log.error("Es IP is null. Config first.");
                return;
            }
        } catch (IOException e) {
            log.error("Preparing config failed: "+e.getMessage());
            throw new RuntimeException(e);
        }

        jedis.select(3);
        this.blockFilePath =blockFilePath;
    }

    public void run() {
        System.out.println("MempoolCleaner running...");
        try {
            while (running) {
                ParseTools.waitForNewItemInDirectory(blockFilePath,running);
                jedis.flushDB();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shutdown() {
        jedis.close();
        running = false;
    }

    public void restart(){
        jedis = new Jedis();
        running = true;
    }
}
