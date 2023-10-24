package order;

import balance.BalanceInfo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.google.gson.Gson;
import config.ConfigAPIP;
import constants.Constants;
import constants.IndicesNames;
import constants.Strings;
import esTools.EsTools;
import fcTools.ParseTools;
import fchClass.Cash;
import fchClass.OpReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import reward.RewardReturn;
import reward.Rewarder;
import rollback.Rollbacker;
import service.ApipService;
import service.Params;
import startAPIP.StartAPIP;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static constants.Constants.BalanceBackupInterval;
import static constants.Constants.RewardInterval;
import static constants.IndicesNames.ORDER;
import static constants.Strings.*;
import static redisTools.ReadRedis.readHashLong;
import static startAPIP.StartAPIP.getNameOfService;

public class OrderScanner implements Runnable {
    private volatile AtomicBoolean running = new AtomicBoolean(true);
    private static final Logger log = LoggerFactory.getLogger(OrderScanner.class);
    public static  String serviceName;
    private final ElasticsearchClient esClient;
    private final Gson gson = new Gson();
    private ApipService service;

    private Params params=new Params();

    private final String listenDir;
    public OrderScanner(String listenPath, ElasticsearchClient esClient) {
        this.listenDir = listenPath;
        this.esClient = esClient;
        this.service = StartAPIP.service;
    }
    public AtomicBoolean isRunning(){
        return running;
    }

    public void run() {
        log.debug("Order scanner begin");
        ConfigAPIP configAPIP = new ConfigAPIP();
        try(Jedis jedis0Common = StartAPIP.jedisPool.getResource()) {
            configAPIP.setConfigFilePath(jedis0Common.hget(CONFIG, CONFIG_FILE_PATH));
            try {
                configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
            } catch (IOException e) {
                log.error("Order scanner read config file wrong.");
                throw new RuntimeException(e);
            }

            if (configAPIP.getEsIp() == null || configAPIP.getEsPort() == 0) {
                log.error("Es IP is null. Config first.");
                return;
            }
            serviceName = configAPIP.getServiceName() + "_";

            service = gson.fromJson(jedis0Common.get(serviceName + Strings.SERVICE), ApipService.class);
            log.debug("Order scanner got the service. SID: {}", service.getSid());

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
            System.out.println("Any Key to continue...");
            int countBackUpBalance = 0;
            int countReward = 0;
            Rewarder rewarder = new Rewarder(esClient);

            while (running.get()) {
                checkIfNewStart();
                checkRollback();
                getNewOrders();

                countBackUpBalance++;
                countReward++;
                if (countBackUpBalance == BalanceBackupInterval) {
                    try {
                        BalanceInfo.backupUserBalanceToEs(esClient);
                        BalanceInfo.deleteOldBalance(esClient);
                    } catch (Exception e) {
                        log.error("Backup user balance, consumeVia, orderVia, or pending reward to ES wrong.", e);
                    }
                    countBackUpBalance = 0;
                }

                if (countReward == RewardInterval) {
                    try {
                        RewardReturn result = rewarder.doReward();
                        if (result.getCode() != 0) {
                            log.error(result.getClass() + ": [" + result.getCode() + "] " + result.getMsg());
                        }
                    } catch (Exception e) {
                        log.error("Do reward wrong.", e);
                    }
                    countReward = 0;
                }
                waitNewOrder();
            }
        }
    }

    private void checkIfNewStart() {
        try(Jedis jedis0Common = StartAPIP.jedisPool.getResource()) {
            String lastHeightStr = jedis0Common.get(StartAPIP.serviceName + "_" + ORDER_LAST_HEIGHT);
            if (lastHeightStr == null) {
                jedis0Common.set(StartAPIP.serviceName + "_" + ORDER_LAST_HEIGHT, "0");
                jedis0Common.set(StartAPIP.serviceName + "_" + Strings.ORDER_LAST_BLOCK_ID, Constants.zeroBlockId);
            }
        }
    }

private void waitNewOrder() {
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    log.debug(LocalDateTime.now().format(formatter) + "  Wait for new order...");
    ParseTools.waitForChangeInDirectory(listenDir,running);
}

    private void checkRollback() {
        try(Jedis jedis0Common = StartAPIP.jedisPool.getResource()) {
            long lastHeight = ReadRedis.readLong(StartAPIP.serviceName + "_" + ORDER_LAST_HEIGHT);
            String lastBlockId = jedis0Common.get(StartAPIP.serviceName + "_" + Strings.ORDER_LAST_BLOCK_ID);
            try {
                if (Rollbacker.isRolledBack(esClient, lastHeight, lastBlockId))
                    Rollbacker.rollback(esClient, lastHeight - 30);
            } catch (IOException e) {
                log.debug("Order rollback wrong.");
                e.printStackTrace();
            } catch (Exception e) {
                log.debug("Order rollback wrong.");
                throw new RuntimeException(e);
            }
        }
    }

    private void getNewOrders() {
        long lastHeight = ReadRedis.readLong( StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT);
        ArrayList<Cash> cashList = getNewCashList(lastHeight,params.getAccount());
        if (cashList != null && cashList.size() > 0) {
            setLastOrderInfoToRedis(cashList);
            getValidOrderList(cashList);
        }
    }

    private void getValidOrderList(ArrayList<Cash> cashList) {
        try(Jedis jedis0Common = StartAPIP.jedisPool.getResource()) {
            ArrayList<Order> orderList = getNewOrderList(cashList);
            if (orderList.size() == 0) return;

            String isCheckOrderOpReturn = jedis0Common.hget(CONFIG, Strings.CHECK_ORDER_OPRETURN);
            Map<String, OrderInfo> validOpReturnOrderInfoMap;

            if ("true".equals(isCheckOrderOpReturn)) {
                ArrayList<String> txidList = getTxIdList(orderList);
                validOpReturnOrderInfoMap = getValidOpReturnOrderInfoMap(txidList);

                for (Order order : orderList) {
                    OrderInfo orderInfo = validOpReturnOrderInfoMap.get(order.getTxId());
                    if (orderInfo == null) continue;
                    String via = orderInfo.getVia();
                    if (via != null) order.setVia(via);
                }
            }

            ArrayList<String> orderIdList = new ArrayList<>();
            for (Order order : orderList) {
                String payer = order.getFromFid();
                if (payer != null) {
                    long balance = readHashLong(jedis0Common, StartAPIP.serviceName + "_" + Strings.FID_BALANCE, payer);
                    jedis0Common.hset(StartAPIP.serviceName + "_" + Strings.FID_BALANCE, payer, String.valueOf(balance + order.getAmount()));
                } else continue;

                String via = order.getVia();
                if (via != null) {
                    order.setVia(via);
                    long viaT = ReadRedis.readHashLong(jedis0Common, StartAPIP.serviceName + "_" + Strings.ORDER_VIA, via);
                    jedis0Common.hset(StartAPIP.serviceName + "_" + Strings.CONSUME_VIA, via, String.valueOf(viaT + order.getAmount()));
                }

                log.debug("New order from [" + order.getFromFid() + "]: " + order.getAmount() / 100000000 + " F");

                orderIdList.add(order.getOrderId());
            }
            try {
                String index = getNameOfService(ORDER);
                EsTools.bulkWriteList(esClient, index, orderList, orderIdList, Order.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> getTxIdList(ArrayList<Order> orderList) {
        ArrayList<String> txIdList = new ArrayList<>();
        for(Order order :orderList){
            txIdList.add(order.getTxId());
        }
        return txIdList;
    }

    private ArrayList<Order> getNewOrderList(ArrayList<Cash> cashList) {
        long minPayment = (long) Double.parseDouble(params.getMinPayment()) * 100000000;

        ArrayList<Order> orderList = new ArrayList<>();

        Iterator<Cash> iterator = cashList.iterator();
        while (iterator.hasNext()) {
            Cash cash = iterator.next();
            if (cash.getValue() < minPayment) {
                iterator.remove();
                continue;
            }
            String issuer = cash.getIssuer();
            if(issuer.equals(params.getAccount())||issuer.equals(service.getOwner())){
                iterator.remove();
                continue;
            }

            Order order = new Order();
            order.setOrderId(cash.getCashId());
            order.setFromFid(cash.getIssuer());
            order.setAmount(cash.getValue());
            order.setHeight(cash.getBirthHeight());
            order.setTime(cash.getBirthTime());
            order.setToFid(cash.getOwner());
            order.setTxId(cash.getBirthTxId());
            order.setTxIndex(cash.getBirthTxIndex());

            orderList.add(order);
        }
        return orderList;
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

                if(orderOpreturn != null && orderOpreturn.getType().equals("APIP")
                        && orderOpreturn.getSn().equals("0")
                        && orderOpreturn.getData().getOp().equals(Strings.IGNORE)){
                    continue;
                }
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setId(opReturn.getTxId());
                if (orderOpreturn != null
                        && orderOpreturn.getType().equals("APIP")
                        && orderOpreturn.getSn().equals("0")
                        && orderOpreturn.getData().getOp().equals(Strings.BUY)
                        && orderOpreturn.getData().getSid().equals(this.service.getSid())
                ) {
                    orderInfo.setVia(orderOpreturn.getData().getVia());
                }
                validOrderInfoMap.put(opReturn.getTxId(), orderInfo);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
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
        try(Jedis jedis0Common = StartAPIP.jedisPool.getResource()) {
            jedis0Common.set(StartAPIP.serviceName + "_" + ORDER_LAST_HEIGHT, String.valueOf(lastHeight));
            jedis0Common.set(StartAPIP.serviceName + "_" + Strings.ORDER_LAST_BLOCK_ID, lastBlockId);
        }
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
                    "owner",
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
        private String via;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVia() {
            return via;
        }

        public void setVia(String via) {
            this.via = via;
        }
    }
    public void shutdown() {
        running.set(false);
    }
    public void restart(){
        running.set(true);
    }

}
