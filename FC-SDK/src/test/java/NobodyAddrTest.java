import cryptoTools.SHA;
import io.github.novacrypto.base58.Base58;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class NobodyAddrTest {


    @Test
    public void test(){
        getNobodyAddr();
    }
    public static String getNobodyAddr(){
        byte[] addrBytes = new byte[20];
        for (int i = 0; i < 20; i++) {
            addrBytes[i] = (byte) 0xFF;
        }
        System.out.println("Hex:"+ Hex.toHexString(addrBytes));
        System.out.println("Base58:"+Base58.base58Encode(addrBytes));
        String addr = makeAddr(addrBytes);
        System.out.println("Fch address: "+addr);
        return addr;
    }

    public  static String makeAddr(byte[] addrBytes){
        byte[] addrBytesWithHead = new byte[21];
        addrBytesWithHead[0]=(byte)0x23;
        System.arraycopy(addrBytes, 0, addrBytesWithHead, 1, 20);

        byte[] hash = SHA.Sha256x2(addrBytesWithHead);
        byte[] addrEntire = new byte[25];
        System.arraycopy(addrBytesWithHead, 0, addrEntire, 0, 21);
        System.arraycopy(hash, 0, addrEntire, 21, 4);
        return Base58.base58Encode(addrEntire);
    }
}
