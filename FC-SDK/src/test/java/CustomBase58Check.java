import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;

import java.util.Arrays;

public class CustomBase58Check {

    public static void main(String[] args) {
        String prefix = "F";
        int payloadLength = 20; // Adjust this according to your needs

        // Find the right Base58Check encoded string
        String base58CheckEncodedString = findBase58CheckEncodedString(prefix, payloadLength);

        System.out.println("Base58Check encoded string: " + base58CheckEncodedString);
    }

    private static String findBase58CheckEncodedString(String prefix, int payloadLength) {
        byte[] payload = new byte[payloadLength];
        Arrays.fill(payload, (byte) 0); // Fill with zeroes

        // Iterate through the possible payloads until we find the desired string
        while (true) {
            String base58CheckEncodedString = Base58.encode(addCheckSum(payload));

            if (base58CheckEncodedString.startsWith(prefix) && base58CheckEncodedString.substring(1).matches("^X*$")) {
                return base58CheckEncodedString;
            }

            // Increment the payload
            for (int i = payload.length - 1; i >= 0; i--) {
                if (payload[i] != (byte) 0xFF) {
                    payload[i]++;
                    break;
                } else {
                    payload[i] = 0;
                }
            }
        }
    }

    private static byte[] addCheckSum(byte[] input) {
        byte[] checkSum = Arrays.copyOfRange(Sha256Hash.hashTwice(input), 0, 4);
        byte[] output = new byte[input.length + checkSum.length];
        System.arraycopy(input, 0, output, 0, input.length);
        System.arraycopy(checkSum, 0, output, input.length, checkSum.length);
        return output;
    }
}

