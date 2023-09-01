package apipClass;

import apipClass.Fcdsl;
import com.google.gson.Gson;
import cryptoTools.SHA;
import javaTools.BytesTools;

import java.nio.charset.StandardCharsets;

public class DataRequestBody{

    private String url;
    private long time;
    private long nonce;
    private String via;
    private Fcdsl fcdsl;

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public Fcdsl getFcdsl() {
        return fcdsl;
    }

    public void setFcdsl(Fcdsl fcdsl) {
        this.fcdsl = fcdsl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public void makeRequestBody(String url, String via) {
        setTime(System.currentTimeMillis());
        setNonce((BytesTools.bytes4ToLongBE(BytesTools.getRandomBytes(4))));
        setVia(via);
        setUrl(url);
    }

    public byte[] makeRequestBodySign(byte[] symKey) {
        String json = new Gson().toJson(this);
        return SHA.Sha256x2(BytesTools.bytesMerger(json.getBytes(StandardCharsets.UTF_8),symKey));
    }
}
