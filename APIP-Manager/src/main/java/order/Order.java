package order;

public class Order {
    private String orderId;//cash id
    private String fromFid;
    private String toFid;
    private String via;
    private long amount;
    private long time;
    private String txId;
    private long txIndex;
    private long height;

    public static OrderOpReturn getJsonBuyOrder(String sid){
        OrderOpReturn orderOpReturn = new OrderOpReturn();
        OrderOpReturnData data = new OrderOpReturnData();
        data.setOp("buy");
        data.setSid(sid);
        orderOpReturn.setData(data);
        orderOpReturn.setType("APIP");
        orderOpReturn.setSn("1");
        orderOpReturn.setPid("");
        orderOpReturn.setName("OpenAPI");
        orderOpReturn.setVer("1");
        return orderOpReturn;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(long txIndex) {
        this.txIndex = txIndex;
    }


    public String getFromFid() {
        return fromFid;
    }

    public void setFromFid(String fromFid) {
        this.fromFid = fromFid;
    }

    public String getToFid() {
        return toFid;
    }

    public void setToFid(String toFid) {
        this.toFid = toFid;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }
}
