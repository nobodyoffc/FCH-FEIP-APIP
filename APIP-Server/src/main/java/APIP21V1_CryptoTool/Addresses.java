package APIP21V1_CryptoTool;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.ReplyInfo;
import keyTools.KeyTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(ApiNames.APIP21V1Path + ApiNames.AddressesAPI)
public class Addresses extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, RuntimeException {
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

        String input = (String)requestBody.getFcdsl().getOther();

        Map<String, String> addrMap = new HashMap<>();
        String pubKey=null;

        if(input.startsWith("F")||input.startsWith("1")||input.startsWith("D")||input.startsWith("L")){
            byte[] hash160 = KeyTools.addrToHash160(input);
            addrMap = KeyTools.hash160ToAddresses(hash160);
        }else if (input.startsWith("02")||input.startsWith("03")){
            pubKey = input;
            addrMap= KeyTools.pubKeyToAddresses(pubKey);
        }else if(input.startsWith("04")){
            try {
                pubKey = KeyTools.compressPk65To33(input);
                addrMap= KeyTools.pubKeyToAddresses(pubKey);
            } catch (Exception e) {
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Wrong public key.");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        }else{
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("FID or Public Key are needed.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        replier.setData(addrMap);
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