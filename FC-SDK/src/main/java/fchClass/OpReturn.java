package fchClass;

public class OpReturn {

	private String txId;		//txid
	private Long height;		//block height
	private Long time;
	private Integer txIndex;		//tx index in the block
	private String opReturn;	//OP_RETURN text
	private String signer;	//address of the first input.
	private String recipient;	//address of the first output, but the first input address and opReturn output.
	private long cdd;

	public String getTxId() {
		return txId;
	}
	public void setTxId(String txId) {
		this.txId = txId;
	}
	public Long getHeight() {
		return height;
	}
	public void setHeight(Long height) {
		this.height = height;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Integer getTxIndex() {
		return txIndex;
	}
	public void setTxIndex(Integer txIndex) {
		this.txIndex = txIndex;
	}
	public String getOpReturn() {
		return opReturn;
	}
	public void setOpReturn(String opReturn) {
		this.opReturn = opReturn;
	}
	public String getSigner() {
		return signer;
	}
	public void setSigner(String signer) {
		this.signer = signer;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public long getCdd() {
		return cdd;
	}
	public void setCdd(long cdd) {
		this.cdd = cdd;
	}

	
	
}
