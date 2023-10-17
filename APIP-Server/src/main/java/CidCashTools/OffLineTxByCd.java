package CidCashTools;

import APIP0V1_OpenAPI.*;
import apipClass.DataRequestBody;
import constants.ApiNames;
import constants.ReplyInfo;
import fchClass.Cash;
import walletTools.CryptoSign;
import walletTools.DataForOffLineTx;
import walletTools.SendTo;
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

import static constants.Constants.*;
import static walletTools.CryptoSign.parseDataForOffLineTxFromOther;


@WebServlet(ApiNames.ToolsPath + ApiNames.OffLineTxByCdAPI)
public class OffLineTxByCd extends HttpServlet {


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

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());

        DataForOffLineTx dataForSignInCs = parseDataForOffLineTxFromOther(requestBody.getFcdsl().getOther());

        //Check API
        if(dataForSignInCs==null) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String addrRequested = dataForSignInCs.getFromFid();

        long cd = 0;
        try {
            cd = dataForSignInCs.getCd();
            if(cd<=0){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("cd <= 0");
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

        CashListReturn cashListReturn = WalletTools.getCashForCd(addrRequested, cd,Initiator.esClient);

        List<Cash> meetList = cashListReturn.getCashList();
        if(meetList==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("No cashes meet the request: cd >="+cd+" and amount >="+amount+".");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        long totalValue = 0;
        for(Cash cash :meetList){
            totalValue += cash.getValue();
        }

        if(totalValue<amount+1000){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Cashes meeting this cd can't match the total amount of outputs.");
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