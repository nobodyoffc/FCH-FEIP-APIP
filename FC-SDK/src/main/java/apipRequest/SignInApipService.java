package apipRequest;

import apipClass.ResponseBody;
import apipClass.SignInReplyData;
import apipClass.SignInRequestBody;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;
import constants.Strings;
import fcTools.ParseTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HexFormat;

import static apipRequest.PostRequester.requestPost;

public class SignInApipService {

    public static void main(String[]args){
        String priKey = "ee72e6dd4047ef7f4c9886059cbab42eaab08afe7799cbc0539269ee7e2ec30c";
        byte[] priKeyBytes = HexFormat.of().parseHex(priKey);
        String urlHead = "https://cid.cash/APIP";
        SignInReplyData result = signIn(urlHead, null, priKeyBytes);
        System.out.println("Session key : "+result.getSessionKey());
        System.out.println("Session expire at : "+ ParseTools.convertTimestampToDate(result.getExpireTime()));
    }

    public static SignInReplyData signIn(String urlHead,String via,byte[]priKey){
        SignInRequestBody signInRequestBody = new SignInRequestBody();
        ResponseBody responseBody;
        SignInReplyData signInReplyData = null;
        String fid = KeyTools.pubKeyToFchAddr(KeyTools.priKeyToPubKey(priKey));
        try {
            Gson gson = new Gson();
            String url = urlHead + ApiNames.APIP0V1Path + ApiNames.SignInAPI;

            signInRequestBody.makeRequestBody(url, via);

            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put(Strings.FID, fid);

            String requestBodyJson = gson.toJson(signInRequestBody);
            ECKey ecKey = ECKey.fromPrivate(priKey);
            String sign = ecKey.signMessage(requestBodyJson);

            headerMap.put(Strings.Header_SIGN, sign);
            String responseJson = requestPost(url, headerMap, requestBodyJson);

            responseBody = gson.fromJson(responseJson, ResponseBody.class);
            if (responseBody.getCode() != 0) {
                System.out.println(responseJson);
            } else {
                Object data = responseBody.getData();
                Type t = new TypeToken<SignInReplyData>() {
                }.getType();
                signInReplyData = gson.fromJson(gson.toJson(data), t);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("SignIn APIP wrong.");
            return null;
        }
        BytesTools.clearByteArray(priKey);
        return signInReplyData;
    }
}
