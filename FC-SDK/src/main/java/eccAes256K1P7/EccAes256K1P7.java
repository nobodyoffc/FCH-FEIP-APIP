package eccAes256K1P7;

import fipaClass.Affair;
import fipaClass.Op;
import com.google.gson.Gson;
import constants.Constants;
import cryptoTools.Hash;
import fileTools.FileTools;
import fileTools.JsonFileTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

/**
 * * ECDH<p>
 * * secp256k1<p>
 * * AES-256-CBC-PKCS7Padding<p>
 * * By No1_NrC7 with the help of chatGPT
 */

public class EccAes256K1P7 {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public void encrypt(EccAesDataByte eccAesDataByte){
        if (isBadErrorAlgAndType(eccAesDataByte)) return;
        switch (eccAesDataByte.getType()){
            case AsyOneWay,AsyTwoWay -> encryptAsy(eccAesDataByte);
            case SymKey -> encryptWithSymKey(eccAesDataByte);
            case Password -> encryptWithPassword(eccAesDataByte);
            default -> eccAesDataByte.setError("Wrong type: "+ eccAesDataByte.getType());
        }
    }
    public void encrypt(EccAesData eccAesData) {
        if(eccAesData==null)return;
        eccAesData.setError(null);

        if(eccAesData.getType()==null){
            eccAesData.setError("EccAesType is required.");
            eccAesData.clearAllSensitiveData();
            return;
        }
        if(!isGoodEncryptParams(eccAesData)){
            eccAesData.clearAllSensitiveData();
            return;
        }
        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        encrypt(eccAesDataByte);
        EccAesData eccAesData1 = EccAesData.fromEccAesDataByte(eccAesDataByte);
        copyEccAesData(eccAesData1,eccAesData);
    }
    public String encrypt(String msg, String pubKeyB){
        EccAesData eccAesData = new EccAesData(EccAesType.AsyOneWay,msg,pubKeyB);
        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }
    public String encrypt(String msg, String pubKeyB, char[] priKeyA){
        EccAesData eccAesData = new EccAesData(EccAesType.AsyTwoWay,msg,pubKeyB,priKeyA);

        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }
    public String encrypt(String msg, char[] symKey32OrPassword){
        EccAesData eccAesData;
        if(symKey32OrPassword.length==64){
            boolean isHex=isCharArrayHex(symKey32OrPassword);
            if(isHex) eccAesData = new EccAesData(EccAesType.SymKey,msg,symKey32OrPassword);
            else eccAesData = new EccAesData(EccAesType.Password,msg,symKey32OrPassword);
        }else eccAesData = new EccAesData(EccAesType.Password,msg,symKey32OrPassword);

        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }

    public String encrypt(char[] msg,String pubKeyB){
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyOneWay);
        eccAesDataByte.setMsg(BytesTools.charArrayToByteArray(msg,StandardCharsets.UTF_8));
        eccAesDataByte.setPubKeyB(BytesTools.hexToByteArray(pubKeyB));
        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }
    public String encrypt(char[] msg,String pubKeyB,char[] priKeyA ){
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyTwoWay);
        eccAesDataByte.setMsg(BytesTools.charArrayToByteArray(msg,StandardCharsets.UTF_8));
        eccAesDataByte.setPubKeyB(BytesTools.hexToByteArray(pubKeyB));
        eccAesDataByte.setPriKeyA(BytesTools.hexCharArrayToByteArray(priKeyA));
        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }
    public String encrypt(char[] msg,char[] symKey32OrPassword ){
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        if(symKey32OrPassword.length==64){
            boolean isHex =isCharArrayHex(symKey32OrPassword);
            if(isHex) {
                eccAesDataByte.setType(EccAesType.SymKey);
                eccAesDataByte.setSymKey(BytesTools.hexCharArrayToByteArray(symKey32OrPassword));
            }
            else {
                eccAesDataByte.setType(EccAesType.Password);
                eccAesDataByte.setPassword(BytesTools.charArrayToByteArray(symKey32OrPassword,StandardCharsets.UTF_8));
            }
        }else {
            eccAesDataByte.setType(EccAesType.Password);
            eccAesDataByte.setPassword(BytesTools.charArrayToByteArray(symKey32OrPassword,StandardCharsets.UTF_8));
        }

        eccAesDataByte.setMsg(BytesTools.charArrayToByteArray(msg,StandardCharsets.UTF_8));
        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
    }

    private boolean isCharArrayHex(char[] symKey32OrPassword) {
        boolean isHex=false;
        for(char c: symKey32OrPassword){
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                isHex=true;
            }else {
                return false;
            }
        }
        return isHex;
    }

    public String encrypt(File originalFile, char[] symKey) {
        EccAesData eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.SymKey);
        eccAesData.setAlg(Constants.ECC_AES_256_K1_P7);
        eccAesData.setSymKey(symKey);

        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        byte[] symKeyBytes = BytesTools.hexCharArrayToByteArray(symKey);
        return encrypt(originalFile,eccAesDataByte);
    }
    public String encrypt(File originalFile, String pubKeyB) {
        EccAesData eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.AsyOneWay);
        eccAesData.setAlg(Constants.ECC_AES_256_K1_P7);
        eccAesData.setPubKeyB(pubKeyB);

        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        return encrypt(originalFile,eccAesDataByte);
    }

    public String encrypt(File originalFile, String pubKeyB,char[]priKeyA) {
        EccAesData eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.AsyTwoWay);
        eccAesData.setAlg(Constants.ECC_AES_256_K1_P7);
        eccAesData.setPubKeyB(pubKeyB);
        eccAesData.setPriKeyA(priKeyA);

        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        return encrypt(originalFile,eccAesDataByte);
    }

    private String encrypt(File originalFile, EccAesDataByte eccAesDataByte) {
        byte[] msgBytes;
        try (FileInputStream fis = new FileInputStream(originalFile)) {
            msgBytes = fis.readAllBytes();
        }catch (IOException e) {
            return "FileInputStream wrong.";
        }

        if(msgBytes!=null&& msgBytes.length!=0)
            eccAesDataByte.setMsg(msgBytes);

        EccAes256K1P7 ecc = new EccAes256K1P7();
        ecc.encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();

        if(eccAesDataByte.getError()!=null){
            return eccAesDataByte.getError();
        }else {
            String parentPath = originalFile.getParent();
            String originalFileName = originalFile.getName();
            int endIndex = originalFileName.lastIndexOf('.');
            String suffix = "_"+originalFileName.substring(endIndex+1);
            String encryptedFileName = originalFileName.substring(0,endIndex)+suffix+Constants.DOT_FV;
            File encryptedFile = FileTools.getNewFile(parentPath, encryptedFileName);
            if(encryptedFile==null) return "Create encrypted file wrong.";

            try(FileOutputStream fos = new FileOutputStream(encryptedFile)){
                byte[] cipherBytes;
                cipherBytes = eccAesDataByte.getCipher();
                eccAesDataByte.setCipher(null);

                Affair affair = new Affair();
                affair.setOp(Op.encrypt);

                if(eccAesDataByte.getType()==EccAesType.AsyOneWay||eccAesDataByte.getType()==EccAesType.AsyTwoWay)
                    affair.setFid(KeyTools.pubKeyToFchAddr(eccAesDataByte.getPubKeyB()));
                affair.setOid(Hash.Sha256x2(originalFile));
                EccAesData eccAesData = EccAesData.fromEccAesDataByte(eccAesDataByte);
                affair.setData(eccAesData);
                fos.write(new Gson().toJson(affair).getBytes());
                fos.write(cipherBytes);
            } catch (IOException e) {
                return "Write encrypted file wrong.";
            }
        }
        return "Done.";
    }

    public void decrypt(EccAesDataByte eccAesDataByte){
        if (isBadErrorAlgAndType(eccAesDataByte)) return;

        switch (eccAesDataByte.getType()){
            case AsyOneWay,AsyTwoWay -> decryptAsy(eccAesDataByte);
            case SymKey -> decryptWithSymKey(eccAesDataByte);
            case Password -> decryptWithPassword(eccAesDataByte);
        }
    }
    public void decrypt(EccAesData eccAesData){
        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        decrypt(eccAesDataByte);
        EccAesData eccAesData1 = EccAesData.fromEccAesDataByte(eccAesDataByte);
        copyEccAesData(eccAesData1,eccAesData);
    }

    public String decrypt(String eccAesDataJson,char[] key){
        Gson gson = new Gson();
        EccAesData eccAesData = gson.fromJson(eccAesDataJson,EccAesData.class);
        switch (eccAesData.getType()){
            case AsyOneWay -> eccAesData.setPriKeyB(key);
            case AsyTwoWay -> eccAesData.setPriKeyB(key);
            case SymKey -> eccAesData.setSymKey(key);
            case Password -> eccAesData.setPassword(key);
            default -> eccAesData.setError("Wrong EccAesType type"+eccAesData.getType());
        }
        if(eccAesData.getError() !=null){
            return "Error:"+eccAesData.getError();
        }
        decrypt(eccAesData);
        return eccAesData.getMsg();
    }

    public EccAesDataByte decrypt(String eccAesDataJson,byte[] key){
        Gson gson = new Gson();
        EccAesData eccAesData = gson.fromJson(eccAesDataJson,EccAesData.class);
        EccAesDataByte eccAesDataBytes = EccAesDataByte.fromEccAesData(eccAesData);
        switch (eccAesDataBytes.getType()){
            case AsyOneWay, AsyTwoWay -> eccAesDataBytes.setPriKeyB(key);
            case SymKey -> eccAesDataBytes.setSymKey(key);
            case Password -> eccAesDataBytes.setPassword(key);
            default -> eccAesDataBytes.setError("Wrong EccAesType type"+eccAesDataBytes.getType());
        }
        if(eccAesDataBytes.getError() !=null){
            return eccAesDataBytes;
        }
        decrypt(eccAesDataBytes);
        return eccAesDataBytes;
    }

    private String decrypt(File encryptedFile,  EccAesDataByte eccAesDataByte) {

    EccAes256K1P7 ecc = new EccAes256K1P7();
    ecc.decrypt(eccAesDataByte);
    eccAesDataByte.clearAllSensitiveData();

    if(eccAesDataByte.getError()!=null){
        return eccAesDataByte.getError();
    }else {
        String parentPath = encryptedFile.getParent();
        String encryptedFileName = encryptedFile.getName();
        int endIndex1 = encryptedFileName.lastIndexOf('_');
        int endIndex2 = encryptedFileName.lastIndexOf('.');
        String oldSuffix = encryptedFileName.substring(endIndex1+1,endIndex2);
        String originalFileName = encryptedFileName.substring(0,endIndex1)+"."+oldSuffix;

        File originalFile = FileTools.getNewFile(parentPath, originalFileName);
        if(originalFile==null) return "Create recovered file failed.";
        try(FileOutputStream fos = new FileOutputStream(originalFile)){
            fos.write(eccAesDataByte.getMsg());
            return "Done";
        } catch (IOException e) {
            return "Write file wrong";
        }
    }
}
    public String decrypt(File encryptedFile, byte[] priKeyBBytes) {
        byte[] cipherBytes;
        Affair affair;
        Gson gson = new Gson();
        EccAesData eccAesData;
        EccAesDataByte eccAesDataByte;
        try (FileInputStream fis = new FileInputStream(encryptedFile)) {
            affair = JsonFileTools.readObjectFromJsonFile(fis,Affair.class);
            cipherBytes = fis.readAllBytes();
            if(affair==null)return "Error:affair is null.";
            if(affair.getData()==null)return "Error:affair.data is null.";
            eccAesData = gson.fromJson(gson.toJson(affair.getData()),EccAesData.class);
            if(eccAesData==null)return "Got eccAesData null.";
        }catch (IOException e) {
            return "Read file wrong.";
        }
        eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        eccAesDataByte.setPriKeyB(priKeyBBytes);
        eccAesDataByte.setCipher(cipherBytes);
        return decrypt(encryptedFile, eccAesDataByte);
    }
    public String decrypt(File encryptedFile, char[] symKey) {
        byte[] cipherBytes;
        Affair affair;
        Gson gson = new Gson();
        EccAesData eccAesData;
        EccAesDataByte eccAesDataByte;
        try (FileInputStream fis = new FileInputStream(encryptedFile)) {
            affair = JsonFileTools.readObjectFromJsonFile(fis,Affair.class);
            cipherBytes = fis.readAllBytes();
            if(affair==null)return "Error:affair is null.";
            if(affair.getData()==null)return "Error:affair.data is null.";
            eccAesData = gson.fromJson(gson.toJson(affair.getData()),EccAesData.class);
            if(eccAesData==null)return "Got eccAesData null.";
        }catch (IOException e) {
            return "Read file wrong.";
        }
        eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);
        eccAesDataByte.setSymKey(BytesTools.hexCharArrayToByteArray(symKey));
        if(eccAesDataByte.getError()!=null)return "Error:"+eccAesDataByte.getError();
        eccAesDataByte.setCipher(cipherBytes);
        return decrypt(encryptedFile, eccAesDataByte);
    }

    private void decryptAsy(EccAesDataByte eccAesDataByte) {
        if(!isGoodDecryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }

        if(eccAesDataByte.getPubKeyB() == null && eccAesDataByte.getPubKeyA() == null){
            eccAesDataByte.setError("No any public key found.");
            return;
        }

        byte[] priKeyBytes = new byte[0];
        byte[] pubKeyBytes = new byte[0];

        if(eccAesDataByte.getType()==EccAesType.AsyOneWay){
            priKeyBytes=eccAesDataByte.getPriKeyB();
            if(priKeyBytes==null || BytesTools.isFilledKey(priKeyBytes)){
                eccAesDataByte.setError("The private key is null or filled with 0.");
                return;
            }
            pubKeyBytes=eccAesDataByte.getPubKeyA();
        }else if(eccAesDataByte.getType()==EccAesType.AsyTwoWay) {
            boolean found = false;
            if(eccAesDataByte.getPriKeyB() != null && !BytesTools.isFilledKey(eccAesDataByte.getPriKeyB())){
                if(eccAesDataByte.getPubKeyA()!=null){
                    if(isTheKeyPair(eccAesDataByte.getPubKeyA(),eccAesDataByte.getPriKeyB())){
                        if(isTheKeyPair(eccAesDataByte.getPubKeyB(),eccAesDataByte.getPriKeyB())){
                            found = false;
                        }else {
                            found = true;
                            priKeyBytes = eccAesDataByte.getPriKeyB();
                            pubKeyBytes = eccAesDataByte.getPubKeyB();
                        }
                    }else {
                        found = true;
                        priKeyBytes = eccAesDataByte.getPriKeyB();
                        pubKeyBytes = eccAesDataByte.getPubKeyA();
                    }
                }
            }else if(eccAesDataByte.getPubKeyA()!=null && !BytesTools.isFilledKey(eccAesDataByte.getPriKeyA())){
                if(isTheKeyPair(eccAesDataByte.getPubKeyA(),eccAesDataByte.getPriKeyA())){
                    if(isTheKeyPair(eccAesDataByte.getPubKeyB(),eccAesDataByte.getPriKeyA())){
                        found = false;
                    }else {
                        found = true;
                        priKeyBytes = eccAesDataByte.getPriKeyA();
                        pubKeyBytes = eccAesDataByte.getPubKeyB();
                    }
                }else {
                    found = true;
                    priKeyBytes = eccAesDataByte.getPriKeyA();
                    pubKeyBytes = eccAesDataByte.getPubKeyA();
                }

            }

            if(!found){
                eccAesDataByte.setError("Private key or public key absent, or the private key and the public key is a pair.");
                return;
            }
        }else {
            eccAesDataByte.setError("Wrong type:"+eccAesDataByte.getType());
            return;
        }
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            eccAesDataByte.setError("Get sha256 digester failed.");
            return;
        }

        byte[] sharedSecret = getSharedSecret(priKeyBytes, pubKeyBytes);

        byte[] sharedSecretHash = sha256.digest(sharedSecret);
        byte[] secretHashWithIv = addArray(sharedSecretHash,eccAesDataByte.getIv());
        byte[] symKey = sha256.digest(sha256.digest(secretHashWithIv));

        if(eccAesDataByte.getSum()!=null) {
            if (!isGoodAesSum(eccAesDataByte, sha256, symKey)) return;
        }
        byte[] msgBytes = new byte[0];
        try {
            msgBytes = Aes256CbcP7.decrypt(eccAesDataByte.getCipher(),symKey,eccAesDataByte.getIv());
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchAlgorithmException | NoSuchProviderException | IllegalBlockSizeException | BadPaddingException e) {
            eccAesDataByte.setError("Decrypt message wrong: "+e.getMessage());
        }
        eccAesDataByte.setMsg(msgBytes);
        eccAesDataByte.setSymKey(symKey);
        clearByteArray(sharedSecret);
        eccAesDataByte.clearAllSensitiveDataButSymKey();
    }

    private void decryptWithPassword(EccAesDataByte eccAesDataByte) {
        if(!isGoodPasswordDecryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] symKeyBytes = makeSymKeyFromPassword(eccAesDataByte, sha256, eccAesDataByte.getIv());
            eccAesDataByte.setSymKey(symKeyBytes);
            byte[] msg = Aes256CbcP7.decrypt(eccAesDataByte.getCipher(), eccAesDataByte.getSymKey(), eccAesDataByte.getIv());
            eccAesDataByte.setMsg(msg);
            eccAesDataByte.clearAllSensitiveData();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            eccAesDataByte.setError("Decrypt with password wrong: "+ e.getMessage());
        }
    }


    public String encryptAsyOneWayBundle(String msg,String pubKeyB){
        EccAesData eccAesData = new EccAesData(EccAesType.AsyOneWay,msg,pubKeyB);
        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);

        encrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        byte[] bundleBytes = addArray(addArray(eccAesDataByte.getPubKeyA(),eccAesDataByte.getIv()),eccAesDataByte.getCipher());
        if(eccAesDataByte.getError()!=null)return "Error:"+eccAesDataByte.getError();
        return Base64.getEncoder().encodeToString(bundleBytes);
    }

    public String decryptAsyOneWayBundle(String bundle,char[] priKeyB){
        byte[] bundleBytes = Base64.getDecoder().decode(bundle);
        byte[] pubKeyABytes = new byte[33];
        byte[] ivBytes = new byte[16];
        byte[] cipherBytes = new byte[bundleBytes.length-pubKeyABytes.length-ivBytes.length];
        byte[] priKeyBBytes = BytesTools.hexCharArrayToByteArray(priKeyB);

        pubKeyABytes = Arrays.copyOfRange(bundleBytes,0,33);
        ivBytes = Arrays.copyOfRange(bundleBytes,33,49);
        cipherBytes = Arrays.copyOfRange(bundleBytes,33+16,bundleBytes.length);

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyOneWay);
        eccAesDataByte.setIv(ivBytes);
        eccAesDataByte.setPubKeyA(pubKeyABytes);
        eccAesDataByte.setPriKeyB(priKeyBBytes);
        eccAesDataByte.setCipher(cipherBytes);

        decrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        if(eccAesDataByte.getError()!=null){
            return "Error:"+eccAesDataByte.getError();
        }
        return new String(eccAesDataByte.getMsg(), StandardCharsets.UTF_8);
    }

    public String encryptAsyTwoWayBundle(String msg,String pubKeyB,char[] priKeyA){
        EccAesData eccAesData = new EccAesData(EccAesType.AsyTwoWay,msg,pubKeyB,priKeyA);
        EccAesDataByte eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);

        encrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        byte[] bundleBytes = addArray(eccAesDataByte.getIv(),eccAesDataByte.getCipher());
        if(eccAesDataByte.getError()!=null)return "Error:"+eccAesDataByte.getError();
        return Base64.getEncoder().encodeToString(bundleBytes);
    }

    public String decryptAsyTwoWayBundle(String bundle,String pubKeyA,char[] priKeyB){
        byte[] priKeyBBytes = BytesTools.hexCharArrayToByteArray(priKeyB);
        byte[] pubKeyABytes = HexFormat.of().parseHex(pubKeyA);

        byte[] bundleBytes = Base64.getDecoder().decode(bundle);
        byte[] ivBytes = Arrays.copyOfRange(bundleBytes,0,16);
        byte[] cipherBytes = Arrays.copyOfRange(bundleBytes,16,bundleBytes.length);

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyTwoWay);
        eccAesDataByte.setIv(ivBytes);
        eccAesDataByte.setPubKeyA(pubKeyABytes);
        eccAesDataByte.setPriKeyB(priKeyBBytes);
        eccAesDataByte.setCipher(cipherBytes);

        decrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        if(eccAesDataByte.getError()!=null){
            return "Error:"+eccAesDataByte.getError();
        }
        return new String(eccAesDataByte.getMsg(), StandardCharsets.UTF_8);
    }

    public String encryptSymKeyBundle(String msg, char[] symKey) {
        EccAesData eccAesData = new EccAesData(EccAesType.SymKey, msg, symKey);

        encrypt(eccAesData);
        String iv = eccAesData.getIv();
        String cipher = eccAesData.getCipher();
        byte[] ivBytes = HexFormat.of().parseHex(iv);
        byte[] cipherBytes = Base64.getDecoder().decode(cipher);

        byte[]ivCipherBytes = EccAes256K1P7.addArray(ivBytes,cipherBytes);
        String bundle = Base64.getEncoder().encodeToString(ivCipherBytes);
        eccAesData.clearAllSensitiveData();
        if(eccAesData.getError()!=null)return "Error:"+eccAesData.getError();
        return bundle;
    }

    public String encryptPasswordBundle(String msg, char[] password) {
        EccAesData eccAesData = new EccAesData(EccAesType.Password, msg, password);

        encrypt(eccAesData);
        String iv = eccAesData.getIv();
        String cipher = eccAesData.getCipher();
        byte[] ivBytes = HexFormat.of().parseHex(iv);
        byte[] cipherBytes = Base64.getDecoder().decode(cipher);

        byte[]ivCipherBytes = EccAes256K1P7.addArray(ivBytes,cipherBytes);
        String bundle = Base64.getEncoder().encodeToString(ivCipherBytes);
        eccAesData.clearAllSensitiveData();
        if(eccAesData.getError()!=null)return "Error:"+eccAesData.getError();
        return bundle;
    }
    public String decryptSymKeyBundle(String bundle, char[] symKey) {
        EccAesDataByte eccAesDataByte = makeIvCipherToEccAesDataByte(Base64.getDecoder().decode(bundle));
        eccAesDataByte.setSymKey(BytesTools.hexCharArrayToByteArray(symKey));
        decrypt(eccAesDataByte);

        if(eccAesDataByte.getError()!=null)return "Error:"+eccAesDataByte.getError();

        return new String(eccAesDataByte.getMsg(),StandardCharsets.UTF_8);
    }

    public byte[] encryptAsyOneWayBundle(byte[] msg,byte[] pubKeyB){

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyOneWay);
        eccAesDataByte.setMsg(msg);
        eccAesDataByte.setPubKeyB(pubKeyB);

        encrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        if(eccAesDataByte.getError()!=null)return null;
        return addArray(addArray(eccAesDataByte.getPubKeyA(),eccAesDataByte.getIv()),eccAesDataByte.getCipher());
    }

    public byte[] decryptAsyOneWayBundle(byte[] bundle,byte[] priKeyB){

        byte[] pubKeyA = Arrays.copyOfRange(bundle,0,33);
        byte[] ivBytes = Arrays.copyOfRange(bundle,33,49);
        byte[] cipherBytes = Arrays.copyOfRange(bundle,33+16,bundle.length);

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyOneWay);
        eccAesDataByte.setIv(ivBytes);
        eccAesDataByte.setPubKeyA(pubKeyA);
        eccAesDataByte.setPriKeyB(priKeyB);
        eccAesDataByte.setCipher(cipherBytes);

        decrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        if(eccAesDataByte.getError()!=null)return ("Error:"+eccAesDataByte.getError()).getBytes();
        return eccAesDataByte.getMsg();
    }

    public byte[] encryptAsyTwoWayBundle(byte[] msg,byte[] pubKeyB,byte[] priKeyA){
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyTwoWay);
        eccAesDataByte.setMsg(msg);
        eccAesDataByte.setPubKeyB(pubKeyB);
        eccAesDataByte.setPriKeyA(priKeyA);

        encrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        if(eccAesDataByte.getError()!=null)return null;
        return addArray(eccAesDataByte.getIv(),eccAesDataByte.getCipher());
    }

    public byte[] decryptAsyTwoWayBundle(byte[] bundle,byte[] pubKeyA,byte[] priKeyB){

        byte[] iv = Arrays.copyOfRange(bundle,0,16);
        byte[] cipher = Arrays.copyOfRange(bundle,16,bundle.length);

        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.AsyTwoWay);
        eccAesDataByte.setIv(iv);
        eccAesDataByte.setCipher(cipher);
        eccAesDataByte.setPubKeyA(pubKeyA);
        eccAesDataByte.setPriKeyB(priKeyB);

        decrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();

        if(eccAesDataByte.getError()!=null)return ("Error:"+eccAesDataByte.getError()).getBytes();
        return eccAesDataByte.getMsg();
    }

    public byte[] encryptSymKeyBundle(byte[] msg, byte[] symKey) {
        EccAesDataByte eccAesDataByte = new EccAesDataByte();

        eccAesDataByte.setType(EccAesType.SymKey);
        eccAesDataByte.setSymKey(symKey);
        eccAesDataByte.setMsg(msg);

        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return addArray(eccAesDataByte.getIv(),eccAesDataByte.getCipher());
    }
    public byte[] decryptSymKeyBundle(byte[] bundle, byte[] symKey) {
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        byte[] iv = Arrays.copyOfRange(bundle,0,16);
        byte[] cipher = Arrays.copyOfRange(bundle,16,bundle.length);
        eccAesDataByte.setType(EccAesType.SymKey);
        eccAesDataByte.setSymKey(symKey);
        eccAesDataByte.setIv(iv);
        eccAesDataByte.setCipher(cipher);

        decrypt(eccAesDataByte);
        if(eccAesDataByte.getError()!=null)return ("Error:"+eccAesDataByte.getError()).getBytes();
        return eccAesDataByte.getMsg();
    }
    public byte[] encryptPasswordBundle(byte[] msg, byte[] password) {
        EccAesDataByte eccAesDataByte = new EccAesDataByte();

        eccAesDataByte.setType(EccAesType.Password);
        eccAesDataByte.setPassword(password);
        eccAesDataByte.setMsg(msg);

        encrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveData();
        return addArray(eccAesDataByte.getIv(),eccAesDataByte.getCipher());
    }
    public byte[] decryptPasswordBundle(byte[] bundle, byte[] password) {
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        byte[] iv = Arrays.copyOfRange(bundle,0,16);
        byte[] cipher = Arrays.copyOfRange(bundle,16,bundle.length);
        eccAesDataByte.setType(EccAesType.Password);
        eccAesDataByte.setPassword(password);
        eccAesDataByte.setIv(iv);
        eccAesDataByte.setCipher(cipher);

        decrypt(eccAesDataByte);
        if(eccAesDataByte.getError()!=null)return ("Error:"+eccAesDataByte.getError()).getBytes();
        return eccAesDataByte.getMsg();
    }

    private void encryptAsy(EccAesDataByte eccAesDataByte){
        if(!isGoodEncryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            eccAesDataByte.setError("Create sha256 digester wrong: "+e.getMessage());
            return;
        }

        // Generate IV
        byte[] iv = getRandomIv();
        eccAesDataByte.setIv(iv);

        //Make sharedSecret
        byte[] sharedSecret;
        ECPrivateKeyParameters ecPriKeyAParams;
        byte[] sharedPubKeyBytes;

        byte[] sharedSecretHash;
        byte[] priKeyABytes = eccAesDataByte.getPriKeyA();
        if(priKeyABytes!=null){
            ecPriKeyAParams =priKeyFromBytes(priKeyABytes);
            sharedSecret = getSharedSecret(priKeyABytes, eccAesDataByte.getPubKeyB());
            sharedSecretHash = sha256.digest(sharedSecret);
            sharedPubKeyBytes = pubKeyToBytes(pubKeyFromPriKey(ecPriKeyAParams));
        }else{
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
            ECDomainParameters domainParameters = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());

            // Generate EC key pair for sender
            ECKeyPairGenerator generator = new ECKeyPairGenerator();
            generator.init(new ECKeyGenerationParameters(domainParameters, new SecureRandom()));

            AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

            ECPrivateKeyParameters newPriKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            byte[] newPriKeyBytes = priKeyToBytes(newPriKey);
            sharedSecret = getSharedSecret(newPriKeyBytes, eccAesDataByte.getPubKeyB());

            sharedSecretHash = sha256.digest(sharedSecret);
            sharedPubKeyBytes = pubKeyToBytes(pubKeyFromPriKey(newPriKey));
        }
        eccAesDataByte.setPubKeyA(sharedPubKeyBytes);

        byte []secretWithIv = addArray(sharedSecretHash,iv);

        byte[] aesKey = sha256.digest(sha256.digest(secretWithIv));
        eccAesDataByte.setSymKey(aesKey);

        // Encrypt the original AES key with the shared secret key
        aesEncrypt(eccAesDataByte);
    }

    private void encryptWithPassword(EccAesDataByte eccAesDataByte) {

        eccAesDataByte.setType(EccAesType.Password);
        if(!isGoodEncryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }

        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            eccAesDataByte.setError("Create sha256 digester wrong:"+e.getMessage());
            return;
        }
        byte[] iv = getRandomIv();
        eccAesDataByte.setIv(iv);
        byte[] symKeyBytes = makeSymKeyFromPassword(eccAesDataByte, sha256, iv);
        eccAesDataByte.setSymKey(symKeyBytes);
        aesEncrypt(eccAesDataByte);
    }

    private void encryptWithSymKey(EccAesDataByte eccAesDataByte) {

        if(!isGoodEncryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }
        eccAesDataByte.setType(EccAesType.SymKey);
        isGoodEncryptParams(eccAesDataByte);
        eccAesDataByte.setIv(getRandomIv());
        aesEncrypt(eccAesDataByte);
        eccAesDataByte.clearAllSensitiveDataButSymKey();
    }

    private void decryptWithSymKey(EccAesDataByte eccAesDataByte) {
        if(!isGoodSymKeyDecryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }
        try {
            byte[] msg = Aes256CbcP7.decrypt(eccAesDataByte.getCipher(), eccAesDataByte.getSymKey(), eccAesDataByte.getIv());
            eccAesDataByte.setMsg(msg);
            eccAesDataByte.clearAllSensitiveDataButSymKey();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            eccAesDataByte.setError("Decrypt with symKey wrong: "+ e.getMessage());
        }
    }

    public static EccAesDataByte makeIvCipherToEccAesDataByte(byte[]ivCipherBytes){
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setAlg(Constants.ECC_AES_256_K1_P7);
        eccAesDataByte.setType(EccAesType.SymKey);
        byte[] iv = Arrays.copyOfRange(ivCipherBytes, 0, 16);
        byte[] cipher = Arrays.copyOfRange(ivCipherBytes, 16, ivCipherBytes.length);
        eccAesDataByte.setIv(iv);
        eccAesDataByte.setCipher(cipher);
        return eccAesDataByte;
    }

    private void aesEncrypt(EccAesDataByte eccAesDataByte)  {

        if(!isGoodEncryptParams(eccAesDataByte)){
            eccAesDataByte.clearAllSensitiveData();
            return;
        }

        byte[] iv = eccAesDataByte.getIv();
        byte[] msgBytes = eccAesDataByte.getMsg();
        byte[] symKeyBytes = eccAesDataByte.getSymKey();
        MessageDigest sha256;
        byte[] cipher;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
            cipher = Aes256CbcP7.encrypt(msgBytes, symKeyBytes, iv);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            eccAesDataByte.setError("Aes encrypting wrong: "+e.getMessage());
            return;
        }
        byte[] sum4 = getSum4(sha256, symKeyBytes, iv, cipher);
        eccAesDataByte.setCipher(cipher);
        eccAesDataByte.setSum(sum4);
        eccAesDataByte.setMsg(null);
        eccAesDataByte.clearAllSensitiveDataButSymKey();
    }
    private byte[] makeSymKeyFromPassword(EccAesDataByte eccAesDataByte, MessageDigest sha256, byte[] iv) {
        byte[] symKeyBytes = sha256.digest(addArray(sha256.digest(eccAesDataByte.getPassword()), iv));
        return symKeyBytes;
    }

    private byte[] getSharedSecret(byte[] priKeyBytes, byte[] pubKeyBytes) {

        ECPrivateKeyParameters priKey = priKeyFromBytes(priKeyBytes);
        ECPublicKeyParameters pubKey = pubKeyFromBytes(pubKeyBytes);
        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(priKey);
        return agreement.calculateAgreement(pubKey).toByteArray();
    }

    public static ECPrivateKeyParameters priKeyFromHex(String privateKeyHex) {
        BigInteger privateKeyValue = new BigInteger(privateKeyHex, 16); // Convert hex to BigInteger
        X9ECParameters ecParameters = org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1"); // Use the same curve name as in key pair generation
        ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN(), ecParameters.getH());
        return new ECPrivateKeyParameters(privateKeyValue, domainParameters);
    }

    public static ECPrivateKeyParameters priKeyFromBytes(byte[] privateKey) {
        return priKeyFromHex(HexFormat.of().formatHex(privateKey));
    }
    public static ECPublicKeyParameters pubKeyFromPriKey(ECPrivateKeyParameters privateKey) {
        X9ECParameters ecParameters = org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1");
        ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN(), ecParameters.getH());

        ECPoint Q = domainParameters.getG().multiply(privateKey.getD()); // Scalar multiplication of base point (G) and private key

        return new ECPublicKeyParameters(Q, domainParameters);
    }

    public ECPublicKeyParameters pubKeyFromBytes(byte[] publicKeyBytes) {

        X9ECParameters ecParameters = org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1");
        ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN(), ecParameters.getH());

        ECCurve curve = domainParameters.getCurve();

        ECPoint point = curve.decodePoint(publicKeyBytes);

        return new ECPublicKeyParameters(point, domainParameters);
    }

    public ECPublicKeyParameters pubKeyFromHex(String publicKeyHex) {
        return pubKeyFromBytes(HexFormat.of().parseHex(publicKeyHex));
    }
    public String pubKeyToHex(ECPublicKeyParameters publicKey) {
        return Hex.toHexString(pubKeyToBytes(publicKey));
    }

    public static byte[] pubKeyToBytes(ECPublicKeyParameters publicKey) {
        return publicKey.getQ().getEncoded(true);
    }

    public String priKeyToHex(ECPrivateKeyParameters privateKey) {
        BigInteger privateKeyValue = privateKey.getD();
        String hex = privateKeyValue.toString(16);
        while (hex.length() < 64) {  // 64 is for 256-bit key
            hex = "0" + hex;
        }
        return hex;
    }


    public byte[] priKeyToBytes(ECPrivateKeyParameters privateKey) {
        return HexFormat.of().parseHex(priKeyToHex(privateKey));//Hex.decode(priKeyToHex(privateKey));
    }

    public static byte[] addArray(byte[] original, byte[] add) {
        byte[] total = new byte[original.length+add.length];  // For AES-256
        System.arraycopy(original, 0, total, 0, original.length);
        System.arraycopy(add, 0, total, original.length, add.length);
        return total;
    }

    public byte[] getPartOfBytes(byte[] original, int offset, int length) {
        byte[] part = new byte[length];
        System.arraycopy(original, offset, part, 0, part.length);
        return part;
    }
    private static byte[] getRandomIv() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        return iv;
    }
    private boolean isBadErrorAlgAndType(EccAesDataByte eccAesDataByte) {
        if(eccAesDataByte.getError()!=null){
            eccAesDataByte.setError("There was an error. Check it at first:"+eccAesDataByte.getError()+" .");
            eccAesDataByte.clearAllSensitiveData();
            return true;
        }

        if(eccAesDataByte.getAlg()==null){
            eccAesDataByte.setAlg(Constants.ECC_AES_256_K1_P7);
        }else if(!eccAesDataByte.getAlg().equals(Constants.ECC_AES_256_K1_P7)){
            eccAesDataByte.setError("This method only used by the algorithm "+Constants.ECC_AES_256_K1_P7+" .");
            eccAesDataByte.clearAllSensitiveData();
            return true;
        }

        if(eccAesDataByte.getType()==null){
            eccAesDataByte.setError("EccAesType is required.");
            eccAesDataByte.clearAllSensitiveData();
            return true;
        }
        return false;
    }

    private boolean isGoodEncryptParams(EccAesDataByte eccAesDataByte) {
        EccAesType type = eccAesDataByte.getType();
        switch (type){
            case AsyOneWay -> {
                return isGoodAsyOneWayEncryptParams(eccAesDataByte);
            }
            case AsyTwoWay -> {
                return isGoodAsyTwoWayEncryptParams(eccAesDataByte);
            }
            case SymKey -> {
                return isGoodSymKeyEncryptParams(eccAesDataByte);
            }
            case Password -> {
                return isGoodPasswordEncryptParams(eccAesDataByte);
            }
            default -> eccAesDataByte.setError("Wrong type: "+eccAesDataByte.getType());
        }
        return true;
    }

    private boolean isGoodEncryptParams(EccAesData eccAesData) {
        EccAesType type = eccAesData.getType();
        switch (type){
            case AsyOneWay -> {
                return isGoodAsyOneWayEncryptParams(eccAesData);
            }
            case AsyTwoWay -> {
                return isGoodAsyTwoWayEncryptParams(eccAesData);
            }
            case SymKey -> {
                return isGoodSymKeyEncryptParams(eccAesData);
            }
            case Password -> {
                return isGoodPasswordEncryptParams(eccAesData);
            }
            default -> eccAesData.setError("Wrong type: "+eccAesData.getType());
        }
        return true;
    }

    private boolean isGoodPasswordEncryptParams(EccAesDataByte eccAesDataByte) {

        if(eccAesDataByte.getMsg()==null){
            eccAesDataByte.setError(EccAesType.Password.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesDataByte.getPassword()==null){
            eccAesDataByte.setError(EccAesType.Password.name()+" parameters lack password.");
            return false;
        }

        return true;
    }

    private boolean isGoodSymKeyEncryptParams(EccAesDataByte eccAesDataByte) {

        if(eccAesDataByte.getMsg()==null){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesDataByte.getSymKey()==null){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack symKey.");
            return false;
        }

        if(eccAesDataByte.getSymKey().length!=Constants.SYM_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameter symKey should be "+Constants.SYM_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getSymKey().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAsyTwoWayEncryptParams(EccAesDataByte eccAesDataByte) {

        if(eccAesDataByte.getMsg()==null){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesDataByte.getPubKeyB()==null){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack pubKeyB.");
            return false;
        }

        if(eccAesDataByte.getPriKeyA()==null){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack priKeyA.");
            return false;
        }

        if(eccAesDataByte.getPubKeyB().length!= Constants.PUBLIC_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameter pubKeyB should be "+Constants.PUBLIC_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPubKeyB().length +" now.");
            return false;
        }

        if(eccAesDataByte.getPriKeyA().length!= Constants.PRIVATE_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameter priKeyA should be "+Constants.PRIVATE_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPriKeyA().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAsyOneWayEncryptParams(EccAesDataByte eccAesDataByte) {
        if(eccAesDataByte.getMsg()==null){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesDataByte.getPubKeyB()==null){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameters lack pubKeyB.");
            return false;
        }

        if(eccAesDataByte.getPubKeyB().length!= Constants.PUBLIC_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameter symKey should be "+Constants.PUBLIC_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getSymKey().length +" now.");
            return false;
        }

        return true;
    }


    private boolean isGoodPasswordEncryptParams(EccAesData eccAesData) {

        if(eccAesData.getMsg()==null){
            eccAesData.setError(EccAesType.Password.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesData.getPassword()==null){
            eccAesData.setError(EccAesType.Password.name()+" parameters lack password.");
            return false;
        }

        return true;
    }

    private boolean isGoodSymKeyEncryptParams(EccAesData eccAesData) {

        if(eccAesData.getMsg()==null){
            eccAesData.setError(EccAesType.SymKey.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesData.getSymKey()==null){
            eccAesData.setError(EccAesType.SymKey.name()+" parameters lack symKey.");
            return false;
        }

        if(eccAesData.getSymKey().length!=Constants.SYM_KEY_BYTES_LENGTH*2){
            eccAesData.setError(EccAesType.SymKey.name()+" parameter symKey should be "+Constants.SYM_KEY_BYTES_LENGTH*2+" characters. It is "+ eccAesData.getSymKey().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAsyTwoWayEncryptParams(EccAesData eccAesData) {

        if(eccAesData.getMsg()==null){
            eccAesData.setError(EccAesType.AsyTwoWay.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesData.getPubKeyB()==null){
            eccAesData.setError(EccAesType.AsyTwoWay.name()+" parameters lack pubKeyB.");
            return false;
        }

        if(eccAesData.getPriKeyA()==null){
            eccAesData.setError(EccAesType.AsyTwoWay.name()+" parameters lack priKeyA.");
            return false;
        }

        if(eccAesData.getPubKeyB().length()!= Constants.PUBLIC_KEY_BYTES_LENGTH*2){
            eccAesData.setError(EccAesType.AsyTwoWay.name()+" parameter pubKeyB should be "+Constants.PUBLIC_KEY_BYTES_LENGTH*2+" characters. It is "+ eccAesData.getPubKeyB().length() +" now.");
            return false;
        }

        if(eccAesData.getPriKeyA().length!= Constants.PRIVATE_KEY_BYTES_LENGTH*2){
            eccAesData.setError(EccAesType.AsyTwoWay.name()+" parameter priKeyA should be "+Constants.PRIVATE_KEY_BYTES_LENGTH+" characters. It is "+ eccAesData.getPriKeyA().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAsyOneWayEncryptParams(EccAesData eccAesData) {
        if(eccAesData.getMsg()==null){
            eccAesData.setError(EccAesType.AsyOneWay.name()+" parameters lack msg.");
            return false;
        }

        if(eccAesData.getPubKeyB()==null){
            eccAesData.setError(EccAesType.AsyOneWay.name()+" parameters lack pubKeyB.");
            return false;
        }

        if(eccAesData.getPubKeyB().length()!= Constants.PUBLIC_KEY_BYTES_LENGTH*2){
            eccAesData.setError(EccAesType.AsyOneWay.name()+" parameter symKey should be "+Constants.PUBLIC_KEY_BYTES_LENGTH+" characters. It is "+ eccAesData.getSymKey().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAesSum(EccAesDataByte eccAesDataByte, MessageDigest sha256, byte[] symKey) {
        byte[] sum4 = getSum4(sha256, symKey, eccAesDataByte.getIv(), eccAesDataByte.getCipher());
        if (!Arrays.equals(sum4, eccAesDataByte.getSum())) {
            eccAesDataByte.setError("The sum  is not equal to the value of sha256(symKey+iv+cipher).");
            return false;
        }
        return true;
    }

    private boolean isGoodDecryptParams(EccAesDataByte eccAesDataByte) {
        EccAesType type = eccAesDataByte.getType();
        switch (type){
            case AsyOneWay -> {
                return isGoodAsyOneWayDecryptParams(eccAesDataByte);
            }
            case AsyTwoWay -> {
                return isGoodAsyTwoWayDecryptParams(eccAesDataByte);
            }
            case SymKey -> {
                return isGoodSymKeyDecryptParams(eccAesDataByte);
            }
            case Password -> {
                return isGoodPasswordDecryptParams(eccAesDataByte);
            }
            default -> eccAesDataByte.setError("Wrong type: "+eccAesDataByte.getType());
        }
        return true;
    }


    private boolean isGoodPasswordDecryptParams(EccAesDataByte eccAesDataByte) {

        if(eccAesDataByte.getCipher()==null){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack cipher.");
            return false;
        }

        if(eccAesDataByte.getIv()==null){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack iv.");
            return false;
        }

        if(eccAesDataByte.getPassword()==null||isZero(eccAesDataByte.getPassword())){
            eccAesDataByte.setError(EccAesType.Password.name()+" parameters lack password.");
            return false;
        }

        if(eccAesDataByte.getIv().length!= Constants.IV_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameter iv should be "+Constants.IV_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getIv().length +" now.");
            return false;
        }
        return true;
    }

    private boolean isGoodSymKeyDecryptParams(EccAesDataByte eccAesDataByte) {
        if(eccAesDataByte.getCipher()==null){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack cipher.");
            return false;
        }

        if(eccAesDataByte.getIv()==null){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack iv.");
            return false;
        }

        if(eccAesDataByte.getSymKey()==null||isZero(eccAesDataByte.getSymKey())){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameters lack symKey.");
            return false;
        }

        if(eccAesDataByte.getIv().length!= Constants.IV_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameter iv should be "+Constants.IV_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getIv().length +" now.");
            return false;
        }

        if(eccAesDataByte.getSymKey()!=null && eccAesDataByte.getSymKey().length!= Constants.SYM_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.SymKey.name()+" parameter symKey should be "+Constants.SYM_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getSymKey().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAsyTwoWayDecryptParams(EccAesDataByte eccAesDataByte) {

        if(eccAesDataByte.getCipher()==null){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack cipher.");
            return false;
        }

        if(eccAesDataByte.getIv()==null){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack iv.");
            return false;
        }

        if(eccAesDataByte.getPubKeyA()==null || eccAesDataByte.getPubKeyA()==null){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack pubKeyA and pubKeyB.");
            return false;
        }

        if((eccAesDataByte.getPriKeyB()==null||isZero(eccAesDataByte.getPriKeyB()))
                && (eccAesDataByte.getPriKeyA()==null||isZero(eccAesDataByte.getPriKeyA()))){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameters lack both priKeyA and priKeyB.");
            return false;
        }

        if(eccAesDataByte.getPubKeyA()!=null && eccAesDataByte.getPubKeyA().length!= Constants.PUBLIC_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameter pubKeyA should be "+Constants.PUBLIC_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPubKeyA().length +" now.");
            return false;
        }

        if(eccAesDataByte.getPubKeyB()!=null && eccAesDataByte.getPubKeyB().length!= Constants.PUBLIC_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameter pubKeyB should be "+Constants.PUBLIC_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPubKeyB().length +" now.");
            return false;
        }

        if(eccAesDataByte.getPriKeyA()!=null && eccAesDataByte.getPriKeyA().length!= Constants.PRIVATE_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameter priKeyA should be "+Constants.PRIVATE_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPriKeyA().length +" now.");
            return false;
        }

        if(eccAesDataByte.getPriKeyB()!=null && eccAesDataByte.getPriKeyB().length!= Constants.PRIVATE_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyTwoWay.name()+" parameter priKeyB should be "+Constants.PRIVATE_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPriKeyB().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isGoodAsyOneWayDecryptParams(EccAesDataByte eccAesDataByte) {
        if(eccAesDataByte.getCipher()==null){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameters lack cipher.");
            return false;
        }

        if(eccAesDataByte.getIv()==null){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameters lack iv.");
            return false;
        }

        if(eccAesDataByte.getPubKeyA()==null){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameters lack pubKeyA.");
            return false;
        }

        if(eccAesDataByte.getPriKeyB()==null||isZero(eccAesDataByte.getPriKeyB())){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameters lack priKeyB.");
            return false;
        }

        if(eccAesDataByte.getPubKeyA().length!= Constants.PUBLIC_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameter pubKeyA should be "+Constants.PUBLIC_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPubKeyA().length +" now.");
            return false;
        }

        if(eccAesDataByte.getPriKeyB().length!= Constants.PRIVATE_KEY_BYTES_LENGTH){
            eccAesDataByte.setError(EccAesType.AsyOneWay.name()+" parameter priKeyB should be "+Constants.PRIVATE_KEY_BYTES_LENGTH+" bytes. It is "+ eccAesDataByte.getPriKeyB().length +" now.");
            return false;
        }

        return true;
    }

    private boolean isZero(byte[] bytes){
        for(byte b:bytes){
            if(b!=0)return false;
        }
        return true;
    }

    private byte[] getSum4(MessageDigest sha256, byte[] symKey, byte[] iv, byte[] cipher) {
        byte[] sum32 = sha256.digest(addArray(symKey, addArray(iv, cipher)));
        return getPartOfBytes(sum32, 0, 4);
    }

    public static boolean isTheKeyPair(byte[]pubKeyByte, byte[]priKeyByte) {
        ECPrivateKeyParameters priKey = priKeyFromBytes(priKeyByte);
        byte[] pubKeyFromPriKey = pubKeyToBytes(pubKeyFromPriKey(priKey));
        return Arrays.equals(pubKeyByte,pubKeyFromPriKey);
    }

    public void copyEccAesData(EccAesData fromEccAesData,EccAesData ToEccAesData) {
        ToEccAesData.setType(fromEccAesData.getType());
        ToEccAesData.setAlg(fromEccAesData.getAlg());
        ToEccAesData.setMsg(fromEccAesData.getMsg());
        ToEccAesData.setCipher(fromEccAesData.getCipher());
        ToEccAesData.setSymKey(fromEccAesData.getSymKey());
        ToEccAesData.setPassword(fromEccAesData.getPassword());
        ToEccAesData.setPubKeyA(fromEccAesData.getPubKeyA());
        ToEccAesData.setPubKeyB(fromEccAesData.getPubKeyB());
        ToEccAesData.setPriKeyA(fromEccAesData.getPriKeyA());
        ToEccAesData.setPriKeyB(fromEccAesData.getPriKeyB());
        ToEccAesData.setIv(fromEccAesData.getIv());
        ToEccAesData.setSum(fromEccAesData.getSum());
        ToEccAesData.setError(fromEccAesData.getError());
    }
    public void clearByteArray(byte[] array) {
        Arrays.fill(array, (byte) 0);
    }

}
