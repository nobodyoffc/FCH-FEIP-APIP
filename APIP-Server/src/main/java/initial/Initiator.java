package initial;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import config.ConfigAPIP;
import constants.Strings;
import esTools.NewEsClient;
import freecashRPC.NewFcRpcClient;
import javaTools.JsonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import redis.clients.jedis.JedisPool;
import redisTools.GetJedis;
import service.ApipService;
import service.Params;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.io.*;

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
    private final NewEsClient newEsClient = new NewEsClient();
    public static JsonRpcHttpClient fcClient;
    public static ConfigAPIP configAPIP;

    @Override
    public void destroy(){
        log.debug("Destroy APIP server...");
        jedisPool.close();
        try {
            newEsClient.shutdownClient();
        } catch (IOException e) {
            log.debug("Shutdown NewEsClient wrong.");
        }

        log.debug("APIP server is stopped.");
    }
    @Override
    public void init(ServletConfig config) {
        log.debug("init starting...");

        jedisPool = GetJedis.createJedisPool();

        //Get config.json
        configAPIP = new ConfigAPIP();
        Gson gson = new Gson();

        try(Jedis jedis = jedisPool.getResource()) {
            configAPIP.setConfigFilePath(jedis.hget(CONFIG, CONFIG_FILE_PATH));
            log.debug("Load config.json from " + configAPIP.getConfigFilePath());

            //Get ES client
            log.debug("Create esClient for " + this.getClass() + "...");

            boolean failed = false;
            try {
                configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
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
                jedisPool.close();
                return;
            }

            forbidFreeGet = configAPIP.isForbidFreeGet();
            serviceName = configAPIP.getServiceName();
            try {
                service = gson.fromJson(jedis.get(serviceName +"_" + Strings.SERVICE), ApipService.class);

                if(service ==null )log.error("Reading service from redis failed.");

                log.debug("Service: "+ JsonTools.getNiceString(service));

                params = service.getParams();

            }catch (Exception e){
                log.error("Get service or nPrice from redis wrong.");
                jedisPool.close();
                return;
            }
        }

        String rpcIp = configAPIP.getRpcIp();
        int rpcPort = configAPIP.getRpcPort();
        String rpcUser = configAPIP.getRpcUser();
        String rpcPassword = configAPIP.getRpcPassword();

        try {
            log.debug("Create FcRpcClient for "+this.getClass());
            NewFcRpcClient newFcRpcClient = new NewFcRpcClient(rpcIp, rpcPort,rpcUser,rpcPassword);
            fcClient = newFcRpcClient.getClientSilent();
        } catch (Exception e) {
            log.error("Creating FchRpcClient failed."+ e.getMessage());
            throw new RuntimeException(e);
        }

        log.debug("APIP server initiated successfully.");
    }
}
