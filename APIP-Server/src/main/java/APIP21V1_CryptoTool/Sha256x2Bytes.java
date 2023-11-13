package APIP21V1_CryptoTool;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.ReplyInfo;
import cryptoTools.Hash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HexFormat;

@WebServlet(ApiNames.APIP21V1Path +ApiNames.Sha256x2BytesAPI)
public class Sha256x2Bytes extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, RuntimeException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String text;

        try {
            text = (String)dataCheckResult.getDataRequestBody().getFcdsl().getOther();
        }catch (Exception e){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Can not get parameters correctly from Json string.");
            writer.write(replier.reply1020OtherError(addr));
            e.printStackTrace();
            return;
        }

        byte[] textBytes = HexFormat.of().parseHex(text);
        replier.setData(HexFormat.of().formatHex(Hash.Sha256x2(textBytes)));

        if (Replier.makeSingleReplier(response, replier, dataCheckResult, addr)) return;

        writer.write(replier.reply0Success(addr));
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        return requestBody.getFcdsl().getOther() != null;
    }
}