package tools;

import constants.ApiNames;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import static constants.ApiNames.apiList;
import static constants.ApiNames.freeApiList;
import static constants.Constants.FchToSatoshi;
import static constants.Strings.*;

public class ApipTools {

    public static String getApiNameFromUrl(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex != url.length() - 1) {
            String name = url.substring(lastSlashIndex + 1);
            if(apiList.contains(name)||freeApiList.contains(name)) {
                return name;
            }
            return "";
        } else {
            return "";  // Return empty string if '/' is the last character or not found
        }

    }

    public static int getNPrice(String apiName, Jedis jedis){
        try {
            return Integer.parseInt(jedis.hget(N_PRICE,apiName));
        }catch (Exception e){
           return -1;
        }
    }

}
