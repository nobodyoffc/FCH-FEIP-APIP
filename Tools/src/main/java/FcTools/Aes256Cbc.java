package FcTools;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Aes256Cbc {

	    public static void main(String[] args) throws Exception {
	        // Read the encrypted data from a file
	        byte[] encryptedData = Files.readAllBytes(Paths.get("encrypted.txt"));

	        // Read the key and IV from files
	        byte[] keyBytes1 = Files.readAllBytes(Paths.get("key.txt"));
	        byte[] ivBytes1 = Files.readAllBytes(Paths.get("iv.txt"));

	        // Convert key and IV hex strings to bytes
	        String keyHex = new String(keyBytes1, StandardCharsets.UTF_8);
	        String ivHex = new String(ivBytes1, StandardCharsets.UTF_8);

	        byte[] keyBytes = hexStringToByteArray(keyHex.substring(0, 64));
	        byte[] ivBytes = hexStringToByteArray(ivHex.substring(0, 32));

	        // Create the SecretKey using the key bytes
	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        PBEKeySpec keySpec = new PBEKeySpec(new String(keyBytes, StandardCharsets.UTF_8).toCharArray(), ivBytes, 65536, 256);
	        SecretKey secretKey = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");

	        // Decrypt the data
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
	        byte[] decryptedData = cipher.doFinal(encryptedData);

	        // Print the decrypted data
	        System.out.println(new String(decryptedData, StandardCharsets.UTF_8));
	    }

	    private static byte[] hexStringToByteArray(String hexString) {
	        int len = hexString.length();
	        byte[] byteArray = new byte[len / 2];
	        for (int i = 0; i < len; i += 2) {
	            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
	                    + Character.digit(hexString.charAt(i + 1), 16));
	        }
	        return byteArray;
	    }
	}



