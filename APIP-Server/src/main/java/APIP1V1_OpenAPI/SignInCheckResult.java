package APIP1V1_OpenAPI;

public class SignInCheckResult {

    private SignInRequestBody connectRequestBody;
    private String fid;
    private String pubKey;
    private String sessionName;
    private String sessionKey;

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

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }



    public SignInRequestBody getConnectRequestBody() {
        return connectRequestBody;
    }

    public void setConnectRequestBody(SignInRequestBody connectRequestBody) {
        this.connectRequestBody = connectRequestBody;
    }
}
