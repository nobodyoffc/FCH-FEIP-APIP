package fc;

import fcTools.Base58;
import fcTools.FchMainNetwork;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;
import org.junit.Test;

import java.security.SecureRandom;

public class Wallet {
@Test
    public void genPriKey() {
    for (int i = 0; i < 10; i++){
        ECKey ecKey = new ECKey(new SecureRandom());
    System.out.println("PubKey:" + ecKey.getPublicKeyAsHex());
    System.out.println("PriKey:" + ecKey.getPrivateKeyAsWiF(new FchMainNetwork()));
    System.out.println("FID:" + ecKey.toAddress(new FchMainNetwork()));
    }
    }
    @Test
    public void convertPriKey(){
        String priKey = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        ;
        System.out.println(KeyTools.getPriKey32(priKey));
    }
}
