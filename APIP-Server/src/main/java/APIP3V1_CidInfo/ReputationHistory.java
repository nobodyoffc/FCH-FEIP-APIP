package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import identity.RepuHist;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import APIP1V1_FCDSL.Sort;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.ReputationHistoryAPI)
public class ReputationHistory extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("height",false,"index",false,null,null);

        //Request
        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<RepuHist> meetList;
        try {
            meetList = esRequest.doRequest(IndicesNames.REPUTATION_HISTORY, sort, RepuHist.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", ApiNames.ReputationHistoryAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(),nPrice);

    }
}

