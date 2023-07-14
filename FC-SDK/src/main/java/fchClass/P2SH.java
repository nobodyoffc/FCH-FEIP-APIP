package fchClass;

public class P2SH {
	private String fid;
	private String redeemScript;
	private int m;
	private int n;
	private String pubKeys[];
	private String fids[];

	private long birthHeight;
	private long birthTime;
	private String birthTxId;

	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getRedeemScript() {
		return redeemScript;
	}
	public void setRedeemScript(String redeemScript) {
		this.redeemScript = redeemScript;
	}
	public int getM() {
		return m;
	}
	public void setM(int m) {
		this.m = m;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public String[] getPubKeys() {
		return pubKeys;
	}
	public void setPubKeys(String[] pubKeys) {
		this.pubKeys = pubKeys;
	}
	public long getBirthHeight() {
		return birthHeight;
	}
	public void setBirthHeight(long birthHeight) {
		this.birthHeight = birthHeight;
	}
	public long getBirthTime() {
		return birthTime;
	}
	public void setBirthTime(long birthTime) {
		this.birthTime = birthTime;
	}
	public String getBirthTxId() {
		return birthTxId;
	}
	public void setBirthTxId(String birthTxId) {
		this.birthTxId = birthTxId;
	}
	public String[] getFids() {
		return fids;
	}

	public void setFids(String[] fids) {
		this.fids = fids;
	}
	
}
