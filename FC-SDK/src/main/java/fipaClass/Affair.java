package fipaClass;

public class Affair {
    private String meta = "FV";
    private Op op;
    private String relation;
    private String fid;
    private String oid;
    private String fidB;
    private String oidB;
    private Object data;
    private String dataBase64;

    public String getDataBase64() {
        return dataBase64;
    }

    public void setDataBase64(String dataBase64) {
        this.dataBase64 = dataBase64;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getFidB() {
        return fidB;
    }

    public void setFidB(String fidB) {
        this.fidB = fidB;
    }

    public String getOidB() {
        return oidB;
    }

    public void setOidB(String oidB) {
        this.oidB = oidB;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
