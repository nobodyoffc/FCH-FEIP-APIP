package initial;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.google.gson.Gson;
import data.Cash;
import es.EsTools;
import fcTools.ParseTools;
import opReturn.OpReturn;
import order.Order;
import order.OrderOpReturn;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import rollback.Rollbacker;
import service.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static redisTools.ReadRedis.readHashLong;

public class OrderScanner extends Thread{
    private ElasticsearchClient esClient = null;
    private Jedis jedis0;
    private Gson gson = new Gson();
    private Service service = StartWeb.service;
    private Long price = StartWeb.price;

    public void run(){
        jedis0 = new Jedis();
        String ip = jedis0.get("esIp");
        int port = Integer.valueOf(jedis0.get("esPort"));
        try {
            esClient = NewEsClient.getClientHttp(ip,port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("SID: "+service.getSid()+"\nService Name: "+service.getStdName());

        int count = 0;
        while(true) {
            service = gson.fromJson(jedis0.get(RedisKeys.Service),Service.class);
            price = (long)(service.getParams().getPricePerRequest()*100000000);
            checkRollback();
            getNewOrders();

            count++;
            if(count==1440){
                jedis0.save();
                count = 0;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(LocalDateTime.now().format(formatter) +"  Scan new order...");
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkRollback() {
        long lastHeight = ReadRedis.readLong(jedis0, RedisKeys.OrderLastHeight);
        String lastBlockId = jedis0.get(RedisKeys.OrderLastBlockId);
        if(lastBlockId==null)return;
        try {
            if(Rollbacker.isRolledBack(esClient,lastHeight,lastBlockId)){
                try {
                    Rollbacker.rollback(esClient,lastHeight-30);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getNewOrders() {
        ArrayList<Cash> cashList = getNewCashList();

        if(cashList!=null) {
            setLastOrderInfoToRedis(cashList);
            filterValidOrderList(cashList);
        }
        return;
    }

    private void filterValidOrderList(ArrayList<Cash> cashList) {

        ArrayList<String> txidList = new ArrayList<>();

        long minPayment =  (long) service.getParams().getMinPayment()*100000000;

        ArrayList<Order> orderList = new ArrayList<>();
        Iterator<Cash> iterator = cashList.iterator();
        while(iterator.hasNext()){
            Cash cash = iterator.next();
            if(cash.getValue()<minPayment)iterator.remove();

            txidList.add(cash.getTxId());

            Order order = new Order();
            order.setId(cash.getId());
            order.setAmount(cash.getValue());
            order.setHeight(cash.getBirthHeight());
            order.setTime(cash.getBirthTime());
            order.setToAddr(cash.getAddr());
            order.setTxid(cash.getTxId());
            order.setTxIndex(cash.getTxIndex());

            orderList.add(order);

        }

        Map<String,String> senderMap = getSenderMap(orderList,txidList);

        ArrayList<Order> validOrderList = new ArrayList<Order>();
        ArrayList<String> idList = new ArrayList<>();

        for(Order order:orderList){
            String payer = senderMap.get(order.getTxid());
            if(payer!=null) {
                order.setFromAddr(payer);
                long balance = readHashLong(jedis0,RedisKeys.Balance,payer);
                jedis0.hset(RedisKeys.Balance, payer, String.valueOf(balance + order.getAmount()));
                System.out.println("New order from ["+order.getFromAddr()+"]: " + order.getAmount()/100000000+" F");
                validOrderList.add(order);
                idList.add(order.getId());
            }
        }

        try {
            EsTools.bulkWriteList(esClient, EsIndices.Order,validOrderList,idList,Order.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getSenderMap(ArrayList<Order> orderList, ArrayList<String> txidList) {

        EsTools.MgetResult<OpReturn> result1 = new EsTools.MgetResult<>();

        try {
            result1 = EsTools.getMultiByIdList(esClient, EsIndices.OpReturn, txidList, OpReturn.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<OpReturn> opReturnList = result1.getResultList();
        Map<String,String> senderMap = new HashMap<>();
        for(OpReturn opReturn: opReturnList){
            try {

                String goodOp = ParseTools.strToJson(opReturn.getOpReturn());

                order.OrderOpReturn orderOpreturn = gson.fromJson(goodOp, OrderOpReturn.class);

                if( orderOpreturn.getType().equals("APIP")
                        && orderOpreturn.getSn().equals("1")
                        && orderOpreturn.getData().getOp().equals("buy")
                        && orderOpreturn.getData().getSid().equals(service.getSid())
                ){
                    senderMap.put(opReturn.getId(), opReturn.getSigner());
                }
            }catch (Exception e){ }

        }
        return senderMap;
    }


    private void setLastOrderInfoToRedis(ArrayList<Cash> cashList) {
        Cash lastCash = cashList.get(cashList.size()-1);
        jedis0.set(RedisKeys.OrderLastHeight, String.valueOf(lastCash.getBirthHeight()));
        jedis0.set(RedisKeys.OrderLastBlockId, String.valueOf(lastCash.getBlockId()));
    }

    private ArrayList<Cash> getNewCashList() {
        ArrayList<Cash> cashList = new ArrayList<>();
        try {
            long lastHeight = ReadRedis.readLong(jedis0,RedisKeys.OrderLastHeight);
            cashList = EsTools.rangeGt(
                    esClient,
                    EsIndices.Cash,
                    "birthHeight",
                    lastHeight,
                    "birthHeight",
                    SortOrder.Asc,
                    500,
                    "addr",
                    service.getParams().getAccount(),
                    Cash.class);
            if(cashList.size()==0)return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return cashList;
    }
}
