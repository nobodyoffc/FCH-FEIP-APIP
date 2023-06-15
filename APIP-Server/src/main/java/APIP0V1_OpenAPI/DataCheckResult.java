package APIP0V1_OpenAPI;

public class DataCheckResult {

    private DataRequestBody dataRequestBody;
    private String addr;
    private String pubKey;
    private String sessionName;
    private String sessionKey;

    public DataRequestBody getDataRequestBody() {
        return dataRequestBody;
    }

    public void setDataRequestBody(DataRequestBody dataRequestBody) {
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
