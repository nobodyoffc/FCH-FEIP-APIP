package initial;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;

import config.ConfigAPIP;
import constants.Strings;
import esTools.NewEsClient;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import service.ApipService;
import service.Params;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import static constants.Strings.*;


public class Initiator extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(Initiator.class);
    public static  String serviceName;
    public static ElasticsearchClient esClient= null;
    public static JedisPool jedisPool;

    //Can be changed:
    public static ApipService service;
    public static Params params;

    public static boolean isPricePerRequest;
    public static boolean forbidFreeGet;

    @Override
    public void destroy(){
        jedisPool.close();
        esClient.shutdown();
        log.debug("APIP server is stopped.");
    }
    @Override
    public void init(ServletConfig config) {
        log.debug("init starting...");
        NewEsClient newEsClient = new NewEsClient();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        createJedisPools();

        //Get config.json
        ConfigAPIP configAPIP = new ConfigAPIP();
        Gson gson = new Gson();

        try(Jedis jedis = jedisPool.getResource()) {
            configAPIP.setConfigFilePath(jedis.hget(CONFIG, CONFIG_FILE_PATH));
            log.debug("Load config.json from " + configAPIP.getConfigFilePath());

            //Get ES client
            log.debug("Create esClient for " + this.getClass() + "...");

            boolean failed = false;
            try {
                configAPIP = configAPIP.getClassInstanceFromFile(br, ConfigAPIP.class);
                if (configAPIP.getEsIp() == null || configAPIP.getEsPort() == 0 || configAPIP.getConfigFilePath() == null) {
                    log.debug("Config is not ready when initiating APIP web.");
                    failed = true;
                }
                esClient = newEsClient.getEsClientSilent(configAPIP, jedis);
                if (esClient == null) {
                    newEsClient.shutdownClient();
                    log.error("Creating ES client failed when initiating APIP web.");
                    failed = true;
                }
            } catch (Exception e) {
                log.error("Initiating ApipServer failed. \n" + e.getMessage());
                failed = true;
            }
            if(failed){
                jedis.close();
                jedisPool.close();
                return;
            }

            forbidFreeGet = configAPIP.isForbidFreeGet();
            serviceName = configAPIP.getServiceName();
            try {
                service = gson.fromJson(jedis.get(serviceName +"_" + Strings.SERVICE), ApipService.class);
//                sid = service.getSid();

                if(service ==null )log.error("Reading service from redis failed.");

                log.debug("Service: "+ ParseTools.gsonString(service));

                params = service.getParams();

//                Map<String, String> nPriceMapStr = jedis.hgetAll(N_PRICE);
//                nPriceMap = makeStrMapToIntegerMap(nPriceMapStr);


            }catch (Exception e){
                log.error("Get service or nPrice from redis wrong.");
                jedis.close();
                jedisPool.close();
                return;
            }
        }



        log.debug("APIP server initiated successfully.");



//        if (configAPIP.isScanMempool()) {
//            log.debug("Clean mempool data in Redis...");
//            MempoolCleaner mempoolCleaner = new MempoolCleaner(configAPIP.getBlockFilePath());
//            Thread thread = new Thread(mempoolCleaner);
//            thread.start();
//
////            MempoolScanner mempoolScanner = new MempoolScanner();
////            mempoolScanner.start();
//            log.debug("Start mempoolScanner");
//            MempoolScanner mempoolScanner = new MempoolScanner(esClient);
//            Thread thread1 = new Thread(mempoolScanner);
//            thread1.start();
//
//            log.debug("Mempool scanner is running.");
//        }
//
//        log.debug("Start order scanner...");
//        String listenPath = configAPIP.getListenPath();
//
//        OrderScanner orderScanner = new OrderScanner(listenPath, esClient);
//        Thread thread2 = new Thread(orderScanner);
//        thread2.start();
//        log.debug("Order scanner is running.");
    }

    public static Map<String, Integer> makeStrMapToIntegerMap(Map<String, String> nPriceMapStr) {
        Map<String,Integer> nPriceMap = new HashMap<>();
        if(nPriceMapStr !=null){
            for(String key: nPriceMapStr.keySet()){
                try{
                    nPriceMap.put(key, Integer.parseInt(nPriceMapStr.get(key)));
                }catch (Exception ignore){}
            }
        }
        return nPriceMap;
    }

    private static void createJedisPools() {
        try {
            log.debug("Create jedis pool.......");
            JedisPoolConfig jedisConfig = new JedisPoolConfig();
//            jedisConfig.setMaxTotal(128);
//            jedisConfig.setMaxIdle(64);
//            jedisConfig.setMinIdle(32);
//            jedisConfig.setTestOnBorrow(true);
//            jedisConfig.setTestOnReturn(true);
//            jedisConfig.setTestWhileIdle(true);
            jedisPool = new JedisPool(jedisConfig, "localhost",6379,10000);
            log.debug("Jedis pool created.");
        }catch (Exception e){
            log.debug("Create jedisPool or jedis wrong. ",e);
        }
    }
    public static boolean isFreeGetForbidden(PrintWriter writer) {
        if(forbidFreeGet){
            writer.write("Sorry, the freeGet APIs were closed.");
            return true;
        }
        return false;
    }
}
