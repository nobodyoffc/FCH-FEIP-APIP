package FchClass;

public class TxMark {
	private String txId;
	private long outValue;	//tx hashs
	private long fee;	//tx hashs
	private long cdd;	//tx hashs
	
	public String getTxId() {
		return txId;
	}
	public void setTxId(String txId) {
		this.txId = txId;
	}
	public long getOutValue() {
		return outValue;
	}
	public void setOutValue(long outValue) {
		this.outValue = outValue;
	}
	public long getFee() {
		return fee;
	}
	public void setFee(long fee) {
		this.fee = fee;
	}
	public long getCdd() {
		return cdd;
	}
	public void setCdd(long cdd) {
		this.cdd = cdd;
	}

}
