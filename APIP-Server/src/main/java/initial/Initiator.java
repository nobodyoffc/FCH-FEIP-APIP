package initial;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import config.ConfigAPIP;
import constants.Strings;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import mempool.MempoolCleaner;
import mempool.MempoolScanner;
import order.OrderScanner;
import servers.NewEsClient;
import service.ApipService;
import service.Params;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serial;

import static constants.Constants.Million;
import static constants.Strings.*;

public class Initiator extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    public static  String serviceName;
    public static Jedis jedis0Common = new Jedis();
    public static Jedis jedis1Session = new Jedis();
    public static Jedis jedis2Nonce = new Jedis();
    public static Jedis jedis3Mempool = new Jedis();

    static {
        jedis1Session.select(1);
    }

    static {
        jedis2Nonce.select(2);
    }

    static {
        jedis3Mempool.select(3);
    }

    public static ElasticsearchClient esClient= null;
    public static ApipService service = new ApipService();
    public static Gson gson = new Gson();

    //TODO remove this
    public static Long price = 0L;
    public static long windowTime = 0;
    public static boolean isPricePerKBytes;
    public static boolean isPricePerRequest;
    private static final Logger log = LoggerFactory.getLogger(Initiator.class);

    public static boolean isFreeGetForbidden(PrintWriter writer) {

        boolean forbidFreeGet = Boolean.parseBoolean(jedis0Common.hget(CONFIG,FORBID_FREE_GET));
        if(forbidFreeGet){
            writer.write("Sorry, the freeGet APIs were closed.");
            return true;
        }
        return false;
    }

    public static long readPrice() {
        Jedis jedis = new Jedis();
        long price = 0;
        try {
            if (jedis.hexists(CONFIG, PRICE_PER_K_BYTES)) {
                price = (long) Double.parseDouble(jedis.hget(CONFIG, PRICE_PER_K_BYTES)) * Million;
            } else {
                price = (long) Double.parseDouble(jedis.hget(CONFIG, PRICE_PER_REQUEST)) * Million;
            }
        }catch (Exception e){
            jedis.close();
            log.error("Read price from jedis wrong. ",e);
            return 0;
        }
        return price;
    }

    @Override
    public void init(ServletConfig config) {
        log.debug("init starting...");
        NewEsClient newEsClient = new NewEsClient();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        //Get config.json
        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis0Common.hget(CONFIG,CONFIG_FILE_PATH));
        log.debug("Load config.json from "+configAPIP.getConfigFilePath());

        //Get ES client
        log.debug("Create esClient for "+this.getClass()+"...");

        try {
            configAPIP = configAPIP.getClassInstanceFromFile(br,ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0||configAPIP.getConfigFilePath()==null ) {
                log.debug("Config is not ready when initiating APIP web.");
            }

            esClient = newEsClient.getEsClientSilent(configAPIP,jedis0Common);

            if (esClient == null) {
                newEsClient.shutdownClient();
                log.error("Creating ES client failed when initiating APIP web.");
                return;
            }
        } catch (Exception e) {
            log.error("Initiating ApipServer failed. \n"+e.getMessage());
            return;
        }

        serviceName = configAPIP.getServiceName()+"_";

        service = gson.fromJson(jedis0Common.get(serviceName+Strings.SERVICE), ApipService.class);

        if(service ==null )log.error("Reading service from redis failed.");

        log.debug("Service: "+ ParseTools.gsonString(service));

        Params params = service.getParams();

        if(params.getPricePerRequest()!=null) {
            price = (long)(Double.parseDouble(params.getPricePerRequest())*100000000);
            isPricePerRequest = true;

            log.debug("PricePerRequest: "+ true);
        }

        if(params.getPricePerKBytes()!=null) {
            price = (long)(Double.parseDouble(params.getPricePerKBytes())*100000000);
            isPricePerKBytes = true;

            log.debug("PricePerKBytes: "+ true);
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

}
