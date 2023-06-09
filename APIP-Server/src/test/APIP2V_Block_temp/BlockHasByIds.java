package APIP2V_Block_temp;

import APIP1V1_OpenAPI.*;
import data.BlockHas;
import initial.Initiator;
import startFCH.IndicesFCH;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.Constant.*;

@WebServlet(Constant.APIP2V1Path + Constant.BlockHasByIdsAPI)
public class BlockHasByIds extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(Constant.CodeInHeader,String.valueOf(Constant.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.
//        Map<String, String> sort = new HashMap<>();
//        sort.put("birthTime","desc");

        //Request
        String index = IndicesFCH.BlockHasIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<BlockHas> meetList;
        try {
            meetList = esRequest.doRequest(index,null, BlockHas.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(Constant.CodeInHeader,String.valueOf(Constant.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Map<String,BlockHas> meetMap = new HashMap<>();
        for(BlockHas blockHas :meetList){
            meetMap.put(blockHas.getId(),blockHas);
        }

        //response
        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        replier.setTotal(meetMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", Constant.BlockHasByIdsAPI));
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
