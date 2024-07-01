package fchClass;

public class CashMark {
	private String cashId;		//input id, the hash of previous txid and index, e.g. the first 32+4 bytes of the input.
	private String owner;
	private Long value;
	private long cdd;

	
	public String getCashId() {
		return cashId;
	}
	public void setCashId(String cashId) {
		this.cashId = cashId;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}
	public long getCdd() {
		return cdd;
	}
	public void setCdd(long cdd) {
		this.cdd = cdd;
	}
	
}
