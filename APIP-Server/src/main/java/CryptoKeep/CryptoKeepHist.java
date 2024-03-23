package CryptoKeep;

public class CryptoKeepHist {
    private String sn;
    private String op;
    private String owner;
    private String registerTxId;
    private String chipId;
    private String version;
    private long time;
    public CryptoKeepHist() {
    }
    public CryptoKeepHist(String sn, String op,long time) {
        this.sn = sn;
        this.op = op;
        this.time = time;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getChipId() {
        return chipId;
    }

    public void setChipId(String chipId) {
        this.chipId = chipId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRegisterTxId() {
        return registerTxId;
    }

    public void setRegisterTxId(String registerTxId) {
        this.registerTxId = registerTxId;
    }
}
