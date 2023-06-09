package APIP2V_Block_temp;

import APIP1V1_OpenAPI.*;
import data.TxHas;
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

@WebServlet(Constant.APIP2V1Path + Constant.TxHasByIdsAPI)
public class TxHasByIds extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API

        //Set default sort.

        //Request
        String index = IndicesFCH.TxHasIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<TxHas> meetList;
        try {
            meetList = esRequest.doRequest(index,null, TxHas.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(Constant.CodeInHeader,String.valueOf(Constant.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Map<String,TxHas> meetMap = new HashMap<>();
        for(TxHas txHas :meetList) {
            meetMap.put(txHas.getId(), txHas);
        }

        //response
        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        replier.setTotal(meetMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", Constant.TxHasByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }
}
