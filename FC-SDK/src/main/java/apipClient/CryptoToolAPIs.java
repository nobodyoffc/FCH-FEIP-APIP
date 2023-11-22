package apipClient;

import apipClass.EncryptIn;
import apipClass.Fcdsl;
import com.google.gson.Gson;
import constants.ApiNames;
import fcTools.ParseTools;
import javaTools.BytesTools;
import javaTools.JsonTools;
import walletTools.DataForOffLineTx;
import walletTools.SendTo;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CryptoToolAPIs {

    public static ApipClient addressesPost(String urlHead, String addrOrPubKey, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(addrOrPubKey);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.AddressesAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient encryptPost(String urlHead, String key,String message, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        EncryptIn encryptIn = new EncryptIn();
        encryptIn.setMsg(message);
        int keyLength = key.length();

        if(keyLength==64)encryptIn.setSymKey(key);
        else if(keyLength==66)encryptIn.setPubKey(key);
        else return null;

        fcdsl.setOther(encryptIn);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.EncryptAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient verifyPost(String urlHead, String signature, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        Map<String,String> signMap = new HashMap<>();

        try {
            Type t = JsonTools.getMapType(String.class,String.class);
            signMap = new Gson().fromJson(signature, t);
            fcdsl.setOther(signMap);
        }catch (Exception e){
            fcdsl.setOther(signature);
        }

        String urlTail = ApiNames.APIP21V1Path + ApiNames.VerifyAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient sha256Post(String urlHead, String text, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(text);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.Sha256API;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient sha256x2Post(String urlHead, String text, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(text);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.Sha256x2API;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient sha256BytesPost(String urlHead, String hex, @Nullable String via, byte[] sessionKey)  {
        if(!BytesTools.isHexString(hex)){
            System.out.println("Error: It's not a hex.");
            return null;
        }

        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(hex);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.Sha256BytesAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient sha256x2BytesPost(String urlHead, String hex, @Nullable String via, byte[] sessionKey)  {
        if(!BytesTools.isHexString(hex)){
            System.out.println("Error: It's not a hex.");
            return null;
        }
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(hex);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.Sha256x2BytesAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient offLineTxPost(String urlHead, String fromFid,List<SendTo>sendToList,String msg, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        DataForOffLineTx dataForOffLineTx = new DataForOffLineTx();
        dataForOffLineTx.setFromFid(fromFid);
        dataForOffLineTx.setSendToList(sendToList);
        dataForOffLineTx.setMsg(msg);
        fcdsl.setOther(dataForOffLineTx);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.OffLineTxAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient offLineTxByCdPost(String urlHead, String fromFid,List<SendTo>sendToList,String msg,int cd, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("21");
        Fcdsl fcdsl = new Fcdsl();
        DataForOffLineTx dataForOffLineTx = new DataForOffLineTx();
        dataForOffLineTx.setCd(cd);
        dataForOffLineTx.setFromFid(fromFid);
        dataForOffLineTx.setSendToList(sendToList);
        dataForOffLineTx.setMsg(msg);
        fcdsl.setOther(dataForOffLineTx);

        String urlTail = ApiNames.APIP21V1Path + ApiNames.OffLineTxByCdAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
}
