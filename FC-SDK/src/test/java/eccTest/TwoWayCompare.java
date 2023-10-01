package eccTest;

import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesDataByte;
import eccAes256K1P7.EccAesType;
import fcTools.Base58;
import keyTools.KeyTools;

import java.util.HexFormat;

public class TwoWayCompare {
    static EccAesData eccAesData;
    static EccAes256K1P7 ecc = new EccAes256K1P7();
    static String cipher;
    public static void main(String[] args) {
        old();
        newOne();
    }
    private static void old(){
        String msg = "hello world";

        String pubKey = "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a";
        eccAesData = new EccAesData(EccAesType.AsyOneWay, msg,pubKey);
        ecc.encrypt(eccAesData);
        cipher=eccAesData.toJson();
        System.out.println(cipher);
    }

    private static void newOne(){
        byte[] priKeyBase58 = Base58.decode("L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8");
        byte[] priKey32 = KeyTools.getPriKey32(priKeyBase58);
        assert priKey32 != null;
        System.out.println("PriKey: "+ HexFormat.of().formatHex(priKey32));
        EccAesDataByte result = ecc.decrypt(cipher, priKey32);
        System.out.println(EccAesData.fromEccAesDataByte(result).toJson());

    }
}
