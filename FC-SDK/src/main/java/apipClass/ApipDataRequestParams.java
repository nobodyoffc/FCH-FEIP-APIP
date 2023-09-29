package apipClass;

import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesDataByte;
import eccAes256K1P7.EccAesType;
import javaTools.BytesTools;
import menu.Inputer;

import java.io.BufferedReader;
import java.util.Base64;
import java.util.HexFormat;

public class ApipDataRequestParams {
    private String urlHead;
    private String apipBuyer;
    private String apipBuyerPriKeyCipher;
    private String sessionKeyCipher;
    private String sessionName;

    public String getApipBuyer() {
        return apipBuyer;
    }

    public void setApipBuyer(String apipBuyer) {
        this.apipBuyer = apipBuyer;
    }

    public String getApipBuyerPriKeyCipher() {
        return apipBuyerPriKeyCipher;
    }

    public void setApipBuyerPriKeyCipher(String apipBuyerPriKeyCipher) {
        this.apipBuyerPriKeyCipher = apipBuyerPriKeyCipher;
    }

    public String getUrlHead() {
        return urlHead;
    }

    public void setUrlHead(String urlHead) {
        this.urlHead = urlHead;
    }

    public String getSessionKeyCipher() {
        return sessionKeyCipher;
    }

    public void setSessionKeyCipher(String sessionKeyCipher) {
        this.sessionKeyCipher = sessionKeyCipher;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getVia() {
        String via = "FErVBq2SzT4GGcH61wSvo6BofQ8U5JL3Cg";
        return via;
    }

    public void inputUrlHead(BufferedReader br) {
        System.out.println("Input the urlHead of APIP service. Enter to skip:");
        String input =Inputer.inputString(br);
        if("".equals(input))return;
        urlHead=input;
    }

    public void inputBuyerPriKeyCipher(BufferedReader br){
        while (true) {
            System.out.println("Input the cipher json of APIP buyer's private key:");
            String cipher = Inputer.inputString(br);
            if (cipher==null||"".equals(cipher))return;
//            if(!BytesTools.isBase64Encoded(cipher)){
//                System.out.println("It's not Base64 encoded. Try again.");
//                continue;
//            }
            apipBuyerPriKeyCipher = cipher;
            return;
        }
    }

    public byte[] decryptApipBuyerPriKey(String cipher,BufferedReader br){
        System.out.println("Decrypt APIP buyer private key...");
        char[] password = inputPassword(br);
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesData eccAesData = new EccAesData();
        eccAesData.fromJson(cipher);
        ecc.decrypt(eccAesData);
        String error = eccAesData.getError();
        if(error==null){
            System.out.println("Error:"+error);
            return null;
        }
        byte[] priKey = EccAesDataByte.fromEccAesData(eccAesData).getMsg();
        String priKeyStr = new String(priKey);
        if(BytesTools.isHexString(priKeyStr)){
            System.out.println("Got APIP buyer priKey from UTF-8 encoded hex.");
            return HexFormat.of().parseHex(priKeyStr);
        } else{
            System.out.println("Got APIP buyer priKey.");
            return priKey;
        }
    }

    public void inputSessionKeyCipher(BufferedReader br) {
        String ask = "Input sessionKey:";
        char[] sessionKey = Inputer.input32BytesKey(br, ask);
        char[] passwordBytes = inputPassword(br);
        assert sessionKey != null;
        byte[] sessionKeyBytes = BytesTools.hexCharArrayToByteArray(sessionKey);
        sessionKeyCipher = encryptKey(sessionKeyBytes,BytesTools.hexCharArrayToByteArray(passwordBytes));
        System.out.println("SessionKeyCipher is: "+sessionKeyCipher);
        BytesTools.clearCharArray(passwordBytes);
    }

    public char[] inputPassword(BufferedReader br) {
        String ask;
        ask = "Input password:";
        char[] password = Inputer.inputPassword(br, ask);
//        byte[] passwordBytes = BytesTools.charArrayToByteArray(password, StandardCharsets.UTF_8);
        BytesTools.clearCharArray(password);
        return password;
    }

    public String encryptKey(byte[] keyBytes, byte[] passwordBytes) {
        EccAes256K1P7 ecc = new EccAes256K1P7();

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.Password);
        eccAesDataByte.setMsg(keyBytes);
        eccAesDataByte.setPassword(passwordBytes);
        ecc.encrypt(eccAesDataByte);
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }
}
