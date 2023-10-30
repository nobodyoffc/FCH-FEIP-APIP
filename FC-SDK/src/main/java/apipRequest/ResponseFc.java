package apipRequest;

import apipClass.ResponseBody;

public class ResponseFc {
    private String from;
    private String to;
    private String sign;
    private ResponseBody responseBody;
    private byte[] replyBytes;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public byte[] getReplyBytes() {
        return replyBytes;
    }

    public void setReplyBytes(byte[] replyBytes) {
        this.replyBytes = replyBytes;
    }
}
