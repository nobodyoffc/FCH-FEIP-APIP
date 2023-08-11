package startAPIP;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import constants.IndicesNames;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static constants.Constants.ORDER;
import static constants.Strings.BALANCE;
import static constants.Strings.REWARD;
import static startAPIP.StartAPIP.getNameOfService;

public class IndicesAPIP {
    private static final Logger log = LoggerFactory.getLogger(IndicesAPIP.class);
    private final Jedis jedis;
    private final ElasticsearchClient esClient;
    private final BufferedReader br;
    public static  final  String  orderMappingJsonStr = "{\"mappings\":{\"properties\":{\"amount\":{\"type\":\"long\"},\"orderId\":{\"type\":\"keyword\"},\"fromFid\":{\"type\":\"wildcard\"},\"height\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"toFid\":{\"type\":\"wildcard\"},\"txId\":{\"type\":\"keyword\"},\"txIndex\":{\"type\":\"long\"},\"txid\":{\"type\":\"keyword\"},\"via\":{\"type\":\"wildcard\"}}}}";
    public static  final  String  balanceMappingJsonStr = "{\"mappings\":{\"properties\":{\"user\":{\"type\":\"text\"},\"consumeVia\":{\"type\":\"text\"},\"orderVia\":{\"type\":\"text\"},\"bestHeight\":{\"type\":\"keyword\"}}}}";
    public static final String  rewardMappingJsonStr = "{\"mappings\":{\"properties\":{\"rewardId\":{\"type\":\"keyword\"},\"rewardT\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"state\":{\"type\":\"keyword\"},\"bestHeight\":{\"type\":\"keyword\"},\"builderList\":{\"type\":\"nested\",\"properties\":{\"fid\":{\"type\":\"keyword\"},\"share\":{\"type\":\"float\"},\"amount\":{\"type\":\"long\"},\"fixed\":{\"type\":\"long\"}}},\"orderViaList\":{\"type\":\"nested\",\"properties\":{\"fid\":{\"type\":\"keyword\"},\"share\":{\"type\":\"float\"},\"amount\":{\"type\":\"long\"},\"fixed\":{\"type\":\"long\"}}},\"consumeViaList\":{\"type\":\"nested\",\"properties\":{\"fid\":{\"type\":\"keyword\"},\"share\":{\"type\":\"float\"},\"amount\":{\"type\":\"long\"},\"fixed\":{\"type\":\"long\"}}},\"costList\":{\"type\":\"nested\",\"properties\":{\"fid\":{\"type\":\"keyword\"},\"share\":{\"type\":\"float\"},\"amount\":{\"type\":\"long\"},\"fixed\":{\"type\":\"long\"}}}}}}";

    public IndicesAPIP(ElasticsearchClient esClient, Jedis jedis, BufferedReader br){
        this.br = br;
        this.jedis = jedis;
        this.esClient = esClient;
    }

    public void menu() throws IOException, InterruptedException {


        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();
        menuItemList.add("List All Indices in ES");
        menuItemList.add("Recreate Order index");
        menuItemList.add("Recreate Balance Backup index");
        menuItemList.add("Recreate Reward index");
        menuItemList.add("Recreate Order, Balance and Reward indices");

        menu.add(menuItemList);
        while(true) {
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> listIndices(br);
                case 2 -> recreateApipIndex(br, esClient, jedis, ORDER, orderMappingJsonStr);
                case 3 -> recreateApipIndex(br, esClient, jedis, BALANCE, balanceMappingJsonStr);
                case 4 -> recreateApipIndex(br, esClient, jedis, REWARD, rewardMappingJsonStr);
                case 5 -> recreateAllApipIndex(br, esClient, jedis);
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void recreateAllApipIndex(BufferedReader br, ElasticsearchClient esClient, Jedis jedis) throws IOException, InterruptedException {
        recreateApipIndex(br, esClient, jedis, ORDER, orderMappingJsonStr);
        recreateApipIndex(br, esClient, jedis, BALANCE, balanceMappingJsonStr);
        recreateApipIndex(br, esClient, jedis, REWARD, rewardMappingJsonStr);
    }

    public static void recreateApipIndex(BufferedReader br, ElasticsearchClient esClient, Jedis jedis, String indexName, String mappingJsonStr) {
        String index = getNameOfService(jedis, indexName);
        try {
            recreateIndex(index, esClient,mappingJsonStr);
        } catch (InterruptedException e) {
            log.debug("Recreate index {} wrong.",index);
        }
        Menu.anyKeyToContinue(br);
    }
    private static void listIndices(BufferedReader br) throws IOException {
        for (IndicesNames.Indices index : IndicesNames.Indices.values()) {
            System.out.println(index.sn()+". "+index.name().toLowerCase());
        }
        br.readLine();
    }

    public static void recreateIndex(String index, ElasticsearchClient esClient, String mappingJsonStr) throws InterruptedException {

        if(esClient==null) {
            System.out.println("Create a Java client for ES first.");
            return;
        }
        try {
            DeleteIndexResponse req = esClient.indices().delete(c -> c.index(index));

            if(req.acknowledged()) {
                log.debug("Index {} was deleted.", index);
            }
        }catch(ElasticsearchException | IOException e) {
            log.debug("Deleting index {} failed.", index,e);
        }

        TimeUnit.SECONDS.sleep(2);

        createIndex(index,esClient,mappingJsonStr);
    }

    public void checkApipIndices() throws IOException {

        String orderIndex = getNameOfService(jedis,ORDER);
        if ( noSuchIndex(esClient, orderIndex)) {
            createIndex(orderIndex,esClient,orderMappingJsonStr);
        }

        String balanceIndex = getNameOfService(jedis, BALANCE);
        if (noSuchIndex(esClient, balanceIndex)) {
            createIndex(balanceIndex,esClient,balanceMappingJsonStr);
        }

        String rewardIndex = getNameOfService(jedis, REWARD);
        if (noSuchIndex(esClient, rewardIndex)) {
            createIndex(rewardIndex,esClient,rewardMappingJsonStr);
        }
    }

    private static void createIndex(String index, ElasticsearchClient esClient, String mappingJsonStr) {

        InputStream orderJsonStrIs = new ByteArrayInputStream(mappingJsonStr.getBytes());
        try {
            CreateIndexResponse req = esClient.indices().create(c -> c.index(index).withJson(orderJsonStrIs));
            orderJsonStrIs.close();
            System.out.println(req.toString());
            if(req.acknowledged()) {
                log.debug("Index {} was created.", index);
            }else {
                log.debug("Creating index {} failed.", index);
            }
        }catch(ElasticsearchException | IOException e) {
            log.debug("Creating index {} failed.", index,e);
        }
    }
    private static boolean noSuchIndex(ElasticsearchClient esClient, String index) throws IOException {
        BooleanResponse result;
        result = esClient.indices().exists(e -> e.index(index));
        return !result.value();
    }
}
