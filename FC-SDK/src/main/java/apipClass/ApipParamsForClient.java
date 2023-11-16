package apipClass;

import apipClient.ApipClient;
import apipClient.OpenAPIs;
import apipTools.ApipTools;
import com.google.gson.Gson;
import constants.Constants;
import cryptoTools.Hash;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesDataByte;
import eccAes256K1P7.EccAesType;
import fcTools.ParseTools;
import feipClass.Service;
import fileTools.JsonFileTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import menu.Inputer;
import menu.Menu;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static constants.Constants.APIP_PARAMS_JSON;

public class ApipParamsForClient {
    private String urlHead;
    private String apipBuyer;
    private String apipBuyerPriKeyCipher;
    private String sessionKeyCipher;
    private String sessionName;
    private long sessionExpire;
    private String via;
    private String sid;

    private static final Logger log = LoggerFactory.getLogger(ApipParamsForClient.class);


    @Nullable
    public SignInData requestApipSessionKey(byte[] priKey, String mode) {
        OpenAPIs signIn = new OpenAPIs();
        System.out.println("SignIn APIP (sid)"+sid+" ...");
        ApipClient result = signIn.signInEccPost(this.getUrlHead(), this.getVia(), priKey.clone(), mode);
        if(result==null){
            System.out.println("Get sessionKey cipher from APIP server failed.");
            return null;
        }
        SignInData signInData = OpenAPIs.makeSignInData(result.getResponseBodyStr());

        if(signInData.getSessionKey()==null && signInData.getSessionKeyCipher()==null){
            ResponseBody responseBody = result.getResponseBody();
            System.out.println(responseBody.getMessage());
            return null;
        }
        return signInData;
    }

    public static ApipParamsForClient checkApipParams(BufferedReader br, byte[] passwordBytes) {

        ApipParamsForClient apipParamsForClient = ApipParamsForClient.readApipParamsFromFile();

        byte[] sessionKey;

        if(apipParamsForClient == null) {
            apipParamsForClient = createApipParams(br, passwordBytes);
            if (apipParamsForClient == null) return null;
        }

        boolean revised = false;

        if(apipParamsForClient.getUrlHead() == null){
            apipParamsForClient.inputUrlHead(br);
            System.out.println("Request the service information...");
            Service service = getService(apipParamsForClient.urlHead);
            apipParamsForClient.setSid(service.getSid());
            revised = true;
        }
        if(apipParamsForClient.getApipBuyerPriKeyCipher() == null){
            apipParamsForClient.inputBuyerPriKeyCipher(br, Hash.Sha256x2(passwordBytes));
            sessionKey = apipParamsForClient.makeSession(br, passwordBytes,null);
            if (sessionKey==null) return null;
            revised = true;
        }

        if(apipParamsForClient.getSessionKeyCipher()==null){
            sessionKey = apipParamsForClient.makeSession(br, passwordBytes,null);
            if (sessionKey==null) return null;
            revised = true;
        }

        if(revised) ApipParamsForClient.writeApipParamsToFile(apipParamsForClient, APIP_PARAMS_JSON);

        return apipParamsForClient;
    }

    @Nullable
    public static ApipParamsForClient createApipParams(BufferedReader br, byte[] passwordBytes) {
        byte[] sessionKey;
        ApipParamsForClient apipParamsForClient = new ApipParamsForClient();

        System.out.println("Input the urlHead of the APIP service. Enter to set as 'https://cid.cash/APIP':");

        String urlHead = Inputer.inputString(br);

        if("".equals(urlHead)){
            urlHead = "https://cid.cash/APIP";
        }
        apipParamsForClient.setUrlHead(urlHead);

        System.out.println("Request the service information...");
        Service service = getService(urlHead);
        if(service==null) {
            System.out.println("Get APIP service wrong.");
            return null;
        }
        apipParamsForClient.setSid(service.getSid());

        System.out.println("Input the via Fid. Enter to ignore:");
        String via = Inputer.inputString(br);
        if(!"".equals(via)) apipParamsForClient.setVia(via);

        apipParamsForClient.inputBuyerPriKeyCipher(br, Hash.Sha256x2(passwordBytes));
        sessionKey = apipParamsForClient.makeSession( br, passwordBytes,null);
        if (sessionKey ==null ) return null;

        ApipParamsForClient.writeApipParamsToFile(apipParamsForClient, APIP_PARAMS_JSON);
        return apipParamsForClient;
    }

    private static Service getService(String urlHead) {
        ApipClient apipClient = new OpenAPIs().getService(urlHead);

        Service service;
        try {
            Object serviceObj = apipClient.getResponseBody().getData();
            Gson gson = new Gson();
            service = gson.fromJson(gson.toJson(serviceObj),Service.class);
        }catch (Exception e){
            System.out.println("Get service of "+urlHead+" wrong.");
            e.printStackTrace();
            return null;
        }
        System.out.println("Got the service:");
        System.out.println(ParseTools.gsonString(service));
        return service;
    }

    public void updateApipParams(BufferedReader br, byte[] passwordBytes) {
        byte[] sessionKey;
        String input;

        System.out.println("The urlHead:\n"+urlHead+"\nInput the new one. Enter to skip:");
        input = Inputer.inputString(br);
        if(!"".equals(input)){
            urlHead=input;
            Service service = getService(urlHead);
            if(service==null)return;
            sid = service.getSid();
        }


        System.out.println("The via FID:\n"+via+"\nInput the new one. Enter to skip:");
        input = Inputer.inputString(br);
        if(!"".equals(input))via=input;

        while(true) {
            System.out.println("The buyerPriKeyCipher:\n" + apipBuyerPriKeyCipher + ".\nChange it? y/n:");
            input = Inputer.inputString(br);
            if("n".equals(input))break;
            if ("y".equals(input)) {
                inputBuyerPriKeyCipher(br, Hash.Sha256x2(passwordBytes));
                sessionKey = makeSession(br, passwordBytes, null);
                if (sessionKey == null) return;
                break;
            }
            System.out.println("Wrong input. Try again.");
        }

        ApipParamsForClient.writeApipParamsToFile(this, APIP_PARAMS_JSON);
        System.out.println("APIP service updated to "+urlHead+" at (sid)"+sid+".");
        System.out.println("The buyer is "+apipBuyer);
        Menu.anyKeyToContinue(br);
    }

    private byte[] makeSession(BufferedReader br, byte[] passwordBytes,String mode) {
        byte[] priKey = this.getApipBuyerPriKey(Hash.Sha256x2(passwordBytes), br);

        SignInData signInData = requestApipSessionKey(priKey, mode);

        if (signInData == null) {
            log.error("Sign in APIP failed.");
            return null;
        }

        String sessionKeyCipher = makeCipherFromApipToCipherLocal(signInData.getSessionKeyCipher(), priKey, Hash.Sha256x2(passwordBytes));

        if (sessionKeyCipher == null) {
            log.error("Handle sessionKey failed.");
            return null;
        }

        setSessionKeyCipher(sessionKeyCipher);

        byte[] sessionKey = decryptSessionKey(Hash.Sha256x2(passwordBytes));

        if(sessionKey==null){
            log.error("Decrypt sessionKey failed.");
            return null;
        }

        setSessionName(ApipTools.getSessionName(sessionKey));
        setSessionExpire(signInData.getExpireTime());

        BytesTools.clearByteArray(priKey);
        return sessionKey;
    }

    @Nullable
    public static String makeCipherFromApipToCipherLocal(String cipher, byte[] priKey, byte[] symKey) {
        String sessionKeyCipher;
        byte[] sessionKey = ApipTools.decryptSessionKeyWithPriKey(cipher, priKey.clone());
        if(sessionKey  == null){
            System.out.println("Decrypt sessionKey wrong.");
            return null;
        }

        sessionKeyCipher = EccAes256K1P7.encryptKey(sessionKey , symKey.clone());
        if(sessionKeyCipher.contains("Error")){
            System.out.println("Get sessionKey wrong:"+sessionKeyCipher);
        }
        return sessionKeyCipher;
    }
    public static void writeApipParamsToFile(ApipParamsForClient apipParamsForClient, String fileName) {
        JsonFileTools.writeObjectToJsonFile(apipParamsForClient, fileName,false);
    }

    public static ApipParamsForClient readApipParamsFromFile() {
        File file = new File(APIP_PARAMS_JSON);

        ApipParamsForClient apipParamsForClient;
        try {
            if(!file.exists()){
                boolean done = file.createNewFile();
                if(!done){
                    System.out.println("Create "+APIP_PARAMS_JSON+" wrong.");
                }
            }
            FileInputStream fis = new FileInputStream(file);
            apipParamsForClient = JsonFileTools.readObjectFromJsonFile(fis,ApipParamsForClient.class);
            if(apipParamsForClient!=null)return apipParamsForClient;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public byte[] getApipBuyerPriKey(byte[] initSymKey, BufferedReader br) {
        String priKeyCipher = this.apipBuyerPriKeyCipher;
        if(priKeyCipher==null){
            inputBuyerPriKeyCipher(br,initSymKey);
            priKeyCipher = this.apipBuyerPriKeyCipher;

            if(priKeyCipher==null) {
                System.out.println("Get priKeyCipher failed.");
                return null;
            }
            System.out.println("Got priKeyCipher from config file: "+priKeyCipher);
        }
        return decryptApipBuyerPriKey(priKeyCipher,initSymKey);
    }

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
        System.out.println("Input the urlHead of the APIP service. Enter to set as 'https://cid.cash/APIP':");
        String input =Inputer.inputString(br);
        if(input.endsWith("/"))input=input.substring(0,input.length()-1);
        if("".equals(input)){
            urlHead = "https://cid.cash/APIP";
        }
        urlHead=input;
    }

    public void inputBuyerPriKeyCipher(BufferedReader br,byte[] initSymKey){
//        byte[] priKey32 = new byte[0];
        while (true) {

            byte[] priKey32 = inputCipherGetPriKey(br);
            if(priKey32==null)return;

            apipBuyer = KeyTools.priKeyToFid(priKey32);
            System.out.println("Your main APIP buyer is: \n" + apipBuyer);
            String buyerPriKeyCipher = encrypt32BytesKeyWithSymKeyBytes(priKey32, initSymKey.clone());
            if (buyerPriKeyCipher == null) continue;
            apipBuyerPriKeyCipher = buyerPriKeyCipher;
            return;
        }
    }

    @Nullable
    public static byte[] inputCipherGetPriKey(BufferedReader br) {
        System.out.println("""
                Input  the private key.
                'b' for Base58 code.\s
                'c' for the cipher json.
                'h' for hex.
                other to exit:""");

        String input = Inputer.inputString(br);

        byte[] priKey32 = new byte[0];
        switch (input){
            case "b"->{
                do {
                    System.out.println("Input Apip buyer's private key in Base58:");
                    char[] priKeyBase58 = Inputer.inputPriKeyWif(br);
                    priKey32 = KeyTools.getPriKey32(BytesTools.utf8CharArrayToByteArray(priKeyBase58));
                    String buyer = KeyTools.priKeyToFid(priKey32);
                    System.out.println(buyer);
                    System.out.println("Is this your buyer FID? y/n:");
                    input = Inputer.inputString(br);
                } while (!"y".equals(input));
            }
            case "c"->{
                do {
                    System.out.println("Input the private key cipher json by  "+ Constants.ECC_AES_256_K1_P7+":");
                    String cipher = Inputer.inputString(br);
                    if (cipher == null || "".equals(cipher)) break;

                    String ask = "Input the password to decrypt this priKey:";
                    char[] userPassword = Inputer.inputPassword(br, ask);

                    EccAes256K1P7 ecc = new EccAes256K1P7();
                    byte[] userPasswordBytes = BytesTools.utf8CharArrayToByteArray(userPassword);

                    EccAesDataByte eccAesDataByte;
                    if (cipher.contains("SymKey"))
                        eccAesDataByte = ecc.decrypt(cipher, Hash.Sha256x2(userPasswordBytes));
                    else eccAesDataByte = ecc.decrypt(cipher, userPasswordBytes);

                    BytesTools.clearCharArray(userPassword);

                    if (eccAesDataByte.getError() != null) {
                        System.out.println("Decrypt apipBuyerPriKeyCipher from input wrong." + eccAesDataByte.getError());
                        System.out.println("Try again.");
                        continue;
                    }
                    priKey32 = KeyTools.getPriKey32(eccAesDataByte.getMsg());
                    System.out.println("Your main APIP buyer is: \n" + KeyTools.priKeyToFid(priKey32));
                    System.out.println("Is it right? y/n");
                    input = Inputer.inputString(br);
                } while (!"y".equals(input));
            }
            case "h"->{
                do {
                    char[] priKeyHex = Inputer.input32BytesKey(br,"Input Apip buyer's private key in Hex:");
                    if(priKeyHex==null)break;
                    priKey32 = BytesTools.hexCharArrayToByteArray(priKeyHex);
                    String buyer = KeyTools.priKeyToFid(priKey32);
                    System.out.println(buyer);
                    System.out.println("Is this your buyer FID? y/n:");
                    input = Inputer.inputString(br);
                } while (!"y".equals(input));
            }
            default -> {
                return null;
            }
        }
        return priKey32;
    }

    @Nullable
    public static String encrypt32BytesKeyWithSymKeyBytes(byte[] key32, byte[] initSymKey) {

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        EccAes256K1P7 ecc = new EccAes256K1P7();
        eccAesDataByte.setMsg(key32);

        eccAesDataByte.setType(EccAesType.SymKey);
        eccAesDataByte.setSymKey(initSymKey);
        ecc.encrypt(eccAesDataByte);
        if(eccAesDataByte.getError()!=null){
            System.out.println(eccAesDataByte.getError());
            System.out.println("Try again.");
            return null;
        }
        BytesTools.clearByteArray(eccAesDataByte.getMsg());
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }

    public byte[] decryptApipBuyerPriKey(String cipher, byte[] initSymKey) {
        System.out.println("Decrypt APIP buyer private key...");
        EccAes256K1P7 ecc = new EccAes256K1P7();
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

    public byte[] decryptSessionKey(byte[] symKey) {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataByte = ecc.decrypt(sessionKeyCipher, symKey);
        if(eccAesDataByte.getError()!=null){
            System.out.println("Decrypt initSessionKey error: "+eccAesDataByte.getError());
            return null;
        }
        return eccAesDataByte.getMsg();
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
