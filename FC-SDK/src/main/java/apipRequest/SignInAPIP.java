package apipRequest;

import apipClass.ResponseBody;
import apipClass.SignInRequestBody;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;
import constants.Strings;
import constants.UpStrings;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesDataByte;
import fcTools.Base58;
import fcTools.ParseTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
        SignInApipReplyData result = signIn(urlHead, null, priKey32Bytes);
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

        byte[] sessionKey = decryptSessionKey(sessionKeyCipher, priKey32Bytes);
        System.out.println("SessinKey: "+HexFormat.of().formatHex(sessionKey));
        String sessionName = makeSessionName(sessionKey);
        System.out.println("SessinName:"+sessionName);
    }

    private static String makeSessionName(byte[] sessionKey) {
        byte[] sessionNameBytes = Arrays.copyOf(sessionKey,6);
        return HexFormat.of().formatHex(sessionNameBytes);
    }

    public static SignInApipReplyData signIn(String urlHead, String via, byte[]priKey){
        SignInRequestBody signInRequestBody = new SignInRequestBody();
        ResponseBody responseBody;
        SignInApipReplyData signInReplyData = null;
        String fid = KeyTools.pubKeyToFchAddr(KeyTools.priKeyToPubKey(priKey));
        try {
            Gson gson = new Gson();
            String url = urlHead + ApiNames.APIP0V1Path + ApiNames.SignInAPI;

            signInRequestBody.makeRequestBody(url, via,Strings.RENEW);

            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put(Strings.FID, fid);

            String requestBodyJson = gson.toJson(signInRequestBody);
            ECKey ecKey = ECKey.fromPrivate(priKey);
            String sign = ecKey.signMessage(requestBodyJson);

            headerMap.put(UpStrings.SIGN, sign);
            String responseJson = requestPost(url, headerMap, requestBodyJson);

            responseBody = gson.fromJson(responseJson, ResponseBody.class);
            if (responseBody.getCode() != 0) {
                System.out.println(responseJson);
            } else {
                Object data = responseBody.getData();
                Type t = new TypeToken<SignInApipReplyData>() {
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

    public static SignInApipReplyData signInEcc(String urlHead, String via, byte[]priKey,String mode){
//TODO
        System.out.println("PriKey:"+HexFormat.of().formatHex(priKey));

        SignInRequestBody signInRequestBody = new SignInRequestBody();
        ResponseBody responseBody;
        SignInApipReplyData signInReplyData = null;
        String fid = KeyTools.pubKeyToFchAddr(KeyTools.priKeyToPubKey(priKey));
        try {
            Gson gson = new Gson();
            String url = urlHead + ApiNames.APIP0V1Path + ApiNames.SignInEccAPI;

            signInRequestBody.makeRequestBody(url, via,mode);

            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put(Strings.FID, fid);

            String requestBodyJson = gson.toJson(signInRequestBody);
            ECKey ecKey = ECKey.fromPrivate(priKey);

            String sign = ecKey.signMessage(requestBodyJson);

            headerMap.put(UpStrings.SIGN, sign);
            String responseJson = requestPost(url, headerMap, requestBodyJson);

            responseBody = gson.fromJson(responseJson, ResponseBody.class);
            if (responseBody.getCode() != 0) {
                System.out.println(responseJson);
            } else {
                Object data = responseBody.getData();
                Type t = new TypeToken<SignInApipReplyData>() {
                }.getType();
                signInReplyData = gson.fromJson(gson.toJson(data), t);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("SignIn APIP wrong.");
            return null;
        }
        return signInReplyData;
    }

    public static byte[] decryptSessionKey(String cipher, byte[] priKey) {
        System.out.println("Got sessionKey cipher from APIP server.");

        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataBytes = ecc.decrypt(cipher, priKey);
        if(eccAesDataBytes.getError()!=null){
            System.out.println("Decrypt sessionKey wrong which is from APIP server: "+eccAesDataBytes.getError());
            BytesTools.clearByteArray(priKey);
            return null;
        }
        String sessionKeyHex = new String(eccAesDataBytes.getMsg(), StandardCharsets.UTF_8);
        return HexFormat.of().parseHex(sessionKeyHex);
    }
}
