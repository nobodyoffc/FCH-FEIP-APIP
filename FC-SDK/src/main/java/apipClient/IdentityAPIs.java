package apipClient;

import apipClass.ClientCodeMessage;
import apipClass.Fcdsl;
import constants.ApiNames;
import keyTools.KeyTools;

import javax.annotation.Nullable;

public class IdentityAPIs {

    public ApipClient cidInfoByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.CidInfoByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient cidInfoSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.CidInfoSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient cidHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");


        String urlTail = ApiNames.APIP3V1Path + ApiNames.CidHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient homepageHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.HomepageHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient noticeFeeHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.NoticeFeeHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient reputationHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.ReputationHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient fidCidSeekPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {

        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        if(fcdsl.getQuery().getPart()==null) {
            System.out.println("This API needs a 'part' query.");
            apipClient.setCode(ClientCodeMessage.Code9BadQuery);
            apipClient.setMessage(ClientCodeMessage.Msg9BadQuery);
            return null;
        }
        if(fcdsl.getQuery().getPart().getFields()==null) {
            System.out.println("This API needs a 'part' query.");
            apipClient.setCode(ClientCodeMessage.Code9BadQuery);
            apipClient.setMessage(ClientCodeMessage.Msg9BadQuery);
            return null;
        }

        String urlTail = ApiNames.APIP3V1Path + ApiNames.FidCidSeekAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient fidCidGet(String urlHead,String id){
        ApipClient apipClient = new ApipClient();

        if(!id.contains("_")){
            if(!KeyTools.isValidFchAddr(id)){
                System.out.println("Bad id.");
                return null;
            }
        }

        apipClient.setSn("3");
        apipClient.addNewApipUrl(urlHead,ApiNames.APIP3V1Path + ApiNames.GetFidCidAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public ApipClient nobodyByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.NobodyByIdsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
    public ApipClient nobodySearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.NobodySearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public ApipClient avatarsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("17");
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(ids);
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP17V1Path + ApiNames.AvatarsAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }
}
