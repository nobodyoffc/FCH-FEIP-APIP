//package initial;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import com.google.gson.Gson;
//import config.ConfigAPIP;
//import constants.Strings;
//import mempool.MempoolCleaner;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import redis.clients.jedis.Jedis;
//import service.ApipService;
//import service.Params;
//
//import java.io.IOException;
//import java.util.Map;
//
//import static constants.Strings.*;
//import static initial.Initiator.*;
//
//public class ServiceScanner implements Runnable{
//
//    private volatile boolean running = true;
//    private String listenPath;
//    private ElasticsearchClient esClient;
//    private ConfigAPIP configAPIP;
//    private static final Logger log = LoggerFactory.getLogger(ServiceScanner.class);
//
//    public ServiceScanner(String listenPath,ElasticsearchClient esClient) {
//        this.listenPath = listenPath;
//        this.esClient = esClient;
//    }
//
//    /*
//        检查：1）ES中给定sid的service是否更新：service，params。更新的话要修改configAPIP和redis
//            2）config.json文件是否更新：
//            3）需要两个scanner
//     */
//    @Override
//    public void run() {
//        ConfigAPIP configAPIP = new ConfigAPIP();
//
//
//        configAPIP.setConfigFilePath(jedis.hget(CONFIG,CONFIG_FILE_PATH));
//        try {
//            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
//            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0) {
//                log.error("Es IP is null. Config first.");
//                return;
//            }
//        } catch (IOException e) {
//            log.error("Preparing config failed: "+e.getMessage());
//            throw new RuntimeException(e);
//        }
//
//        jedis.select(3);
//        this.blockFilePath =blockFilePath;
//    }
//    public void resetService(){
//        serviceName = null;
//        Initiator.price = null;
//        Initiator.windowTime = 0;
//        Initiator.service = null;
//        sid=null;
//        isPricePerKBytes;
//        isPricePerRequest;
//        Initiator.bestHeight = 0L;
//
//
//
//        Initiator.nPriceMap = null;
//        Initiator.forbidFreeGet=false;
//
//        Gson gson = new Gson();
//        ConfigAPIP configAPIP = new ConfigAPIP();
//
//        Initiator.forbidFreeGet = configAPIP.isForbidFreeGet();
//        try(Jedis jedis = Initiator.jedisPool.getResource()) {
//
//            String serviceStr = jedis.get(Initiator.serviceName + Strings.SERVICE);
//
//            Initiator.service = gson.fromJson(serviceStr, ApipService.class);
//            Initiator.params = Initiator.service.getParams();
//
//            Initiator.bestHeight = redisTools.ReadRedis.readLong(jedis, Strings.BEST_HEIGHT);
//
//            serviceName = configAPIP.getServiceName() + "_";
//            Initiator.service = gson.fromJson(jedis.get(serviceName + Strings.SERVICE), ApipService.class);
//
//            Map<String, String> nPriceMapStr = jedis.hgetAll(N_PRICE);
//            Initiator.nPriceMap = makeStrMapToIntegerMap(nPriceMapStr);
//        }
//    }
//}
