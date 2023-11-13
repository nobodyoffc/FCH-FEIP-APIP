package apipClass;

import java.util.Arrays;

public class Part {
    private String[] fields;
    private String value;
    private String isCaseInsensitive;


    public Part addNewValue(String value) {
        this.value=value;
        return this;
    }
    public Part addNewFields(String... fields) {
        this.fields=fields;
        return this;
    }
    public Part appendFields(String field) {
        String[] newFields = Arrays.copyOf(fields, fields.length + 1);
        newFields[fields.length] = field;
        fields=newFields;
        return this;
    }

    public String getIsCaseInsensitive() {
        return isCaseInsensitive;
    }

    public void setIsCaseInsensitive(String isCaseInsensitive) {
        this.isCaseInsensitive = isCaseInsensitive;
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
