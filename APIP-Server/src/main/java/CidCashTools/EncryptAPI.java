package CidCashTools;

import APIP0V1_OpenAPI.*;
import EccAes256K1P7.EccAes256K1P7;
import com.google.gson.Gson;
import constants.ReplyInfo;
import data.EncryptIn;
import initial.Initiator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static constants.ApiNames.EncryptAPI;
import static constants.Constants.*;

@WebServlet(ToolsPath +EncryptAPI)
public class EncryptAPI extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, RuntimeException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        if (RequestChecker.checkPublicSessionKey(response, replier, writer, addr)) return;

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
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
            encryptIn = gson.fromJson(gson.toJson(dataCheckResult.getDataRequestBody().getFcdsl().getOther()),EncryptIn.class);
        }catch (Exception e){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Can't get parameters correctly from Json string.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        EccAes256K1P7 eccAes = new EccAes256K1P7();
        String cipher = null;
        try {
            if(encryptIn.getSymKey()!=null){
                    cipher = eccAes.encryptWithSymKey(encryptIn.getSymKey(),encryptIn.getSymKey());
            }else if (encryptIn.getPubKey()!=null){
                cipher = eccAes.encrypt(encryptIn.getMsg(), encryptIn.getPubKey());
            }else{
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("No symKey or pubKey.");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchProviderException |
                 BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException |
                 NoSuchPaddingException e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Something wrong when encrypting message.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        replier.setData(cipher);
        replier.setGot(1);
        replier.setTotal(1);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", EncryptAPI));
        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr,nPrice);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);

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