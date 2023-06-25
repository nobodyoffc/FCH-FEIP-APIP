package FeipClass;

public class Box {
    private String bid;
    private String name;
    private String desc;
    private Object contain;
    private String cipher;
    private String alg;
    private boolean active;
    private String owner;
    private long birthTime;
    private long birthHeight;
    private String lastTxId;
    private long lastTime;
    private long lastHeight;

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }
    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Object getContain() {
        return contain;
    }

    public void setContain(Object contain) {
        this.contain = contain;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(long birthTime) {
        this.birthTime = birthTime;
    }

    public long getBirthHeight() {
        return birthHeight;
    }

    public void setBirthHeight(long birthHeight) {
        this.birthHeight = birthHeight;
    }

    public String getLastTxId() {
        return lastTxId;
    }

    public void setLastTxId(String lastTxId) {
        this.lastTxId = lastTxId;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getLastHeight() {
        return lastHeight;
    }

    public void setLastHeight(long lastHeight) {
        this.lastHeight = lastHeight;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
