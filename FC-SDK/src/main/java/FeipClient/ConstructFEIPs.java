package FeipClient;

import constants.FeipNames;
import feipClass.FcInfo;
import feipClass.ServiceData;
import javaTools.JsonTools;

public class ConstructFEIPs {

    public static String buyService(String sid){
        FcInfo fcInfo = new FcInfo();
        ServiceData serviceData = new ServiceData();
        serviceData.setSid(sid);
        serviceData.setOp(FeipNames.BUY);
        fcInfo.setData(serviceData);
        return JsonTools.getString(fcInfo);
    }
}
