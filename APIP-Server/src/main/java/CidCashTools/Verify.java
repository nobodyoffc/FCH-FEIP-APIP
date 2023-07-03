package CidCashTools;

import APIP0V1_OpenAPI.*;
import com.google.gson.Gson;
import data.VerifyIn;
import fcTools.ParseTools;
import initial.Initiator;
import org.bitcoinj.core.ECKey;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;

import static api.Constant.*;
@WebServlet(ToolsPath +VerifyAPI)
public class Verify extends HttpServlet {
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

        SignShort signShort = new SignShort();
        try {
            Gson gson = new Gson();
            //verifyIn =  gson.fromJson(gson.toJson(dataCheckResult.getDataRequestBody().getFcdsl().getOther()), VerifyIn.class);
            String inputJson = gson.toJson(dataCheckResult.getDataRequestBody().getFcdsl().getOther());
            if(inputJson.contains("signature")&&inputJson.contains("message")&&inputJson.contains("address")){
                SignFull signFull = new SignFull();
                signFull = gson.fromJson(inputJson,SignFull.class);
                signShort.fid = signFull.address;
                signShort.msg = signFull.message;
                signShort.sign = signFull.signature;
            }else if(inputJson.contains("----")){
                signShort = parseOldSign(inputJson);
            }else {
                signShort = gson.fromJson(inputJson,SignShort.class);
            }
        }catch (Exception e){
            response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
            replier.setData("Can't get parameters correctly from Json string.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        boolean isGoodSign = false;
        if(signShort.fid!=null&& signShort.msg!=null && signShort.sign!=null){

            String sign = signShort.sign.replace("\\u003d", "=");
            try {
                String signPubKey = ECKey.signedMessageToKey(signShort.msg, sign).getPublicKeyAsHex();
                if(signShort.fid.equals(keyTools.KeyTools.pubKeyToFchAddr(signPubKey))){
                    isGoodSign=true;
                }
            } catch (SignatureException e) {
                response.setHeader(CodeInHeader, String.valueOf(Code1020OtherError));
                replier.setData("Something wrong when checking signature.");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        }

        replier.setData(isGoodSign);
        replier.setGot(1);
        replier.setTotal(1);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", VerifyAPI));
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

    private SignShort parseOldSign(String oldSign){
        String[] elm = oldSign.split("----");
        SignShort signShort = new SignShort();
        signShort.msg = elm[0];
        signShort.fid = elm[1];
        signShort.sign = elm[2];

        signShort.msg = signShort.msg.replaceAll("\"","");
        signShort.sign = signShort.sign.replaceAll("\"","");
        signShort.sign = signShort.sign.replaceAll("\\u003d","=");

        ParseTools.gsonPrint(signShort);
        return signShort;
    }

    private static class SignFull {
        String address;
        String message;
        String signature;
    }

    private static class SignShort {
        String fid;
        String msg;
        String sign;
    }
}
