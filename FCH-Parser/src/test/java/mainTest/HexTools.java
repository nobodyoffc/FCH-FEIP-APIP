package mainTest;

import javaTools.BytesTools;

public class HexTools {
    public static void main(String[] args) {
        String str = "a2acdb0e06b7bb0ee d9fcbbb2118c9905 8abc404abcb9a686c00000000000000";
        System.out.println(BytesTools.revertHexBy2(str));
    }
}
