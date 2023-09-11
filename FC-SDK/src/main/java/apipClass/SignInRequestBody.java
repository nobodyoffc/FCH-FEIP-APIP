package apipClass;

import javaTools.BytesTools;

public class SignInRequestBody {
    private String url;
    private long time;
    private long nonce;
    private String mode;
    private String via;

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    private String pubKey;

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
}
