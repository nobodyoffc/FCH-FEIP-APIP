package FeipClient;

import apipClass.ApipParamsForClient;
import apipClient.IdentityAPIs;
import constants.Constants;
import constants.UpStrings;
import eccAes256K1P7.EccAes256K1P7;
import feipClass.FcInfo;
import feipClass.MasterData;
import fipaClass.Algorithm;
import javaTools.JsonTools;
import keyTools.KeyTools;
import txTools.TxBuilder;
import walletTools.SendTo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import static constants.Constants.Dust;

public class IdentityFEIPs {
    public static String promise = "The master owns all my rights.";
    public static String setMasterOnChain(byte[] priKey, String masterPubKey){
        FcInfo fcInfo = new FcInfo(Constants.FEIP,"6","6", UpStrings.MASTER);

        MasterData masterData = new MasterData();
        masterData.setMaster(KeyTools.pubKeyToFchAddr(masterPubKey));
        masterData.setAlg(Algorithm.EccAes256K1P7_No1_NrC7.getName());
        byte[] priKeyCipher = new EccAes256K1P7().encryptAsyOneWayBundle(priKey.clone(), HexFormat.of().parseHex(masterPubKey));
        masterData.setCipherPriKey(Base64.getEncoder().encodeToString(priKeyCipher));
        masterData.setPromise(promise);

        fcInfo.setData(masterData);

        return JsonTools.getString(fcInfo);
    }

    public static String setMasterOnChain(String priKeyCipher, String ownerOrItsPubKey, ApipParamsForClient apipParams, byte[] sessionKey, byte[] symKey) {

        String ownerPubKey;
        if(KeyTools.isValidFchAddr(ownerOrItsPubKey)) {
            ownerPubKey = IdentityAPIs.getPubKey(ownerOrItsPubKey, apipParams, sessionKey);
        }else if(KeyTools.isValidPubKey(ownerOrItsPubKey)){
            ownerPubKey=ownerOrItsPubKey;
        }else return null;

        byte[] priKey = EccAes256K1P7.decryptKey(priKeyCipher,symKey.clone());
        if(priKey==null)return null;
        String masterJson = setMasterOnChain(priKey, ownerPubKey);
        SendTo sendTo = new SendTo();
        sendTo.setFid(ownerOrItsPubKey);
        sendTo.setAmount(Dust);
        List<SendTo> sendToList = new ArrayList<>();
        sendToList.add(sendTo);
        String txId = TxBuilder.sendTxForMsgByAPIP(apipParams,symKey, priKey,sendToList,masterJson);
        return txId;
    }
}
