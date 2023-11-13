package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.Sort;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Cash;
import apipClass.Fcdsl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static walletTools.WalletTools.checkUnconfirmed;


@WebServlet(ApiNames.APIP18V1Path + ApiNames.CashValidLiveAPI)
public class CashValidLive extends HttpServlet {

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
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.
        ArrayList<Sort> sort = Sort.makeSortList("cd",true,"value",true,"cashId",true);

        //Add condition

        if(requestBody.getFcdsl()==null)requestBody.setFcdsl(new Fcdsl());
        requestBody.getFcdsl().setFilterTerms("valid","true");

        //Request
        String index = IndicesNames.CASH;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Cash> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Cash.class);
            if(meetList==null){
                return;
            }
            checkUnconfirmed(requestBody.getFcdsl().getQuery().getTerms().getValues()[0],meetList);
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());

    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getQuery()==null)
            return false;
        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("fid"))
            return false;
        return true;
    }
}