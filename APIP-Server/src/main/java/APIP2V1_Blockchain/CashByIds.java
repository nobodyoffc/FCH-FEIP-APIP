package APIP2V1_Blockchain;

import APIP1V1_OpenAPI.*;
import FchClass.Cash;
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

@WebServlet(APIP2V1Path + CashByIdsAPI)
public class CashByIds extends HttpServlet {

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

        //Set default sort.
//        Map<String, String> sort = new HashMap<>();
//        sort.put("birthTime","desc");

        //Request
        String index = IndicesFCH.CashIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Cash> meetList;
        try {
            meetList = esRequest.doRequest(index,null, Cash.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Map<String,Cash> meetMap = new HashMap<>();
        for(Cash cash :meetList){
            meetMap.put(cash.getCashId(),cash);
        }

        //response

        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        replier.setTotal(meetMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", CashByIdsAPI));
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