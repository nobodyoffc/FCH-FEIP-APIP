package eccTest;

import eccAes256K1P7.EccAes256K1P7;
import javaTools.BytesTools;
import appUtils.Inputer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class KeyClear {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String ask = "Input password:";
        char[] password = Inputer.inputPassword(br,ask);
        char[] passwordClone = password.clone();
        System.out.println("password:"+String.valueOf(password));
        BytesTools.clearCharArray(password);
        System.out.println("password:"+String.valueOf(password));
        System.out.println("passwordClone:"+String.valueOf(passwordClone));

        EccAes256K1P7 ecc = new EccAes256K1P7();

        extracted(passwordClone.clone(), ecc);
        System.out.println("passwordClone:"+String.valueOf(passwordClone));

        byte[] passwordCloneBytes = BytesTools.utf8CharArrayToByteArray(passwordClone);
        BytesTools.clearCharArray(passwordClone);
        System.out.println(String.valueOf(passwordClone));
        System.out.println(new String(passwordCloneBytes));

    }

    private static void extracted(final char[] passwordClone, EccAes256K1P7 ecc) {
        String msg = "hello";
        System.out.println("Cipher: "+ ecc.encrypt(msg, passwordClone));
    }
}
