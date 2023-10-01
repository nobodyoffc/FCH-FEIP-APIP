package fc;

import java.util.Arrays;
import java.util.HexFormat;

import static fcTools.Base58.base58CharArrayToByteArray;

public class Base58CharToBytes {
    public static void main(String[] args) {
        char[] base58Chars = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa".toCharArray();
        byte[] bytes = base58CharArrayToByteArray(base58Chars);
        System.out.println(HexFormat.of().formatHex(bytes));
        System.out.println(Arrays.toString(bytes));
    }
}

