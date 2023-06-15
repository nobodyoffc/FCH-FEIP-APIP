package initial;

import AesEcc.AES128;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import fcTools.ParseTools;
import opReturn.OpReFileTools;
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

    @Override
    public void init(ServletConfig config) {
        NewEsClient newEsClient = new NewEsClient();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Redis started: " + jedis0Common.toString());
        }catch (Exception e){
            System.out.println("Startup redis first.");
            return;
        }

        System.out.println("Create esClient for "+this.getClass());
        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis0Common.get(RedisKeys.ConfigFilePath));
        System.out.println(configAPIP.getConfigFilePath());

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
                System.out.println("ElasticSearch is not ready.");
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        service = gson.fromJson(jedis0Common.get(IndicesAPIP.ServiceIndex), ApipService.class);
        System.out.println("Service: "+ ParseTools.gsonString(service));

        Params params = service.getParams();

        if(params.getPricePerRequest()!=null) {
            price = (long)(Double.parseDouble(params.getPricePerRequest())*100000000);
            isPricePerRequest = true;
            System.out.println("PricePerRequest: "+ true);
        }

        if(params.getPricePerKBytes()!=null) {
            price = (long)(Double.parseDouble(params.getPricePerKBytes())*100000000);
            isPricePerKBytes = true;
            System.out.println("PricePerKBytes: "+ true);
        }

        if (jedis0Common.get("windowTime") == null) {
            jedis0Common.set("windowTime", "5000");
        }
        windowTime = Long.parseLong(jedis0Common.get("windowTime"));
        System.out.println("windowTime: "+ windowTime);

        if (configAPIP.isScanMempool()) {
            System.out.println("Clean mempool data in Redis...");
            MempoolCleaner mempoolCleaner = new MempoolCleaner(configAPIP.getBlockFilePath());
            mempoolCleaner.start();

            MempoolScanner mempoolScanner = new MempoolScanner();
            mempoolScanner.start();
            System.out.println("Mempool scanner started: "+ true);
        }

        System.out.println("Start order scanner...");
        String opReturnFilePath = configAPIP.getOpReturnFilePath();
        String lastOpReturnFileName = OpReFileTools.getLastOpReturnFileName(opReturnFilePath);

        OrderScanner orderScanner = new OrderScanner(opReturnFilePath+lastOpReturnFileName);
        orderScanner.start();

    }

    public static String getEsPassword(ConfigAPIP configAPIP, Jedis jedis) throws Exception {
        String esPasswordCipher = jedis.get(RedisKeys.EsPasswordCypher);
        if(esPasswordCipher==null)return null;
        return AES128.decryptFc(esPasswordCipher,configAPIP.getRandomSymKeyHex());
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
