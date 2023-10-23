package fc;

import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;

public class KeyTest {
    public static void main(String[] args) {
        ECKey ecKey = new ECKey();
        String pubKey = ecKey.getPublicKeyAsHex();
        System.out.println(pubKey);
        String pubKey04 = KeyTools.recoverPK33ToPK65(pubKey);
        System.out.println(pubKey04);
    }
}
