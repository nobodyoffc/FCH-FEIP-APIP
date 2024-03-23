package CryptoKeep;

public class CryptoKeep {
    private String sn;
    private String chipId;
    private String version;
    private long madeTime;
    private long registerTime;
    private String owner;
    private String seller;
    public CryptoKeep() {
    }
    public CryptoKeep(String sn, String chipId, String version, long madeTime) {
        this.sn = sn;
        this.chipId = chipId;
        this.version = version;
        this.madeTime = madeTime;
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

    public long getMadeTime() {
        return madeTime;
    }

    public void setMadeTime(long madeTime) {
        this.madeTime = madeTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }
}
