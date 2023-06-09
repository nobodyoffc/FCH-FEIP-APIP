package APIP6V1_Service;

import APIP1V1_OpenAPI.*;
import construct.ServiceHistory;
import fc_dsl.Fcdsl;
import fc_dsl.Query;
import fc_dsl.Terms;
import initial.Initiator;
import startFEIP.IndicesFEIP;

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
import java.util.ArrayList;
import fc_dsl.Sort;
import static api.Constant.*;


@WebServlet(APIP6V1Path +ServiceRateHistoryAPI)
public class ServiceRateHistory extends HttpServlet {

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

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("height",false,"index",false,null,null);

        //Add condition
        if(requestBody.getFcdsl()==null)requestBody.setFcdsl(new Fcdsl());
        requestBody.getFcdsl().setFilterTerms("op","rate");

        //Request
        String index = IndicesFEIP.ServiceHistIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<ServiceHistory> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, ServiceHistory.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", ServiceRateHistoryAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

    }
}