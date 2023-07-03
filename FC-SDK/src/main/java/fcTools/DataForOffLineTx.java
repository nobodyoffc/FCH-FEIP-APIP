package fcTools;

import java.util.List;

public class DataForOffLineTx {
    private String fromFid;
    private List<SendTo> sendToList;
    private long cd;
    private String msg;

    public String getFromFid() {
        return fromFid;
    }

    public void setFromFid(String fromFid) {
        this.fromFid = fromFid;
    }

    public List<SendTo> getSendToList() {
        return sendToList;
    }

    public void setSendToList(List<SendTo> sendToList) {
        this.sendToList = sendToList;
    }

    public long getCd() {
        return cd;
    }

    public void setCd(long cd) {
        this.cd = cd;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
