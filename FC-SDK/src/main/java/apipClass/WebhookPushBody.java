package apipClass;

public class WebhookPushBody {
    private String fromSid;
    private String method;
    private String sessionName;
    private String data;
    private String sign;
    private String balance;
    private String bestHeight;

    public String getFromSid() {
        return fromSid;
    }

    public void setFromSid(String fromSid) {
        this.fromSid = fromSid;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(String bestHeight) {
        this.bestHeight = bestHeight;
    }
}
