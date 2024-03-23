package CryptoKeep;

public class CryptoKeepData {
    private String type;
    private String ver;
    private Data data;

    // Constructors, getters, and setters
    public CryptoKeepData() {
    }

    public CryptoKeepData(String type, String ver, Data data) {
        this.type = type;
        this.ver = ver;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String op;
        private String sn;
        private String fidChipIdHash;

        // Constructors, getters, and setters
        public Data() {
        }

        public Data(String op, String sn, String hash) {
            this.op = op;
            this.sn = sn;
            this.fidChipIdHash = hash;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getFidChipIdHash() {
            return fidChipIdHash;
        }

        public void setFidChipIdHash(String fidChipIdHash) {
            this.fidChipIdHash = fidChipIdHash;
        }
    }
}

