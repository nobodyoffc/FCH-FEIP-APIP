package Payment;

import java.util.Map;

public class payParams {
    private Map<String,Integer> gainProportion;
    private Map<String,Long> cost;
    private Long incomeT;

    public Long getIncomeT() {
        return incomeT;
    }

    public void setIncomeT(Long incomeT) {
        this.incomeT = incomeT;
    }

    public Map<String, Integer> getGainProportion() {
        return gainProportion;
    }

    public void setGainProportion(Map<String, Integer> gainProportion) {
        this.gainProportion = gainProportion;
    }

    public Map<String, Long> getCost() {
        return cost;
    }

    public void setCost(Map<String, Long> cost) {
        this.cost = cost;
    }
}
