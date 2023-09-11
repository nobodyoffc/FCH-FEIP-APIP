package webhook;

import apipClass.WebhookInfo;
import apipRequest.PostRequester;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.Constants;
import constants.IndicesNames;
import constants.Strings;
import cryptoTools.SHA;
import esTools.EsTools;
import fcTools.ParseTools;
import fchClass.Cash;
import javaTools.BytesTools;
import org.bouncycastle.pqc.legacy.crypto.rainbow.util.GF2Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import startAPIP.StartAPIP;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static constants.Strings.*;

public class Pusher implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(Pusher.class);
    private boolean running;
    private final String listenDir;
    private ElasticsearchClient esClient;
    private Map<String,Map<String,WebhookInfo>> methodFidEndpointInfoMapMap;

    public Pusher(boolean running, String listenDir, ElasticsearchClient esClient) {
        this.running = running;
        this.listenDir = listenDir;
        this.esClient = esClient;
    }

    @Override
    public void run() {

        try(Jedis jedis = new Jedis()) {

            List<WebhookInfo> webhookInfoList;
            webhookInfoList = getWebhookInfoListFromEs(esClient);

            makeMethodFidEndpointMapMap(webhookInfoList);

            // <method:<owner:webhookInfo>>

            jedis.select(Constants.RedisDb4Webhook);
            jedis.flushDB();
            setNewCashByFidsMapMapIntoRedis(methodFidEndpointInfoMapMap,jedis);

            while (running) {
                readMethodFidWebhookInfoMapMapFromRedis(jedis);
                ParseTools.waitForChangeInDirectory(listenDir, running);
                TimeUnit.SECONDS.sleep(3);
                pushWebhooks(methodFidEndpointInfoMapMap);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void readMethodFidWebhookInfoMapMapFromRedis(Jedis jedis) {
        Gson gson = new Gson();
        methodFidEndpointInfoMapMap = new HashMap<>();

        //Method: newCashByFids
        Map<String, String> newCashByFidsHookInfoStrMap = jedis.hgetAll(ApiNames.NewCashByFidsAPI);
        Map<String,WebhookInfo> newCashByFidsHookInfoMap = new HashMap<>();
        for(String owner: newCashByFidsHookInfoStrMap.keySet()){
            String webhookInfoStr = newCashByFidsHookInfoStrMap.get(owner);
            newCashByFidsHookInfoMap.put(owner,gson.fromJson(webhookInfoStr, WebhookInfo.class));
        }
        methodFidEndpointInfoMapMap.put(ApiNames.NewCashByFidsAPI,newCashByFidsHookInfoMap);

        //More method:
    }

    private void setNewCashByFidsMapMapIntoRedis(Map<String, Map<String, WebhookInfo>> methodFidEndpointInfoMapMap, Jedis jedis) {
        Gson gson = new Gson();
        for(String method:methodFidEndpointInfoMapMap.keySet()){
            Map<String, WebhookInfo> ownerWebhookInfoMap = methodFidEndpointInfoMapMap.get(method);
            for(String owner:ownerWebhookInfoMap.keySet()){
                WebhookInfo webhookInfo = ownerWebhookInfoMap.get(owner);
                jedis.hset(method,owner,gson.toJson(webhookInfo));
            }
        }
    }

    private void makeMethodFidEndpointMapMap(List<WebhookInfo> webhookInfoList) {

        Map<String, WebhookInfo> newCashByFidsWebhookInfoMap = new HashMap<>();
        for (WebhookInfo webhookInfo : webhookInfoList) {
            switch (webhookInfo.getMethod()) {
                case ApiNames.NewCashByFidsAPI -> newCashByFidsWebhookInfoMap.put(webhookInfo.getOwner(), webhookInfo);
            }
        }
        methodFidEndpointInfoMapMap.put(ApiNames.NewCashByFidsAPI, newCashByFidsWebhookInfoMap);
    }

    private List<WebhookInfo> getWebhookInfoListFromEs(ElasticsearchClient esClient) {
        try {
            ArrayList<WebhookInfo> webhookInfoList = EsTools.getAllList(esClient,IndicesNames.WEBHOOK, Strings.HOOK_ID,SortOrder.Asc, WebhookInfo.class);
            return webhookInfoList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void pushWebhooks(Map<String, Map<String, WebhookInfo>> methodFidWatchedFidsMapMap) {
        for(String method: methodFidWatchedFidsMapMap.keySet()){
            switch (method){
                case ApiNames.NewCashByFidsAPI-> putNewCashByFids(methodFidWatchedFidsMapMap.get(method));
            }
        }
    }

    private void putNewCashByFids(Map<String, WebhookInfo> ownerWebhookInfoMap) {
        for(String owner:ownerWebhookInfoMap.keySet()){
            WebhookInfo webhookInfo = ownerWebhookInfoMap.get(owner);
            ArrayList<Cash> newCashList = getNewCashList(webhookInfo);
            if(newCashList==null)return;
            pushNewCashList(webhookInfo,newCashList);
        }
    }

    private void pushNewCashList(WebhookInfo webhookInfo, ArrayList<Cash> newCashList) {
        Gson gson = new Gson();
        String dataStr = gson.toJson(newCashList);
        byte[] dataBytes = dataStr.getBytes(StandardCharsets.UTF_8);
        String method = ApiNames.NewCashByFidsAPI;
        String sessionName ;
        String sessionKey;
        String balance;
        try(Jedis jedis = new Jedis()){

            balance = jedis.hget(Strings.FID_BALANCE,webhookInfo.getOwner());
            String nPrice = jedis.hget(Strings.N_PRICE, ApiNames.NewCashByFidsAPI);
            long price = Long.parseLong(jedis.hget(CONFIG,PRICE));
            float nPriceF = Float.parseFloat(nPrice);
            long balanceL = Long.parseLong(nPrice);
            boolean isPricePerRequest = Boolean.parseBoolean(jedis.hget(CONFIG, IS_PRICE_PER_REQUEST));
            if(isPricePerRequest){
                long newbalanceL = (long) (balanceL-(price*nPriceF));
                balance = String.valueOf(newbalanceL);
                jedis.hset(Strings.FID_BALANCE,webhookInfo.getOwner(),balance);
            }

            sessionName = jedis.hget(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME,webhookInfo.getOwner());
            jedis.select(1);
            sessionKey = jedis.hget(sessionName,Strings.SESSION_KEY);
        }catch (Exception e){
            log.error("Operate redis wrong when pushNewCashList.",e);
            return;
        }
        byte[] sessionKeyBytes = HexFormat.of().parseHex(sessionKey);

        String sign =HexFormat.of().formatHex(SHA.Sha256x2(BytesTools.bytesMerger(dataBytes,sessionKeyBytes)));

        String endpoint = webhookInfo.getEndpoint();


        HashMap<String, String> headMap = new HashMap<>();
        headMap.put(Constants.SIGN,sign);
        headMap.put(Constants.SESSION_NAME,sessionName);
        headMap.put(Constants.METHOD,method);
        headMap.put(Constants.BALANCE,balance);

        PostRequester.requestPost(endpoint,headMap,dataStr);
    }

    private ArrayList<Cash> getNewCashList(WebhookInfo webhookInfo) {
        String[] fids = (String[])webhookInfo.getData();
        try {
            return EsTools.getListByTerms(esClient,ApiNames.NewCashByFidsAPI,Strings.OWNER,fids,Strings.CASH_ID,SortOrder.Asc,Cash.class);
        } catch (IOException e) {
            log.error("Get new cash list for "+ApiNames.NewCashByFidsAPI+" from ES wrong.",e);
            return null;
        }
    }
}
