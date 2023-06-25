package EccAes256K1P7;

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
 * * Cipher structure: [Compressed pubKey 33bytes] + [salt 4bytes] + [cipherOfMsg with variable length]<p>
 * * By No1_NrC7 with the help of chatGPT
 */

public class EccAes256K1P7 {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private String symKey;

    /**
     * @return Base64 = [pubKey 33bytes] + [iv 16bytes] + [cipher variable length]
     */
    public String encrypt(String msgUtf8, String hisPubKeyHex) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        return encrypt(msgUtf8,hisPubKeyHex,null);
    }
    /**
     * @param myPriKeyHex If being null, a random priKey will be used to encrypt.
     * @return Base64 = [pubKey 33bytes] + [iv 16bytes] + [cipher variable length]
     */
    public String encrypt(String msgUtf8, String hisPubKeyHex, String myPriKeyHex) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        byte[] msgBytes = msgUtf8.getBytes(StandardCharsets.UTF_8);
        byte[] hisPubKeyBytes = HexFormat.of().parseHex(hisPubKeyHex);
        byte[] myPriKeyBytes=null;
        if(myPriKeyHex!=null) myPriKeyBytes= HexFormat.of().parseHex(myPriKeyHex);
        byte[] cipherBytes = encrypt(msgBytes, hisPubKeyBytes, myPriKeyBytes);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    /**
     * @param myPriKeyBytes If being null, a random priKey will be used to encrypt.
     * @return bytes = [pubKey 33bytes] + [iv 16bytes] + [cipher variable length]
     */
    public byte[] encrypt(byte[] msgBytes, byte[] hisPubKeyBytes, byte[] myPriKeyBytes) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        // Generate IV
        byte[] iv = getRandomIv();

        //Make sharedSecret
        byte[] sharedSecret;
        ECPrivateKeyParameters myPriKey;
        byte[] sharedPubKeyBytes;

        if(myPriKeyBytes!=null){
            myPriKey =priKeyFromBytes(myPriKeyBytes);
            sharedSecret = getSharedSecret(myPriKeyBytes, hisPubKeyBytes);
            sharedPubKeyBytes = pubKeyToBytes(pubKeyFromPriKey(myPriKey));
        }else{
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
            ECDomainParameters domainParameters = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());

            // Generate EC key pair for sender
            ECKeyPairGenerator generator = new ECKeyPairGenerator();
            generator.init(new ECKeyGenerationParameters(domainParameters, new SecureRandom()));

            AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

            ECPrivateKeyParameters newPriKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            byte[] newPriKeyBytes = priKeyToBytes(newPriKey);
            sharedSecret = getSharedSecret(newPriKeyBytes, hisPubKeyBytes);
            sharedPubKeyBytes = pubKeyToBytes(pubKeyFromPriKey(newPriKey));
        }

        byte []secretWithIv = addArray(sharedSecret,iv);

        byte[] aesKey = sha256.digest(sha256.digest(secretWithIv));

        this.symKey = Hex.toHexString(aesKey);

        // Encrypt the original AES key with the shared secret key
        return getAesCipherBundle(sharedPubKeyBytes, iv,msgBytes, aesKey);
    }

    public String encryptWithSymKey(String msg,String symKey) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        byte[] symKeyBytes = HexFormat.of().parseHex(symKey);
        byte[] cipherBytes = encryptWithSymKey(msgBytes,symKeyBytes);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }
    public byte[] encryptWithSymKey(byte[] msgBytes, byte[] symKeyBytes) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {

        byte[] iv = getRandomIv();
        byte[] preFix = new byte[]{0};

        return getAesCipherBundle(preFix,  iv, msgBytes,symKeyBytes);
    }
    public String encryptWithPassword(String msg,String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = encryptWithPassword(msgBytes,passwordBytes);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }
    public byte[] encryptWithPassword(byte[] msgBytes, byte[] passwordBytes) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        byte[] iv = getRandomIv();
        byte[] preFix = new byte[]{0x01};

        byte[] symKeyBytes = sha256.digest(sha256.digest(addArray(passwordBytes,iv)));

        return getAesCipherBundle(preFix, iv, msgBytes, symKeyBytes);
    }

    private byte[] getAesCipherBundle(byte[] pubKeyOrPreFix, byte[] iv,  byte[] msgBytes, byte[] symKeyBytes) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] cipher = Aes256CbcP7.encrypt(msgBytes, symKeyBytes, iv);
        byte[] sum32 = sha256.digest(addArray(symKeyBytes,addArray(iv, cipher)));
        byte[] sum4 = getPartOfBytes(sum32,0, 4);

        byte[] bundle = new byte[pubKeyOrPreFix.length+iv.length + cipher.length+sum4.length];

        System.arraycopy(pubKeyOrPreFix, 0, bundle, 0, pubKeyOrPreFix.length);
        System.arraycopy(iv, 0, bundle, pubKeyOrPreFix.length, iv.length);
        System.arraycopy(cipher, 0, bundle, pubKeyOrPreFix.length+iv.length, cipher.length);
        System.arraycopy(sum4, 0, bundle, pubKeyOrPreFix.length+iv.length+cipher.length, sum4.length);

        return bundle;
    }

    /**
     * @param cipherBase64 cipher in Base64 = [pubKey 33bytes] + [iv 16bytes] + [cipher variable length]
     * @return plaintext in UTF-8
     */
    public String decrypt(String cipherBase64, String priKeySymKeyHexOrPasswordUtf8) throws Exception {
        return decrypt(cipherBase64,priKeySymKeyHexOrPasswordUtf8,null);
    }

        /**
         * @param cipherBase64 cipher in Base64 = [pubKey 33bytes] + [iv 16bytes] + [cipher variable length]
         * @return plaintext in UTF-8
         */
    public String decrypt(String cipherBase64, String priKeySymKeyHexOrPasswordUtf8,String pubKeyHex) throws Exception {
        byte[] cipherBytes = Base64.getDecoder().decode(cipherBase64);
        byte[] priKeySymKeyOrPassword;
        if(cipherBytes[0]==0x01){
            priKeySymKeyOrPassword= priKeySymKeyHexOrPasswordUtf8.getBytes(StandardCharsets.UTF_8);
        }else {
            priKeySymKeyOrPassword = HexFormat.of().parseHex(priKeySymKeyHexOrPasswordUtf8);
        }
        byte[] pubKeyBytes = null;
        if(pubKeyHex!=null){
            pubKeyBytes = HexFormat.of().parseHex(pubKeyHex);
        }

        return new String(decrypt(cipherBytes,priKeySymKeyOrPassword,pubKeyBytes),StandardCharsets.UTF_8);
    }

    /**
     *
     * @param cipherBytes  = [pubKey 33bytes] + [iv 16bytes] + [cipher variable length]
     * @return plaintext in bytes
     */
    public byte[] decrypt(byte[] cipherBytes, byte[] priKeySymKeyOrPasswordBytes,byte[] pubKeyBytes) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        byte[] iv;
        byte[] sum4;
        byte[] cipher;

        byte[] symKeyBytes;

        switch (cipherBytes[0]) {
            case 0x00 -> {  //symKey
                symKeyBytes = priKeySymKeyOrPasswordBytes;
                pubKeyBytes = new byte[1];
                iv = new byte[16];
                System.arraycopy(cipherBytes, 1, iv, 0, iv.length);
            }
            case 0x01 -> {  //password
                pubKeyBytes = new byte[1];
                iv = new byte[16];
                System.arraycopy(cipherBytes, 1, iv, 0, iv.length);
                symKeyBytes = sha256.digest(sha256.digest(addArray(priKeySymKeyOrPasswordBytes, iv)));
            }
            case 0x02, 0x03 -> {
                if (pubKeyBytes == null) {
                    pubKeyBytes = new byte[33];
                    System.arraycopy(cipherBytes, 0, pubKeyBytes, 0, pubKeyBytes.length);
                }
                iv = new byte[16];
                System.arraycopy(cipherBytes, pubKeyBytes.length, iv, 0, iv.length);
                byte[] sharedSecret = getSharedSecret(priKeySymKeyOrPasswordBytes, pubKeyBytes);
                byte[] secretWithSalt = addArray(sharedSecret, iv);
                symKeyBytes = sha256.digest(sha256.digest(secretWithSalt));
            }
            case 0x04 -> throw new Exception("Compress the public key first.");
            default -> throw new Exception("Wrong ciphertext. The ciphertext should begin with 0x00 - 0x03.");
        }

        sum4 = new byte[4];
        cipher = new byte[cipherBytes.length-pubKeyBytes.length-iv.length-sum4.length];

        System.arraycopy(cipherBytes, pubKeyBytes.length+iv.length, cipher, 0, cipher.length);
        System.arraycopy(cipherBytes, pubKeyBytes.length+iv.length+cipher.length, sum4, 0, sum4.length);

        //Check sum
        byte[] aesBundle = addArray(symKeyBytes, addArray(iv, cipher));

        byte[] sum32 = sha256.digest(aesBundle);
        byte[] newSum4 = getPartOfBytes(sum32,0,4);
        if(!Arrays.equals(sum4,newSum4)){
            throw new Exception ("Sum checking of the cipher failed.");
        }

        this.symKey = Hex.toHexString(symKeyBytes);

        return Aes256CbcP7.decrypt(cipher,symKeyBytes,iv);
    }


    /**
     *
     * @param cipherBytes PubKey will be taken off
     */
    public byte[] decryptWithSymKey(byte[] cipherBytes, byte[] SymKeyBytes) throws Exception {
        byte[] cipherWithoutPubKey;
        switch (cipherBytes[0]) {
            case 0x00, 0x01 -> cipherWithoutPubKey = cipherBytes;
            case 0x02, 0x03 ->
                    cipherWithoutPubKey = addArray(new byte[]{0x00}, getPartOfBytes(cipherBytes, 33, cipherBytes.length - 33));
            default -> {
                return null;
            }
        }
        return  decrypt(cipherWithoutPubKey,SymKeyBytes,null);
    }

        public byte[] decryptWithPassword(byte[] cipherBytes, byte[] passwordBytes) throws Exception {
            byte[] cipherWithoutPubKey;
            switch (cipherBytes[0]) {
            case 0x00, 0x01 -> {
                cipherBytes[0]=0x01;
                cipherWithoutPubKey= cipherBytes;
            }
            case 0x02, 0x03 -> cipherWithoutPubKey = addArray(new byte[]{0x01}, getPartOfBytes(cipherBytes, 33, cipherBytes.length - 33));
            default -> {
                return null;
            }
        }
        return  decrypt(cipherWithoutPubKey,passwordBytes,null);
    }

    /**
     *
     * @param cipherBase64 PubKey will be taken off
     */
    public String decryptWithSymKey(String cipherBase64, String symKeyHex) throws Exception {
        byte[] cipherBytes = Base64.getDecoder().decode(cipherBase64);
        byte[] symKeyBytes = HexFormat.of().parseHex(symKeyHex);
        return new String(decryptWithSymKey(cipherBytes,symKeyBytes),StandardCharsets.UTF_8);
    }

    public String decryptWithPassword(String cipherBase64, String passwordUtf8) throws Exception {
        byte[] cipherBytes = Base64.getDecoder().decode(cipherBase64);
        byte[] passwordBytes = passwordUtf8.getBytes(StandardCharsets.UTF_8);
        return new String(decryptWithPassword(cipherBytes,passwordBytes),StandardCharsets.UTF_8);
    }

    private byte[] getSharedSecret(byte[] priKeyBytes, byte[] pubKeyBytes) {

        ECPrivateKeyParameters priKey = priKeyFromBytes(priKeyBytes);
        ECPublicKeyParameters pubKey = pubKeyFromBytes(pubKeyBytes);
        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(priKey);
        return agreement.calculateAgreement(pubKey).toByteArray();
    }

    public ECPrivateKeyParameters priKeyFromHex(String privateKeyHex) {
        BigInteger privateKeyValue = new BigInteger(privateKeyHex, 16); // Convert hex to BigInteger
        X9ECParameters ecParameters = org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1"); // Use the same curve name as in key pair generation
        ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN(), ecParameters.getH());
        return new ECPrivateKeyParameters(privateKeyValue, domainParameters);
    }

    public ECPrivateKeyParameters priKeyFromBytes(byte[] privateKey) {
        return priKeyFromHex(Hex.toHexString(privateKey));
    }
    public ECPublicKeyParameters pubKeyFromPriKey(ECPrivateKeyParameters privateKey) {
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

    public byte[] pubKeyToBytes(ECPublicKeyParameters publicKey) {
        return publicKey.getQ().getEncoded(true); // Use 'true' for compressed public key
    }
    public String priKeyToHex(ECPrivateKeyParameters privateKey) {
        BigInteger privateKeyValue = privateKey.getD();
        return privateKeyValue.toString(16);
    }

    public byte[] priKeyToBytes(ECPrivateKeyParameters privateKey) {
        return HexFormat.of().parseHex(priKeyToHex(privateKey));//Hex.decode(priKeyToHex(privateKey));
    }

    public void setSymKey(String symKey) {
        this.symKey = symKey;
    }
    public byte[] addArray(byte[] original, byte[] add) {
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

    public String getSymKey() {
        return symKey;
    }
}
