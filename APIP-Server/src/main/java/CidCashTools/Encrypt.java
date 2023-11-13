package CidCashTools;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import com.google.gson.Gson;
import constants.ReplyInfo;
import apipClass.EncryptIn;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesType;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static constants.ApiNames.EncryptAPI;
import static constants.ApiNames.ToolsPath;

@WebServlet(ToolsPath +EncryptAPI)
public class Encrypt extends HttpServlet {
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

        EncryptIn encryptIn;
        try {
            Gson gson = new Gson();
            String otherJson = gson.toJson(dataCheckResult.getDataRequestBody().getFcdsl().getOther());
            encryptIn = gson.fromJson(otherJson,EncryptIn.class);
        }catch (Exception e){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Can't get parameters correctly from Json string.");
            writer.write(replier.reply1020OtherError(addr));
            e.printStackTrace();
            return;
        }
        EccAes256K1P7 eccAes = new EccAes256K1P7();
        EccAesData eccAesData;
        String cipher;
        if(encryptIn.getSymKey()!=null){
            char[] symKey= encryptIn.getSymKey().toCharArray();
            if(symKey.length<64)eccAesData = new EccAesData(EccAesType.Password, encryptIn.getMsg(), symKey);
            else eccAesData = new EccAesData(EccAesType.SymKey, encryptIn.getMsg(), symKey);
            eccAes.encrypt(eccAesData);
            cipher = eccAesData.toJson();
        }else if (encryptIn.getPubKey()!=null){
            eccAesData = new EccAesData(EccAesType.AsyOneWay,encryptIn.getMsg(),encryptIn.getPubKey().toCharArray());
            eccAesData.setPubKeyB(encryptIn.getPubKey());
            eccAes.encrypt(eccAesData);
            eccAesData.clearAllSensitiveData();
            cipher = eccAesData.toJson();
        }else{
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("No symKey or pubKey.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        if(eccAesData.getError()!=null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(eccAesData.getError());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        replier.setData(cipher);
        if (Replier.makeSingleReplier(response, replier, dataCheckResult, addr)) return;

        writer.write(replier.reply0Success(addr));
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        return requestBody.getFcdsl().getOther() != null;
    }
}