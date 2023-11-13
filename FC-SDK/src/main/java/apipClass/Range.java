package apipClass;

import java.util.Arrays;

public class Range {
    private String[] fields;
    private String gt;
    private String gte;
    private String lt;
    private String lte;

    static enum op{
        GT("gt"),
        Lt("lt"),
        GTE("gte"),
        LTE("lte");
        op(String name) {
        }
    }

    public Range addNewFields(String... fields) {
        this.fields=fields;
        return this;
    }
    public Range appendFields(String field) {
        String[] newFields = Arrays.copyOf(fields, fields.length + 1);
        newFields[fields.length] = field;
        fields=newFields;
        return this;
    }

    public Range addGte(String gte) {
        this.gte = gte;
        return this;
    }

    public Range addLt(String lt) {
        this.lt = lt;
        return this;
    }

    public Range addLte(String lte) {
        this.lte = lte;
        return this;
    }

    public Range addGt(String gt) {
        this.gt = gt;
        return this;
    }

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
