package swapData;

public class swapPrice {
    private String sid;
    private String gTick;
    private String mTick;
    private double price;
    private long time;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getgTick() {
        return gTick;
    }

    public void setgTick(String gTick) {
        this.gTick = gTick;
    }

    public String getmTick() {
        return mTick;
    }

    public void setmTick(String mTick) {
        this.mTick = mTick;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
