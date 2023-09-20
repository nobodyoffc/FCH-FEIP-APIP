package webhook;

import apipClass.WebhookInfo;
import apipClass.WebhookPushBody;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import startAPIP.StartAPIP;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static constants.Strings.*;

public class Pusher implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(Pusher.class);
    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private final String listenDir;
    private final ElasticsearchClient esClient;
    private Map<String,Map<String,WebhookInfo>> methodFidEndpointInfoMapMap = new HashMap<>();
    private long sinceHeight;

    public Pusher(String listenDir, ElasticsearchClient esClient) {
        this.esClient = esClient;
        this.listenDir = listenDir;
    }

    @Override
    public void run() {

        try(Jedis jedis = new Jedis()) {
            long bestHeight = Long.parseLong(jedis.get(BEST_HEIGHT));
            sinceHeight=bestHeight;
            List<WebhookInfo> webhookInfoList;
            webhookInfoList = getWebhookInfoListFromEs(esClient);
            makeMethodFidEndpointMapMap(webhookInfoList);

            // <method:<owner:webhookInfo>>

            jedis.select(Constants.RedisDb4Webhook);
            jedis.flushDB();
            setNewCashByFidsMapMapIntoRedis(methodFidEndpointInfoMapMap,jedis);

            while (running.get()) {
                jedis.select(Constants.RedisDb4Webhook);
                readMethodFidWebhookInfoMapMapFromRedis(jedis);
                ParseTools.waitForChangeInDirectory(listenDir, running);
                TimeUnit.SECONDS.sleep(3);
                jedis.select(0);
                bestHeight = Long.parseLong(jedis.get(BEST_HEIGHT));
                if(sinceHeight<bestHeight) {
                    pushWebhooks(methodFidEndpointInfoMapMap, sinceHeight);
                    sinceHeight=bestHeight;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.debug("Run pusher thread wrong.");
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
        if(methodFidEndpointInfoMapMap==null||methodFidEndpointInfoMapMap.size()==0)return;
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
        if(webhookInfoList==null || webhookInfoList.size()==0)return;
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
            return EsTools.getAllList(esClient,StartAPIP.serviceName.toLowerCase()+"_"+IndicesNames.WEBHOOK, Strings.HOOK_ID,SortOrder.Asc, WebhookInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Read webhook info failed.");
            return null;
        }
    }

    private void pushWebhooks(Map<String, Map<String, WebhookInfo>> methodFidWatchedFidsMapMap,long sinceHeight) {

        for(String method: methodFidWatchedFidsMapMap.keySet()){
            switch (method){
                case ApiNames.NewCashByFidsAPI-> putNewCashByFids(methodFidWatchedFidsMapMap.get(method),sinceHeight);
            }
        }
    }

    private void putNewCashByFids(Map<String, WebhookInfo> ownerWebhookInfoMap,long sinceHeight) {
        for(String owner:ownerWebhookInfoMap.keySet()){
            WebhookInfo webhookInfo = ownerWebhookInfoMap.get(owner);
            ArrayList<Cash> newCashList = getNewCashList(webhookInfo,sinceHeight);
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
        String bestHeight;
        try(Jedis jedis = new Jedis()){
            jedis.select(0);
            balance = jedis.hget(StartAPIP.serviceName+"_"+Strings.FID_BALANCE,webhookInfo.getOwner());
            if(balance==null)return;
            String nPrice = jedis.hget(StartAPIP.serviceName+"_"+Strings.N_PRICE, ApiNames.NewCashByFidsAPI);
            long price = (long)(Double.parseDouble(jedis.hget(CONFIG,PRICE))*Constants.FchToSatoshi);
            float nPriceF = Float.parseFloat(nPrice);
            long balanceL = Long.parseLong(balance);
            bestHeight = jedis.get(BEST_HEIGHT);
            boolean isPricePerRequest = Boolean.parseBoolean(jedis.hget(CONFIG, IS_PRICE_PER_REQUEST));
            if(isPricePerRequest){
                long newBalanceL = (long) (balanceL-(price*nPriceF));
                balance = String.valueOf(newBalanceL);
                jedis.hset(Strings.FID_BALANCE,webhookInfo.getOwner(),balance);
            }

            sessionName = jedis.hget(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME,webhookInfo.getOwner());
            if(sessionName==null)return;
            jedis.select(1);
            sessionKey = jedis.hget(sessionName,Strings.SESSION_KEY);
        }catch (Exception e){
            log.error("Operate redis wrong when pushNewCashList.",e);
            return;
        }
        byte[] sessionKeyBytes = HexFormat.of().parseHex(sessionKey);

        String sign =HexFormat.of().formatHex(SHA.Sha256x2(BytesTools.bytesMerger(dataBytes,sessionKeyBytes)));

        String endpoint = webhookInfo.getEndpoint();

        WebhookPushBody postBody = new WebhookPushBody();

        postBody.setData(dataStr);
        postBody.setBalance(balance);
        postBody.setBestHeight(bestHeight);
        postBody.setFromSid(StartAPIP.service.getSid());
        postBody.setMethod(method);
        postBody.setSessionName(sessionName);
        postBody.setSign(sign);

        String result = PostRequester.requestPost(endpoint,null,gson.toJson(postBody));
        System.out.println(result);
    }

    private ArrayList<Cash> getNewCashList(WebhookInfo webhookInfo,long sinceHeight) {
        Gson gson = new Gson();

        DataOfNewCashListByIds data = gson.fromJson(gson.toJson(webhookInfo.getData()),DataOfNewCashListByIds.class);
        String[] fids = data.getFids();
        try {
            return EsTools.getListByTermsSinceHeight(esClient,IndicesNames.CASH,Strings.OWNER,fids,sinceHeight,Strings.CASH_ID,SortOrder.Asc,Cash.class);
        } catch (IOException e) {
            log.error("Get new cash list for "+ApiNames.NewCashByFidsAPI+" from ES wrong.",e);
            return null;
        }
    }
    public void shutdown() {
        running.set(false);
    }
    public AtomicBoolean isRunning() {
        return running;
    }

    public void setRunning(AtomicBoolean running) {
        this.running = running;
    }
}