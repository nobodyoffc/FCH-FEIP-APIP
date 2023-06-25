package scanner;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import FchClass.Block;
import FchClass.Cash;
import fcTools.ParseTools;
import initial.Initiator;
import opReturn.OpReturn;
import order.Order;
import order.OrderOpReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.BlockFileTools;

import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import rollback.Rollbacker;
import servers.EsTools;
import servers.NewEsClient;
import service.ApipService;
import service.Params;
import startAPIP.ConfigAPIP;
import startAPIP.IndicesAPIP;
import startAPIP.RedisKeys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static initial.Initiator.getEsPassword;
import static redisTools.ReadRedis.readHashLong;

public class OrderScanner extends Thread {
    private final long IntervalMinutes = 1440;
    private ElasticsearchClient esClient = null;
    private Jedis jedis0Common;
    private final Gson gson = new Gson();
    private ApipService service;

    private Params params=new Params();

    private final String opReturnFileStr;
    private static final Logger log = LoggerFactory.getLogger(OrderScanner.class);
    public OrderScanner(String opReturnFile) {
        this.opReturnFileStr = opReturnFile;
    }
    public void run() {

        jedis0Common = new Jedis();
        service = gson.fromJson(jedis0Common.get(IndicesAPIP.ServiceIndex), ApipService.class);
        params = service.getParams();
        //Create ES client
        log.debug("Create esClient for "+this.getClass());

        ConfigAPIP configAPIP = new ConfigAPIP();
        configAPIP.setConfigFilePath(jedis0Common.get(RedisKeys.ConfigFilePath));
        NewEsClient newEsClient = new NewEsClient();

        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0) System.out.println("Es IP is null. Config first.");

            if(jedis0Common.get(RedisKeys.EsPasswordCypher)!=null){
                String esPassword = getEsPassword(configAPIP,jedis0Common);
                if(esPassword==null){
                    log.error("Decrypting ES password from redis failed.");
                    return;
                }
                esClient = newEsClient.getClientHttps(configAPIP.getEsIp(), configAPIP.getEsPort(),configAPIP.getEsUsername(),esPassword);
            }else{
                esClient = newEsClient.getClientHttp(configAPIP.getEsIp(), configAPIP.getEsPort());
            }
            if (esClient == null) {
                newEsClient.shutdownClient();
                log.error("Creating ES client failed for order scanner.");
                return;
            }
        } catch (Exception e) {
            log.error("Some thing wrong when creating ES client for order scanner.");
            throw new RuntimeException(e);
        }

        System.out.println("SID: " + service.getSid()
                + "\nService Name: "
                + service.getStdName()
                + "\nAccount: " + params.getAccount());

        int count = 0;
        while (true) {
            checkIfNewStart();
            reloadService();
            checkRollback();
            getNewOrders();
            count++;
            if (count == IntervalMinutes) {
                jedis0Common.save();
                backupBalanceToEs() ;
                count = 0;
            }
            waitNewBlock();
        }
    }
    private void checkIfNewStart() {
        String lastHeightStr = jedis0Common.get(RedisKeys.OrderLastHeight);
        if(lastHeightStr==null){
            jedis0Common.set(RedisKeys.OrderLastHeight,"0");
            jedis0Common.set(RedisKeys.OrderLastBlockId,"00000000cbe04361b1d6de82b893a7d8419e76e99dd2073ac0db2ba0e652eea8");
        }
    }

    private void checkIfNewStart1() {
        long lastHeight;
        String lastBlockId = jedis0Common.get(RedisKeys.BestBlockId);
        if (lastBlockId==null) {
            System.out.println("Input the beginning height for scanning. Enter to start with the best height");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                String input = br.readLine();
                if ("".equals(input)) {
                    jedis0Common.set(RedisKeys.OrderLastHeight, jedis0Common.get(RedisKeys.BestHeight));
                    jedis0Common.set(RedisKeys.OrderLastBlockId, jedis0Common.get(RedisKeys.BestBlockId));
                } else {
                    lastHeight = Long.parseLong(input);
                    jedis0Common.set(RedisKeys.OrderLastHeight, String.valueOf(lastHeight));
                    Block block = BlockFileTools.getBlockByHeight(esClient, lastHeight);
                    if (block != null) {
                        lastBlockId = block.getBlockId();
                        jedis0Common.set(RedisKeys.OrderLastBlockId, lastBlockId);
                    } else {
                        throw new Throwable("Wrong height.");
                    }
                }
                br.close();
            } catch (IOException e) {
                System.out.println("Failed to set order height.");
                throw new RuntimeException(e);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitNewBlock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(LocalDateTime.now().format(formatter) + "  Wait for new order...");
        ParseTools.waitForNewItemInFile(opReturnFileStr);
    }

    private void reloadService() {
        service = gson.fromJson(jedis0Common.get(RedisKeys.Service), ApipService.class);

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
        long lastHeight = ReadRedis.readLong(jedis0Common, RedisKeys.OrderLastHeight);
        String lastBlockId = jedis0Common.get(RedisKeys.OrderLastBlockId);
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
        ArrayList<Cash> cashList = getNewCashList();
        if (cashList != null && cashList.size() > 0) {
//            System.out.println("Got " + cashList.size() + " cashes of " + params.getAccount());
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
            order.setVia(orderInfo.getVias());
            goodOrderIdList.add(order.getCashId());

            long balance = readHashLong(jedis0Common, RedisKeys.Balance, payer);
            jedis0Common.hset(RedisKeys.Balance, payer, String.valueOf(balance + order.getAmount()));
            System.out.println("New order from [" + order.getFromAddr() + "]: " + order.getAmount() / 100000000 + " F");
        }

        try {
            EsTools.bulkWriteList(esClient, IndicesAPIP.OrderIndex, orderList, goodOrderIdList, Order.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, OrderInfo> getValidOrderInfoMap(ArrayList<String> txidList) {
        Map<String, OrderInfo> validOrderInfoMap = new HashMap<>();
        EsTools.MgetResult<OpReturn> result1 = new EsTools.MgetResult<>();

        try {
            result1 = EsTools.getMultiByIdList(esClient, IndicesAPIP.OpReturnIndex, txidList, OpReturn.class);
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
                    orderInfo.setVias(orderOpreturn.getData().getVia());
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

    private void setLastOrderInfoToRedis(ArrayList<Cash> cashList) {
        long lastHeight = 0;
        String lastBlockId = null;
        for (Cash cash : cashList) {
            if (cash.getBirthHeight() > lastHeight) {
                lastHeight = cash.getBirthHeight();
                lastBlockId = cash.getBirthBlockId();
            }
        }
        jedis0Common.set(RedisKeys.OrderLastHeight, String.valueOf(lastHeight));
        jedis0Common.set(RedisKeys.OrderLastBlockId, lastBlockId);
    }

    private ArrayList<Cash> getNewCashList() {
        ArrayList<Cash> cashList = new ArrayList<>();
        try {
            long lastHeight = ReadRedis.readLong(jedis0Common, RedisKeys.OrderLastHeight);
            cashList = EsTools.rangeGt(
                    esClient,
                    IndicesAPIP.CashIndex,
                    "birthHeight",
                    lastHeight,
                    "cashId",
                    SortOrder.Asc,
                    "fid",
                    params.getAccount(),
                    Cash.class);
            if (cashList.size() == 0) return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cashList;
    }
    public void backupBalanceToEs()  {
        Map<String,Object> balanceMap = new HashMap<>();
        String lastHeight = jedis0Common.get(RedisKeys.OrderLastHeight);
        balanceMap.put("balance",jedis0Common.hgetAll(RedisKeys.Balance));
        balanceMap.put("height",lastHeight);
        try {
            esClient.index(i->i.index(IndicesAPIP.BalanceHistory).id(lastHeight).document(balanceMap));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to backup balance.");
        }

        try {
            DeleteByQueryResponse result = esClient.deleteByQuery(d -> d.index(IndicesAPIP.BalanceHistory).query(q -> q.range(r -> r.field("height").lt(JsonData.of(Long.parseLong(lastHeight) - (IntervalMinutes * 10))))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class OrderInfo {
        private String id;
        private String sender;
        private String[] vias;

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

        public String[] getVias() {
            return vias;
        }

        public void setVias(String[] vias) {
            this.vias = vias;
        }
    }
}
