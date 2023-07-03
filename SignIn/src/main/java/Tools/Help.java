package Tools;

import org.bouncycastle.crypto.params.ECPublicKeyParameters;

import java.util.Base64;
import java.util.HexFormat;

public class Help {
    public static void main(String[] args) throws Exception {

        System.out.println("----------------------");
        System.out.println("Encode: ");
        System.out.println("    message: UTF-8");
        System.out.println("    key: Hex");
        System.out.println("    ciphertext: Base64");
        System.out.println("----------------------");
        System.out.println("Ciphertext structure:");
        System.out.println("    AES:");
        System.out.println("        iv16bytes + cipher");
        System.out.println();
        System.out.println("    ECC:");
        System.out.println("        pubKey33bytes begin with '02'or '03' + iv16bytes + cipher + checkSum4bytes");
        System.out.println("        or");
        System.out.println("        '00' + iv16bytes + cipher + checkSum4bytes");
        System.out.println("----------------------");

        String msg = "hello world!";
        System.out.println("msg: "+msg);
        System.out.println();
        // ECC Test
        EccAes256K1P7 ecc = new EccAes256K1P7();
        System.out.println("symKey: \n  "+ ecc.getSymKey());
        System.out.println("No symKey by far.");
        System.out.println("----------------------\nECC");
        System.out.println();
        String recipientPubKeyHex = "02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67";

        // Encrypt with new keys
        String cipherBase64 = ecc.encrypt(msg,recipientPubKeyHex);
        System.out.println("Cipher with a new key pair in ECC format:\n  "+cipherBase64);
        System.out.println();

        //Decrypt with new keys
        String recipientPriKeyHex = "ee72e6dd4047ef7f4c9886059cbab42eaab08afe7799cbc0539269ee7e2ec30c";
        String plaintext = ecc.decrypt(cipherBase64,recipientPriKeyHex);
        System.out.println("Plaintext: \n  "+ plaintext);
        System.out.println();
        //Encrypt with given keys

        String senderPriKeyHex = "a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575";
        ECPublicKeyParameters senderPublicKey = ecc.pubKeyFromPriKey(ecc.priKeyFromHex(senderPriKeyHex));
        String senderPubKeyHex = ecc.pubKeyToHex(senderPublicKey);

        cipherBase64 = ecc.encrypt(msg,recipientPubKeyHex,senderPriKeyHex);
        System.out.println("Cipher encrypted with a given priKey in ECC format:\n  "+cipherBase64);
        System.out.println();
        //Decrypt
        plaintext = ecc.decrypt(cipherBase64,recipientPriKeyHex);
        System.out.println("Plaintext by recipientPriKey: \n  "+ plaintext);
        System.out.println();

        plaintext = ecc.decrypt(cipherBase64,senderPriKeyHex,recipientPubKeyHex);
        System.out.println("Plaintext by senderPriKey: \n  "+ plaintext);
        System.out.println();

        //AES test
        System.out.println("----------------------\nAES");
        String symKey = ecc.getSymKey();
        System.out.println("symKey: \n  "+ symKey);
        System.out.println();
        System.out.println("Once you encrypted or decrypted, a new random symKey had generated.");
        System.out.println();
        String cipherByAes = Aes256CbcP7.encrypt(msg, symKey);
        System.out.println("Cipher encrypted with a symKey in AES format: \n  "+cipherByAes);
        System.out.println();
        String msgDecryptedByAes = Aes256CbcP7.decrypt(cipherByAes,symKey);
        System.out.println("Msg decrypted with a symKey from AES format cipher: \n  "+ msgDecryptedByAes);
        System.out.println();

        String onlyAesCipher = ecc.encryptWithSymKey(msg,symKey);
        System.out.println("Cipher encrypted with a symKey, without pubKey, and in ECC format: \n  "+onlyAesCipher);
        System.out.println();
        String msgDecryptFromAesInEcc = ecc.decrypt(onlyAesCipher,symKey);
        System.out.println("Msg decrypted with the symKey, from the cipher without pubKey, and in ECC format: \n  "+ msgDecryptFromAesInEcc);
        System.out.println();

        //Decrypt a cipher begin with a public key with a symKey ignoring the public key:
        String msgByDecryptWithSymKey = ecc.decryptWithSymKey(onlyAesCipher,symKey);
        System.out.println("Msg decrypted with the symKey from the cipher with a pubKey: \n  "+ msgByDecryptWithSymKey);
        System.out.println();

        //Encrypt with password
        String password = "myPassword";
        System.out.println("Password: \n  "+ password);
        System.out.println();
        String passwordAesCipher = ecc.encryptWithPassword(msg,password);
        System.out.println("Cipher encrypted with a UTF-8 password in ECC format: \n  "+passwordAesCipher);
        System.out.println("cipherInHex: "+HexFormat.of().formatHex(Base64.getDecoder().decode(passwordAesCipher)));

        System.out.println();
        String msgDecryptWithPassword = ecc.decrypt(passwordAesCipher, password);
        System.out.println("Msg decrypted with the password in ECC format: \n  "+ msgDecryptWithPassword);

        String msgDecryptWithSpecialPassword = ecc.decryptWithPassword(passwordAesCipher, password);

        System.out.println("Msg decrypted with the password by decryptWithPassword: \n  "+ msgDecryptWithSpecialPassword);
        System.out.println("----------------------");
    }
}
