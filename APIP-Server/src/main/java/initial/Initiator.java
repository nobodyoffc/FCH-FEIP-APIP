package initial;

import EccAes256K1P7.Aes256CbcP7;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import fcTools.ParseTools;
import opReturn.OpReFileTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import scanner.MempoolCleaner;
import scanner.MempoolScanner;
import scanner.OrderScanner;
import servers.NewEsClient;
import service.ApipService;
import service.Params;
import startAPIP.ConfigAPIP;
import startAPIP.IndicesAPIP;
import startAPIP.RedisKeys;
import startFEIP.StartFEIP;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.io.*;

public class Initiator extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
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
    public static Long price = 0L;
    public static long windowTime = 0;
    public static boolean isPricePerKBytes;
    public static boolean isPricePerRequest;
    private static final Logger log = LoggerFactory.getLogger(StartFEIP.class);
    @Override
    public void init(ServletConfig config) {
        log.debug("init starting...");
        NewEsClient newEsClient = new NewEsClient();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            log.debug("Redis started: " + jedis0Common.toString());
        }catch (Exception e){
            log.error("Jedis server is not ready. Start it.");
            return;
        }

        log.debug("Create esClient for "+this.getClass()+"...");

        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis0Common.get(RedisKeys.ConfigFilePath));
        if(configAPIP.getConfigFilePath()==null){
            log.debug("Config file path wasn't set yet. Run ApipManager.jar first.");
            return;
        }

        log.debug("configAPIP path: "+configAPIP.getConfigFilePath());

        try {
            configAPIP = configAPIP.getClassInstanceFromFile(br,ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0||configAPIP.getConfigFilePath()==null ) configAPIP.config(br);

            if(jedis0Common.get(RedisKeys.EsPasswordCypher)!=null){
                String esPassword = getEsPassword(configAPIP,jedis0Common);
                if(esPassword==null)return;
                esClient = newEsClient.getClientHttps(configAPIP.getEsIp(), configAPIP.getEsPort(),configAPIP.getEsUsername(),esPassword);
            }else{
                esClient = newEsClient.getClientHttp(configAPIP.getEsIp(), configAPIP.getEsPort());
            }
            if (esClient == null) {
                newEsClient.shutdownClient();
                log.error("Creating ES client failed.");
                return;
            }
        } catch (Exception e) {
            log.error("Initiating ApipServer failed. \n"+e.getMessage());
        }


        service = gson.fromJson(jedis0Common.get(RedisKeys.Service), ApipService.class);

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

        if (jedis0Common.get("windowTime") == null) {
            jedis0Common.set("windowTime", "5000");
        }
        windowTime = Long.parseLong(jedis0Common.get("windowTime"));

        log.debug("windowTime: "+ windowTime);

        if (configAPIP.isScanMempool()) {

            log.debug("Clean mempool data in Redis...");
            MempoolCleaner mempoolCleaner = new MempoolCleaner(configAPIP.getBlockFilePath());
            mempoolCleaner.start();

            MempoolScanner mempoolScanner = new MempoolScanner();
            mempoolScanner.start();

            log.debug("Mempool scanner is running.");
        }

        log.debug("Start order scanner...");
        String opReturnFilePath = configAPIP.getOpReturnFilePath();
        String lastOpReturnFileName = OpReFileTools.getLastOpReturnFileName(opReturnFilePath);

        OrderScanner orderScanner = new OrderScanner(opReturnFilePath+lastOpReturnFileName);
        orderScanner.start();
        log.debug("Order scanner is running.");

    }

    public static String getEsPassword(ConfigAPIP configAPIP, Jedis jedis) throws Exception {
        String esPasswordCipher = jedis.get(RedisKeys.EsPasswordCypher);
        if(esPasswordCipher==null)return null;
        return Aes256CbcP7.decrypt(esPasswordCipher,configAPIP.getRandomSymKeyHex());
    }

    public static boolean isFreeGetAllowed(PrintWriter writer) {
        boolean allowFreeGet = Boolean.parseBoolean(jedis0Common.get(RedisKeys.AllowFreeGet));
        if(!allowFreeGet){
            writer.write("Sorry, the freeGet APIs were closed.");
            return false;
        }
        return true;
    }
}
