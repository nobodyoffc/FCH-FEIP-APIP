package apipTools;

import FeipClient.IdentityFEIPs;
import apipClass.ApipParamsForClient;
import apipClass.CidInfo;
import apipClient.IdentityAPIs;
import appUtils.Inputer;
import cryptoTools.SHA;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesDataByte;
import javaTools.BytesTools;
import keyTools.KeyTools;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

import static constants.ApiNames.apiList;
import static constants.ApiNames.freeApiList;
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

    public static String getSessionKeySign(byte[] sessionKeyBytes, byte[] dataBytes) {
        return HexFormat.of().formatHex(SHA.Sha256x2(BytesTools.bytesMerger(dataBytes, sessionKeyBytes)));
    }

    public static boolean isGoodSign(String requestBody, String sign, String symKey){
        byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);
        return isGoodSign(requestBodyBytes,sign,HexFormat.of().parseHex(symKey));
    }

    public static boolean isGoodSign(byte[] requestBodyBytes, String sign, byte[] symKey){
        if(sign==null||requestBodyBytes==null)return false;
        byte[] signBytes = BytesTools.bytesMerger(requestBodyBytes, symKey);
        String doubleSha256Hash = HexFormat.of().formatHex(SHA.Sha256x2(signBytes));
        return (sign.equals(doubleSha256Hash));
    }

    public static String getSessionName(byte[] sessionKey) {
        if (sessionKey==null)return null;
        return HexFormat.of().formatHex(Arrays.copyOf(sessionKey,6));
    }

    public static byte[] decryptSessionKeyWithPriKey(String cipher, byte[] priKey) {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataBytes = ecc.decrypt(cipher, priKey.clone());
        if(eccAesDataBytes.getError()!=null){
            System.out.println("Decrypt sessionKey wrong: "+eccAesDataBytes.getError());
            BytesTools.clearByteArray(priKey);
            return null;
        }
        String sessionKeyHex = new String(eccAesDataBytes.getMsg(), StandardCharsets.UTF_8);
        return HexFormat.of().parseHex(sessionKeyHex);
    }

    public static void checkMaster(String priKeyCipher, byte[] initSymKey, ApipParamsForClient initApipParams, BufferedReader br) {
        byte[] priKey = EccAes256K1P7.decryptKey(priKeyCipher,initSymKey);
        if(priKey==null){
            throw new RuntimeException("Failed to decrypt priKey.");
        }
        String fid = KeyTools.priKeyToFid(priKey);
        byte[] sessionKey = initApipParams.decryptSessionKey(initSymKey.clone());
        CidInfo cid = IdentityAPIs.getCidInfo(fid,initApipParams,sessionKey);
        if(cid!=null) {
            if(cid.getMaster()!=null)return;
            if(Inputer.askIfYes(br,"Assign a master for "+fid+"? y/n:")) {
                while(true) {
                    String master = Inputer.inputString(br, "Input the master FID or pubKey:");
                    if(KeyTools.isValidPubKey(master) || KeyTools.isValidFchAddr(master)) {
                        String result = IdentityFEIPs.setMasterOnChain(priKeyCipher, master, initApipParams, sessionKey,initSymKey);
                        if(result==null) System.out.println("Failed to set master.");;
                        if (BytesTools.isHexString(result)) System.out.println("Master was set at txId: " + result);
                        else System.out.println(result);
                        break;
                    }
                }
            }
        }else {
            System.out.println("Failed to get CID information of "+fid+".");
        }
    }
}
