package apipClass;

import com.google.gson.Gson;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesDataByte;
import eccAes256K1P7.EccAesType;
import fcTools.Base58;
import feipClass.Service;
import javaTools.BytesTools;
import keyTools.KeyTools;
import menu.Inputer;
import org.checkerframework.checker.units.qual.A;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
            EccAesData eccAesData;
            try{
                eccAesData = new Gson().fromJson(cipher,EccAesData.class);
                if(eccAesData==null ||eccAesData.getCipher()==null){
                    System.out.println("Get priKey cipher failed. Try again.");
                    continue;
                }else if(eccAesData.getError()!=null){
                    System.out.println("Error:"+eccAesData.getError()+". Try again.");
                    continue;
                }
                apipBuyerPriKeyCipher = cipher;
                return;
            }catch (Exception ignore){
                System.out.println("Parse priKey cipher to json wrong.Try again.");
            }
        }
    }

    public byte[] decryptApipBuyerPriKey(String cipher,BufferedReader br) {
        System.out.println("Decrypt APIP buyer private key...");
        char[] password = inputPassword(br);
        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesDataByte eccAesDataByte = ecc.decrypt(cipher, BytesTools.charArrayToByteArray(password, StandardCharsets.UTF_8));
        EccAesDataByte eccAesDataByte = ecc.decrypt(cipher,BytesTools.charArrayToByteArray(password,StandardCharsets.UTF_8));

        if(eccAesDataByte.getError()!=null){
            System.out.println("Error: "+eccAesDataByte.getError());
            return null;
        }

        byte[] priKeyBytes = eccAesDataByte.getMsg();
        char[] priKeyChars = BytesTools.byteArrayToUtf8CharArray(priKeyBytes);

        if(priKeyChars[0]=='0'&&priKeyChars[1]=='x'){
            char[]temp = new char[priKeyChars.length-2];
            System.arraycopy(priKeyChars, 2, temp, 0, temp.length);
            Arrays.fill(priKeyChars,(char)0);
            priKeyChars = temp;
        }

        if(BytesTools.isHexCharArray(priKeyChars)){
            byte[] priKey32 = BytesTools.hexCharArrayToByteArray(priKeyChars);
            Arrays.fill(priKeyChars,(char)0);
            if(priKey32.length==32)return priKey32;
            else {
                Arrays.fill(priKeyChars,(char)0);
                return null;
            }
        }

        if(priKeyChars[0]=='L' || priKeyChars[0]=='K'||priKeyChars[0]=='5'){
            byte[]priKey = Base58.base58CharArrayToByteArray(priKeyChars);
            byte[] priKey32 = KeyTools.getPriKey32(priKey);
            Arrays.fill(priKey,(byte)0);
            return priKey32;
        }

        return null;

//        String priKeyStr = new String(priKey);
//        if (BytesTools.isHexString(priKeyStr)) {
//            System.out.println("Got APIP buyer priKey from UTF-8 encoded hex.");
//            return HexFormat.of().parseHex(priKeyStr);
//        } else {
//            System.out.println("Got APIP buyer priKey.");
//            return priKey;
//        }
    }

    public void inputSessionKeyCipher(BufferedReader br) {
        String ask = "Input sessionKey:";
        char[] sessionKey = Inputer.input32BytesKey(br, ask);
        char[] password = inputPassword(br);
        assert sessionKey != null;
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.Password);
        eccAesDataByte.setMsg(BytesTools.hexCharArrayToByteArray(sessionKey));
        eccAesDataByte.setPassword(BytesTools.charArrayToByteArray(password, StandardCharsets.UTF_8));
        ecc.encrypt(eccAesDataByte);//encryptKey(sessionKeyBytes,BytesTools.hexCharArrayToByteArray(passwordBytes));
        sessionKeyCipher = EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
        System.out.println("SessionKeyCipher is: "+sessionKeyCipher);
        BytesTools.clearCharArray(password);
    }

    public char[] inputPassword(BufferedReader br) {
        String ask;
        ask = "Input password:";
        return Inputer.inputPassword(br, ask);
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
