package APIP0V1_OpenAPI;

import apipClass.Fcdsl;

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

    String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    long getTime() {
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

}
