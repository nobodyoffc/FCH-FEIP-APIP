import java.math.BigInteger;
import java.util.Arrays;

public class SpecialAddress {

    public static void main(String[] args) {
        byte[] input = new byte[21]; // Adjust the length based on the desired number of 'X' characters
        Arrays.fill(input, (byte) 0);

        String encoded = Base58.encode(input);
        encoded = encoded.replace('1', 'X');
        encoded = "F" + encoded.substring(1);

        System.out.println(encoded);
    }

    public static class Base58 {
        private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
        private static final BigInteger BASE = BigInteger.valueOf(58);

        public static String encode(byte[] input) {
            BigInteger value = new BigInteger(1, input);
            StringBuilder encoded = new StringBuilder();

            while (value.compareTo(BASE) >= 0) {
                BigInteger[] quotientAndRemainder = value.divideAndRemainder(BASE);
                value = quotientAndRemainder[0];
                encoded.append(ALPHABET[quotientAndRemainder[1].intValue()]);
            }

            encoded.append(ALPHABET[value.intValue()]);
            for (byte b : input) {
                if (b == 0) {
                    encoded.append(ALPHABET[0]);
                } else {
                    break;
                }
            }

            return encoded.reverse().toString();
        }

        public static byte[] decode(String input) {
            BigInteger value = BigInteger.ZERO;

            for (char c : input.toCharArray()) {
                value = value.multiply(BASE).add(BigInteger.valueOf(Arrays.binarySearch(ALPHABET, c)));
            }

            byte[] decoded = value.toByteArray();
            int leadingZeros = 0;
            for (char c : input.toCharArray()) {
                if (c == ALPHABET[0]) {
                    leadingZeros++;
                } else {
                    break;
                }
            }

            byte[] result = new byte[decoded.length + leadingZeros];
            System.arraycopy(decoded, 0, result, leadingZeros, decoded.length);
            return result;
        }
    }
}
