package apipClient;

import apipClass.Fcdsl;
import constants.ApiNames;

import javax.annotation.Nullable;

public class PersonalAPIs {
    public ApipClient boxByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("10");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP10V1Path + ApiNames.BoxByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient boxSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("10");

        String urlTail = ApiNames.APIP10V1Path + ApiNames.BoxSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient boxHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("10");

        String urlTail = ApiNames.APIP10V1Path + ApiNames.BoxHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient contactByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("11");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP11V1Path + ApiNames.ContactByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient contactsPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("11");

        String urlTail = ApiNames.APIP11V1Path + ApiNames.ContactsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient contactsDeletedPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("11");

        String urlTail = ApiNames.APIP11V1Path + ApiNames.ContactsDeletedAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient secretByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("12");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP12V1Path + ApiNames.SecretByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient secretsPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("12");

        String urlTail = ApiNames.APIP12V1Path + ApiNames.SecretsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient secretsDeletedPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("12");

        String urlTail = ApiNames.APIP12V1Path + ApiNames.SecretsDeletedAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient mailByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("13");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP13V1Path + ApiNames.MailByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient mailsPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("13");

        String urlTail = ApiNames.APIP13V1Path + ApiNames.MailsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient mailsDeletedPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("13");

        String urlTail = ApiNames.APIP13V1Path + ApiNames.MailsDeletedAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient mailThreadPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("13");

        String urlTail = ApiNames.APIP13V1Path + ApiNames.MailThreadAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
}
