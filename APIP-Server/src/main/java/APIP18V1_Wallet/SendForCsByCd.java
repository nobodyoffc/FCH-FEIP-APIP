package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import FchClass.Cash;
import fcTools.CryptoSigner;
import fcTools.DataForSignInCs;
import fcTools.FcConstant;
import fcTools.SendTo;
import initial.Initiator;
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
import static fcTools.CryptoSigner.parseDataForSignInCsFromOther;


@WebServlet(APIP18V1Path + SendForCsByCdAPI)
public class SendForCsByCd extends HttpServlet {


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

        DataForSignInCs dataForSignInCs = parseDataForSignInCsFromOther(requestBody.getFcdsl().getOther());

        //Check API
        if(dataForSignInCs==null) {
            response.setHeader(CodeInHeader, String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String addrRequested = dataForSignInCs.getFromFid();

        long cd = 0;
        try {
            cd = dataForSignInCs.getCd();
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

        long amount = 0;
        if(dataForSignInCs.getSendToList()!=null) {
            for (SendTo sendTo : dataForSignInCs.getSendToList()) {
                if (sendTo.getAmount() < 0.0001) {
                    response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
                    replier.setData("The amount must be more than 0.0001 fch.");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
                amount += (long) (sendTo.getAmount() * FcConstant.FchToSatoshi);
            }
        }

        List<Cash> meetList = WalletTools.getCashForCd(replier, addrRequested, cd);

        if(meetList==null){
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        long totalValue = 0;
        for(Cash cash :meetList){
            totalValue += cash.getValue();
        }

        if(totalValue<amount+1000){
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            replier.setData("Cashes meeting this cd can't match the total amount of outputs.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }



        String rawTxForCs = CryptoSigner.makeRawTxForCs(dataForSignInCs,meetList);

        replier.setData(rawTxForCs);
        replier.setGot(1);
        replier.setTotal(1);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", SendForCsByCdAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }
}