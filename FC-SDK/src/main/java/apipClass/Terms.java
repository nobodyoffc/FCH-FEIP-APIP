package apipClass;

import java.util.Arrays;

public class Terms {
    private String[] fields;
    private String[] values;

    public Terms addNewFields(String... fields) {
        this.fields=fields;
        return this;
    }
    public Terms appendFields(String field) {
        String[] newFields = Arrays.copyOf(fields, fields.length + 1);
        newFields[fields.length] = field;
        fields=newFields;
        return this;
    }

    public Terms addNewValues(String... values) {
        this.values=values;
        return this;
    }
    public Terms appendValues(String value) {
        String[] newValues = Arrays.copyOf(this.values, this.values.length + 1);
        newValues[this.values.length] = value;
        this.values =newValues;
        return this;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String... values) {
        this.values = values;
    }
}
