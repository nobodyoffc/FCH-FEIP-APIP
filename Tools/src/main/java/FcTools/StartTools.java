package FcTools;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bitcoinj.core.ECKey;
import EccAes256K1P7.EccAes256K1P7;


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
        itemList.add("Encrypt with password");
        itemList.add("Encrypt with public key EccAesFc");
        
        itemList.add("Decrypt with symKey");
        itemList.add("Decrypt with password");
        itemList.add("Decrypt with private key EccAesFc");
        itemList.add("Timestamp now");
        menu.add(itemList);

        while(true) {
            System.out.println("<<FreeConsesus Tools>> v1.0.0 by No1_NrC7");
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1:
                    getRandom(br);
                    break;
                case 2:
                    try {
                        pubKeyToAddrs(br);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    findNiceFid(br);
                    break;
                case 4:
                    hex32Base58(br);
                    break;
                    
                case 5:
                    sha256String(br);
                    break;
                case 6:
                    sha256File(br);
                    break;
                case 7:
                    sha256x2String(br);
                    break;
                case 8:
                    sha256x2File(br);
                    break;
                    
                case 9:
                    try{
                        symKeySign(br);
                    }catch (Exception e){
                        System.out.println("Bad input.");
                    }
                    break;
                case 10:
                    signWithEcdsa();
                    break;
                case 11:
                    signWithSchnorr();
                    break;
                case 12:
                    signFchTx();
                    break;
                case 13:
                    verifyEcdsa();
                    break;
                case 14:
                    verifySchnorr();
                    break;
                case 15:
                    encryptWithSymKey(br);
                    break;
                case 16:
                    encryptWithPassword(br);
                    break;
                case 17:
                    encryptWithFcEcc(br);
                    br.readLine();
                    break;
                case 18:
                    decryptWithSymKey(br);
                    break;
                case 19:
                    decryptWithPassword(br);
                    break;
                case 20:
                    decryptWithEccFC(br);
                    break;
                case 21:
                    gainTimestamp();
                    br.readLine();
                    break;

                case 0:
                    br.close();
                    System.out.println("Bye.");
                    return;
            }
        }
    }

    private static void gainTimestamp() {
        long timestamp = System.currentTimeMillis();
        System.out.println(timestamp);
    }

    private static void encryptWithSymKey(BufferedReader br) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException {
        // TODO Auto-generated method stub

        System.out.println("Input the symKey in hex:");
        String symKey = br.readLine();
        if(symKey.length()!=64) {
            System.out.println("The symKey should be 32 bytes in hex.");
            return;
        }
        System.out.println("Input the plaintext:");
        String msg = br.readLine();
        EccAes256K1P7 ecc = new EccAes256K1P7();

        String cipher = ecc.encryptWithSymKey(msg, symKey);

        System.out.println("Ciphertext:\n"+ cipher);
        br.close();

    }

    private static void decryptWithSymKey(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("Input the symKey in hex:");
        String symKey = br.readLine();
        if(symKey.length()!=64) {
            System.out.println("The symKey should be 32 bytes in hex.");
            return;
        }
        System.out.println("Input the cipherext:");
        String cipher = br.readLine();

        EccAes256K1P7 ecc = new EccAes256K1P7();

        String msg = ecc.decrypt(cipher, symKey);
        System.out.println("Plaintext:\n"+ msg);
        br.readLine();
    }

    private static void encryptWithPassword(BufferedReader br) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException {
        // TODO Auto-generated method stub

        System.out.println("Input the password:");
        String password = br.readLine();
        if("".equals(password)) {
            return;
        }
        System.out.println("Input the plaintext:");
        String msg = br.readLine();
        EccAes256K1P7 ecc = new EccAes256K1P7();

        String cipher = ecc.encryptWithPassword(msg, password);

        System.out.println("Ciphertext:\n"+ cipher);
        br.readLine();

    }

    private static void decryptWithPassword(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("Input the password in hex:");
        String password = br.readLine();
        if("".equals(password)) {
            return;
        }
        System.out.println("Input the cipherext:");
        String cipher = br.readLine();

        EccAes256K1P7 ecc = new EccAes256K1P7();

        String msg = ecc.decrypt(cipher, password);
        System.out.println("Plaintext:\n"+ msg);
        br.readLine();
    }


    private static void decryptWithEccFC(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("Input the recipent private key in hex:");
        String priKey = br.readLine();

        if ("".equals(priKey)) {
            return;
        }

        EccAes256K1P7 ecc = new EccAes256K1P7();


        System.out.println("Input the ciphertext in Bast64:");
        String input = br.readLine();

        String textUtf8 = ecc.decrypt(input,priKey);
        
        System.out.println("\n# decrypted: ");
        System.out.println(textUtf8);
        br.readLine();

    }

    private static void decryptWithBtcEcc() {
        // TODO Auto-generated method stub

        System.out.println("Input the ciphertext encrypted with BtcAlgorithm:");
    }

    private static void encryptWithFcEcc(BufferedReader br) throws Exception {
        // TODO Auto-generated method stub

        System.out.println("Input the recipent public key in hex:");
        String pubKey = br.readLine();

        EccAes256K1P7 ecc = new EccAes256K1P7();

        System.out.println("Input the plaintext to be encrypted with EccAes256P7K1 of FC:");
        String input = br.readLine();

        String cipher = ecc.encrypt(input,pubKey);

        System.out.println("\n# Ciphertext:\n"+ cipher);

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
            System.out.println("Longer than 8 bytes, in hex:\n----\n"+BytesTools.bytesToHexStringBE(bytes)+"\n----");
        }
        br.readLine();
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
                System.out.println("New: "+KeyTools.priKey32To38(input));
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
//        for(String type: addrMap.keySet()){
//            System.out.println(type+": "+ addrMap.get(type));
//        }      
        System.out.println("FCH"+": "+ addrMap.get("fchAddr"));
        System.out.println("BTC"+": "+ addrMap.get("btcAddr"));
        System.out.println("ETH"+": "+ addrMap.get("ethAddr"));
        
        System.out.println("----");
        br.readLine();
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
            br.readLine();
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
            br.readLine();
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
        br.readLine();
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
        br.readLine();
    }

    private static void symKeySign(BufferedReader br) throws IOException {
        System.out.println("Input the symKey in hex, enter to exit:");
        String symKey = br.readLine();
        if("".equals(symKey)) {
            return;
        }

        while(true) {
            System.out.println("Input text to be signed, enter to input, 'q' to exit:");
            String text = inputString(br);
            if("q".equals(text))return;
            System.out.println("----");
            System.out.println("Signature:");
            System.out.println("----");
            System.out.println(getSign(symKey,text));
            System.out.println("----");
        }
    }

    private static String inputString(BufferedReader br) throws IOException {
        StringBuilder input = new StringBuilder();

        String line=null;

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
        byte[] textBytes = text.getBytes();
        byte[] keyBytes = BytesTools.hexToByteArray(symKey);
        byte[] bytes = BytesTools.bytesMerger(textBytes,keyBytes);
        System.out.println("----");
        System.out.println("SymKey: ");
        System.out.println("----");
        System.out.println(symKey);
        System.out.println("------");
        System.out.println("Raw text: ");
        System.out.println("----");
        System.out.println(new String(bytes));
        System.out.println("------");
        byte[] signBytes = Hash.Sha256x2(bytes);
        return BytesTools.bytesToHexStringBE(signBytes);
    }
}
