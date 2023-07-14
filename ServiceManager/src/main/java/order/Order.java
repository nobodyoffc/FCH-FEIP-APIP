package order;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import servers.EsTools;

import java.io.IOException;

public class Order {
    private String cashId;//cash id
    private String fromAddr;
    private String toAddr;
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

    public static void createOrderIndex(ElasticsearchClient esClient, String OrderIndex) throws IOException {
        String orderJsonStr = "{\"mappings\":{\"properties\":{\"cashId\":{\"type\":\"wildcard\"},\"fromAddr\":{\"type\":\"wildcard\"},\"toAddr\":{\"type\":\"wildcard\"},\"vias\":{\"type\":\"wildcard\"},\"amount\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"txIndex\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"}}}}";
        EsTools.createIndex(esClient, OrderIndex, orderJsonStr);
    }
    public static void deleteOrderIndex(ElasticsearchClient esClient, String OrderIndex) throws IOException {
        EsTools.deleteIndex(esClient, OrderIndex);
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getCashId() {
        return cashId;
    }

    public void setCashId(String cashId) {
        this.cashId = cashId;
    }

    public long getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(long txIndex) {
        this.txIndex = txIndex;
    }


    public String getFromAddr() {
        return fromAddr;
    }

    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
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
