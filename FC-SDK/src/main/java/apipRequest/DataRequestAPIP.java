package apipRequest;

import cryptoTools.SHA;
import javaTools.BytesTools;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class DataRequestAPIP {
    public static boolean isGoodSign(String requestBody, String sign, String symKey){
        byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);
        return isGoodSign(requestBodyBytes,sign,HexFormat.of().parseHex(symKey));
    }
    public static boolean isGoodSign(byte[] requestBodyBytes, String sign, byte[] symKey){
        if(sign==null||requestBodyBytes==null)return false;
        byte[] signBytes = BytesTools.bytesMerger(requestBodyBytes, symKey);
        String doubleSha256Hash = HexFormat.of().formatHex(SHA.Sha256x2(signBytes));
        return (sign.equals(doubleSha256Hash));
    }
}
