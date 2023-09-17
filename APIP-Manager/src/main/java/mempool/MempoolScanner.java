package mempool;

import freecashRPC.NewFcRpcClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import fchClass.Cash;
import fchClass.Tx;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.TxInMempool;
import redis.clients.jedis.Jedis;
import config.ConfigAPIP;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static constants.Strings.CONFIG;
import static constants.Strings.CONFIG_FILE_PATH;
import static freecashRPC.FcRpcMethods.getRawTx;
import static freecashRPC.FcRpcMethods.getTxIds;
import static parser.RawTxParser.parseMempoolTx;

public class MempoolScanner implements Runnable {
    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private static final Logger log = LoggerFactory.getLogger(MempoolScanner.class);
    private Jedis jedis = new Jedis();
    private final long IntervalSeconds = 5;
    private final ElasticsearchClient esClient;

    private final Gson gson = new Gson();

    public static JsonRpcHttpClient fcClient;

    public MempoolScanner(ElasticsearchClient esClient) {
        this.esClient = esClient;
        try {
            jedis.select(0);
        }catch (Exception e){
            log.error("Redis is not read when select 3 in mempool scanning.");
        }

        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis.hget(CONFIG,CONFIG_FILE_PATH));
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
        } catch (IOException e) {
            log.error("Preparing config failed: "+e.getMessage());
            throw new RuntimeException(e);
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
    }

    public void run() {
        jedis.select(3);
        log.debug("Scanning mempool.");
        System.out.println("Any key to continue...");

        while (running.get()) {
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
                    if(jedis.hget("tx",txid)==null){
                        log.debug("Got unconfirmed TX : "+txid);
                        String rawTxHex = null;
                        try {
                            rawTxHex = getRawTx(fcClient, txid);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            log.error("Get raw tx of " + txid + " wrong.");
                        }
                        TxInMempool txInMempoolMap = null;
                        try {
                            txInMempoolMap = parseMempoolTx(esClient,rawTxHex, txid);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("Parse tx of " + txid + " wrong.");
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
        jedis.hset("tx",tx.getTxId(),ParseTools.gsonString(tx));
    }

    private void addSpendCashesToRedis(List<Cash> inList) {
        for(Cash cash:inList){
            if(jedis.hget("spendCashes",cash.getCashId())==null) {
                jedis.hset("spendCashes", cash.getCashId(), ParseTools.gsonString(cash));
            }
            else{
                log.debug("Double spend : "+ ParseTools.gsonString(cash));
            }
        }
    }

    private void addNewCashesToRedis(List<Cash> outList) {
        for(Cash cash:outList){
            jedis.hset("newCashes",cash.getCashId(),ParseTools.gsonString(cash));
        }
    }

    private void addAddressInfoToRedis(List<Cash> inList, List<Cash> outList) {
        String txValueMapKey = "txValueMap";
        String netKey = "net";
        String spendCountKey = "spendCount";
        String spendValueKey = "spendValue";
        String spendCashesKey = "spendCashes";
        String incomeCountKey = "incomeCount";
        String incomeValueKey = "incomeValue";
        String incomeCashesKey = "incomeCashes";

        Map<String, Long> fidNetMap = new HashMap<>();

        for (Cash cash : inList) {
            String fid = cash.getOwner();
            String txId = cash.getSpendTxId();
            //income数量，income金额，spend数量，spend金额，fid的net净变化, 交易中净变化
            int spendCount = 0;
            long spendValue = 0;
            String[] spendCashes = new String[0];

            Map<String,Long> txValueMap;

            String netStr = jedis.hget(fid, netKey);
            long net;
            if(netStr==null) net = 0;
            else net = Long.parseLong(netStr);
            net = net-cash.getValue();

            String spendCountStr = jedis.hget(fid, spendCountKey);
            String spendValueStr = jedis.hget(fid, spendValueKey);
            String txValueMapStr = jedis.hget(fid, txValueMapKey);

            //Load existed values from redis
            if ( spendCountStr!= null) {
                spendCount = Integer.parseInt(spendCountStr);
                spendValue = Long.parseLong(spendValueStr);
                spendCashes = gson.fromJson(jedis.hget(fid,spendCashesKey),String[].class);
                if(spendCashes==null)spendCashes = new String[0];
            }
            spendValue += cash.getValue();

            spendCount++;
            String[] newSpendCashes = new String[spendCashes.length+1];
            System.arraycopy(spendCashes,0,newSpendCashes,0,spendCashes.length);
            newSpendCashes[newSpendCashes.length-1]=cash.getCashId();

            if(txValueMapStr!=null){
                Type mapType = new TypeToken<Map<String, Long>>(){}.getType();

                txValueMap = gson.fromJson(txValueMapStr,mapType);
            }else {
                txValueMap = new HashMap<>();
            }
            if(txValueMap.get(txId)!=null){
                txValueMap.put(txId,txValueMap.get(txId)-cash.getValue());
            }else{
                txValueMap.put(txId, -cash.getValue());
            }

            jedis.hset(fid, spendValueKey, String.valueOf(spendValue));
            jedis.hset(fid, spendCountKey, String.valueOf(spendCount));
            jedis.hset(fid,spendCashesKey,ParseTools.gsonString(newSpendCashes));
            jedis.hset(fid, netKey, String.valueOf(net));
            jedis.hset(fid, txValueMapKey, ParseTools.gsonString(txValueMap));
        }

        for (Cash cash : outList) {
            String fid = cash.getOwner();
            String txId = cash.getBirthTxId();
            //income数量，income金额，income数量，income金额，net净变化
            long net;
            String netStr = jedis.hget(fid, netKey);
            if(netStr==null) net = 0;
            else net = Long.parseLong(netStr);
            net = net + cash.getValue();

            int incomeCount = 0;
            long incomeValue = 0;
            String[] incomeCashes = new String[0];
            Map<String,Long> txValueMap;

            if (jedis.hget(fid, incomeCountKey) != null) {
                incomeCount = Integer.parseInt(jedis.hget(fid, incomeCountKey));
                incomeValue = Long.parseLong(jedis.hget(fid, incomeValueKey));
                incomeCashes = gson.fromJson(jedis.hget(fid,incomeCashesKey),String[].class);
                if(incomeCashes==null)incomeCashes = new String[0];
            }

            incomeValue += cash.getValue();

            incomeCount++;

            String[] newIncomeCashes = new String[incomeCashes.length+1];
            System.arraycopy(incomeCashes,0,newIncomeCashes,0,incomeCashes.length);
            newIncomeCashes[newIncomeCashes.length-1]=cash.getCashId();

            String txValueMapStr = jedis.hget(fid, txValueMapKey);
            if(txValueMapStr!=null){
                Type mapType = new TypeToken<Map<String, Long>>(){}.getType();
                txValueMap = gson.fromJson(txValueMapStr,mapType);
            }else {
                txValueMap = new HashMap<>();
            }

            txValueMap.merge(txId, cash.getValue(), Long::sum);

            jedis.hset(fid, incomeValueKey, String.valueOf(incomeValue));
            jedis.hset(fid, incomeCountKey, String.valueOf(incomeCount));
            jedis.hset(fid,incomeCashesKey,ParseTools.gsonString(newIncomeCashes));
            jedis.hset(fid, netKey, String.valueOf(net));
            jedis.hset(fid, txValueMapKey, ParseTools.gsonString(txValueMap));
        }

    }
    public void shutdown() {
        jedis.close();
        running.set(false);
    }
    public void restart(){
        jedis = new Jedis();
        running.set(true);
    }

    public AtomicBoolean getRunning() {
        return running;
    }
}
