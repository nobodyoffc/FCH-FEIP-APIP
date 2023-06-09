package scanner;

import fcTools.ParseTools;
import parser.BlockFileTools;
import redis.clients.jedis.Jedis;

public class MempoolCleaner extends Thread{
    private Jedis jedis3Unconfirmed;
    private String blockFilePath;

    public MempoolCleaner(String blockFilePath) {
        this.jedis3Unconfirmed = new Jedis();
        jedis3Unconfirmed.select(3);
        this.blockFilePath =blockFilePath;
    }

    public void run() {
        System.out.println("MempoolCleaner running...");
        try {
            while (true) {
                String lastBlockFileName = BlockFileTools.getLastBlockFileName(blockFilePath);
                ParseTools.waitForNewItemInFile(blockFilePath+lastBlockFileName);
                jedis3Unconfirmed.flushDB();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
