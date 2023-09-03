package apipClass;

import eccAes256K1P7.EccAes256K1P7;
import javaTools.BytesTools;
import menu.Inputer;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApipDataRequestParams {
    private String urlHead;
    private String sessionKeyCipher;
    private String sessionName;

    public String getUrlHead() {
        return urlHead;
    }

    public void setUrlHead(String urlHead) {
        this.urlHead = urlHead;
    }

    public String getSessionKeyCipher() {
        return sessionKeyCipher;
    }

    public void setSessionKeyCipher(String sessionKeyCipher) {
        this.sessionKeyCipher = sessionKeyCipher;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getVia() {
        String via = "FErVBq2SzT4GGcH61wSvo6BofQ8U5JL3Cg";
        return via;
    }

    public void inputUrlHead(BufferedReader br) {
        System.out.println("Input the urlHead of APIP service. Enter to skip:");
        String input =Inputer.inputString(br);
        if("".equals(input))return;
        urlHead=input;
    }

    public void inputSessionKeyCipher(BufferedReader br) {
        String ask = "Input sessionKey:";
        char[] sessionKey = Inputer.input32BytesKey(br, ask);
        assert sessionKey != null;
        byte[] sessionKeyBytes = BytesTools.hexCharArrayToByteArray(sessionKey);
        ask = "Input password to encrypt the sessionKey:";
        char[] password = Inputer.inputPassword(br, ask);
        byte[] passwordBytes = BytesTools.charArrayToByteArray(password, StandardCharsets.UTF_8);
        sessionKeyCipher = Base64.getEncoder().encodeToString(encryptSessionKey(sessionKeyBytes,passwordBytes));
        System.out.println("SessionKeyCipher is: "+sessionKeyCipher);
    }

    private byte[] encryptSessionKey(byte[] sessionKeyBytes, byte[] passwordBytes) {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        return ecc.encryptPasswordBundle(sessionKeyBytes,passwordBytes);
    }
}
