package apipRequest;

public class ResponseFc {
    private String from;
    private String to;
    private String sign;
    private Reply reply;
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

    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    public byte[] getReplyBytes() {
        return replyBytes;
    }

    public void setReplyBytes(byte[] replyBytes) {
        this.replyBytes = replyBytes;
    }
}
