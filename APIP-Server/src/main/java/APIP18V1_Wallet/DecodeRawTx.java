package APIP18V1_Wallet;

import APIP1V1_OpenAPI.*;
import RPC.FcRpcMethods;
import RPC.NewFcRpcClient;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import fcTools.ParseTools;
import initial.Initiator;
import startAPIP.ConfigAPIP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static api.Constant.*;
import static scanner.MempoolScanner.fcClient;

@WebServlet(APIP18V1Path +DecodeRawTxAPI)
public class DecodeRawTx extends HttpServlet {
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
        String rawTxHex = dataCheckResult.getDataRequestBody().getFcdsl().getOther();

        String result = "";
        try {
             result = FcRpcMethods.decodeTx(fcClient,rawTxHex);
        } catch (Throwable e) {
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
        }

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
