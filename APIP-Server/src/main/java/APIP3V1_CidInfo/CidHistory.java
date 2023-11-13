package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.Fcdsl;
import apipClass.Sort;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import identity.CidHist;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.CidHistoryAPI)
public class CidHistory extends HttpServlet {

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

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        List<CidHist> meetList;

        //Add filter
        if(requestBody.getFcdsl()==null)requestBody.setFcdsl(new Fcdsl());
        requestBody.getFcdsl().setFilterTerms("sn","3");

        //Set default sort.

        ArrayList<Sort> sort = Sort.makeSortList("height",false,"index",false,null,null);

        try {
            meetList = esRequest.doRequest(IndicesNames.CID_HISTORY, sort, CidHist.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

}
