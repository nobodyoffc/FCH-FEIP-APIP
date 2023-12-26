package apipClient;

import apipClass.Fcdsl;
import constants.ApiNames;
import constants.Strings;

import javax.annotation.Nullable;

public class BlockchainAPIs {

    public static ApipClient blockByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.BlockByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient blockByHeightsPost(String urlHead, String[] heights, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.addNewQuery().addNewTerms().addNewFields(Strings.HEIGHT).addNewValues(heights);
        apipClient.setRawFcdsl(fcdsl);
        String urlTail = ApiNames.APIP2V1Path + ApiNames.BlockByHeightsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient blockSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.BlockSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient cashValidPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.CashValidAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient cashByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.CashByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient cashSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.CashSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient fidByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.FidByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient fidSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.FidSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient opReturnByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.OpReturnByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient opReturnSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.OpReturnSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient p2shByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.P2shByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient p2shSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.P2shSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient txByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP2V1Path + ApiNames.TxByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public static ApipClient txSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("2");

        String urlTail = ApiNames.APIP2V1Path + ApiNames.TxSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
}
