package fc;

import java.util.HexFormat;

public class Hex {
    public static void main(String[] args) {
        String s = "hello";
        byte[] b = s.getBytes();
        String hex = HexFormat.of().formatHex(b);
        System.out.println("java HexFormat.of().formatHex:"+hex);
        System.out.println("formatHex:"+formatHex(b));
        System.out.println("java "+new String(HexFormat.of().parseHex(hex)));
        System.out.println("parseHex "+new String(parseHex(hex)));

    }
    public static String formatHex1(byte[] b) {
        byte[] vsnFileByte = new byte[b.length];
        System.arraycopy(b, 0, vsnFileByte, 0, b.length);
        return new String(vsnFileByte).toLowerCase();
    }
    public static String formatHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString().toLowerCase();
    }
    public static byte[] parseHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
