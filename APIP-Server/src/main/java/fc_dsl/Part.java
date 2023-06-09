package fc_dsl;

public class Part {
    private String[] fields;
    private String value;
    private String isCaseInsensitive;

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
