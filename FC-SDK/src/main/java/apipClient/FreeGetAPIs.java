package apipClient;

import constants.ApiNames;

public class FreeGetAPIs {

    public ApipClient broadcast(String urlHead, String rawTx){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.BroadcastAPI+"?rawTx="+rawTx);
        apipClient.get();
        return apipClient;
    }
    public ApipClient getApps(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        if(id==null)apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetAppsAPI);
        else apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetAppsAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public ApipClient getServices(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        if(id==null)apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetServicesAPI);
        else apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetServicesAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public ApipClient getAvatar(String urlHead, String fid){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetAvatarAPI+"?fid="+fid);
        apipClient.get();
        return apipClient;
    }

    public ApipClient getCashes(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetCashesAPI+"?fid="+id);
        apipClient.get();
        return apipClient;
    }

    public ApipClient getFidCid(String urlHead, String id){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetFidCidAPI+"?id="+id);
        apipClient.get();
        return apipClient;
    }

    public ApipClient getFreeService(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetFreeServiceAPI);
        apipClient.get();
        return apipClient;
    }

    public ApipClient getTotals(String urlHead){
        ApipClient apipClient = new ApipClient();
        apipClient.addNewApipUrl(urlHead, ApiNames.FreeGetPath + ApiNames.GetTotalsAPI);
        apipClient.get();
        return apipClient;
    }
}
