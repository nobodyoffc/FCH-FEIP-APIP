package Tools;

import org.bitcoinj.core.Sha256Hash;

import java.nio.charset.StandardCharsets;

public class SymSign {
    public static void main(String[] args) {

        String str = "{\"url\":\"https://cid.cash/APIP/apip3/v1/cidInfoByIds\",\"time\":1677673821267,\"nonce\":1024,\"fcdsl\":{\"ids\":[\"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\"]}}";
        String symKey = "f7d01170888cc4d816a8d6b4f1f9b45455682531ad52fda835e81d69243d8c8f";

        System.out.println(symSign(str,symKey));
    }

    public static String symSign(String text,String symKeyHex){
       byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
       byte[] symKeyBytes = hexToByteArray(symKeyHex);
        byte[] bundle = bytesMerger(textBytes, symKeyBytes);

        byte[] hashBytes = Sha256Hash.hashTwice(bundle);//Sha256Hash.hash(Sha256Hash.hash(bundle));

        return bytesToHex(hashBytes);
    }

    private static byte[] bytesMerger(byte[] a, byte[] b) {
        byte[] ab = new byte[a.length+ b.length];

        System.arraycopy(a,0,ab,0, a.length);
        System.arraycopy(b,0,ab, a.length, b.length);

        return ab;
    }

    public static byte[] hexToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
