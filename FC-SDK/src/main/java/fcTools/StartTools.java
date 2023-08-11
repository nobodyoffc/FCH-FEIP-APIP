package fcTools;


import constants.Constants;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesType;
import fileTools.FileTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import menu.Menu;
import org.bitcoinj.core.ECKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class StartTools {

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Menu menu = new Menu();
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add("Generate a random");
        itemList.add("Get addresses of a pubKey");
        itemList.add("Find a nice FID ending with");
        itemList.add("Swap priKey Hex32 and Base58");
        
        itemList.add("Sha256-string");
        itemList.add("Sha256-file");
        itemList.add("Sha256x2-string");
        itemList.add("Sha256x2-file");
        
        itemList.add("Sign string with symKey sha256x2");
        itemList.add("Sign with ECDSA");
        itemList.add("Sign with Schnorr");
        
        itemList.add("Sign TX of FCH");
        itemList.add("Verify ECDSA");
        itemList.add("Verify Schnorr");
        
        itemList.add("Encrypt with symKey");
        itemList.add("Encrypt with symKey to ivCipher");
        itemList.add("Encrypt with password");
        itemList.add("Encrypt with public key EccAes256K1P7");
        itemList.add("Encrypt file with symKey EccAes256K1P7");
        itemList.add("Encrypt file with public key EccAes256K1P7");
        
        itemList.add("Decrypt with symKey");
        itemList.add("Decrypt with symKey from ivCipher");
        itemList.add("Decrypt with password");
        itemList.add("Decrypt with private key EccAes256K1P7");
        itemList.add("Decrypt file with symKey EccAes256K1P7");
        itemList.add("Decrypt file with public key EccAes256K1P7");
        itemList.add("Timestamp now");
        menu.add(itemList);

        while(true) {
            System.out.println("<<FreeConsesus Tools>> v1.0.0 by No1_NrC7");
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> getRandom(br);
                case 2 -> {
                    try {
                        pubKeyToAddrs(br);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case 3 -> findNiceFid(br);
                case 4 -> hex32Base58(br);
                case 5 -> sha256String(br);
                case 6 -> sha256File(br);
                case 7 -> sha256x2String(br);
                case 8 -> sha256x2File(br);
                case 9 -> symKeySign(br);
                case 10 -> signWithEcdsa();
                case 11 -> signWithSchnorr();
                case 12 -> signFchTx();
                case 13 -> verifyEcdsa();
                case 14 -> verifySchnorr();
                case 15 -> encryptWithSymKey(br);
                case 16 -> encryptWithSymKeyIvCipher(br);
                case 17 -> encryptWithPassword(br);
                case 18 -> encryptAsy(br);
                case 19 -> encryptFileWithSymKey(br);
                case 20 -> encryptFileAsy(br);
                case 21 -> decryptWithSymKey(br);
                case 22 -> decryptWithSymKeyIvCipher(br);
                case 23 -> decryptWithPassword(br);
                case 24 -> decryptAsy(br);
                case 25 -> decryptFileSymKey(br);
                case 26 -> decryptFileAsy(br);
                case 27 -> {
                    gainTimestamp();
                    Menu.anyKeyToContinue(br);
                }
                case 0 -> {
                    br.close();
                    System.out.println("Bye.");
                    return;
                }
            }
        }
    }

    private static void decryptFileSymKey(BufferedReader br) {
        File encryptedFile = FileTools.getAvailableFile(br);
        if(encryptedFile==null||encryptedFile.length()> Constants.MAX_FILE_SIZE_M * Constants.M_BYTES)return;
        char[] symKey = EccAes256K1P7.inputSymKey(br);
        String result = EccAes256K1P7.decryptFileWithSymKey(encryptedFile,symKey);
        System.out.println(result);
        Menu.anyKeyToContinue(br);
    }

    private static void encryptFileWithSymKey(BufferedReader br) {
        File originalFile = FileTools.getAvailableFile(br);
        if(originalFile==null||originalFile.length()> Constants.MAX_FILE_SIZE_M * Constants.M_BYTES)return;
        char[] symKey = EccAes256K1P7.inputSymKey(br);
        System.out.println(EccAes256K1P7.encryptFileSymKey(originalFile, symKey));
        Menu.anyKeyToContinue(br);
    }

    private static void decryptWithSymKeyIvCipher(BufferedReader br) {
        System.out.println("Input ivCipher in Base64:");
        String ivCipherStr;
        try {
            ivCipherStr = br.readLine();
            if("".equals(ivCipherStr))return;
        } catch (IOException e) {
            System.out.println("BufferedReader wrong;");
            return;
        }
        char[] symKey = EccAes256K1P7.inputSymKey(br);
        String eccAesDataJson = EccAes256K1P7.decryptSymKeyFromIvCipher(ivCipherStr, symKey);
        System.out.println(eccAesDataJson);
        Menu.anyKeyToContinue(br);
    }

//    private static char[] inputSymKeyOld(BufferedReader br) {
//        char[] symKey = new char[64];
//        int num = 0;
//        try {
//            num = br.read(symKey);
//        } catch (IOException e) {
//            System.out.println("BufferedReader wrong.");
//            return null;
//        }
//        if(num!=64 || !BytesTools.isHexCharArray(symKey)){
//            System.out.println("The symKey should be 64 characters in hex.");
//            return null;
//        }
//        return symKey;
//    }


    private static void encryptWithSymKeyIvCipher(BufferedReader br) {
        String msg = EccAes256K1P7.inputMsg(br);
        char[] symKey = EccAes256K1P7.inputSymKey(br);
        EccAes256K1P7 ecc = new EccAes256K1P7();
        String ivCipher = ecc.encryptSymKeyGetIvCipher(msg, symKey);
        System.out.println(ivCipher);
        Menu.anyKeyToContinue(br);
    }



    private static void decryptFileAsy(BufferedReader br) {
        File encryptedFile = FileTools.getAvailableFile(br);
        if(encryptedFile==null||encryptedFile.length()> Constants.MAX_FILE_SIZE_M * Constants.M_BYTES)return;

        System.out.println("Input the recipient private key in hex:");
        char[] priKey = new char[64];
        int num = 0;
        try {
            num = br.read(priKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(num!=64 || !javaTools.BytesTools.isHexCharArray(priKey)){
            System.out.println("The symKey should be 64 characters in hex.");
        }
        String result = EccAes256K1P7.decryptFileWithPriKey(encryptedFile,javaTools.BytesTools.hexCharArrayToByteArray(priKey));
        System.out.println(result);
        Menu.anyKeyToContinue(br);
    }

    private static void encryptFileAsy(BufferedReader br) {

        File originalFile = FileTools.getAvailableFile(br);
        if(originalFile==null||originalFile.length()> Constants.MAX_FILE_SIZE_M * Constants.M_BYTES)return;
        String pubKeyB;
        pubKeyB = getPubKey(br);
        if (pubKeyB == null) return;
        System.out.println(EccAes256K1P7.encryptFileAsy(originalFile, pubKeyB));
        Menu.anyKeyToContinue(br);
    }

    private static String getPubKey(BufferedReader br) {
        System.out.println("Input the recipient public key in hex:");
        String pubKeyB;
        try {
            pubKeyB = br.readLine();
        } catch (IOException e) {
            System.out.println("BufferedReader wrong:"+e.getMessage());
            return null;
        }
        if(pubKeyB.length()!=66){
            System.out.println("The public key should be 66 characters of hex.");
        }
        return pubKeyB;
    }


    private static void gainTimestamp() {
        long timestamp = System.currentTimeMillis();
        System.out.println(timestamp);
    }

    private static void encryptWithSymKey(BufferedReader br)  {
        String msg = EccAes256K1P7.inputMsg(br);
        if(msg==null)return;
        char[] symKey = EccAes256K1P7.inputSymKey(br);
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesData eccAesData = new EccAesData(EccAesType.SymKey,msg,symKey);

        ecc.encrypt(eccAesData);

        System.out.println(eccAesData.toJson());
        Menu.anyKeyToContinue(br);
    }

    private static void decryptWithSymKey(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub

        System.out.println("Input the json string of EccAesData:");
        String eccAesDataJson = br.readLine();

        System.out.println("Input the symKey in hex:");
        char[] symKey = EccAes256K1P7.inputSymKey(br);
        if(symKey==null)return;

        EccAes256K1P7 ecc = new EccAes256K1P7();
        eccAesDataJson = ecc.decryptWithSymKey(eccAesDataJson,symKey);
        System.out.println(eccAesDataJson);
        Menu.anyKeyToContinue(br);
    }

    private static void encryptWithPassword(BufferedReader br) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException {
        // TODO Auto-generated method stub

        System.out.println("Input the password no longer than 64:");
        char[] passwordBuffer = new char[64];
        int num = br.read(passwordBuffer);

        if(num==0){
            System.out.println("Get your password wrong.");
            return;
        }

        char[] password = new char[num];
        System.arraycopy(passwordBuffer, 0, password, 0, num);

        System.out.println("Input the plaintext:");
        String msg = br.readLine();
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesData eccAesData = new EccAesData(EccAesType.Password,msg,password);
        ecc.encrypt(eccAesData);
        System.out.println(eccAesData.toJson());

        Menu.anyKeyToContinue(br);
    }

    private static void decryptWithPassword(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("Input the password no longer than 64:");
        char[] passwordBuffer = new char[64];
        int num = br.read(passwordBuffer);

        if(num==0){
            System.out.println("Get your password wrong.");
            return;
        }

        char[] password = new char[num];
        System.arraycopy(passwordBuffer, 0, password, 0, num);
        System.out.println("Input the json string of EccAesData:");
        String eccAesDataJson = br.readLine();

        EccAes256K1P7 ecc = new EccAes256K1P7();

        System.out.println(ecc.decryptWithPassword(eccAesDataJson,password));

        Menu.anyKeyToContinue(br);
    }


    private static void decryptAsy(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub

        System.out.println("Input the json string of EccAesData:");
        String eccAesDataJson = br.readLine();

        decryptAsyJson(br, eccAesDataJson);

        Menu.anyKeyToContinue(br);

    }

    private static void decryptAsyJson(BufferedReader br, String eccAesDataJson)  {
        System.out.println("Input the recipient private key in hex:");
        char[] priKey = new char[64];
        int num;
        try {
            num = br.read(priKey);
        } catch (IOException e) {
            System.out.println("BufferedReader wrong.");
            return;
        }
        if(num!=64 || !BytesTools.isHexCharArray(priKey)){
            System.out.println("The private key should be 64 characters in hex.");
        }

        EccAes256K1P7 ecc = new EccAes256K1P7();

        EccAesData eccAesData = ecc.decryptAsy(eccAesDataJson, priKey);

        System.out.println(eccAesData.toJson());
    }

    private static void decryptWithBtcEcc() {
        // TODO Auto-generated method stub

        System.out.println("Input the ciphertext encrypted with BtcAlgorithm:");
    }

    private static void encryptAsy(BufferedReader br)  {

        EccAesData eccAesData = getEncryptedEccAesData(br);
        if (eccAesData == null) return;
        if(eccAesData.getError()!=null){
            System.out.println(eccAesData.getError());
        }else System.out.println(eccAesData.toJson());
        Menu.anyKeyToContinue(br);
    }

    private static EccAesData getEncryptedEccAesData(BufferedReader br) {
        System.out.println("Input the recipient public key in hex:");
        String pubKeyB;
        String msg;
        try {
            pubKeyB = br.readLine();
            if (pubKeyB.length() != 66) {
                System.out.println("The public key should be 66 characters of hex.");
                return null;
            }
            System.out.println("Input the msg:");
            msg = br.readLine();
        }catch (Exception e){
            System.out.println("BufferedReader wrong.");
            return null;
        }
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesData eccAesData = new EccAesData(EccAesType.AsyOneWay,msg,pubKeyB);

        ecc.encrypt(eccAesData);
        return eccAesData;
    }

    private static void encryptWithBtcEcc() {
        // TODO Auto-generated method stub
        System.out.println("encryptWithBtcAlgo is under developing:");
    }

    private static void verifySchnorr() {
        // TODO Auto-generated method stub
        System.out.println("verifySchnorr is under developing:");
    }

    private static void verifyEcdsa() {
        // TODO Auto-generated method stub
        System.out.println("verifyEcdsa is under developing:");
    }

    private static void signFchTx() {
        // TODO Auto-generated method stub
        System.out.println("signFchTx is under developing:");
    }

    private static void signWithSchnorr() {
        // TODO Auto-generated method stub
        System.out.println("signWithSchnorr is under developing:");
    }

    private static void signWithEcdsa() {
        // TODO Auto-generated method stub
        System.out.println("signWithEcdsa is under developing:");
    }

    public static void getRandom(BufferedReader br) throws IOException {
        // TODO Auto-generated method stub
        SecureRandom secureRandom = new SecureRandom();
        int len =0;
        while(true) {
            System.out.println("Input the bytes length of the random you want. Enter to exit:");
            String input = br.readLine();
            if ("".equals(input)) {
                return;
            }

            try {
                len = Integer.parseInt(input);
                break;
            }catch(Exception e) {
                continue;
            }
        }
        byte[] bytes = new byte[len];
        secureRandom.nextBytes(bytes);


        if (bytes.length <= 8) {
            // Create a ByteBuffer with the byte array
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.put(bytes);
            buffer.flip();
            
            // Convert the byte array to a long
            long value = 0;
            
            // Read the bytes from the ByteBuffer
            for (int i = 0; i < bytes.length; i++) {
                value = (value << 8) | (buffer.get() & 0xFF);
            }
            
            System.out.println("No longer than 8 bytes, in number:\n----\n"+Math.abs(value)+"\n----");
        }else {
            System.out.println("Longer than 8 bytes, in hex:\n----\n"+ BytesTools.bytesToHexStringBE(bytes)+"\n----");
        }
        Menu.anyKeyToContinue(br);
    }

    private static void hex32Base58(BufferedReader br) throws IOException {
        String input=null;
        while (true) {
            System.out.println("Input 32 bytes hex or base58 string, enter to exit:");
            input = br.readLine();
            if ("".equals(input)) {
                return;
            }
            if(input.length()==64){
                System.out.println("Hex to Base58:"+"\n----");
                System.out.println("New: "+ KeyTools.priKey32To38(input));
                System.out.println("Old: "+KeyTools.priKey32To37(input)+"\n----");

            }else if(input.length()==52){
                System.out.println("Base58 WIF compressed to Hex:"+"\n----");
                System.out.println(KeyTools.getPriKey32(input)+"\n----");
            }else if(input.length()==51){
                System.out.println("Base58 WIF to Hex:"+"\n----");
                System.out.println(KeyTools.getPriKey32(input)+"\n----");
            }else{
                System.out.println("Only 64 chars hex or 52 chars base58 string can be accepted.");
            };
        }
    }

    private static void findNiceFid(BufferedReader br) throws IOException {
        String input = null;
        SimpleDateFormat sdf = new SimpleDateFormat();
        Date begin = new Date();
        System.out.println(sdf.format(begin));
        while (true) {
            System.out.println("Input 4 characters you want them be in the end of your fid, enter to exit:");
            input = br.readLine();
            if ("".equals(input)) {
                return;
            }
            if(input.length()!=4){
                System.out.println("Input 4 characters you want them be in the end of your fid:");
            }else break;
        }
        long i =0;
        long j = 0;
        while (true) {
            ECKey ecKey = new ECKey();
            String fid = KeyTools.pubKeyToFchAddr(ecKey.getPubKey());
            if(fid.substring(30).equals(input)){
                System.out.println("----");
                System.out.println("FID:"+fid);
                System.out.println("PubKey: "+ecKey.getPublicKeyAsHex());
                System.out.println("PriKey: "+ecKey.getPrivateKeyAsHex());
                System.out.println("----");
                System.out.println("Begin at: "+sdf.format(begin));
                Date end = new Date();
                System.out.println("End at: "+sdf.format(end));
                System.out.println("----");
                break;
            }
            i++;
            if(i%1000000==0) {
                j++;
                System.out.println(sdf.format(new Date())+": "+j+" million tryings.");
            }
        }
    }

    private static void pubKeyToAddrs(BufferedReader br) throws IOException {
        System.out.println("Input the public key, enter to exit:");
        String pubKey = br.readLine();
        if ("".equals(pubKey)) {
            return;
        }
        Map<String, String> addrMap = KeyTools.pubKeyToAddresses(pubKey);


        System.out.println("----");

        System.out.println("FCH"+": "+ addrMap.get("fchAddr"));
        System.out.println("BTC"+": "+ addrMap.get("btcAddr"));
        System.out.println("ETH"+": "+ addrMap.get("ethAddr"));
        
        System.out.println("----");
        Menu.anyKeyToContinue(br);
    }

    private static void sha256File(BufferedReader br) throws IOException {
        while(true) {
            System.out.println("Input the full path of the file to be hashed, enter to exit:");
            String filePath = br.readLine();
            if ("".equals(filePath)) {
                return;
            }
            // Create a File object with the specified path
            File file = new File(filePath);
            // Check if the file exists
            if(file.isDirectory()){
                System.out.println("It's a directory.");
                break;
            }
            if (file.exists()) {
                System.out.println("File name: " + file.getName());
            } else {
                System.out.println("File does not exist.");
                break;
            }
            String hash = Hash.Sha256(file);
            System.out.println("----");
            System.out.println("file:" + filePath);
            System.out.println("sha256:" + hash );
            System.out.println("----");
            Menu.anyKeyToContinue(br);
        }
    }

    private static void sha256x2File(BufferedReader br) throws IOException {
        while(true) {
            System.out.println("Input the full path of the file to be hashed, enter to exit:");
            String filePath = br.readLine();
            if ("".equals(filePath)) {
                return;
            }
            // Create a File object with the specified path
            File file = new File(filePath);
            // Check if the file exists
            if(file.isDirectory()){
                System.out.println("It's a directory.");
                break;
            }
            if (file.exists()) {
                System.out.println("File name: " + file.getName());
            } else {
                System.out.println("File does not exist.");
                break;
            }
            String hash = Hash.Sha256x2(file);
            System.out.println("----");
            System.out.println("file:" + filePath );
            System.out.println("sha256:" + hash);
            System.out.println("----");
            Menu.anyKeyToContinue(br);
        }
    }

    private static void sha256String(BufferedReader br) throws IOException {
        System.out.println("Input the string to be hashed:");
        String text = inputString(br);
        String hash = Hash.Sha256(text);
        System.out.println("----");
        System.out.println("raw string:");
        System.out.println("----");
        System.out.println(text);
        System.out.println("----");
        System.out.println("sha256:" +hash);
        System.out.println("----");
        Menu.anyKeyToContinue(br);
    }

    private static void sha256x2String(BufferedReader br) throws IOException {
        System.out.println("Input the string to be hashed:");
        String text = inputString(br);
        String hash = Hash.Sha256x2(text);
        System.out.println("----");
        System.out.println("raw string:");
        System.out.println("----");
        System.out.println(text);
        System.out.println("----");
        System.out.println("sha256:" +hash);
        System.out.println("----");
        Menu.anyKeyToContinue(br);
    }

    private static void symKeySign(BufferedReader br) {
        System.out.println("Input the symKey in hex, enter to exit:");
        String symKey;
        try {
            symKey = br.readLine();

            if("".equals(symKey)) {
                return;
            }

            while(true) {
                System.out.println("Input text to be signed, enter to input, 'q' to exit:");
                String text = inputString(br);
                if("q".equals(text))return;
                String sign = getSign(symKey,text);
                System.out.println("----");
                System.out.println("Signature:");
                System.out.println("----");
                System.out.println(sign);
                System.out.println("----");
            }
        } catch (IOException e) {
            System.out.println("BufferedReader wrong.");
        }
    }

    private static String inputString(BufferedReader br) throws IOException {
        StringBuilder input = new StringBuilder();

        String line;

        while (true) {
            line = br.readLine();
            if("".equals(line)||"\n".equals(line))break;
            input.append(line).append("\n");
        }

        // Access the complete input as a string
        String text = input.toString();

        if(text.endsWith("\n")) {
            text = text.substring(0, input.length()-1);
        }
        return text;
    }


    private static String getSign(String symKey,String text) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = BytesTools.hexToByteArray(symKey);
        byte[] bytes = BytesTools.bytesMerger(textBytes,keyBytes);
        System.out.println("----");
        System.out.println("Content in hex to be signed: ");
        System.out.println("----");
        System.out.println(HexFormat.of().formatHex(bytes));
//        System.out.println("------");
        byte[] signBytes = Hash.Sha256x2(bytes);
        return BytesTools.bytesToHexStringBE(signBytes);
    }
}
