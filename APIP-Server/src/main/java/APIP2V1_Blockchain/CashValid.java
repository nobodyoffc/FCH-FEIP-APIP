package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import apipClass.*;
import constants.*;
import fchClass.Cash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


@WebServlet(ApiNames.APIP2V1Path + ApiNames.CashValidAPI)
public class CashValid extends HttpServlet {

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
        ArrayList<Sort> sort = Sort.makeSortList("cd",true,"value",false,"cashId",true);

        //Add condition
        Fcdsl fcdsl = Fcdsl.addExceptTermsToFcdsl(requestBody, FieldNames.VALID, Values.FALSE);
        requestBody.setFcdsl(fcdsl);

        //Request
        String index = IndicesNames.CASH;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Cash> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Cash.class);
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
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getQuery()==null)
            return false;
        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("owner"))
            return false;
        return true;
    }
}