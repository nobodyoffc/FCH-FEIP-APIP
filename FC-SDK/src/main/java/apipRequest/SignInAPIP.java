package apipRequest;

import apipClass.ResponseBody;
import apipClass.RequestBody;
import apipTools.ApipTools;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;
import constants.Strings;
import constants.UpStrings;
import fcTools.Base58;
import fcTools.ParseTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HexFormat;

import static apipRequest.PostRequester.requestPost;

public class SignInAPIP {

    public static void main(String[]args){

        //注意私钥格式，Base58编码私钥，需要先解码为字节数组，再转换为32字节私钥。
        String priKeyBase58 = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        byte[] priKey38Byte = Base58.decode(priKeyBase58);
        byte[] priKey32Bytes = KeyTools.getPriKey32(priKey38Byte);

        //直接使用32字节私钥
        //priKeyHex = "a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575";
        //byte[] priKeyBytes = HexFormat.of().parseHex(priKeyHex);

        //signIn接口，sessionKey不加密
        String urlHead = "https://qm.cash/APIP";
        System.out.println("Request signIn:");
        SignInApipReplyData result = signIn(urlHead, null, priKey32Bytes,Strings.REFRESH);
        System.out.println("Session key : "+result.getSessionKey());
        System.out.println("Session expire at : "+ ParseTools.convertTimestampToDate(result.getExpireTime()));

        //signInEcc接口，返回加密后的sessionKeyCipher

        //注意：私钥用过之后会被填充0，需要重新获取。
        priKey32Bytes = KeyTools.getPriKey32(priKey38Byte);
        //priKeyBytes = HexFormat.of().parseHex(priKeyHex);
        System.out.println();
        System.out.println("Request signInEcc:");

        result = signInEcc(urlHead,null,priKey32Bytes,Strings.RENEW);
        String sessionKeyCipher = result.getSessionKeyCipher();
        System.out.println("SessionKeyCipher: "+sessionKeyCipher);

        byte[] sessionKey = ApipTools.decryptSessionKeyWithPriKey(sessionKeyCipher, priKey32Bytes);
        System.out.println("SessinKey: "+HexFormat.of().formatHex(sessionKey));
        String sessionName = ApipTools.getSessionName(sessionKey);
        System.out.println("SessinName:"+sessionName);
    }

    public static SignInApipReplyData signIn(String urlHead, String via, byte[]priKey, String mode){
        RequestBody signInRequestBody = new RequestBody();
        SignInApipReplyData signInReplyData;
        String fid = KeyTools.pubKeyToFchAddr(KeyTools.priKeyToPubKey(priKey));
        try {
            String url = urlHead + ApiNames.APIP0V1Path + ApiNames.SignInAPI;
            signInReplyData = doSignInRequest(fid, url, priKey, signInRequestBody, via, mode);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("SignIn APIP wrong.");
            return null;
        }
        BytesTools.clearByteArray(priKey);
        return signInReplyData;
    }

    private static SignInApipReplyData makeResult( String responseJson) {
        Gson gson = new Gson();
        ResponseBody responseBody = gson.fromJson(responseJson, ResponseBody.class);
        SignInApipReplyData signInReplyData;
        if (responseBody.getCode() != 0) {
            System.out.println(responseJson);
            signInReplyData = new SignInApipReplyData();
        } else {
            Object data = responseBody.getData();
            Type t = new TypeToken<SignInApipReplyData>() {
            }.getType();
            signInReplyData = gson.fromJson(gson.toJson(data), t);
        }
        return signInReplyData;
    }

    public static SignInApipReplyData signInEcc(String urlHead, String via, byte[]priKey,String mode){

        RequestBody signInRequestBody = new RequestBody();
        SignInApipReplyData signInReplyData;
        String fid = KeyTools.pubKeyToFchAddr(KeyTools.priKeyToPubKey(priKey));
        try {
            String url = urlHead + ApiNames.APIP0V1Path + ApiNames.SignInEccAPI;
            signInReplyData = doSignInRequest(fid, url, priKey, signInRequestBody, via, mode);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("SignIn APIP wrong.");
            return null;
        }
        return signInReplyData;
    }

    public static SignInApipReplyData doSignInRequest(String fid, String url, byte[] priKey, RequestBody signInRequestBody, String via, String mode) {
        Gson gson = new Gson();
        SignInApipReplyData signInReplyData;
        signInRequestBody.makeRequestBody(url, via,mode);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(Strings.FID, fid);

        String requestBodyJson = gson.toJson(signInRequestBody);

        ECKey ecKey = ECKey.fromPrivate(priKey);

        String sign = ecKey.signMessage(requestBodyJson);

        headerMap.put(UpStrings.SIGN, sign);
        String responseJson = requestPost(url, headerMap, requestBodyJson);

        signInReplyData = makeResult(responseJson);
        return signInReplyData;
    }

}
