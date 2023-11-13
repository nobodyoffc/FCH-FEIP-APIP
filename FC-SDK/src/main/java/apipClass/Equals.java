package apipClass;

import java.util.Arrays;

public class Equals {
    private String[] fields;
    private String[] values;

    public Equals addNewFields(String... fields) {
        this.fields=fields;
        return this;
    }
    public Equals appendFields(String field) {
        String[] newFields = Arrays.copyOf(fields, fields.length + 1);
        newFields[fields.length] = field;
        fields=newFields;
        return this;
    }

    public Equals addNewValues(String... values) {
        this.values=values;
        return this;
    }
    public Equals appendValues(String field) {
        String[] newValues = Arrays.copyOf(values, values.length + 1);
        newValues[values.length] = field;
        values=newValues;
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

    public void setValues(String[] values) {
        this.values = values;
    }
}
