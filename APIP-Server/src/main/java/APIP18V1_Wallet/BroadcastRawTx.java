package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import RPC.FcRpcMethods;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static api.Constant.*;
import static scanner.MempoolScanner.fcClient;

@WebServlet(APIP18V1Path +BroadcastTxAPI)
public class BroadcastRawTx extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        Object rawTxHex = dataCheckResult.getDataRequestBody().getFcdsl().getOther();

        String result = "";
        try {
             result = FcRpcMethods.sendTx(fcClient,rawTxHex);
        } catch (Throwable e) {
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            replier.setData(e.getMessage());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        if(result.contains("{")){
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            replier.setData(result);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }


        if(result.startsWith("\""))result=result.substring(1);
        if(result.endsWith("\""))result=result.substring(0,result.length()-2);

        replier.setData(result);
        replier.setGot(1);
        replier.setTotal(1);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", DecodeRawTxAPI));
        response.setHeader(CodeInHeader, String.valueOf(Code0Success));
        String reply = replier.reply0Success(addr,nPrice);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(SignInHeader,sign);

        writer.write(reply);
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getOther()==null)
            return false;
        return true;
    }
}
