package APIP18V1_Wallet;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.*;
import fchClass.Block;
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

import static constants.Strings.FID_BALANCE;


@WebServlet(ApiNames.APIP18V1Path + ApiNames.FeeRateAPI)
public class FeeRate extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        Double feeRate = calcFeeRate(Initiator.esClient);
        if(feeRate==null){
            replier.reply1020OtherError("Calculating fee rate wrong.");
        }else replier.setData(feeRate);
        if (Replier.makeSingleReplier(response, replier, dataCheckResult, addr)) return;

        writer.write(replier.reply0Success(addr));
    }


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

        Double feeRate = calcFeeRate(Initiator.esClient);
        if(feeRate==null){
            replier.reply1020OtherError("Calculating fee rate wrong.");
        }else replier.setData(feeRate);
        writer.write(replier.reply0Success());
    }

    public static Double calcFeeRate(ElasticsearchClient esClient) throws IOException {
        SearchResponse<Block> result = esClient.search(s -> s.index(IndicesNames.BLOCK).size(20).sort(sort -> sort.field(f -> f.field(Strings.HEIGHT).order(SortOrder.Desc))), Block.class);
        if(result==null || result.hits()==null)return null;
        List<Block> blockList = new ArrayList<>();
        Block expensiveBlock = new Block();
        expensiveBlock.setFee(0);
        for(Hit<Block> hit :result.hits().hits()){
            Block block = hit.source();
            if(block.getTxCount()==0) continue;

            blockList.add(block);
            if (block.getFee()>expensiveBlock.getFee())
                expensiveBlock = block;
        }
        if(blockList.isEmpty())return 0D;
        blockList.remove(expensiveBlock);
        if(blockList.isEmpty())return 0d;
        long feeSum=0;
        long netBlockSizeSum = 0;
        for(Block block :blockList){
            feeSum += block.getFee();
            netBlockSizeSum += block.getSize()- Constants.EMPTY_BLOCK_SIZE;
        }
        return  (double) (feeSum / netBlockSizeSum) /1000;
    }
}
