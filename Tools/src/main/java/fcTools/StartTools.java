//package fcTools;
//
//
//import java.io.*;
//import java.nio.ByteBuffer;
//import java.security.*;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import javax.crypto.BadPaddingException;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//
//import FipaClass.Affair;
//import FipaClass.Op;
//import com.google.gson.Gson;
//import constants.Constants;
//import eccAes256K1P7.EccAesData;
//import eccAes256K1P7.EccAesDataByte;
//import eccAes256K1P7.EccAesType;
//import fileTools.FileTools;
//import fileTools.JsonFileTools;
//import org.bitcoinj.core.ECKey;
//import eccAes256K1P7.EccAes256K1P7;
//
//
//public class StartTools {
//
//    public static void main(String[] args) throws Exception {
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//        Menu menu = new Menu();
//        ArrayList<String> itemList = new ArrayList<>();
//        itemList.add("Generate a random");
//        itemList.add("Get addresses of a pubKey");
//        itemList.add("Find a nice FID ending with");
//        itemList.add("Swap priKey Hex32 and Base58");
//
//        itemList.add("Sha256-string");
//        itemList.add("Sha256-file");
//        itemList.add("Sha256x2-string");
//        itemList.add("Sha256x2-file");
//
//        itemList.add("Sign string with symKey sha256x2");
//        itemList.add("Sign with ECDSA");
//        itemList.add("Sign with Schnorr");
//
//        itemList.add("Sign TX of FCH");
//        itemList.add("Verify ECDSA");
//        itemList.add("Verify Schnorr");
//
//        itemList.add("Encrypt with symKey");
//        itemList.add("Encrypt with password");
//        itemList.add("Encrypt with public key EccAes256K1P7");
//        itemList.add("Encrypt file with public key EccAes256K1P7");
//
//        itemList.add("Decrypt with symKey");
//        itemList.add("Decrypt with password");
//        itemList.add("Decrypt with private key EccAes256K1P7");
//        itemList.add("Decrypt file with public key EccAes256K1P7");
//        itemList.add("Timestamp now");
//        menu.add(itemList);
//
//        while(true) {
//            System.out.println("<<FreeConsesus Tools>> v1.0.0 by No1_NrC7");
//            menu.show();
//            int choice = menu.choose(br);
//            switch (choice) {
//                case 1:
//                    getRandom(br);
//                    break;
//                case 2:
//                    try {
//                        pubKeyToAddrs(br);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    break;
//                case 3:
//                    findNiceFid(br);
//                    break;
//                case 4:
//                    hex32Base58(br);
//                    break;
//
//                case 5:
//                    sha256String(br);
//                    break;
//                case 6:
//                    sha256File(br);
//                    break;
//                case 7:
//                    sha256x2String(br);
//                    break;
//                case 8:
//                    sha256x2File(br);
//                    break;
//
//                case 9:
//                    try{
//                        symKeySign(br);
//                    }catch (Exception e){
//                        System.out.println("Bad input.");
//                    }
//                    break;
//                case 10:
//                    signWithEcdsa();
//                    break;
//                case 11:
//                    signWithSchnorr();
//                    break;
//                case 12:
//                    signFchTx();
//                    break;
//                case 13:
//                    verifyEcdsa();
//                    break;
//                case 14:
//                    verifySchnorr();
//                    break;
//                case 15:
//                    encryptWithSymKey(br);
//                    break;
//                case 16:
//                    encryptWithPassword(br);
//                    break;
//                case 17:
//                    encryptAsy(br);
//                    br.readLine();
//                    break;
//                case 18:
//                    encryptFileAsy(br);
//                    br.readLine();
//                    break;
//                case 19:
//                    decryptWithSymKey(br);
//                    break;
//                case 20:
//                    decryptWithPassword(br);
//                    break;
//                case 21:
//                    decryptAsy(br);
//                    break;
//                case 22:
//                    decryptFileAsy(br);
//                    break;
//                case 23:
//                    gainTimestamp();
//                    br.readLine();
//                    break;
//                case 0:
//                    br.close();
//                    System.out.println("Bye.");
//                    return;
//            }
//        }
//    }
//
//    private static void decryptFileAsy(BufferedReader br) {
//        File encryptedFile = FileTools.getAvailableFile(br);
//        if(encryptedFile==null||encryptedFile.length()> Constants.MAX_FILE_SIZE_M * Constants.M_BYTES)return;
//
//        System.out.println("Input the recipient private key in hex:");
//        char[] priKey = new char[64];
//        int num = 0;
//        try {
//            num = br.read(priKey);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        if(num!=64 || !javaTools.BytesTools.isHexCharArray(priKey)){
//            System.out.println("The symKey should be 64 characters in hex.");
//        }
//        String result = EccAes256K1P7.decryptFile(encryptedFile,javaTools.BytesTools.hexCharArrayToByteArray(priKey));
//        System.out.println(result);
//    }
//
//    private static void encryptFileAsy(BufferedReader br) {
//
//        File originalFile = FileTools.getAvailableFile(br);
//        if(originalFile==null||originalFile.length()> Constants.MAX_FILE_SIZE_M * Constants.M_BYTES)return;
//
//        System.out.println("Input the recipient public key in hex:");
//        String pubKeyB;
//        try {
//            pubKeyB = br.readLine();
//        } catch (IOException e) {
//            System.out.println("BufferedReader wrong:"+e.getMessage());
//            return;
//        }
//        if(pubKeyB.length()!=66){
//            System.out.println("The public key should be 66 characters of hex.");
//        }
//
//        EccAesData eccAesData = new EccAesData();
//        eccAesData.setType(EccAesType.AsyOneWay);
//        eccAesData.setPubKeyB(pubKeyB);
//
//        EccAes256K1P7.encryptFile(originalFile, eccAesData, pubKeyB);
//    }
//
//    public static void encryptFile(File originalFile, EccAesData eccAesData, String pubKeyB) {
//        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
//        byte[] pubKeyBBytes = HexFormat.of().parseHex(pubKeyB);
//        EccAes256K1P7.encryptFile(originalFile,eccAesDataByte,pubKeyBBytes);
//    }
//    public static void encryptFile(File originalFile, EccAesDataByte eccAesDataByte, byte[] pubKeyBBytes) {
//        byte[] msgBytes;
//        try (FileInputStream fis = new FileInputStream(originalFile)) {
//            msgBytes = fis.readAllBytes();
//        }catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        if(msgBytes!=null&& msgBytes.length!=0) eccAesDataByte.setMsg(msgBytes);
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        ecc.encrypt(eccAesDataByte);
//        if(eccAesDataByte.getError()!=null){
//            System.out.println(eccAesDataByte.getError());
//        }else {
//            String parentPath = originalFile.getParent();
//            String originalFileName = originalFile.getName();
//            int endIndex = originalFileName.lastIndexOf('.');
//            String suffix = "_"+originalFileName.substring(endIndex+1);
//            String encryptedFileName = originalFileName.substring(0,endIndex)+suffix+Constants.DOT_FV;
//            File encryptedFile = FileTools.getNewFile(parentPath, encryptedFileName);
//            if(encryptedFile==null) return;
//            try(FileOutputStream fos = new FileOutputStream(encryptedFile)){
//                byte[] cipherBytes = new byte[0];
//                cipherBytes = eccAesDataByte.getCipher();
//                eccAesDataByte.setCipher(null);
//                Affair affair = new Affair();
//                affair.setOp(Op.encrypt);
//                affair.setFid(KeyTools.pubKeyToFchAddr(pubKeyBBytes));
//                affair.setOid(Hash.Sha256x2(originalFile));
//                EccAesData eccAesData = EccAesData.fromEccAesDataByte(eccAesDataByte);
//                affair.setData(eccAesData);
//                fos.write(new Gson().toJson(affair).getBytes());
//                fos.write(cipherBytes);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    public static String decryptFile(File encryptedFile, byte[] priKeyBBytes) {
//        byte[] cipherBytes;
//        Affair affair;
//        Gson gson = new Gson();
//        EccAesData eccAesData;
//        EccAesDataByte eccAesDataByte;
//        try (FileInputStream fis = new FileInputStream(encryptedFile)) {
//            affair = JsonFileTools.readObjectFromJsonFile(fis,Affair.class);
//            cipherBytes = fis.readAllBytes();
//            if(affair==null)return "Error:affair is null.";
//            if(affair.getData()==null)return "Error:affair.data is null.";
//            eccAesData = gson.fromJson(gson.toJson(affair.getData()),EccAesData.class);
//            if(eccAesData==null)return "Got eccAesData null.";
//        }catch (IOException e) {
//            return "Read file wrong.";
//        }
//        eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
//        eccAesDataByte.setPriKeyB(priKeyBBytes);
//        eccAesDataByte.setCipher(cipherBytes);
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        ecc.decrypt(eccAesDataByte);
//
//        if(eccAesDataByte.getError()!=null){
//            return eccAesDataByte.getError();
//        }else {
//            String parentPath = encryptedFile.getParent();
//            String encryptedFileName = encryptedFile.getName();
//            int endIndex1 = encryptedFileName.lastIndexOf('_');
//            int endIndex2 = encryptedFileName.lastIndexOf('.');
//            String oldSuffix = encryptedFileName.substring(endIndex1+1,endIndex2);
//            String originalFileName = encryptedFileName.substring(0,endIndex1)+"."+oldSuffix;
//
//            File originalFile = FileTools.getNewFile(parentPath, originalFileName);
//            if(originalFile==null) return "Create recovered file failed.";
//            try(FileOutputStream fos = new FileOutputStream(originalFile)){
//                fos.write(eccAesDataByte.getMsg());
//                return "Done";
//            } catch (IOException e) {
//                return "Write file wrong";
//            }
//        }
//    }
//
//
//    private static void gainTimestamp() {
//        long timestamp = System.currentTimeMillis();
//        System.out.println(timestamp);
//    }
//
//    private static void encryptWithSymKey(BufferedReader br) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException {
//        // TODO Auto-generated method stub
//        System.out.println("Input the plaintext:");
//        String msg = br.readLine();
//
//        System.out.println("Input the symKey in hex:");
//        char[] symKey = new char[64];
//        int num = br.read(symKey);
//        if(num!=64 || !javaTools.BytesTools.isHexCharArray(symKey)){
//            System.out.println("The symKey should be 32 bytes in hex.");
//            return;
//        }
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesData eccAesData = new EccAesData(EccAesType.SymKey,msg,symKey);
//
//        ecc.encrypt(eccAesData);
//
//        System.out.println(eccAesData.toJson());
//        br.readLine();
//    }
//
//    private static void decryptWithSymKey(BufferedReader br) throws Exception {
//        // TODO Auto-generated method stub
//
//        System.out.println("Input the json string of EccAesData:");
//        String eccAesDataJson = br.readLine();
//
//        System.out.println("Input the symKey in hex:");
//        char[] symKey = new char[64];
//        int num = br.read(symKey);
//        if(num!=64 || !javaTools.BytesTools.isHexCharArray(symKey)){
//            System.out.println("The symKey should be 64 characters in hex.");
//        }
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesData eccAesData = new EccAesData();
//        if(eccAesData.getType()!=EccAesType.SymKey){
//            System.out.println("The type should be 'SymKey' instead of "+eccAesData.getType());
//        }
//        ecc.decryptWithSymKey(eccAesDataJson,symKey);
//        if(eccAesData.getError()!=null){
//            System.out.println(eccAesData.getError());
//        }else System.out.println(eccAesData.toJson());
//        br.readLine();
//    }
//
//    private static void encryptWithPassword(BufferedReader br) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException {
//        // TODO Auto-generated method stub
//
//        System.out.println("Input the password no longer than 64:");
//        char[] passwordBuffer = new char[64];
//        int num = br.read(passwordBuffer);
//
//        if(num==0){
//            System.out.println("Get your password wrong.");
//            return;
//        }
//
//        char[] password = new char[num];
//        System.arraycopy(passwordBuffer, 0, password, 0, num);
//
//        System.out.println("Input the plaintext:");
//        String msg = br.readLine();
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesData eccAesData = new EccAesData(EccAesType.Password,msg,password);
//        ecc.encrypt(eccAesData);
//        System.out.println(eccAesData.toJson());
//
//        br.readLine();
//
//    }
//
//    private static void decryptWithPassword(BufferedReader br) throws Exception {
//        // TODO Auto-generated method stub
//        System.out.println("Input the password no longer than 64:");
//        char[] passwordBuffer = new char[64];
//        int num = br.read(passwordBuffer);
//
//        if(num==0){
//            System.out.println("Get your password wrong.");
//            return;
//        }
//
//        char[] password = new char[num];
//        System.arraycopy(passwordBuffer, 0, password, 0, num);
//        System.out.println("Input the json string of EccAesData:");
//        String eccAesDataJson = br.readLine();
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesData eccAesData = new EccAesData();
//        if(eccAesData.getType()!=EccAesType.Password){
//            System.out.println("The type should be 'Password' instead of "+eccAesData.getType());
//        }
//        ecc.decryptWithPassword(eccAesDataJson,password);
//        if(eccAesData.getError()!=null){
//            System.out.println(eccAesData.getError());
//        }else System.out.println(eccAesData.toJson());
//
//        br.readLine();
//    }
//
//
//    private static void decryptAsy(BufferedReader br) throws Exception {
//        // TODO Auto-generated method stub
//
//        System.out.println("Input the json string of EccAesData:");
//        String eccAesDataJson = br.readLine();
//
//        System.out.println("Input the recipient private key in hex:");
//        char[] priKey = new char[64];
//        int num = br.read(priKey);
//        if(num!=64 || !javaTools.BytesTools.isHexCharArray(priKey)){
//            System.out.println("The symKey should be 64 characters in hex.");
//        }
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//
//        String resultJson = ecc.decryptAsy(eccAesDataJson, priKey);
//
//        System.out.println("\n# decrypted: "+ resultJson);
//
//        br.readLine();
//
//    }
//
//    private static void decryptWithBtcEcc() {
//        // TODO Auto-generated method stub
//
//        System.out.println("Input the ciphertext encrypted with BtcAlgorithm:");
//    }
//
//    private static void encryptAsy(BufferedReader br) throws Exception {
//
//        System.out.println("Input the recipient public key in hex:");
//        String pubKeyB = br.readLine();
//        if(pubKeyB.length()!=66){
//            System.out.println("The public key should be 66 characters of hex.");
//        }
//        System.out.println("Input the msg:");
//        String msg = br.readLine();
//
//        EccAes256K1P7 ecc = new EccAes256K1P7();
//        EccAesData eccAesData = new EccAesData(EccAesType.AsyOneWay,msg,pubKeyB);
//
//        ecc.encrypt(eccAesData);
//        if(eccAesData.getError()!=null){
//            System.out.println(eccAesData.getError());
//        }else System.out.println("Msg: "+eccAesData.getMsg());
//
//    }
//
//    private static void encryptWithBtcEcc() {
//        // TODO Auto-generated method stub
//        System.out.println("encryptWithBtcAlgo is under developing:");
//    }
//
//    private static void verifySchnorr() {
//        // TODO Auto-generated method stub
//        System.out.println("verifySchnorr is under developing:");
//    }
//
//    private static void verifyEcdsa() {
//        // TODO Auto-generated method stub
//        System.out.println("verifyEcdsa is under developing:");
//    }
//
//    private static void signFchTx() {
//        // TODO Auto-generated method stub
//        System.out.println("signFchTx is under developing:");
//    }
//
//    private static void signWithSchnorr() {
//        // TODO Auto-generated method stub
//        System.out.println("signWithSchnorr is under developing:");
//    }
//
//    private static void signWithEcdsa() {
//        // TODO Auto-generated method stub
//        System.out.println("signWithEcdsa is under developing:");
//    }
//
//    public static void getRandom(BufferedReader br) throws IOException {
//        // TODO Auto-generated method stub
//        SecureRandom secureRandom = new SecureRandom();
//        int len =0;
//        while(true) {
//            System.out.println("Input the bytes length of the random you want. Enter to exit:");
//            String input = br.readLine();
//            if ("".equals(input)) {
//                return;
//            }
//
//            try {
//                len = Integer.parseInt(input);
//                break;
//            }catch(Exception e) {
//                continue;
//            }
//        }
//        byte[] bytes = new byte[len];
//        secureRandom.nextBytes(bytes);
//
//
//        if (bytes.length <= 8) {
//            // Create a ByteBuffer with the byte array
//            ByteBuffer buffer = ByteBuffer.allocate(8);
//            buffer.put(bytes);
//            buffer.flip();
//
//            // Convert the byte array to a long
//            long value = 0;
//
//            // Read the bytes from the ByteBuffer
//            for (int i = 0; i < bytes.length; i++) {
//                value = (value << 8) | (buffer.get() & 0xFF);
//            }
//
//            System.out.println("No longer than 8 bytes, in number:\n----\n"+Math.abs(value)+"\n----");
//        }else {
//            System.out.println("Longer than 8 bytes, in hex:\n----\n"+BytesTools.bytesToHexStringBE(bytes)+"\n----");
//        }
//        br.readLine();
//    }
//
//    private static void hex32Base58(BufferedReader br) throws IOException {
//        String input=null;
//        while (true) {
//            System.out.println("Input 32 bytes hex or base58 string, enter to exit:");
//            input = br.readLine();
//            if ("".equals(input)) {
//                return;
//            }
//            if(input.length()==64){
//                System.out.println("Hex to Base58:"+"\n----");
//                System.out.println("New: "+KeyTools.priKey32To38(input));
//                System.out.println("Old: "+KeyTools.priKey32To37(input)+"\n----");
//
//            }else if(input.length()==52){
//                System.out.println("Base58 WIF compressed to Hex:"+"\n----");
//                System.out.println(KeyTools.getPriKey32(input)+"\n----");
//            }else if(input.length()==51){
//                System.out.println("Base58 WIF to Hex:"+"\n----");
//                System.out.println(KeyTools.getPriKey32(input)+"\n----");
//            }else{
//                System.out.println("Only 64 chars hex or 52 chars base58 string can be accepted.");
//            };
//        }
//    }
//
//    private static void findNiceFid(BufferedReader br) throws IOException {
//        String input = null;
//        SimpleDateFormat sdf = new SimpleDateFormat();
//        Date begin = new Date();
//        System.out.println(sdf.format(begin));
//        while (true) {
//            System.out.println("Input 4 characters you want them be in the end of your fid, enter to exit:");
//            input = br.readLine();
//            if ("".equals(input)) {
//                return;
//            }
//            if(input.length()!=4){
//                System.out.println("Input 4 characters you want them be in the end of your fid:");
//            }else break;
//        }
//        long i =0;
//        long j = 0;
//        while (true) {
//            ECKey ecKey = new ECKey();
//            String fid = KeyTools.pubKeyToFchAddr(ecKey.getPubKey());
//            if(fid.substring(30).equals(input)){
//                System.out.println("----");
//                System.out.println("FID:"+fid);
//                System.out.println("PubKey: "+ecKey.getPublicKeyAsHex());
//                System.out.println("PriKey: "+ecKey.getPrivateKeyAsHex());
//                System.out.println("----");
//                System.out.println("Begin at: "+sdf.format(begin));
//                Date end = new Date();
//                System.out.println("End at: "+sdf.format(end));
//                System.out.println("----");
//                break;
//            }
//            i++;
//            if(i%1000000==0) {
//                j++;
//                System.out.println(sdf.format(new Date())+": "+j+" million tryings.");
//            }
//        }
//    }
//
//    private static void pubKeyToAddrs(BufferedReader br) throws IOException {
//        System.out.println("Input the public key, enter to exit:");
//        String pubKey = br.readLine();
//        if ("".equals(pubKey)) {
//            return;
//        }
//        Map<String, String> addrMap = KeyTools.pubKeyToAddresses(pubKey);
//
//
//        System.out.println("----");
//
//        System.out.println("FCH"+": "+ addrMap.get("fchAddr"));
//        System.out.println("BTC"+": "+ addrMap.get("btcAddr"));
//        System.out.println("ETH"+": "+ addrMap.get("ethAddr"));
//
//        System.out.println("----");
//        br.readLine();
//    }
//
//    private static void sha256File(BufferedReader br) throws IOException {
//        while(true) {
//            System.out.println("Input the full path of the file to be hashed, enter to exit:");
//            String filePath = br.readLine();
//            if ("".equals(filePath)) {
//                return;
//            }
//            // Create a File object with the specified path
//            File file = new File(filePath);
//            // Check if the file exists
//            if(file.isDirectory()){
//                System.out.println("It's a directory.");
//                break;
//            }
//            if (file.exists()) {
//                System.out.println("File name: " + file.getName());
//            } else {
//                System.out.println("File does not exist.");
//                break;
//            }
//            String hash = Hash.Sha256(file);
//            System.out.println("----");
//            System.out.println("file:" + filePath);
//            System.out.println("sha256:" + hash );
//            System.out.println("----");
//            br.readLine();
//        }
//    }
//
//    private static void sha256x2File(BufferedReader br) throws IOException {
//        while(true) {
//            System.out.println("Input the full path of the file to be hashed, enter to exit:");
//            String filePath = br.readLine();
//            if ("".equals(filePath)) {
//                return;
//            }
//            // Create a File object with the specified path
//            File file = new File(filePath);
//            // Check if the file exists
//            if(file.isDirectory()){
//                System.out.println("It's a directory.");
//                break;
//            }
//            if (file.exists()) {
//                System.out.println("File name: " + file.getName());
//            } else {
//                System.out.println("File does not exist.");
//                break;
//            }
//            String hash = Hash.Sha256x2(file);
//            System.out.println("----");
//            System.out.println("file:" + filePath );
//            System.out.println("sha256:" + hash);
//            System.out.println("----");
//            br.readLine();
//        }
//    }
//
//    private static void sha256String(BufferedReader br) throws IOException {
//        System.out.println("Input the string to be hashed:");
//        String text = inputString(br);
//        String hash = Hash.Sha256(text);
//        System.out.println("----");
//        System.out.println("raw string:");
//        System.out.println("----");
//        System.out.println(text);
//        System.out.println("----");
//        System.out.println("sha256:" +hash);
//        System.out.println("----");
//        br.readLine();
//    }
//
//    private static void sha256x2String(BufferedReader br) throws IOException {
//        System.out.println("Input the string to be hashed:");
//        String text = inputString(br);
//        String hash = Hash.Sha256x2(text);
//        System.out.println("----");
//        System.out.println("raw string:");
//        System.out.println("----");
//        System.out.println(text);
//        System.out.println("----");
//        System.out.println("sha256:" +hash);
//        System.out.println("----");
//        br.readLine();
//    }
//
//    private static void symKeySign(BufferedReader br) throws IOException {
//        System.out.println("Input the symKey in hex, enter to exit:");
//        String symKey = br.readLine();
//        if("".equals(symKey)) {
//            return;
//        }
//
//        while(true) {
//            System.out.println("Input text to be signed, enter to input, 'q' to exit:");
//            String text = inputString(br);
//            if("q".equals(text))return;
//            String sign = getSign(symKey,text);
//            System.out.println("----");
//            System.out.println("Signature:");
//            System.out.println("----");
//            System.out.println(sign);
//            System.out.println("----");
//        }
//    }
//
//    private static String inputString(BufferedReader br) throws IOException {
//        StringBuilder input = new StringBuilder();
//
//        String line;
//
//        while (true) {
//            line = br.readLine();
//            if("".equals(line)||"\n".equals(line))break;
//            input.append(line).append("\n");
//        }
//
//        // Access the complete input as a string
//        String text = input.toString();
//
//        if(text.endsWith("\n")) {
//            text = text.substring(0, input.length()-1);
//        }
//        return text;
//    }
//
//
//    private static String getSign(String symKey,String text) {
//        byte[] textBytes = text.getBytes();
//        byte[] keyBytes = BytesTools.hexToByteArray(symKey);
//        byte[] bytes = BytesTools.bytesMerger(textBytes,keyBytes);
//        System.out.println("----");
////        System.out.println("SymKey: ");
////        System.out.println("----");
////        System.out.println(symKey);
////        System.out.println("------");
//        System.out.println("Content in hex to be signed: ");
//        System.out.println("----");
//        System.out.println(HexFormat.of().formatHex(bytes));
////        System.out.println("------");
//        byte[] signBytes = Hash.Sha256x2(bytes);
//        return BytesTools.bytesToHexStringBE(signBytes);
//    }
//}
