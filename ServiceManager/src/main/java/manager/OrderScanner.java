package manager;

import API.Initiator;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.google.gson.Gson;
import config.ConfigService;
import constants.Constants;
import constants.IndicesNames;
import constants.Strings;
import fcTools.ParseTools;
import fchClass.Cash;
import fchClass.OpReturn;
import fchClass.TxHas;
import order.Order;
import order.OrderOpReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import rollback.Rollbacker;
import servers.EsTools;
import servers.NewEsClient;
import service.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static manager.StartManager.SERVICE_NAME;
import static redisTools.ReadRedis.readHashLong;

public class OrderScanner extends Thread {
    private final long IntervalMinutes = 1440;
    private ElasticsearchClient esClient = null;
    private Jedis jedis0Common;
    private final Gson gson = new Gson();
    private Service service;

    private service.Params params=new service.Params();

    private final String listenDir;
    private static final Logger log = LoggerFactory.getLogger(OrderScanner.class);
    public OrderScanner(String lishenDir) {
        this.listenDir = lishenDir;
    }
    public void run() {

        jedis0Common = new Jedis();
        service = gson.fromJson(jedis0Common.get(SERVICE_NAME + Strings.SERVICE_OBJECT), Service.class);

        params = service.getParams();

        String serviceAccount = params.getAccount();
        if (serviceAccount == null) {
            log.error("No service account.");
            return;
        }

        //Create ES client
        log.debug("Create esClient for " + this.getClass());

        ConfigService configAPIP = new ConfigService();
        configAPIP.setConfigFilePath(jedis0Common.get(Strings.CONFIG_FILE_PATH));
        NewEsClient newEsClient = new NewEsClient();

        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigService.class);
            if (configAPIP.getEsIp() == null || configAPIP.getEsPort() == 0) {
                log.error("Es IP is null. Config first.");
                return;
            }
            esClient = newEsClient.getEsClientSilent(configAPIP, jedis0Common);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.debug("SID: " + service.getSid()
                + "\nService Name: "
                + service.getStdName()
                + "\nAccount: " + params.getAccount());

        System.out.println("CidCashAccount: " + serviceAccount);

        int count = 0;
        while (true) {
            checkIfNewStart();
            reloadService();
            serviceAccount = getCidCashAccount(jedis0Common);
            checkRollback();
            checkCidCashRollback();
            getNewOrders();
            getNewCidCashOrders();
            count++;
            if (count == IntervalMinutes) {
                jedis0Common.save();
                backupBalanceToEs();
                count = 0;
            }
            waitNewBlock();
        }

    }
        private void getNewCidCashOrders() {
        long lastHeight = ReadRedis.readLong(jedis0Common, SERVICE_NAME + Strings.ORDER_LAST_HEIGHT);
        ArrayList<Cash> cashList = getNewCashList(lastHeight, SERVICE_NAME + Strings.ACCOUNT);
        if (cashList != null && cashList.size() > 0) {
//            System.out.println("Got " + cashList.size() + " cashes of " + params.getAccount());
            setLastCidCashOrderInfoToRedis(cashList);
            getValidOrderList(cashList);
        }
    }

    private void checkCidCashRollback() {
        long lastHeight = ReadRedis.readLong(jedis0Common, SERVICE_NAME + Strings.ORDER_LAST_HEIGHT);
        String lastBlockId = jedis0Common.get(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID);
        try {
            if (Rollbacker.isRolledBack(esClient, lastHeight, lastBlockId))
                Rollbacker.ccRollback(esClient, lastHeight - 30);
        } catch (IOException e) {
            System.out.println("Cid.cash order rollback wrong.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Cid.cash order rollback wrong.");
            throw new RuntimeException(e);
        }
    }

    private String getCidCashAccount(Jedis jedis0Common) {
        return jedis0Common.get(SERVICE_NAME + Strings.ACCOUNT);
    }

    private void checkIfNewStart() {
        String lastHeightStr = jedis0Common.get(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT);
        if(lastHeightStr==null){
            jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT,"0");
            jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID,"00000000cbe04361b1d6de82b893a7d8419e76e99dd2073ac0db2ba0e652eea8");
        }
    }

    private void waitNewBlock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(LocalDateTime.now().format(formatter) + "  Wait for new order...");
        ParseTools.waitForNewItemInDirectory(listenDir);
    }

    private void reloadService() {
        service = gson.fromJson(jedis0Common.get(SERVICE_NAME + Strings.SERVICE_OBJECT), Service.class);

        if (params.getPricePerRequest() != null) {
            Initiator.price = (long) (Double.parseDouble(params.getPricePerRequest()) * 100000000);
            Initiator.isPricePerRequest = true;
        }

        if (params.getPricePerKBytes() != null) {
            Initiator.price = (long) (Double.parseDouble(params.getPricePerKBytes()) * 100000000);
            Initiator.isPricePerKBytes = true;
        }
    }

    private void checkRollback() {
        long lastHeight = ReadRedis.readLong(jedis0Common, SERVICE_NAME + Strings.ORDER_LAST_HEIGHT);
        String lastBlockId = jedis0Common.get(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID);
        try {
            if (Rollbacker.isRolledBack(esClient, lastHeight, lastBlockId))
                Rollbacker.rollback(esClient, lastHeight - 30);
        } catch (IOException e) {
            System.out.println("Order rollback wrong.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Order rollback wrong.");
            throw new RuntimeException(e);
        }
    }

    private void getNewOrders() {
        long lastHeight = ReadRedis.readLong(jedis0Common, SERVICE_NAME + Strings.ORDER_LAST_HEIGHT);
        ArrayList<Cash> cashList = getNewCashList(lastHeight,params.getAccount());
        if (cashList != null && cashList.size() > 0) {
//            System.out.println("Got " + cashList.size() + " cashes of " + params.getAccount());
            setLastOrderInfoToRedis(cashList);
            getValidOrderList(cashList);
        }
    }

    private void getValidCidCashOrderList(ArrayList<Cash> cashList) {

        ArrayList<String> txidList = new ArrayList<>();

        long minPayment = (long) Double.parseDouble(jedis0Common.hget(SERVICE_NAME + Strings.PARAMS, Strings.MIN_PAYMENT)) * 100000000;

        ArrayList<Order> orderList = new ArrayList<>();
        Iterator<Cash> iterator = cashList.iterator();
        while (iterator.hasNext()) {
            Cash cash = iterator.next();
            if (cash.getValue() < minPayment) iterator.remove();

            txidList.add(cash.getBirthTxId());

            Order order = new Order();
            order.setCashId(cash.getCashId());
            order.setAmount(cash.getValue());
            order.setHeight(cash.getBirthHeight());
            order.setTime(cash.getBirthTime());
            order.setToAddr(cash.getFid());
            order.setTxId(cash.getBirthTxId());
            order.setTxIndex(cash.getBirthTxIndex());

            orderList.add(order);
        }

        if(txidList.size()==0)return;
        Map<String, OrderInfo> validOrderInfoMap = getOrderInfoMap(txidList);

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
            order.setFromAddr(payer);
            goodOrderIdList.add(order.getCashId());

            long balance = readHashLong(jedis0Common, SERVICE_NAME + Strings.BALANCE, payer);
            jedis0Common.hset(SERVICE_NAME + Strings.BALANCE, payer, String.valueOf(balance + order.getAmount()));
            System.out.println("New cid.cash order from [" + order.getFromAddr() + "]: " + order.getAmount() / 100000000 + " F");
        }

        try {
            EsTools.bulkWriteList(esClient, SERVICE_NAME + Constants.ORDER, orderList, goodOrderIdList, Order.class);
        } catch (Exception e) {
            e.printStackTrace();
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
            order.setCashId(cash.getCashId());
            order.setAmount(cash.getValue());
            order.setHeight(cash.getBirthHeight());
            order.setTime(cash.getBirthTime());
            order.setToAddr(cash.getFid());
            order.setTxId(cash.getBirthTxId());
            order.setTxIndex(cash.getBirthTxIndex());

            orderList.add(order);
        }

        if(txidList.size()==0)return;

        Map<String, OrderInfo> validOrderInfoMap = getValidOrderInfoMap(txidList);

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
            order.setFromAddr(payer);
            order.setVia(orderInfo.getVia());
            goodOrderIdList.add(order.getCashId());

            long balance = readHashLong(jedis0Common, SERVICE_NAME + Strings.BALANCE, payer);
            jedis0Common.hset(SERVICE_NAME + Strings.BALANCE, payer, String.valueOf(balance + order.getAmount()));
            System.out.println("New order from [" + order.getFromAddr() + "]: " + order.getAmount() / 100000000 + " F");
        }

        try {
            EsTools.bulkWriteList(esClient, SERVICE_NAME + Constants.ORDER, orderList, goodOrderIdList, Order.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, OrderInfo> getValidOrderInfoMap(ArrayList<String> txidList) {
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
                    System.out.println("Invalid order. ID:"+opReturn.getTxId());
                    continue;
                }
                if (orderOpreturn.getType().equals("APIP")
                        && orderOpreturn.getSn().equals("1")
                        && orderOpreturn.getData().getOp().equals("buy")
                        && orderOpreturn.getData().getSid().equals(service.getSid())
                ) {
                    OrderInfo orderInfo = new OrderInfo();
                    orderInfo.setId(opReturn.getTxId());
                    orderInfo.setSender(opReturn.getSigner());
                    orderInfo.setVia(orderOpreturn.getData().getVia());
                    validOrderInfoMap.put(opReturn.getTxId(), orderInfo);
                }else{
                    //TODO
                    System.out.println("Invalid order. ID:"+opReturn.getTxId());
                    ParseTools.gsonPrint(orderOpreturn);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return validOrderInfoMap;
    }

    private Map<String, OrderInfo> getOrderInfoMap(ArrayList<String> txidList) {
        Map<String, OrderInfo> validOrderInfoMap = new HashMap<>();
        EsTools.MgetResult<TxHas> result1 = new EsTools.MgetResult<>();

        try {
            result1 = EsTools.getMultiByIdList(esClient, IndicesNames.TX_HAS, txidList, TxHas.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result1.getResultList() == null || result1.getResultList().size() == 0) return validOrderInfoMap;

        List<TxHas> txList = result1.getResultList();

        for (TxHas tx : txList) {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(tx.getTxId());
            orderInfo.setSender(tx.getInMarks().get(0).getFid());
            validOrderInfoMap.put(tx.getTxId(),orderInfo);
        }
        return validOrderInfoMap;
    }

    private void setLastCidCashOrderInfoToRedis(ArrayList<Cash> cashList) {
        long lastHeight = 0;
        String lastBlockId = null;
        for (Cash cash : cashList) {
            if (cash.getBirthHeight() > lastHeight) {
                lastHeight = cash.getBirthHeight();
                lastBlockId = cash.getBirthBlockId();
            }
        }
        jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT, String.valueOf(lastHeight));
        jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID, lastBlockId);
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
        jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT, String.valueOf(lastHeight));
        jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID, lastBlockId);
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
    public void backupBalanceToEs()  {

        String lastHeight = jedis0Common.get(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT);
        Map<String, String> balanceMap = jedis0Common.hgetAll(SERVICE_NAME + Strings.BALANCE);
        balanceMap.put("height",String.valueOf(lastHeight));

        ArrayList<String> keyList = new ArrayList<>(balanceMap.keySet());
        ArrayList<String> valueList = new ArrayList<>(balanceMap.values());
        try {
            EsTools.bulkWriteList(esClient, SERVICE_NAME + Strings.BALANCE,valueList,keyList,String.class);
        } catch (Exception e) {
            log.error("Backup balance to ES error.");
        }
    }
    class OrderInfo {
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
}
