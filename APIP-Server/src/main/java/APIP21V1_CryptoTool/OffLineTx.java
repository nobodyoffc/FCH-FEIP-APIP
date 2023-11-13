package APIP21V1_CryptoTool;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.DataRequestHandler;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.ReplyInfo;
import fchClass.Cash;
import initial.Initiator;
import walletTools.CashListReturn;
import walletTools.CryptoSign;
import walletTools.DataForOffLineTx;
import walletTools.SendTo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static constants.Constants.FchToSatoshi;
import static walletTools.CryptoSign.parseDataForOffLineTxFromOther;
import static walletTools.WalletTools.getCashListForPay;


@WebServlet(ApiNames.APIP21V1Path + ApiNames.OffLineTxAPI)
public class OffLineTx extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if (dataCheckResult == null) return;

        String addr = dataCheckResult.getAddr();

//        if (RequestChecker.isPublicSessionKey(response, replier, writer, addr)) return;

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());

        DataForOffLineTx dataForSignInCs = parseDataForOffLineTxFromOther(requestBody.getFcdsl().getOther());

        //Check API
        if(dataForSignInCs==null) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String addrRequested = dataForSignInCs.getFromFid();

        long amount = 0;
        if(dataForSignInCs.getSendToList()!=null) {
            for (SendTo sendTo : dataForSignInCs.getSendToList()) {
                if (sendTo.getAmount() < 0.0001) {
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    replier.setData("The amount must be more than 0.0001 fch.");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
                amount += (long) (sendTo.getAmount() * FchToSatoshi);
            }
        }

        try {
            if(amount<=0){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("amount <= 0");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        } catch (Exception e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(e);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        //response
        List<Cash> meetList;
        CashListReturn cashListReturn = getCashListForPay(amount,addrRequested,Initiator.esClient);
        meetList = cashListReturn.getCashList();

        if(meetList==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        String rawTxForCs = CryptoSign.makeRawTxForCs(dataForSignInCs,meetList);

        replier.setData(rawTxForCs);
        replier.setGot(1);
        replier.setTotal(1);

        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

}