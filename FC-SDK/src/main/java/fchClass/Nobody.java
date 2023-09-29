package fchClass;

public class Nobody {
    private String fid;
    private String priKey;
    private long deathTime;
    private long deathHeight;
    private String deathTxId;
    private int deathTxIndex;
    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public long getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(long deathTime) {
        this.deathTime = deathTime;
    }

    public long getDeathHeight() {
        return deathHeight;
    }

    public void setDeathHeight(long deathHeight) {
        this.deathHeight = deathHeight;
    }

    public String getDeathTxId() {
        return deathTxId;
    }

    public void setDeathTxId(String deathTxId) {
        this.deathTxId = deathTxId;
    }

    public int getDeathTxIndex() {
        return deathTxIndex;
    }

    public void setDeathTxIndex(int deathTxIndex) {
        this.deathTxIndex = deathTxIndex;
    }
}
