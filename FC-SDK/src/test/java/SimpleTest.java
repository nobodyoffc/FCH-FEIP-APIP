import constants.ApiNames;
import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesData;
import eccAes256K1P7.EccAesType;
import fipaClass.Algorithm;
import javaTools.BytesTools;
import javaTools.JsonTools;
import appUtils.Shower;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SimpleTest {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String data = """
                    A signature are requested:
                    \tRequest header:
                    \t\tSessionName = <The session name which is the hex of the first 6 bytes of the sessionKey>
                    \t\tSign = <The value of double sha256 of the request body bytes adding the sessionKey bytes.>
                    1. 
                    2. {}
                    3. {}
                    """
                .formatted("urlHead"+ ApiNames.APIP0V1Path + ApiNames.SignInAPI,"nonce","timestamp");
        System.out.println(data);
    }

    private static void enumTest() {
        System.out.println(Algorithm.EcdsaBtcMsg_No1_NrC7.ordinal());
        System.out.println(Algorithm.EcdsaBtcMsg_No1_NrC7.getName());
        System.out.println(Algorithm.ECC256k1_AES256CBC.ordinal());
    }

    private static void eccAesDataTest() {
        String pubKeyAStr = "pubKeyA";
        String ivStr = "iv";
        String cipherStr = "Aj25FHwyKPtHT4XzvYLEBrNHi9Z519Mx33W2SP49DO8Tohkl0q0kI5zdEIAOHh0Qb/+SbJ8Z5PAruXFYCuo0t/2owcxaZ5d6oLG6gZgQtOtUvokff7W5AILR5Mqrw0iIrw==";
        byte[] cipherBytes = Base64.getDecoder().decode(cipherStr);
        String cipherHex = HexFormat.of().formatHex(cipherBytes);
        System.out.println(cipherHex);
        String pubKeyA = cipherHex.substring(0, 66);
        String hmac= cipherHex.substring(cipherHex.length()-64);
        String iv= cipherHex.substring(66,66+32);
        String cipherCipherHex = cipherHex.substring(66+32,cipherHex.length()-64);
        String cipher = Base64.getEncoder().encodeToString(HexFormat.of().parseHex(cipherCipherHex));

        char[] priKeyB = "a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575".toCharArray();
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesData eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.AsyOneWay);
        eccAesData.setIv(iv);
        eccAesData.setCipher(cipher);
        eccAesData.setPriKeyB(priKeyB);
        eccAesData.setPubKeyA(pubKeyA);

        JsonTools.gsonPrint(eccAesData);
    }

    private static void charArrayAndByteArray() {
        String str = "hello!马云兄！";
        char[] chars = str.toCharArray();

        System.out.println(str);
        System.out.println(chars);

        byte[] bytes = BytesTools.utf8CharArrayToByteArray(chars);
        System.out.println("bytes: "+ HexFormat.of().formatHex(bytes));
        char[] hex = BytesTools.byteArrayToHexCharArray(bytes);
        System.out.println(hex);

        char[] base64 = BytesTools.byteArrayToBase64CharArray(bytes);
        System.out.println(base64);
        byte[] hexBytes = BytesTools.hexCharArrayToByteArray(hex);
        byte[] base64Bytes = BytesTools.base64CharArrayToByteArray(base64);

        if(Arrays.equals(hexBytes,base64Bytes)){
            System.out.println("Equal!");
            System.out.println(BytesTools.byteArrayToUtf8CharArray(hexBytes));
        }else{
            System.out.println("Not equal!!!");
        }
    }
}
