package fchClass;

public class CashMark {
	private String cashId;		//input id, the hash of previous txid and index, e.g. the first 32+4 bytes of the input.
	private String fid;	//input address
	private long value;		//input value
	private long cdd;		//input value

	
	public String getCashId() {
		return cashId;
	}
	public void setCashId(String cashId) {
		this.cashId = cashId;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public long getCdd() {
		return cdd;
	}
	public void setCdd(long cdd) {
		this.cdd = cdd;
	}
	
}
