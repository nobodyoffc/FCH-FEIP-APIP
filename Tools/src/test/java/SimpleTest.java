import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HexFormat;

public class SimpleTest {
    public static void main(String[] args) throws IOException {

        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32];
        byte[] tokey = new byte[32];

        secureRandom.nextBytes(key);
        secureRandom.nextBytes(tokey);

        System.out.println(HexFormat.of().formatHex(tokey));
        BigInteger keyB = new BigInteger(key);
        BigInteger tokeyB = new BigInteger(tokey);

        BigInteger result = keyB.xor(tokeyB);

        BigInteger to2 = result.xor(keyB);

        System.out.println(HexFormat.of().formatHex(to2.toByteArray()));



    }
}
