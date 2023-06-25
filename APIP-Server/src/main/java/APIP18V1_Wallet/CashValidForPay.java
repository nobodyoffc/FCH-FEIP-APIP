package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import FchClass.Cash;
import APIP1V1_FCDSL.Sort;
import initial.Initiator;
import startFCH.IndicesFCH;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static api.Constant.*;
import static tools.WalletTools.getCashListForPay;


@WebServlet(APIP18V1Path + CashValidForPayAPI)
public class CashValidForPay extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if (dataCheckResult == null) return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if (!isThisApiRequest(requestBody)) {
            response.setHeader(CodeInHeader, String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.
        ArrayList<Sort> defaultSort = Sort.makeSortList("cd", true, "value", false, "cashId", true);

        //Request
        String index = IndicesFCH.CashIndex;
        String addrRequested = requestBody.getFcdsl().getQuery().getTerms().getValues()[0];

        long amount = 0;
        try {
            amount = (long)(Double.parseDouble((String)requestBody.getFcdsl().getOther())*100000000);
            if(amount<=0){
                response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
                replier.setData("amount <= 0");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        } catch (Exception e) {
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            replier.setData(e);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        //response
        List<Cash> meetList = getCashListForPay(amount,addrRequested,replier);
        if(meetList==null){
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
        }
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", CashValidForPayAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getQuery()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms()==null)
            return false;
        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("fid"))
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms().getValues().length!=1)
            return false;
        if(requestBody.getFcdsl().getOther()==null)
            return false;
        return true;
    }
}