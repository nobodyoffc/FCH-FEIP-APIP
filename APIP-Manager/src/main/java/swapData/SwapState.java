package swapData;

public class SwapState{
    private String sid;
    private double gSum;
    private double mSum;
    private String gBestBlockId;
    private long gBestHeight;
    private String mBestBlockId;
    private long mBestHeight;
    private long lastTime;
    private long lastSn;
    private String lastId;
    private double gPendingSum;
    private double mPendingSum;


    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void lastSnPlus1(){
        this.lastSn++;
    }
    public double getgSum() {
        return gSum;
    }

    public void setgSum(double gSum) {
        this.gSum = gSum;
    }

    public double getmSum() {
        return mSum;
    }

    public void setmSum(double mSum) {
        this.mSum = mSum;
    }
    public String getgBestBlockId() {
        return gBestBlockId;
    }

    public void setgBestBlockId(String gBestBlockId) {
        this.gBestBlockId = gBestBlockId;
    }

    public long getgBestHeight() {
        return gBestHeight;
    }

    public void setgBestHeight(long gBestHeight) {
        this.gBestHeight = gBestHeight;
    }

    public String getmBestBlockId() {
        return mBestBlockId;
    }

    public void setmBestBlockId(String mBestBlockId) {
        this.mBestBlockId = mBestBlockId;
    }

    public long getmBestHeight() {
        return mBestHeight;
    }

    public void setmBestHeight(long mBestHeight) {
        this.mBestHeight = mBestHeight;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getLastSn() {
        return lastSn;
    }

    public void setLastSn(long lastSn) {
        this.lastSn = lastSn;
    }

    public String getLastId() {
        return lastId;
    }

    public void setLastId(String lastId) {
        this.lastId = lastId;
    }

//
//    public double getgLpSum() {
//        return gLpSum;
//    }
//
//    public void setgLpSum(double gLpSum) {
//        this.gLpSum = gLpSum;
//    }
//
//    public double getmLpSum() {
//        return mLpSum;
//    }
//
//    public void setmLpSum(double mLpSum) {
//        this.mLpSum = mLpSum;
//    }



    public double getgPendingSum() {
        return gPendingSum;
    }

    public void setgPendingSum(double gPendingSum) {
        this.gPendingSum = gPendingSum;
    }

    public double getmPendingSum() {
        return mPendingSum;
    }

    public void setmPendingSum(double mPendingSum) {
        this.mPendingSum = mPendingSum;
    }
}
