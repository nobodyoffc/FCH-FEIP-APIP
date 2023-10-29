package apipRequest;

import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesDataByte;
import eccAes256K1P7.EccAesType;
import javaTools.BytesTools;
import keyTools.KeyTools;
import menu.Inputer;

import java.io.BufferedReader;

public class ApipParamsForUser {
    private String urlHead;
    private String apipBuyer;
    private String apipBuyerPriKeyCipher;
    private String sessionKeyCipher;
    private String sessionName;
    private long sessionExpire;
    private String via;

    public long getSessionExpire() {
        return sessionExpire;
    }

    public void setSessionExpire(long sessionExpire) {
        this.sessionExpire = sessionExpire;
    }

    public String getApipBuyer() {
        return apipBuyer;
    }

    public void setApipBuyer(String apipBuyer) {
        this.apipBuyer = apipBuyer;
    }

    public String getApipBuyerPriKeyCipher() {
        return apipBuyerPriKeyCipher;
    }

    public void setVia(String via) {
        this.via = via;
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
        return via;
    }

    public void inputUrlHead(BufferedReader br) {
        System.out.println("Input the urlHead of APIP service. Enter to skip:");
        String input =Inputer.inputString(br);
        if(input.endsWith("/"))input=input.substring(0,input.length()-1);
        if("".equals(input))return;
        urlHead=input;
    }

    public void inputBuyerPriKeyCipher(BufferedReader br,byte[] initSymKey){
        while (true) {
            System.out.println("Input the cipher json of APIP buyer's private key:");
            String cipher = Inputer.inputString(br);
            if (cipher==null||"".equals(cipher))return;

            String ask = "Input the password to decrypt this priKey:";
            char[] userPassword = Inputer.inputPassword(br, ask);

            EccAes256K1P7 ecc = new EccAes256K1P7();
            byte[] userPasswordBytes = BytesTools.utf8CharArrayToByteArray(userPassword);

            EccAesDataByte eccAesDataByte = ecc.decrypt(cipher, userPasswordBytes);
            BytesTools.clearCharArray(userPassword);

            if(eccAesDataByte.getError()!=null){
                System.out.println("Decrypt apipBuyerPriKeyCipher from input wrong."+eccAesDataByte.getError());
                System.out.println("Try again.");
                continue;
            }


            byte[] priKey32 = KeyTools.getPriKey32(eccAesDataByte.getMsg());
            apipBuyer= KeyTools.priKeyToFid(priKey32);
            System.out.println("Your main APIP buyer is: \n"+apipBuyer);

            eccAesDataByte.setMsg(priKey32);

            eccAesDataByte.setType(EccAesType.SymKey);
            eccAesDataByte.setSymKey(initSymKey);
            ecc.encrypt(eccAesDataByte);
            if(eccAesDataByte.getError()!=null){
                System.out.println(eccAesDataByte.getError());
                System.out.println("Try again.");
                continue;
            }
            BytesTools.clearByteArray(eccAesDataByte.getMsg());
            apipBuyerPriKeyCipher = EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
            return;
        }
    }

    public byte[] decryptApipBuyerPriKey(String cipher, byte[] initSymKey) {
        System.out.println("Decrypt APIP buyer private key...");
        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesDataByte eccAesDataByte = ecc.decrypt(cipher, BytesTools.charArrayToByteArray(password, StandardCharsets.UTF_8));
        EccAesDataByte eccAesDataByte = ecc.decrypt(cipher,initSymKey.clone());

        if(eccAesDataByte.getError()!=null){
            System.out.println("Error: "+eccAesDataByte.getError());
            return null;
        }
        return eccAesDataByte.getMsg();
    }

    public void inputSessionKeyCipher(BufferedReader br,final byte[] initSymKey) {
        String ask = "Input sessionKey:";
        char[] sessionKey = Inputer.input32BytesKey(br, ask);
        assert sessionKey != null;
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.SymKey);
        eccAesDataByte.setMsg(BytesTools.hexCharArrayToByteArray(sessionKey));
        eccAesDataByte.setSymKey(initSymKey);
        ecc.encrypt(eccAesDataByte);//encryptKey(sessionKeyBytes,BytesTools.hexCharArrayToByteArray(passwordBytes));
        sessionKeyCipher = EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
        System.out.println("SessionKeyCipher is: "+sessionKeyCipher);
    }

    public byte[] decryptInitSessionKey(final byte[] initSymKey) {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataByte = ecc.decrypt(sessionKeyCipher, initSymKey.clone());
        if(eccAesDataByte.getError()!=null){
            System.out.println("Decrypt initSessionKey error: "+eccAesDataByte.getError());
            return null;
        }
        return eccAesDataByte.getMsg();
    }
}
