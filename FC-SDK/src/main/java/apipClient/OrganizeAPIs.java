package apipClient;

import apipClass.Fcdsl;
import constants.ApiNames;

import javax.annotation.Nullable;

public class OrganizeAPIs {


    public ApipClient groupByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("8");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);


        String urlTail = ApiNames.APIP8V1Path + ApiNames.GroupByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient groupSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("8");


        String urlTail = ApiNames.APIP8V1Path + ApiNames.GroupSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient groupOpHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("8");

        String urlTail = ApiNames.APIP8V1Path + ApiNames.GroupOpHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient groupMembersPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("8");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP8V1Path + ApiNames.GroupMembersAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient myGroupsPost(String urlHead, String fid, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("8");
        Fcdsl fcdsl = new Fcdsl();

        fcdsl.addNewQuery().addNewTerms().addNewFields("members").addNewValues(fid);

        String urlTail = ApiNames.APIP8V1Path + ApiNames.MyGroupsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }


    public ApipClient teamByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient teamSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient teamOpHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamOpHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient teamRateHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamRateHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient teamMembersPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamMembersAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient teamExMembersPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamExMembersAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient teamOtherPersonsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);

        String urlTail = ApiNames.APIP9V1Path + ApiNames.TeamOtherPersonsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient myTeamsPost(String urlHead, String fid, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("9");
        Fcdsl fcdsl = new Fcdsl();

        fcdsl.addNewQuery().addNewTerms().addNewFields("members").addNewValues(fid);

        String urlTail = ApiNames.APIP9V1Path + ApiNames.MyTeamsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    
}
