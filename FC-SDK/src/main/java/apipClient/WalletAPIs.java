package apipClient;

import apipClass.Fcdsl;
import constants.ApiNames;
import constants.FieldNames;
import fcTools.ParseTools;

import javax.annotation.Nullable;

public class WalletAPIs {

    public static ApipClient broadcastTxPost(String urlHead, String txHex, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("18");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(txHex);

        String urlTail = ApiNames.APIP18V1Path + ApiNames.BroadcastTxAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient feeRateGet(String urlHead){
        ApipClient apipClient = new ApipClient();
        String urlTail = ApiNames.APIP18V1Path + ApiNames.FeeRateAPI;
        apipClient.addNewApipUrl(urlHead, urlTail);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient feeRatePost(String urlHead,@Nullable String via, byte[] sessionKey){
        ApipClient apipClient = new ApipClient();
        String urlTail = ApiNames.APIP18V1Path + ApiNames.FeeRateAPI;
        boolean isGood = apipClient.post(urlHead,urlTail, null, via, sessionKey);
        if(!isGood)return null;

        return apipClient;
    }

    public static ApipClient decodeRawTxPost(String urlHead, String rawTx, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("18");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setOther(rawTx);

        String urlTail = ApiNames.APIP18V1Path + ApiNames.DecodeRawTxAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient cashValidForPayPost(String urlHead, String fid,double amount, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("18");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.addNewQuery().addNewTerms().addNewFields(FieldNames.OWNER).addNewValues(fid);
        amount = ParseTools.roundDouble8(amount);
        fcdsl.setOther(String.valueOf(amount));
        String urlTail = ApiNames.APIP18V1Path + ApiNames.CashValidForPayAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient cashValidForCdPost(String urlHead, String fid,int cd, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("18");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.addNewQuery().addNewTerms().addNewFields(FieldNames.OWNER).addNewValues(fid);
        fcdsl.setOther(String.valueOf(cd));
        String urlTail = ApiNames.APIP18V1Path + ApiNames.CashValidForCdAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient unconfirmedPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("18");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP18V1Path + ApiNames.UnconfirmedAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
}
