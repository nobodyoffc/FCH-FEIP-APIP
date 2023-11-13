package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Cash;

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

@WebServlet(ApiNames.APIP2V1Path + ApiNames.CashByIdsAPI)
public class CashByIds extends HttpServlet {

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
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.
//        Map<String, String> sort = new HashMap<>();
//        sort.put("birthTime","desc");

        //Request
        String index = IndicesNames.CASH;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Cash> meetList;
        try {
            meetList = esRequest.doRequest(index,null, Cash.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
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
        esRequest.writeSuccess(dataCheckResult.getSessionKey());

        return;
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }
}
