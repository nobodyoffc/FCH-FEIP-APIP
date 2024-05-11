package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.Sort;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Block;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.BlockSearchAPI)
public class BlockSearch extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API

        //Set default sort.
        ArrayList<Sort> sort = Sort.makeSortList("height",false,"blockId",true,null,null);

        //Request
        String index = IndicesNames.BLOCK;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Block> blockList;
        try {
            blockList = esRequest.doRequest(index,sort, Block.class);
            if(blockList==null||blockList.size()==0){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
//
//        List<String> idList = new ArrayList<>();
//        for(Block block : blockList){
//            idList.add(block.getBlockId());
//        }
//
//        List<BlockHas> blockHasList = null;
//        try {
//            blockHasList = EsTools.getMultiByIdList(Initiator.esClient, IndicesNames.BLOCK_HAS, idList, BlockHas.class).getResultList();
//            if(blockHasList==null||blockHasList.size()==0){
//                return;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
//            writer.write(replier.reply1012BadQuery(addr));
//        }
//
//        List<BlockInfo> meetList = BlockInfo.mergeBlockAndBlockHas(blockList, blockHasList);

        //response

        replier.setData(blockList);
        replier.setGot(blockList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }
}