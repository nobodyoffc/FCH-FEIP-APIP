package apipClient;

import apipClass.Fcdsl;
import apipClass.ResponseBody;
import apipClass.SignInData;
import apipTools.ApipTools;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HexFormat;

public class OpenAPIs {
    public ApipClient getService(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.APIP0V1Path + ApiNames.GetServiceAPI);
        apipClient.get();
        return apipClient;
    }
    public ApipClient signInPost(String urlHead, String via, byte[] priKey, String mode)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("0");
        String urlTail = ApiNames.APIP0V1Path + ApiNames.SignInAPI;
        doSignIn(apipClient,urlHead, via, priKey, urlTail,mode);

        return apipClient;
    }
    public ApipClient signInEccPost(String urlHead, @Nullable String via, byte[] priKey, @Nullable String mode)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("0");
        String urlTail = ApiNames.APIP0V1Path + ApiNames.SignInEccAPI;
        doSignIn(apipClient,urlHead, via, priKey, urlTail,mode);

        SignInData signInData = makeSignInData(apipClient.getResponseBodyStr());

        apipClient.getResponseBody().setData(signInData);
        return apipClient;
    }

    @Nullable
    private ApipClient doSignIn(ApipClient apipClient, String urlHead, @Nullable String via, byte[] priKey, String urlTail, @Nullable String mode) {

        try {
            apipClient.asySignPost(urlHead, urlTail, via, null, priKey, mode);
        } catch (IOException e) {
            System.out.println("Do post wrong.");
            return null;
        }

        return apipClient;
    }

    public static SignInData makeSignInData(String responseJson) {
        Gson gson = new Gson();
        ResponseBody responseBody = gson.fromJson(responseJson, ResponseBody.class);
        SignInData signInReplyData;
        if (responseBody.getCode() != 0) {
            System.out.println(responseJson);
            signInReplyData = new SignInData();
        } else {
            Object data = responseBody.getData();
            Type t = new TypeToken<SignInData>() {
            }.getType();
            signInReplyData = gson.fromJson(gson.toJson(data), t);
        }
        return signInReplyData;
    }
    public ApipClient totalsGet(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead,ApiNames.FreeGetPath+ApiNames.GetTotalsAPI);
        apipClient.get();
        return apipClient;
    }

    public ApipClient totalsPost(String urlHead,@Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("0");
        String urlTail = ApiNames.APIP0V1Path + ApiNames.TotalsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, apipClient.getRawFcdsl(), via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient generalPost(String index, String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("1");
        if(index==null){
            System.out.println("The index name is required.");
            return null;
        }
        if(fcdsl==null)fcdsl=new Fcdsl();

        fcdsl.setIndex(index);

        String urlTail = ApiNames.APIP1V1Path + ApiNames.GeneralAPI;
        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;

        return apipClient;
    }
}