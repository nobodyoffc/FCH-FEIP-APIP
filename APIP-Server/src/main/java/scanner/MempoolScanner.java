package scanner;

import RPC.NewFcRpcClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import FchClass.Cash;
import FchClass.Tx;
import fcTools.ParseTools;
import parser.TxInMempool;
import redis.clients.jedis.Jedis;
import servers.NewEsClient;
import startAPIP.ConfigAPIP;
import startAPIP.RedisKeys;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static RPC.FcRpcMethods.getRawTx;
import static RPC.FcRpcMethods.getTxIds;
import static initial.Initiator.getEsPassword;
import static initial.Initiator.jedis0Common;
import static parser.RawTxParser.parseMempoolTx;

public class MempoolScanner extends Thread {
    /**
     * 1. getrawmempool 获得交易id数组
     * 2. getmempooltransaction 获得原始交易
     * 3. 解析原始交易，得到 1)input的id列表,2)output新cash列表
     * 4. 从Es获得input的cash信息
     * 5. redis开一个新库：1）id为address；2）key：income数量，income金额，spend数量，spend金额，net净变化。
     * 6. 将所有in和out按addr累加到redis
     * 7. API查询addrs,响应income数量，income金额，spend数量，spend金额，net净变化。
     */
    private final Jedis jedis3Unconfirmed;

    private final long IntervalSeconds = 5;
    private static final NewEsClient newEsClient = new NewEsClient();
    private ElasticsearchClient esClient = null;

    private Gson gson = new Gson();

    public static JsonRpcHttpClient fcClient;

    public MempoolScanner() {
        this.jedis3Unconfirmed = new Jedis();
        jedis3Unconfirmed.select(3);

        System.out.println("Create esClient for "+this.getClass());
        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis0Common.get(RedisKeys.ConfigFilePath));
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0) System.out.println("Es IP is null. Config first.");

            String rpcIp = configAPIP.getRpcIp();
            int rpcPort = configAPIP.getRpcPort();
            String rpcUser = configAPIP.getRpcUser();
            String rpcPassword = configAPIP.getRpcPassword();


            System.out.println("Create FcRpcClient for "+this.getClass());
            NewFcRpcClient newFcRpcClient = new NewFcRpcClient(rpcIp, rpcPort,rpcUser,rpcPassword);
            fcClient = newFcRpcClient.getClient();


            Jedis jedis0Common = new Jedis();
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
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        System.out.println("Scanning mempool...");
        while (true) {
            String[] txIds;

            txIds = getTxIds(fcClient);

            if(txIds==null){
                try {
                    TimeUnit.SECONDS.sleep(IntervalSeconds);
                } catch (InterruptedException e) {
                    continue;
                }
            }else {
                for (String txid : txIds) {
                    if(jedis3Unconfirmed.hget("tx",txid)==null){
                        System.out.println("Got unconfirmed TX : "+txid);
                        String rawTxHex = null;
                        try {
                            rawTxHex = getRawTx(fcClient, txid);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            System.out.println("Get raw tx of " + txid + " wrong.");
                        }
                        TxInMempool txInMempoolMap = null;
                        try {
                            txInMempoolMap = parseMempoolTx(esClient,rawTxHex, txid);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Parse tx of " + txid + " wrong.");
                        }
                        if (txInMempoolMap != null)
                            addMempoolTxToRedis(txInMempoolMap);
                    }
                }
            }
            try {
                TimeUnit.SECONDS.sleep(IntervalSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMempoolTxToRedis(TxInMempool txInMempoolMap) {
        Tx tx = txInMempoolMap.getTx();
        List<Cash> inList = txInMempoolMap.getInCashList();
        List<Cash> outList = txInMempoolMap.getOutCashList();

        addTxToRedis(tx);
        addSpendCashesToRedis(inList);
        addNewCashesToRedis(outList);

        //收入，支出，数量，笔数，净值
        addAddressInfoToRedis(inList,outList);

    }

    private void addTxToRedis(Tx tx) {
        jedis3Unconfirmed.hset("tx",tx.getTxId(),ParseTools.gsonString(tx));
    }

    private void addSpendCashesToRedis(List<Cash> inList) {
        for(Cash cash:inList){
            if(jedis3Unconfirmed.hget("spendCashes",cash.getCashId())==null) {
                jedis3Unconfirmed.hset("spendCashes", cash.getCashId(), ParseTools.gsonString(cash));
            }
            else{
                System.out.println("Double spend : "+ ParseTools.gsonString(cash));
            }
        }
    }

    private void addNewCashesToRedis(List<Cash> outList) {
        for(Cash cash:outList){
            jedis3Unconfirmed.hset("newCashes",cash.getCashId(),ParseTools.gsonString(cash));
        }
    }

    private void addAddressInfoToRedis(List<Cash> inList, List<Cash> outList) {

        for (Cash cash : inList) {
            String fid = cash.getFid();
            //income数量，income金额，spend数量，spend金额，net净变化
            int spendCount = 0;
            long spendValue = 0;
            String[] spendCashes = new String[0];
            String spendCountKey = "spendCount";
            String spendValueKey = "spendValue";
            String spendCashesKey = "spendCashes";

            String spendCountStr = jedis3Unconfirmed.hget(fid, spendCountKey);
            //Load existed values from redis
            if ( spendCountStr!= null) {
                spendCount = Integer.parseInt(spendCountStr);
                spendValue = Long.parseLong(jedis3Unconfirmed.hget(fid, spendValueKey));
                spendCashes = gson.fromJson(jedis3Unconfirmed.hget(fid,spendCashesKey),String[].class);
                if(spendCashes==null)spendCashes = new String[0];
            }
            spendValue += cash.getValue();
            spendCount++;
            String[] newSpendCashes = new String[spendCashes.length+1];
            System.arraycopy(spendCashes,0,newSpendCashes,0,spendCashes.length);
            newSpendCashes[newSpendCashes.length-1]=cash.getCashId();
            jedis3Unconfirmed.hset(fid, spendValueKey, String.valueOf(spendValue));
            jedis3Unconfirmed.hset(fid, spendCountKey, String.valueOf(spendCount));
            jedis3Unconfirmed.hset(fid,spendCashesKey,ParseTools.gsonString(newSpendCashes));
        }

        for (Cash cash : outList) {
            String addr = cash.getFid();
            //income数量，income金额，income数量，income金额，net净变化
            int incomeCount = 0;
            long incomeValue = 0;
            String[] incomeCashes = new String[0];
            String incomeCount1 = "incomeCount";
            String incomeValue1 = "incomeValue";
            String incomeCashesKey = "incomeCashes";
            if (jedis3Unconfirmed.hget(addr, incomeCount1) != null) {
                incomeCount = Integer.parseInt(jedis3Unconfirmed.hget(addr, incomeCount1));
                incomeValue = Long.parseLong(jedis3Unconfirmed.hget(addr, incomeValue1));
                incomeCashes = gson.fromJson(jedis3Unconfirmed.hget(addr,incomeCashesKey),String[].class);
                if(incomeCashes==null)incomeCashes = new String[0];
            }
            incomeValue += cash.getValue();
            incomeCount++;
            String[] newIncomeCashes = new String[incomeCashes.length+1];
            System.arraycopy(incomeCashes,0,newIncomeCashes,0,incomeCashes.length);
            newIncomeCashes[newIncomeCashes.length-1]=cash.getCashId();
            jedis3Unconfirmed.hset(addr, incomeValue1, String.valueOf(incomeValue));
            jedis3Unconfirmed.hset(addr, incomeCount1, String.valueOf(incomeCount));
            jedis3Unconfirmed.hset(addr,incomeCashesKey,ParseTools.gsonString(newIncomeCashes));
        }
    }
}