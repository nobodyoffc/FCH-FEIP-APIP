package eccTest;

import eccAes256K1P7.EccAes256K1P7;
import org.bitcoin.NativeSecp256k1;
import org.bitcoin.NativeSecp256k1Util;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;

import java.util.HexFormat;

import static eccAes256K1P7.EccAes256K1P7.priKeyFromBytes;

public class compareECDH {

    public static void main(String[] args) throws NativeSecp256k1Util.AssertFailException {
        String pubKeyA = "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a";
        String priKeyA = "a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575";
        String pubKeyB = "02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67";
        String priKeyB = "ee72e6dd4047ef7f4c9886059cbab42eaab08afe7799cbc0539269ee7e2ec30c";

        byte[] pubKeyABytes = HexFormat.of().parseHex(pubKeyA);
        byte[] priKeyBBytes = HexFormat.of().parseHex(priKeyB);
        NativeSecp256k1 nativeSecp256k1 = new NativeSecp256k1();
        byte[] secretBtcJ = NativeSecp256k1.createECDHSecret(priKeyBBytes,pubKeyABytes);
        System.out.println(HexFormat.of().formatHex(secretBtcJ));
        byte[] secretBC = getSharedSecret(priKeyBBytes,pubKeyABytes);
        System.out.println(HexFormat.of().formatHex(secretBC));
    }

    private static byte[] getSharedSecret(byte[] priKeyBytes, byte[] pubKeyBytes) {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        ECPrivateKeyParameters priKey = priKeyFromBytes(priKeyBytes);
        ECPublicKeyParameters pubKey = ecc.pubKeyFromBytes(pubKeyBytes);
        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(priKey);
        return agreement.calculateAgreement(pubKey).toByteArray();
    }
}
