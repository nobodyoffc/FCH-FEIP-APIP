package apipClass;

import java.util.Arrays;

public class Match {
    private String[] fields;
    private String value;

    public Match addNewFields(String... fields) {
        this.fields=fields;
        return this;
    }
    public Match appendFields(String field) {
        String[] newFields = Arrays.copyOf(fields, fields.length + 1);
        newFields[fields.length] = field;
        fields=newFields;
        return this;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
