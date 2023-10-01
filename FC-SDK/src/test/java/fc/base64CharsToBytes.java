package fc;
import javaTools.BytesTools;


public class base64CharsToBytes {

    public static void main(String[] args) {
        char[] chars = "Y2hhckFycmF5TmFtZQ==".toCharArray();
        byte[] decodedBytes = BytesTools.base64CharArrayToByteArray(chars);
        System.out.println(new String(decodedBytes));  // Should print "charArrayName"
    }

}
