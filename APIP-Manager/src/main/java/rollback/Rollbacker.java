package rollback;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import fchClass.Block;
import order.Order;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import esTools.EsTools;
import constants.Strings;
import startAPIP.StartAPIP;

import static constants.IndicesNames.BLOCK;

import java.io.IOException;
import java.util.ArrayList;

import static constants.IndicesNames.ORDER;
import static constants.Strings.ORDER_LAST_HEIGHT;
import static fileTools.BlockFileTools.getBlockByHeight;

public class Rollbacker {
    /**
     * 检查上一个orderHeight与orderBid是否一致
     * 不一致则orderHeight减去30
     * 对回滚区块的es的order做减值处理。
    * */
    public static boolean isRolledBack(ElasticsearchClient esClient,long lastHeight,String lastBlockId) throws IOException {
        if(esClient==null) {
            System.out.println("Failed to check rollback. Start a ES client first.");
            return false;
        }

        if (lastHeight==0 || lastBlockId ==null)return false;
        Block block =null;
        try {
            block = EsTools.getById(esClient, BLOCK, lastBlockId, Block.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return block == null;
    }

    public static void rollback(ElasticsearchClient esClient, long height)  {
        ArrayList<Order> orderList = null;
        try {
            String index = StartAPIP.getNameOfService(ORDER);
            orderList= EsTools.getListSinceHeight(esClient, index,"height",height,Order.class);

            if(orderList==null || orderList.size()==0)return;
            minusFromBalance(esClient,orderList);

            Jedis jedis0Common = new Jedis();
            jedis0Common.set(StartAPIP.serviceName+"_"+ORDER_LAST_HEIGHT, String.valueOf(height));
            Block block = getBlockByHeight(esClient, height);
            jedis0Common.set(Strings.ORDER_LAST_BLOCK_ID, block.getBlockId());
            jedis0Common.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void minusFromBalance(ElasticsearchClient esClient, ArrayList<Order> orderList) throws Exception {
        ArrayList<String> idList= new ArrayList<>();
        try(Jedis jedis = StartAPIP.jedisPool.getResource()) {
            for (Order order : orderList) {
                String addr = order.getFromFid();
                long balance = ReadRedis.readHashLong(jedis, StartAPIP.serviceName + "_" + Strings.FID_BALANCE, addr);
                jedis.hset(StartAPIP.serviceName + "_" + Strings.FID_BALANCE, addr, String.valueOf(balance - order.getAmount()));

                idList.add(order.getOrderId());
            }
        }
        String index = StartAPIP.getNameOfService(ORDER);
        EsTools.bulkDeleteList(esClient, index, idList);
    }

}
