package apipClient;

import apipClass.ApipParamsForClient;
import apipClass.CidInfo;
import apipClass.ClientCodeMessage;
import apipClass.Fcdsl;
import constants.ApiNames;
import fchClass.Address;
import javaTools.JsonTools;
import keyTools.KeyTools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class IdentityAPIs {

    public static ApipClient cidInfoByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
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
    public static ApipClient cidInfoSearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.CidInfoSearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient cidHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");


        String urlTail = ApiNames.APIP3V1Path + ApiNames.CidHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient homepageHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.HomepageHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient noticeFeeHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.NoticeFeeHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient reputationHistoryPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.ReputationHistoryAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient fidCidSeekPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {

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

    public static ApipClient fidCidGet(String urlHead,String id){
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

    public static ApipClient nobodyByIdsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
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
    public static ApipClient nobodySearchPost(String urlHead, Fcdsl fcdsl, @Nullable String via, byte[] sessionKey)  {
        ApipClient apipClient = new ApipClient();
        apipClient.setSn("3");
        apipClient.setRawFcdsl(fcdsl);

        String urlTail = ApiNames.APIP3V1Path + ApiNames.NobodySearchAPI;

        boolean isGood = apipClient.post(urlHead,urlTail, fcdsl, via, sessionKey);
        if(!isGood)return null;
        return apipClient;
    }

    public static ApipClient avatarsPost(String urlHead, String[] ids, @Nullable String via, byte[] sessionKey)  {
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

    public static String getPubKey(String fid, ApipParamsForClient apipParams, byte[] symKey) {
        ApipClient apipClient = BlockchainAPIs.fidByIdsPost(apipParams.getUrlHead(),new String[]{fid},apipParams.getVia(),symKey);
        if(apipClient==null ||apipClient.checkResponse()!=0){
            if(apipClient.getMessage()!=null) System.out.println(apipClient.getMessage());
            return null;
        }
        Map<String, Address> addrMap = ApipDataGetter.getAddressMap(apipClient.getResponseBody().getData());
        Address address = addrMap.get(fid);
        if(address==null){
            System.out.println("The pubKey is not shown on-chain.");
            return null;
        }
        String pubKey = address.getPubKey();
        if(pubKey ==null){
            System.out.println("This address "+fid+" has no pubKey on-chain.");
            return null;
        }
        if(!KeyTools.isValidPubKey(pubKey)){
            System.out.println("Invalid pubKey:"+ pubKey);
            return null;
        }
        return pubKey;
    }

    public static CidInfo getCidInfo(String fid, ApipParamsForClient apipParams, byte[] symKey) {
        ApipClient apipClient = IdentityAPIs.cidInfoByIdsPost(apipParams.getUrlHead(),new String[]{fid},apipParams.getVia(),symKey);
        assert apipClient != null;
        if(apipClient.checkResponse()!=0){
            if(apipClient.getMessage()!=null) System.out.println(apipClient.getMessage());
            if(apipClient.getResponseBody()!=null&& apipClient.getResponseBody().getData()!=null)
                System.out.println(JsonTools.getString(apipClient.getResponseBody().getData()));
            return null;
        }
        Map<String,CidInfo> addrMap = ApipDataGetter.getCidInfoMap(apipClient.getResponseBody().getData());
        CidInfo cid = addrMap.get(fid);
        if(cid ==null){
            System.out.println("The pubKey is not shown on-chain.");
            return null;
        }
        return cid;
    }
}
