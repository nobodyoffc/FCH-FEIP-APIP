package apipClient;

import apipClass.Fcdsl;
import constants.ApiNames;

import javax.annotation.Nullable;

public class ConstructAPIs {

    public ApipClient protocolByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("4");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP4V1Path + ApiNames.ProtocolByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient protocolSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("4");

        String urlTail = ApiNames.APIP4V1Path + ApiNames.ProtocolSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient protocolOpHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("4");

        String urlTail = ApiNames.APIP4V1Path + ApiNames.ProtocolOpHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient protocolRateHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("4");
        String urlTail = ApiNames.APIP4V1Path + ApiNames.ProtocolRateHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient codeByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("5");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP5V1Path + ApiNames.CodeByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient codeSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("5");

        String urlTail = ApiNames.APIP5V1Path + ApiNames.CodeSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient codeOpHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("5");

        String urlTail = ApiNames.APIP5V1Path + ApiNames.CodeOpHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient codeRateHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("5");
        String urlTail = ApiNames.APIP5V1Path + ApiNames.CodeRateHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }


    public ApipClient serviceByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("6");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP6V1Path + ApiNames.ServiceByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient serviceSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("6");

        String urlTail = ApiNames.APIP6V1Path + ApiNames.ServiceSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient serviceOpHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("6");

        String urlTail = ApiNames.APIP6V1Path + ApiNames.ServiceOpHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient serviceRateHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("6");
        String urlTail = ApiNames.APIP6V1Path + ApiNames.ServiceRateHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }


    public ApipClient appByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("7");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP7V1Path + ApiNames.AppByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient appSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("7");

        String urlTail = ApiNames.APIP7V1Path + ApiNames.AppSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient appOpHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("7");

        String urlTail = ApiNames.APIP7V1Path + ApiNames.AppOpHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient appRateHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("7");
        String urlTail = ApiNames.APIP7V1Path + ApiNames.AppRateHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
}
