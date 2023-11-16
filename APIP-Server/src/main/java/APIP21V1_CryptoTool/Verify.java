package APIP21V1_CryptoTool;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import constants.Strings;
import fipaClass.Signature;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(ApiNames.APIP21V1Path + ApiNames.VerifyAPI)
public class Verify extends HttpServlet {
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
        String rawSignJson = new Gson().toJson(dataCheckResult.getDataRequestBody().getFcdsl().getOther());
//        Signature.SignShort signShort = Signature.parseSignature(rawSignJson);
        Signature signature = Signature.parseSignature(rawSignJson);

        if(signature==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            Map<String,String> dataMap= new HashMap<>();
            dataMap.put(Strings.ERROR,"Parse signature wrong.");
            replier.setData(dataMap);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        boolean isGoodSign;
        if(signature.getFid()!=null&& signature.getMsg()!=null && signature.getSign()!=null){
            String sign = signature.getSign().replace("\\u003d", "=");
            try {
                String signPubKey = ECKey.signedMessageToKey(signature.getMsg(), sign).getPublicKeyAsHex();
                isGoodSign= signature.getFid().equals(KeyTools.pubKeyToFchAddr(signPubKey));
            } catch (SignatureException e) {
                isGoodSign = false;
            }
        }else{
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            Map<String,String> dataMap= new HashMap<>();
            dataMap.put(Strings.ERROR,"FID, signature or message missed.");
            replier.setData(dataMap);
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        replier.setData(isGoodSign);
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
