package API;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import config.ConfigBase;
import config.ConfigService;
import constants.Constants;
import constants.Strings;
import fcTools.ParseTools;
import manager.OrderScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import servers.NewEsClient;
import service.Params;
import service.Service;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.io.Serial;

import static manager.StartManager.SERVICE_NAME;

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
    public static Service service = new Service();
    public static Gson gson = new Gson();
    public static Long price = 0L;
    public static long windowTime = 0;
    public static boolean isPricePerKBytes;
    public static boolean isPricePerRequest;
    private static final Logger log = LoggerFactory.getLogger(Initiator.class);
    @Override
    public void init(ServletConfig config) {
        log.debug("init starting...");
        NewEsClient newEsClient = new NewEsClient();

        try {
            log.debug("Redis started: " + jedis0Common.toString());
        }catch (Exception e){
            log.error("Jedis server is not ready. Start it.");
            return;
        }

        log.debug("Create esClient for "+this.getClass()+"...");

        ConfigBase configService = new ConfigBase();
        configService.setConfigFilePath(jedis0Common.get(Strings.CONFIG_FILE_PATH));
        if(configService.getConfigFilePath()==null){
            log.debug("Config file path wasn't set yet. Run ApipManager.jar first.");
            return;
        }

        log.debug("configAPIP path: "+configService.getConfigFilePath());
        try {
            configService = configService.getClassInstanceFromFile(ConfigService.class);
            if (configService.getEsIp() == null || configService.getEsPort() == 0) {
                log.error("Es IP is null. Config first.");
                return;
            }
            esClient = newEsClient.getEsClientSilent(configService, jedis0Common);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        service = gson.fromJson(jedis0Common.get(SERVICE_NAME + Strings.SERVICE_OBJECT), Service.class);

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

        if (jedis0Common.get(SERVICE_NAME + Constants.WINDOW_TIME) == null) {
            jedis0Common.set(SERVICE_NAME + Constants.WINDOW_TIME, "5000");
        }
        windowTime = Long.parseLong(jedis0Common.get(SERVICE_NAME + Constants.WINDOW_TIME));

        log.debug("windowTime: "+ windowTime);

        log.debug("Start order scanner...");
        String listenPath = jedis0Common.get(SERVICE_NAME + Strings.LISTEN_PATH);

        OrderScanner orderScanner = new OrderScanner(listenPath);
        orderScanner.start();
        log.debug("Order scanner is running.");
    }

}
