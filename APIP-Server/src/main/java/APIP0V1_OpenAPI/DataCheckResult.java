package APIP0V1_OpenAPI;

import apipClass.RequestBody;

public class DataCheckResult {

    private RequestBody dataRequestBody;
    private String addr;
    private String pubKey;
    private String sessionName;
    private String sessionKey;

    public RequestBody getDataRequestBody() {
        return dataRequestBody;
    }

    public void setDataRequestBody(RequestBody dataRequestBody) {
        this.dataRequestBody = dataRequestBody;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
