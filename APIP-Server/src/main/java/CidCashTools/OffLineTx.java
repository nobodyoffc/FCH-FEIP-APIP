package CidCashTools;

import APIP0V1_OpenAPI.*;
import constants.ApiNames;
import constants.ReplyInfo;
import fchClass.Cash;
import fcTools.CryptoSigner;
import fcTools.DataForOffLineTx;
import fcTools.FcConstant;
import fcTools.SendTo;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static constants.Constants.*;
import static fcTools.CryptoSigner.parseDataForOffLineTxFromOther;
import static tools.WalletTools.getCashListForPay;


@WebServlet(ToolsPath + ApiNames.OffLineTxAPI)
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

        if (RequestChecker.checkPublicSessionKey(response, replier, writer, addr)) return;

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

        long amount = 0;
        if(dataForSignInCs.getSendToList()!=null) {
            for (SendTo sendTo : dataForSignInCs.getSendToList()) {
                if (sendTo.getAmount() < 0.0001) {
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    replier.setData("The amount must be more than 0.0001 fch.");
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
                amount += (long) (sendTo.getAmount() * FcConstant.FchToSatoshi);
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
        List<Cash> meetList = getCashListForPay(amount,addrRequested,replier);
        if(meetList==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        String rawTxForCs = CryptoSigner.makeRawTxForCs(dataForSignInCs,meetList);

        replier.setData(rawTxForCs);
        replier.setGot(1);
        replier.setTotal(1);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", ApiNames.OffLineTxAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }

}