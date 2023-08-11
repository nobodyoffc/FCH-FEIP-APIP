package order;

import balance.BalanceInfo;
import constants.Constants;
import constants.IndicesNames;
import fchClass.Cash;
import fchClass.TxHas;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.google.gson.Gson;
import fcTools.ParseTools;
import fchClass.OpReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import reward.RewardReturn;
import reward.Rewarder;
import rollback.Rollbacker;
import esTools.EsTools;
import service.ApipService;
import service.Params;
import config.ConfigAPIP;
import constants.Strings;
import startAPIP.StartAPIP;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static constants.Constants.*;
import static constants.IndicesNames.ORDER;
import static constants.IndicesNames.TX_HAS;
import static constants.Strings.*;
import static redisTools.ReadRedis.readHashLong;
import static startAPIP.StartAPIP.getNameOfService;

public class OrderScanner implements Runnable {
    private volatile Boolean running = true;
    private static final Logger log = LoggerFactory.getLogger(OrderScanner.class);
    public static  String serviceName;
    private final ElasticsearchClient esClient;
    private Jedis jedis0Common = new Jedis();
    private final Gson gson = new Gson();
    private ApipService service;

    private Params params=new Params();

    private final String listenDir;
    public OrderScanner(String listenPath, ElasticsearchClient esClient) {
        this.listenDir = listenPath;
        this.esClient = esClient;
        this.service = StartAPIP.service;
    }
    public Boolean isRunning(){
        return running;
    }

    public void run() {
        log.debug("Order scanner begin");
        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis0Common.hget(CONFIG,CONFIG_FILE_PATH));
        try {
            configAPIP =  configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
        } catch (IOException e) {
            log.error("Order scanner read config file wrong.");
            throw new RuntimeException(e);
        }

        if (configAPIP.getEsIp() == null || configAPIP.getEsPort() == 0) {
            log.error("Es IP is null. Config first.");
            return;
        }
        serviceName = configAPIP.getServiceName()+"_";

        service = gson.fromJson(jedis0Common.get(serviceName+Strings.SERVICE), ApipService.class);
        log.debug("Order scanner got the service. SID: {}",service.getSid());

        params = service.getParams();
        String serviceAccount = params.getAccount();
        if (serviceAccount == null) {
            log.error("No service account.");
            return;
        }

        log.debug("SID: " + service.getSid()
                + "\nService Name: "
                + service.getStdName()
                + "\nAccount: " + params.getAccount());

        int countBackUpBalance = 0;
        int countReward = 0;
        Rewarder rewarder = new Rewarder(esClient,jedis0Common);

        while (running) {
            checkIfNewStart();
            checkRollback();
            getNewOrders();

            countBackUpBalance++;
            countReward++;
            if (countBackUpBalance == BalanceBackupInterval) {
                try {
                    BalanceInfo.backupUserBalanceToEs(esClient,jedis0Common) ;
                    BalanceInfo.deleteOldBalance(esClient,jedis0Common);
                } catch (Exception e) {
                    log.error("Backup user balance, consumeVia, orderVia, or pending reward to ES wrong.",e);
                }
                countBackUpBalance = 0;
            }

            if (countReward == RewardInterval) {
                try {
                    RewardReturn result = rewarder.doReward();
                    if(result.getCode()!=0){
                        log.error(result.getClass()+": ["+result.getCode()+"] " +result.getMsg());
                    }
                } catch (Exception e) {
                    log.error("Do reward wrong.",e);
                }
                countReward = 0;
            }
            waitNewOrder();

        }
    }

    private void checkIfNewStart() {
        String lastHeightStr = jedis0Common.get(StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT);
        if(lastHeightStr==null){
            jedis0Common.set(StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT,"0");
            jedis0Common.set(StartAPIP.serviceName+"_"+Strings.ORDER_LAST_BLOCK_ID, Constants.zeroBlockId);
        }
    }

private void waitNewOrder() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    log.debug(LocalDateTime.now().format(formatter) + "  Wait for new order...");
    ParseTools.waitForNewItemInDirectory(listenDir,running);
}

    private void checkRollback() {
        long lastHeight = ReadRedis.readLong(jedis0Common, StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT);
        String lastBlockId = jedis0Common.get(StartAPIP.serviceName+"_"+Strings.ORDER_LAST_BLOCK_ID);
        try {
            if (Rollbacker.isRolledBack(esClient, lastHeight, lastBlockId))
                Rollbacker.rollback(esClient, jedis0Common,lastHeight - 30);
        } catch (IOException e) {
            log.debug("Order rollback wrong.");
            e.printStackTrace();
        } catch (Exception e) {
            log.debug("Order rollback wrong.");
            throw new RuntimeException(e);
        }
    }

    private void getNewOrders() {
        long lastHeight = ReadRedis.readLong(jedis0Common, StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT);
        ArrayList<Cash> cashList = getNewCashList(lastHeight,params.getAccount());
        if (cashList != null && cashList.size() > 0) {
//            log.debug("Got " + cashList.size() + " cashes of " + params.getAccount());
            setLastOrderInfoToRedis(cashList);
            getValidOrderList(cashList);
        }
    }

    private void getValidOrderList(ArrayList<Cash> cashList) {

        ArrayList<String> txidList = new ArrayList<>();

        long minPayment = (long) Double.parseDouble(params.getMinPayment()) * 100000000;

        ArrayList<Order> orderList = new ArrayList<>();
        Iterator<Cash> iterator = cashList.iterator();
        while (iterator.hasNext()) {
            Cash cash = iterator.next();
            if (cash.getValue() < minPayment) iterator.remove();

            txidList.add(cash.getBirthTxId());

            Order order = new Order();
            order.setOrderId(cash.getCashId());
            order.setAmount(cash.getValue());
            order.setHeight(cash.getBirthHeight());
            order.setTime(cash.getBirthTime());
            order.setToFid(cash.getFid());
            order.setTxId(cash.getBirthTxId());
            order.setTxIndex(cash.getBirthTxIndex());

            orderList.add(order);
        }
        if(txidList.size()==0)return;

        //Get sender info and check OpReturn;
        Map<String, OrderInfo> validOrderInfoMap = getValidSimpleOrderInfoMap(txidList);

        String isCheckOrderOpReturn = jedis0Common.hget(CONFIG,Strings.CHECK_ORDER_OPRETURN);
        if("true".equals(isCheckOrderOpReturn)) {
            Map<String, OrderInfo> validOpReturnOrderInfoMap = getValidOpReturnOrderInfoMap(txidList);
            //Merge simple order and OpReturn order;
            for (String txId : validOpReturnOrderInfoMap.keySet()) {
                validOrderInfoMap.put(txId, validOpReturnOrderInfoMap.get(txId));
            }
        }

        if (validOrderInfoMap.size() == 0) return;
        Iterator<Order> iterOrder = orderList.iterator();
        ArrayList<String> goodOrderIdList = new ArrayList<>();
        while (iterOrder.hasNext()) {
            Order order = iterOrder.next();
            OrderInfo orderInfo = validOrderInfoMap.get(order.getTxId());
            if (orderInfo == null) {
                iterOrder.remove();
                continue;
            }

            String payer = orderInfo.getSender();
            order.setFromFid(payer);
            if("true".equals(isCheckOrderOpReturn)) order.setVia(orderInfo.getVia());
            goodOrderIdList.add(order.getOrderId());

            long balance = readHashLong(jedis0Common, StartAPIP.serviceName+"_"+Strings.FID_BALANCE, payer);
            jedis0Common.hset(StartAPIP.serviceName+"_"+Strings.FID_BALANCE, payer, String.valueOf(balance + order.getAmount()));

            String via;
            if("true".equals(isCheckOrderOpReturn)){
                via = order.getVia();
                if(via!=null) {
                    long viaT = ReadRedis.readHashLong(jedis0Common, StartAPIP.serviceName+"_"+Strings.ORDER_VIA, via);
                    jedis0Common.hset(StartAPIP.serviceName+"_"+Strings.CONSUME_VIA, via, String.valueOf(viaT + order.getAmount()));
                }
            }

            log.debug("New order from [" + order.getFromFid() + "]: " + order.getAmount() / 100000000 + " F");
        }

        try {
            String index = getNameOfService(jedis0Common,ORDER);
            EsTools.bulkWriteList(esClient, index, orderList, goodOrderIdList, Order.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, OrderInfo> getValidOpReturnOrderInfoMap(ArrayList<String> txidList) {

        Map<String, OrderInfo> validOrderInfoMap = new HashMap<>();
        EsTools.MgetResult<OpReturn> result1 = new EsTools.MgetResult<>();

        try {
            result1 = EsTools.getMultiByIdList(esClient, IndicesNames.OPRETURN, txidList, OpReturn.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result1.getResultList() == null || result1.getResultList().size() == 0) return validOrderInfoMap;

        List<OpReturn> opReturnList = result1.getResultList();

        for (OpReturn opReturn : opReturnList) {
            try {
                String goodOp = ParseTools.strToJson(opReturn.getOpReturn());
                order.OrderOpReturn orderOpreturn = gson.fromJson(goodOp, OrderOpReturn.class);

                if(orderOpreturn == null){
                    log.debug("Invalid order. ID:"+opReturn.getTxId());
                    continue;
                }

                if (orderOpreturn.getType().equals("APIP")
                        && orderOpreturn.getSn().equals("1")
                        && orderOpreturn.getData().getOp().equals("buy")
                        && orderOpreturn.getData().getSid().equals(this.service.getSid())
                ) {
                    OrderInfo orderInfo = new OrderInfo();
                    orderInfo.setId(opReturn.getTxId());
                    orderInfo.setSender(opReturn.getSigner());
                    orderInfo.setVia(orderOpreturn.getData().getVia());
                    validOrderInfoMap.put(opReturn.getTxId(), orderInfo);
                }else{
                    //TODO
                    log.debug("Invalid order. ID:"+opReturn.getTxId());
                    ParseTools.gsonPrint(orderOpreturn);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return validOrderInfoMap;
    }

    private Map<String, OrderInfo> getValidSimpleOrderInfoMap(ArrayList<String> txidList) {
        Map<String, OrderInfo> validOrderInfoMap = new HashMap<>();
        EsTools.MgetResult<TxHas> result1 = new EsTools.MgetResult<>();

        try {
            result1 = EsTools.getMultiByIdList(esClient, TX_HAS, txidList, TxHas.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result1.getResultList() == null || result1.getResultList().size() == 0) return validOrderInfoMap;

        List<TxHas> txList = result1.getResultList();

        for (TxHas tx : txList) {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(tx.getTxId());
            String fid = tx.getInMarks().get(0).getFid();
            if(fid.equals(service.getParams().getAccount())||fid.equals(service.getOwner()))continue;
            orderInfo.setSender(fid);
            validOrderInfoMap.put(tx.getTxId(),orderInfo);
        }
        return validOrderInfoMap;
    }

    private void setLastOrderInfoToRedis(ArrayList<Cash> cashList) {
        long lastHeight = 0;
        String lastBlockId = null;
        for (Cash cash : cashList) {
            if (cash.getBirthHeight() > lastHeight) {
                lastHeight = cash.getBirthHeight();
                lastBlockId = cash.getBirthBlockId();
            }
        }
        jedis0Common.set(StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT, String.valueOf(lastHeight));
        jedis0Common.set(StartAPIP.serviceName+"_"+Strings.ORDER_LAST_BLOCK_ID, lastBlockId);
    }

    private ArrayList<Cash> getNewCashList(long lastHeight, String account) {
        ArrayList<Cash> cashList = new ArrayList<>();
        try {
            cashList = EsTools.rangeGt(
                    esClient,
                    IndicesNames.CASH,
                    "birthHeight",
                    lastHeight,
                    "cashId",
                    SortOrder.Asc,
                    "fid",
                    account,
                    //params.getAccount(),
                    Cash.class);
            if (cashList.size() == 0) return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cashList;
    }

    static class OrderInfo {
        private String id;
        private String sender;
        private String via;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getVia() {
            return via;
        }

        public void setVia(String via) {
            this.via = via;
        }
    }
    public void shutdown() {
        jedis0Common.close();
        running = false;
    }
    public void restart(){
        jedis0Common = new Jedis();
        running = true;
    }

}
