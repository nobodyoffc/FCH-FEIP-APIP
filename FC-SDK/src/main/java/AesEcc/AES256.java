package AesEcc;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public class AES256 {

    private static String iv = "0123456789ABCDEF";
    private static String Algorithm = "AES";
    private static String AlgorithmProvider = "AES/CBC/PKCS5Padding"; //算法/模式/补码方式

    public static String generatorKey() {
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return byteToHexString(key);
    }

    public static IvParameterSpec getIv() throws UnsupportedEncodingException {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("utf-8"));
        return ivParameterSpec;
    }
    public static byte[] encrypt(String src, String key16BytesUtf8) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        byte[] kee = key16BytesUtf8.getBytes("utf-8");
        SecretKey secretKey = new SecretKeySpec(kee, Algorithm);
        IvParameterSpec ivParameterSpec = getIv();
        Cipher cipher = Cipher.getInstance(AlgorithmProvider);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] cipherBytes = cipher.doFinal(src.getBytes(Charset.forName("utf-8")));
        return cipherBytes;
    }

    public static byte[] encrypt(byte[] srcBytes, byte[]key16Bytes) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        SecretKey secretKey = new SecretKeySpec(key16Bytes, Algorithm);
        IvParameterSpec ivParameterSpec = getIv();
        Cipher cipher = Cipher.getInstance(AlgorithmProvider);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] cipherBytes = cipher.doFinal(srcBytes);
        return cipherBytes;
    }

    public static String encryptFc(String srcUTF8, String key16BytesHex) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        byte[] srcBytes = srcUTF8.getBytes(StandardCharsets.UTF_8);
        byte[] kee = HexFormat.of().parseHex(key16BytesHex);

        SecretKey secretKey = new SecretKeySpec(kee, Algorithm);
        IvParameterSpec ivParameterSpec = getIv();
        Cipher cipher = Cipher.getInstance(AlgorithmProvider);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] cipherBytes = cipher.doFinal(srcBytes);
        String cipherBase64 = Base64.getEncoder().encodeToString(cipherBytes);
        return cipherBase64;
    }

public static byte[] decrypt(String src, String key16BytesUtf8) throws Exception {
    byte[] kee = key16BytesUtf8.getBytes("utf-8");
    SecretKey secretKey = new SecretKeySpec(kee, Algorithm);

    IvParameterSpec ivParameterSpec = getIv();
    Cipher cipher = Cipher.getInstance(AlgorithmProvider);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
    byte[] hexBytes = hexStringToBytes(src);
    byte[] plainBytes = cipher.doFinal(hexBytes);
    return plainBytes;
}
    public static byte[] decrypt(byte[] srcBytes, byte[] key16Bytes) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key16Bytes, Algorithm);

        IvParameterSpec ivParameterSpec = getIv();
        Cipher cipher = Cipher.getInstance(AlgorithmProvider);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] plainBytes = cipher.doFinal(srcBytes);
        return plainBytes;
    }

    public static String decryptFc(String cipherBase64, String key16BytesHex) throws Exception {
        byte[] srcBytes = Base64.getDecoder().decode(cipherBase64);
        byte[] kee = HexFormat.of().parseHex(key16BytesHex);

        SecretKey secretKey = new SecretKeySpec(kee, Algorithm);

        IvParameterSpec ivParameterSpec = getIv();
        Cipher cipher = Cipher.getInstance(AlgorithmProvider);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] plainBytes = cipher.doFinal(srcBytes);

        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    public static String byteToHexString(byte[] src) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xff;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                sb.append("0");
            }
            sb.append(hv);
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toUpperCase();
        int mix = hexString.length() % 2;
        int length = hexString.length() / 2 + mix;
        char[] hexChars = hexString.toCharArray();
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2 - mix;
            if (pos < 0) {
                b[i] = charToByte(hexChars[pos + 1]);
            } else {
                b[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
        }
        return b;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}

