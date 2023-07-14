package rollback;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import constants.Constants;
import constants.Strings;
import fchClass.Block;
import order.Order;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import servers.EsTools;

import java.io.IOException;
import java.util.ArrayList;

import static fileTools.BlockFileTools.getBlockByHeight;
import static manager.StartManager.SERVICE_NAME;

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
            block = EsTools.getById(esClient, SERVICE_NAME + Constants.ORDER, lastBlockId, Block.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return block == null;
    }

    public static void rollback(ElasticsearchClient esClient, long height)  {
        ArrayList<Order> orderList = null;
        try {
            orderList= EsTools.getListSinceHeight(esClient, SERVICE_NAME + Constants.ORDER,"height",height,Order.class);

            if(orderList==null || orderList.size()==0)return;
            minusFromBalance(esClient,orderList);

            Jedis jedis0Common = new Jedis();
            jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT, String.valueOf(height));
            Block block = getBlockByHeight(esClient, height);
            assert block != null;
            jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID, block.getBlockId());
            jedis0Common.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void ccRollback(ElasticsearchClient esClient, long height)  {
        ArrayList<Order> orderList = null;
        try {
            orderList= EsTools.getListSinceHeight(esClient, SERVICE_NAME + Constants.ORDER,"height",height,Order.class);

            if(orderList==null || orderList.size()==0)return;
            minusFromBalance(esClient,orderList);

            Jedis jedis0Common = new Jedis();
            jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_HEIGHT, String.valueOf(height));
            Block block = getBlockByHeight(esClient, height);
            jedis0Common.set(SERVICE_NAME + Strings.ORDER_LAST_BLOCK_ID, block.getBlockId());
            jedis0Common.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void minusFromBalance(ElasticsearchClient esClient, ArrayList<Order> orderList) throws Exception {
        ArrayList<String> idList= new ArrayList<>();
        Jedis jedis = new Jedis();
        for(Order order: orderList){
            String addr = order.getFromAddr();
            long balance = ReadRedis.readHashLong(jedis, SERVICE_NAME + Strings.BALANCE, addr);
            jedis.hset(SERVICE_NAME + Strings.BALANCE,addr, String.valueOf(balance-order.getAmount()));

            idList.add(order.getCashId());
        }
        EsTools.bulkDeleteList(esClient, SERVICE_NAME + Constants.ORDER, idList);
    }

}
