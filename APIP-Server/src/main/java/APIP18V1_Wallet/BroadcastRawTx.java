package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import freecashRPC.FcRpcMethods;
import javaTools.BytesTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static initial.Initiator.fcClient;

@WebServlet(ApiNames.APIP18V1Path + ApiNames.BroadcastTxAPI)
public class BroadcastRawTx extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

//        if (RequestChecker.isPublicSessionKey(response, replier, writer, addr)) return;

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Object rawTxHex = dataCheckResult.getDataRequestBody().getFcdsl().getOther();
        Gson gson = new Gson();
        String rawTx = gson.fromJson(gson.toJson(rawTxHex),String.class);

        String result = "";
        try {
             result = FcRpcMethods.sendTx(fcClient,rawTx);
        } catch (Throwable e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(e.getMessage());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        if(result.contains("{")){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(result);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        if(result.startsWith("\""))result=result.substring(1);
        if(result.endsWith("\""))result=result.substring(0,result.length()-1);

        if(!BytesTools.isHexString(result)){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(result);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        replier.setData(result);
        if (Replier.makeSingleReplier(response, replier, dataCheckResult, addr)) return;

        writer.write(replier.reply0Success(addr));
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getOther()==null)
            return false;
        return true;
    }
}
