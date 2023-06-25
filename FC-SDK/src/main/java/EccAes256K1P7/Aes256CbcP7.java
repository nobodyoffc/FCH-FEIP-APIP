package EccAes256K1P7;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

    /**
     * * AES-256-CBC PKCS7Padding<p>
     * * Openssl compatible<p>
     * * Random iv is strongly suggested.<p>
     * * To decrypt with openssl:<p>
     *   1) Take the prefix of iv away from ciphertext.<p>
     *   2) In Hex: echo "23ca33817dbf9f5fc3a19975f3c5b6df" | xxd -r -p | openssl enc -d -aes-256-cbc -iv 24678483ef69dbbc91edfde49b4d88cb -K 50b67af1c9840d968d6591abcd400a8287443ab36569585fb14315312946d2c1 -nosalt -nopad<p>
     *   In Base64: echo "I8ozgX2/n1/DoZl188W23w==" | openssl enc -d -aes-256-cbc -a -iv 24678483ef69dbbc91edfde49b4d88cb -K 50b67af1c9840d968d6591abcd400a8287443ab36569585fb14315312946d2c1 -nosalt<p>
     * * To encrypt with openssl:<p>
     *    echo -n 'hello world!' | openssl enc -aes-256-cbc -K 50b67af1c9840d968d6591abcd400a8287443ab36569585fb14315312946d2c1 -iv 24678483ef69dbbc91edfde49b4d88cb -a -out ciphertext.txt<p>
     *    Put iv in byte array as the prefix of the ciphertext before decrypt it with this code.<p>
     * * The difference of the file structure between the file encrypted with password and the file encrypted with key:<p>
     *    The resulting encrypted file begins with the ASCII string "Salted__" (which is 8 bytes), followed by the 8 bytes of salt that were used in the key derivation function. The rest of the file is the actual encrypted data.<p>
     *    This "Salted__" string is a magic string used by OpenSSL to identify that the encrypted data was salted.<p>
     * * By No1_NrC7 with the help of chatGPT
     */
public class Aes256CbcP7 {
    public static void main(String[] args) throws Exception {

        String plaintext = "hello world!";
        String keyHex = "50b67af1c9840d968d6591abcd400a8287443ab36569585fb14315312946d2c1"; // your-256-bit-key-in-hex. Remember to generate and use a secure random key in real applications
        System.out.println("symKey: "+keyHex);
        String ciphertextWithIvBase64 = encrypt(plaintext, keyHex);
        System.out.println("encrypted with Iv: "+ciphertextWithIvBase64);
        String textDecrypted = decrypt(ciphertextWithIvBase64, keyHex);
        System.out.println("decrypted text: "+textDecrypted);
    }

        /**
         *
         * @param plaintextUtf8   plaintext in UTF-8
         * @param keyHex          64 letters
         * @return                ciphertext in Base64 with 16 bytes iv as prefix
         */
        public static String encrypt(String plaintextUtf8, String keyHex) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
            byte[] plaintextBytes = plaintextUtf8.getBytes(StandardCharsets.UTF_8);
            byte[] key = Hex.decode(keyHex);
            byte[] cipherWithIvBytes = encrypt(plaintextBytes, key);

            return Base64.getEncoder().encodeToString(cipherWithIvBytes);
        }

        /**
         * @return cipher with iv as prefix
         */
        public static byte[] encrypt(byte[] plaintextBytes, byte[] key) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);

            byte[] ciphertextBytes = encrypt(plaintextBytes, key, iv);

            byte[] cipherWithIvBytes = addIvToCipher(iv, ciphertextBytes);
            return cipherWithIvBytes;
        }
    public static byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return  cipher.doFinal(ciphertext);
    }

    /**
     *
     * @param cipherWithIvBase64 with 16 bytes iv as prefix
     * @param keyHex             64 letters
     * @return                   plaintext in UTF-8
     */
    public static  String decrypt(String cipherWithIvBase64, String keyHex) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        byte[] cipherBytesWithIv = Base64.getDecoder().decode(cipherWithIvBase64);
        byte[] key = Hex.decode(keyHex);
        byte[] plaintextBytes = decrypt(cipherBytesWithIv,key);
        return new String(plaintextBytes,StandardCharsets.UTF_8);
    }

    public static byte[] decrypt(byte[] cipherBytesWithIv, byte[] key) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, NoSuchProviderException, InvalidKeyException {

        byte[] iv = new byte[16];
        byte[] cipherBytes = new byte[cipherBytesWithIv.length-16];

        System.arraycopy(cipherBytesWithIv,0,iv,0,16);
        System.arraycopy(cipherBytesWithIv,16,cipherBytes,0,cipherBytes.length);

        return decrypt(cipherBytes, key,iv);
    }

        /**
         * @return cipher without iv
         */
    public static byte[] encrypt(byte[]  plaintext, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(plaintext);
    }

    public static byte[] addIvToCipher(byte[] iv, byte[] ciphertextBytes) {
        byte[] cipherWithIvBytes = new byte[iv.length+ ciphertextBytes.length];
        System.arraycopy(iv,0,cipherWithIvBytes,0, iv.length);
        System.arraycopy(ciphertextBytes,0,cipherWithIvBytes, iv.length, ciphertextBytes.length);
        return cipherWithIvBytes;
    }
}
