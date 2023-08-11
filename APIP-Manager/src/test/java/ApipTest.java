import co.elastic.clients.elasticsearch.ElasticsearchClient;
import config.ConfigAPIP;
import balance.BalanceInfo;
import redis.clients.jedis.Jedis;
import esTools.NewEsClient;

import java.io.IOException;

import static constants.Strings.CONFIG;
import static constants.Strings.CONFIG_FILE_PATH;

public class ApipTest {
    public static void main(String[] args) throws IOException {

        ConfigAPIP configAPIP = new ConfigAPIP();
        Jedis jedis0Common = new Jedis();
        configAPIP.setConfigFilePath(jedis0Common.hget(CONFIG,CONFIG_FILE_PATH));
        try {
            configAPIP =  configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
        } catch (IOException e) {
            System.out.println("Order scanner read config file wrong.");
            throw new RuntimeException(e);
        }
        NewEsClient newEsClient = new NewEsClient();
        ElasticsearchClient esClient=null;
        try{
            esClient = newEsClient.getEsClientSilent(configAPIP, jedis0Common);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Creating ES client failed.");
        }
        BalanceInfo.recoverUserBalanceFromEs(esClient,jedis0Common);
//        BalanceInfo.backupUserBalanceToEs(esClient,jedis0Common);
    }
}
