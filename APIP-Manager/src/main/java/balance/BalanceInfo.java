package balance;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.Strings;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import startAPIP.StartAPIP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static constants.Strings.*;

public class BalanceInfo {
    private static final Logger log = LoggerFactory.getLogger(BalanceInfo.class);
    private String user;
    private long bestHeight;
    private String consumeVia;
    private String orderVia;
    private String pending;

    public String getPending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }

    public static void deleteOldBalance(ElasticsearchClient esClient, Jedis jedis0Common) {
        String index = StartAPIP.getNameOfService(jedis0Common,BALANCE);
        long BALANCE_BACKUP_KEEP_MINUTES=144000;
        long height = ReadRedis.readLong(jedis0Common,BEST_HEIGHT)-BALANCE_BACKUP_KEEP_MINUTES;
        try {
            esClient.deleteByQuery(d -> d.index(index).query(q -> q.range(r -> r.field(BEST_HEIGHT).lt(JsonData.of(height)))));
        }catch (Exception e){
            log.error("Delete old balances in ES error",e);
        }
    }
    public String getConsumeVia() {
        return consumeVia;
    }

    public void setConsumeVia(String consumeVia) {
        this.consumeVia = consumeVia;
    }

    public static void recoverUserBalanceFromEs(ElasticsearchClient esClient, Jedis jedis) {
        Gson gson = new Gson();
        String index = StartAPIP.getNameOfService(jedis,BALANCE);

        String balancesStr = null;
        String viaTStr = null;
        try {
            SearchResponse<BalanceInfo> result = esClient.search(s -> s.index(index).size(1).sort(so -> so.field(f -> f.field(BEST_HEIGHT).order(SortOrder.Desc))), BalanceInfo.class);
            if(result.hits().hits().size()==0){
                System.out.println("No backup found in ES.");
                return;
            }
            balancesStr = Objects.requireNonNull(result.hits().hits().get(0).source()).getUser();
            viaTStr = Objects.requireNonNull(result.hits().hits().get(0).source()).getConsumeVia();
        } catch (IOException e) {
            log.error("Get balance from ES error when recovering balances and viaTStr.",e);
        }
        if(balancesStr!=null){
            Map<String,String> balanceMap = gson.fromJson(balancesStr, new TypeToken<HashMap<String,String>>(){}.getType());

            Map<String,String> viaTMap = gson.fromJson(viaTStr, new TypeToken<HashMap<String,String>>(){}.getType());
            for(String id: balanceMap.keySet()){
                jedis.hset(StartAPIP.serviceName+"_"+Strings.FID_BALANCE,id,balanceMap.get(id));
            }
            for(String id: viaTMap.keySet()){


                jedis.hset(StartAPIP.serviceName+"_"+CONSUME_VIA,id,viaTMap.get(id));
            }
            log.debug("Balances recovered from ES.");
        }else {
            log.debug("Failed recovered balances from ES.");
        }

        if(viaTStr!=null){
            Map<String,String> viaTMap = gson.fromJson(viaTStr, new TypeToken<HashMap<String,String>>(){}.getType());
            for(String id: viaTMap.keySet()){
                jedis.hset(StartAPIP.serviceName+"_"+CONSUME_VIA,id,viaTMap.get(id));
            }
            log.debug("Consuming ViaT recovered from ES.");
        }else {
            log.debug("Failed recovered consuming ViaT from ES.");
        }
    }

    public static void backupUserBalanceToEs(ElasticsearchClient esClient, Jedis jedis0Common)  {

        Map<String, String> balanceMap = jedis0Common.hgetAll(StartAPIP.serviceName+"_"+Strings.FID_BALANCE);
        Map<String, String> consumeViaMap = jedis0Common.hgetAll(StartAPIP.serviceName+"_"+CONSUME_VIA);
        Map<String, String> orderViaMap = jedis0Common.hgetAll(StartAPIP.serviceName+"_"+ORDER_VIA);
        Map<String, String> pendingStrMap = jedis0Common.hgetAll(REWARD_PENDING_MAP);
        Gson gson = new Gson();

        String balanceStr = gson.toJson(balanceMap);
        String consumeViaStr = gson.toJson(consumeViaMap);
        String orderViaStr = gson.toJson(orderViaMap);
        String pendingStr = gson.toJson(pendingStrMap);

        BalanceInfo balanceInfo = new BalanceInfo();

        balanceInfo.setUser(balanceStr);
        balanceInfo.setConsumeVia(consumeViaStr);
        balanceInfo.setOrderVia(orderViaStr);
        balanceInfo.setPending(pendingStr);

        long bestHeight = ReadRedis.readLong(jedis0Common,BEST_HEIGHT);

        balanceInfo.setBestHeight(bestHeight);

        String index = StartAPIP.getNameOfService(jedis0Common,BALANCE);

        IndexResponse result = null;
        try {
            result = esClient.index(i -> i.index(index).id(String.valueOf(bestHeight)).document(balanceInfo));
        } catch (IOException e) {
            log.error("Read ES wrong.",e);
        }

        if(result!=null){
            System.out.println("User balance backup: "+result.result().toString());
        }
        log.debug(result.result().jsonValue());

    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOrderVia() {
        return orderVia;
    }

    public void setOrderVia(String orderVia) {
        this.orderVia = orderVia;
    }

}
