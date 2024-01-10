package apipClient;

import constants.ApiNames;

public class FreeGetAPIs {

    public static ApipClient broadcast(String urlHead, String rawTx){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.BroadcastAPI+"?rawTx="+rawTx);
        apipClient.get();
        return apipClient;
    }
    public static ApipClient getApps(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        if(id==null)apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetAppsAPI);
        else apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetAppsAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient getBestBlock(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetBestBlockAPI);

        apipClient.get();
        return apipClient;
    }

    public static ApipClient getPrices(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetPricesAPI);
        apipClient.get();
        return apipClient;
    }
    public static ApipClient getService(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.APIP0V1Path + ApiNames.GetServiceAPI);
        apipClient.get();
        return apipClient;
    }
    public static ApipClient getServices(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        if(id==null)apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetServicesAPI);
        else apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetServicesAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient getAvatar(String urlHead, String fid){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetAvatarAPI+"?fid="+fid);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient getCashes(String urlHead, String id, double amount){
        ApipClient apipClient = new ApipClient();
        String urlTail = ApiNames.FreeGetPath + ApiNames.GetCashesAPI;
        if(id!=null)urlTail =urlTail+"?fid="+id;
        if(amount!=0)urlTail = urlTail+"?amount="+amount;
        apipClient.addNewApipUrl(urlHead, urlTail);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient getFidCid(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetFidCidAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient getFreeService(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetFreeServiceAPI);
        apipClient.get();
        return apipClient;
    }

    public static ApipClient getTotals(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetTotalsAPI);
        apipClient.get();
        return apipClient;
    }
}
