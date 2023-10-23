package fc;

import fcTools.Base58;
import javaTools.BytesTools;
import keyTools.KeyTools;

import java.util.Arrays;
import java.util.HexFormat;

public class PriKeyToFid {
    public static void main(String[] args) {
        String priKeyL = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        byte[] priKey52 = priKeyL.getBytes();
        System.out.println("priKey52:\n"+HexFormat.of().formatHex(priKey52));

        char[] priKeyChars = BytesTools.byteArrayToUtf8CharArray(priKey52);
        System.out.println("priKeyChars:");
        System.out.println(priKeyChars);

        byte[] priKey1 = Base58.base58CharArrayToByteArray(priKeyChars);
        System.out.println("priKey1:"+HexFormat.of().formatHex(priKey1));
        System.out.println("priKey32:"+HexFormat.of().formatHex(KeyTools.getPriKey32(priKey1)));

        byte[] priKey32 = KeyTools.getPriKey32(Base58.decode(priKeyL));
        System.out.println(HexFormat.of().formatHex(priKey32));
        String fid = KeyTools.priKeyToFid(priKey32);

        System.out.println(fid);
    }


}
