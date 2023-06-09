package fc_dsl;

public class Range {
    private String[] fields;
    private String gt;
    private String gte;
    private String lt;
    private String lte;

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getGt() {
        return gt;
    }

    public void setGt(String gt) {
        this.gt = gt;
    }

    public String getGte() {
        return gte;
    }

    public void setGte(String gte) {
        this.gte = gte;
    }

    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public String getLte() {
        return lte;
    }

    public void setLte(String lte) {
        this.lte = lte;
    }
}
