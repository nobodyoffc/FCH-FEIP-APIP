package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import apipClass.BlockInfo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import constants.Strings;
import esTools.EsTools;
import fchClass.Block;
import fchClass.BlockHas;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.FreeGetPath + ApiNames.GetBestBlockAPI)
public class GetBestBlock extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }
        ElasticsearchClient esClient = Initiator.esClient;

        Block bestBlock = EsTools.getBestBlock(esClient);

        BlockHas blockHas = EsTools.getById(esClient, IndicesNames.BLOCK_HAS, bestBlock.getBlockId(), BlockHas.class);

        List<Block> blockList = new ArrayList<>();
        blockList.add(bestBlock);

        List<BlockHas> blockHasList = new ArrayList<>();
        blockHasList.add(blockHas);

        List<BlockInfo> blockInfos = BlockInfo.mergeBlockAndBlockHas(blockList,blockHasList);


        replier.setTotal(1L);
        replier.setGot(1);
        replier.setData(blockInfos.get(0));
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}