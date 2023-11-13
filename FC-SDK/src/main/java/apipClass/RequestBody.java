package apipClass;

import apipClass.Fcdsl;
import com.google.gson.Gson;
import javaTools.BytesTools;

public class RequestBody {

    private String url;
    private long time;
    private long nonce;
    private String via;
    private Fcdsl fcdsl;
    private String mode;

    public RequestBody(){}
    public RequestBody(String url, String via) {
        setTime(System.currentTimeMillis());
        setNonce((BytesTools.bytes4ToLongBE(BytesTools.getRandomBytes(4))));
        setVia(via);
        setUrl(url);
    }

    public RequestBody(String url, String via, String mode) {
        setTime(System.currentTimeMillis());
        setNonce((BytesTools.bytes4ToLongBE(BytesTools.getRandomBytes(4))));
        setVia(via);
        setUrl(url);
        setMode(mode);
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public void makeRequestBody(String url, String via) {
        setTime(System.currentTimeMillis());
        setNonce((BytesTools.bytes4ToLongBE(BytesTools.getRandomBytes(4))));
        setVia(via);
        setUrl(url);
    }

    public void makeRequestBody(String url, String via,String mode) {
        setTime(System.currentTimeMillis());
        setNonce((BytesTools.bytes4ToLongBE(BytesTools.getRandomBytes(4))));
        setVia(via);
        setUrl(url);
        if(mode!=null)setMode(mode);
    }


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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
