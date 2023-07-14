package Payment;

import java.util.Map;

public class Payroll {
    private Map<String,Long> consumeViaMap;
    private Map<String,Long> orderViaMap;
    private Map<String,Long> costMap;
    private Map<String,Long> gainMap;

    public Map<String, Long> getConsumeViaMap() {
        return consumeViaMap;
    }

    public void setConsumeViaMap(Map<String, Long> consumeViaMap) {
        this.consumeViaMap = consumeViaMap;
    }

    public Map<String, Long> getOrderViaMap() {
        return orderViaMap;
    }

    public void setOrderViaMap(Map<String, Long> orderViaMap) {
        this.orderViaMap = orderViaMap;
    }

    public Map<String, Long> getCostMap() {
        return costMap;
    }

    public void setCostMap(Map<String, Long> costMap) {
        this.costMap = costMap;
    }

    public Map<String, Long> getGainMap() {
        return gainMap;
    }

    public void setGainMap(Map<String, Long> gainMap) {
        this.gainMap = gainMap;
    }
}
