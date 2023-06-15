package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import FchClass.Cash;
import initial.Initiator;
import startFCH.IndicesFCH;
import tools.WalletTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static api.Constant.*;


@WebServlet(APIP18V1Path + CashValidForCdAPI)
public class CashValidForCd extends HttpServlet {

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

        //Request
        String index = IndicesFCH.CashIndex;
        String addrRequested = requestBody.getFcdsl().getQuery().getTerms().getValues()[0];

        long cd = 0;
        try {
            cd = Long.parseLong(requestBody.getFcdsl().getOther());
            if(cd<=0){
                response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
                replier.setData("cd <= 0");
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

//        //Set default sort.
//        ArrayList<Sort> defaultSort = Sort.makeSortList("cd", true, "value", true, "cashId", true);
//
//        List<SortOptions> sortOptionsList;
//        if(requestBody.getFcdsl().getSort()!=null) {
//            sortOptionsList = esRequest.getSortList(requestBody.getFcdsl().getSort());
//        } else {
//            sortOptionsList = esRequest.getSortList(defaultSort);
//        }

        List<Cash> meetList = WalletTools.getCashForCd(replier, addrRequested, cd);

        if(meetList==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        if(meetList.size()==0){
            response.setHeader(CodeInHeader,String.valueOf(Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", CashValidForCdAPI));
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