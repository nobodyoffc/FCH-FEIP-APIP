package mempool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import constants.Constants;
import constants.IndicesNames;
import constants.Strings;
import fcTools.ParseTools;
import fchClass.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import startAPIP.StartAPIP;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static esTools.EsTools.getBestBlock;

public class MempoolCleaner implements Runnable {
    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private String blockFilePath;
    private final ElasticsearchClient esClient;
    private static final Logger log = LoggerFactory.getLogger(MempoolCleaner.class);
    public MempoolCleaner(String blockFilePath, ElasticsearchClient esClient) {
        this.blockFilePath =blockFilePath;
        this.esClient = esClient;
    }

    public void run() {
        System.out.println("MempoolCleaner running...");
        try {
            while (running.get()) {
                ParseTools.waitForChangeInDirectory(blockFilePath,running);
                try(Jedis jedis1 = StartAPIP.jedisPool.getResource()) {
                    jedis1.select(Constants.RedisDb3Mempool);
                    jedis1.flushDB();

                    TimeUnit.SECONDS.sleep(2);
                    jedis1.select(Constants.RedisDb0Common);
                    Block block = getBestBlock(esClient);

                    jedis1.set(Strings.BEST_HEIGHT,String.valueOf(block.getHeight()));
                    jedis1.set(Strings.BEST_BLOCK_ID,block.getBlockId());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shutdown() {
        running.set(false);
    }

    public void restart(){
        running.set(true);
    }
    public AtomicBoolean getRunning() {
        return running;
    }
}
