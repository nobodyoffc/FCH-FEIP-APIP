import co.elastic.clients.elasticsearch.ElasticsearchClient;
import config.ConfigAPIP;
import esTools.NewEsClient;
import javaTools.JsonTools;
import redis.clients.jedis.Jedis;
import reward.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static constants.Strings.*;

public class RewardTest {
    static Rewarder rewarder;
    static RewardInfo rewardInfo;
    public static void main(String[] args) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Result result = createClients();
        rewarder = new Rewarder(result.esClient);
//        setParams(result,br);
//        getLastOrderId(result.esClient);//uncheck
//        getUnpaidRewardInfoMap(result.esClient);//uncheck
//        checkPayment(result.esClient,null);//Uncheck
//        String account = getAccount(result,br);
//        long incomeT = makeIncomeT("",result);
//        rewarder.setIncomeT(incomeT);
//        System.out.println("IncomeT: "+incomeT);
//        RewardParams rewardParams = getRewardParams(result.jedis);
//        rewardInfo = makeRewardInfo(incomeT,rewardParams,result.jedis);
//        long paySum = rewarder.calcSumPay(rewardInfo);
//        System.out.println("Pay sum : "+paySum);
//
//        AffairMaker affairMaker = new AffairMaker(account, rewardInfo,result.esClient,result.jedis);
//
//        DataSignTx dataSignTx = new DataSignTx();
//        affairMaker.setDataSignTx(dataSignTx);
//        affairMaker.setRewardInfo(rewardInfo);
//
//        String affairSignTxJson = affairMaker.makeAffair();
//        System.out.println(affairSignTxJson);
//        Map<String, Long> pendingMap = affairMaker.getPendingMapFromRedis();
//        ParseTools.gsonPrint(pendingMap);

        RewardReturn result1 = rewarder.doReward();
        if(result1.getCode()>0)System.out.println(result1.getMsg());
        close(result);
    }

    private static RewardInfo makeRewardInfo(long incomeT, RewardParams rewardParams, Jedis jedis) {
        RewardInfo rewardInfo = rewarder.makeRewardInfo(incomeT, rewardParams);
        JsonTools.gsonPrint(rewardInfo);
        return rewardInfo;
    }

    private static RewardParams getRewardParams(Jedis jedis) {
        RewardParams rewardParams = Rewarder.getRewardParams();
        JsonTools.gsonPrint(rewardParams);
        return rewardParams;
    }

    private static long makeIncomeT(String s, Result result) {
        Rewarder rewarder = new Rewarder(result.esClient);
        return rewarder.makeIncomeT(null,result.esClient);
    }


    private static void checkPayment(ElasticsearchClient esClient, Map<String, RewardInfo> unpaidMap) {
    }

    private static Map<String, RewardInfo> getUnpaidRewardInfoMap(ElasticsearchClient esClient) {
        return null;
    }

    private static void getLastOrderId(ElasticsearchClient esClient) {
    }
    //TODO


    private static String getAccount(Result result, BufferedReader br) {
        try {
            return result.jedis.hget(result.jedis.hget(CONFIG,SERVICE_NAME)+"_"+ PARAMS_ON_CHAIN, ACCOUNT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setParams(Result result, BufferedReader br) {
        Rewarder rewarder = new Rewarder(result.esClient);
        rewarder.setRewardParameters(br);
    }

    private static void close(Result result) {
        try {
            result.newEsClient().shutdownClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.jedis().close();
    }

    private static Result createClients() {
        ConfigAPIP configAPIP = new ConfigAPIP();
        Jedis jedis0Common = new Jedis();
        configAPIP.setConfigFilePath(jedis0Common.hget(CONFIG,CONFIG_FILE_PATH));
        try {
            configAPIP =  configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
        } catch (IOException e) {
            System.out.println("Rewarding read config file wrong.");
            throw new RuntimeException(e);
        }
        NewEsClient newEsClient = new NewEsClient();
        ElasticsearchClient esClient=null;
        try{
            esClient = newEsClient.getEsClientSilent(configAPIP, jedis0Common);
            System.out.println("Es client created: "+esClient.info());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Creating ES client failed.");
        }
        Result result = new Result(jedis0Common, newEsClient,esClient);
        return result;
    }

    private record Result(Jedis jedis, NewEsClient newEsClient, ElasticsearchClient esClient) {
    }
}
