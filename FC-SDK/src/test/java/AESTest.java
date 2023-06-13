import AesEcc.AES256;
import javaTools.BytesTools;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class AESTest {
    public static void main(String[] args) throws Exception {


        String keyHex="6175d5b19f6deccfe42540bd59e37a2d";



        //String keyHex = BytesTools.bytesToHexStringBE(keyBytes);
        //keyHex = "08afae5cd7c9a9ecb9c4fb2947267747ba12dc778e3780cc0b11ecfc2dbd3c87";

        //System.out.println("keyUtf8: "+keyUtf8);
        System.out.println("keyHex: "+keyHex);


        String srcUtf8 = "BbgGAo5W+BRyTqUVnKcL";

        System.out.println("text:"+srcUtf8);

        //String cipherUtf8 = HexFormat.of().formatHex (AES256.encrypt(src, keyUtf8));
        String cipherBase64= "uOtvwYxAtLOGEgF672tXOF0BCuxI0Z+fOnTxh3OADJY=";//AES256.encryptFc(srcUtf8,keyHex);

        //System.out.println("cipherUtf8: "+ cipherUtf8);
        System.out.println("cipherHBase64: "+ cipherBase64);

        //System.out.println("utf8:"+ new String(AES256.decrypt(cipherUtf8,keyUtf8),StandardCharsets.UTF_8));
        System.out.println("textUtf8: "+ AES256.decryptFc(cipherBase64,keyHex));

    }
}
