package fc;

import keyTools.KeyTools;
import org.junit.Test;

public class Wallet {


    @Test
    public void convertPriKey(){
        String priKey = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        ;
        System.out.println(KeyTools.getPriKey32(priKey));
    }
}
