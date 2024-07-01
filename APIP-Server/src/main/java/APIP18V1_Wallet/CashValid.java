package APIP18V1_Wallet;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.DataRequestHandler;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import apipClient.ApipDataGetter;
import constants.ApiNames;
import constants.FieldNames;
import constants.ReplyInfo;
import constants.Strings;
import fcTools.ParseTools;
import fchClass.Cash;
import initial.Initiator;
import walletTools.CashListReturn;
import walletTools.WalletTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static walletTools.WalletTools.getCashListForPay;


@WebServlet(ApiNames.APIP18V1Path + ApiNames.CashValidAPI)
public class CashValid extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if (dataCheckResult == null) return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if (!isThisApiRequest(requestBody)) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String addrRequested = requestBody.getFcdsl().getQuery().getTerms().getValues()[0];

        Map<String,String> paramMap = ApipDataGetter.getStringMap(requestBody.getFcdsl().getOther());

        String amountStr = paramMap.get(Strings.AMOUNT);
        long amount = 0;
        String cdStr = paramMap.get(Strings.CD);
        long cd = 0;

        String opReturnLengthStr = paramMap.get(FieldNames.OP_RETURN_LENGTH);
        int opReturnLength = 0;

        String outputNumStr = paramMap.get(FieldNames.OP_RETURN_LENGTH);
        int outputNum = 0;

        try {
            if(amountStr!=null){
                amount= ParseTools.coinToSatoshi(Double.parseDouble(amountStr));
                if(amount<0){
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    replier.setData("amount < 0");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
            }

            if(cdStr!=null){
                cd= Long.parseLong(cdStr);
                if(cd<0){
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    replier.setData("cd < 0");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
            }

            if(opReturnLengthStr!=null){
                opReturnLength = Integer.parseInt(opReturnLengthStr);
                if(opReturnLength<0){
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    replier.setData("opReturnLength < 0");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
            }

            if(outputNumStr!=null){
                outputNum= Integer.parseInt(outputNumStr);
                if(outputNum<0){
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    replier.setData("outputNum < 0");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
            }

        } catch (Exception e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(e);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        //response
        CashListReturn cashListReturn = WalletTools.getCashList(amount,cd,opReturnLength,outputNum,addrRequested,Initiator.esClient);

        if(cashListReturn.getCode()!=0){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(cashListReturn.getMsg());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        List<Cash> meetList = cashListReturn.getCashList();
        replier.setData(meetList);
        replier.setGot(meetList.size());
        replier.setTotal(cashListReturn.getTotal());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getQuery()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms().getValues().length!=1)
            return false;
        if(requestBody.getFcdsl().getOther()==null)
            return false;
        return true;
    }
}