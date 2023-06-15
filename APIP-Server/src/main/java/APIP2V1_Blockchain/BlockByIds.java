package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import FchClass.Block;
import FchClass.BlockHas;
import data.BlockInfo;
import initial.Initiator;
import startFCH.IndicesFCH;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.Constant.*;

@WebServlet(APIP2V1Path + BlockByIdsAPI)
public class BlockByIds extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Request

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<BlockHas> blockHasList;
        try {
            blockHasList = esRequest.doRequest(IndicesFCH.BlockHasIndex,null, BlockHas.class);
            if(blockHasList==null||blockHasList.size()==0){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        List<String> idList = new ArrayList<>();
        for(BlockHas blockHas : blockHasList){
            idList.add(blockHas.getBlockId());
        }

        List<Block> blockList = null;
        try {
            blockList = servers.EsTools.getMultiByIdList(Initiator.esClient, IndicesFCH.BlockIndex, idList, Block.class).getResultList();
            if(blockList==null||blockList.size()==0){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
        }

        List<BlockInfo> meetList = BlockInfo.mergeBlockAndBlockHas(blockList, blockHasList);

        Map<String,BlockInfo> meetMap = new HashMap<>();
        for(BlockInfo blockInfo :meetList){
            meetMap.put(blockInfo.getId(),blockInfo);
        }

        //response

        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        replier.setTotal(meetMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", BlockByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

        return;
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }
}
