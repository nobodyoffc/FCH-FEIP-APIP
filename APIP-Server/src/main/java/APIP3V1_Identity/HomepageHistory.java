package APIP3V1_Identity;

import APIP1V1_OpenAPI.*;
import fc_dsl.Fcdsl;
import fc_dsl.Filter;
import fc_dsl.Terms;
import identity.CidHist;
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
import java.util.List;

import fc_dsl.Sort;
import static api.Constant.*;

@WebServlet(APIP3V1Path +HomepageHistoryAPI)
public class HomepageHistory extends HttpServlet {

    @Override
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

        //Add filter
        if(requestBody.getFcdsl()==null)requestBody.setFcdsl(new Fcdsl());
        requestBody.getFcdsl().setFilterTerms("sn","9");

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("height",false,"index",false,null,null);

        //Request
        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<CidHist> meetList;
        try {
            meetList = esRequest.doRequest(IndicesFEIP.CidHistIndex, sort, CidHist.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Make data


        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", HomepageHistoryAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(),nPrice);

        return;
    }

}
