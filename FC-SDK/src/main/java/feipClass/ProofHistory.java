package feipClass;

public class ProofHistory {
	
	private String txId;
	private long height;
	private int index;
	private long time;
	private String signer;
	private String recipient;

	private String proofId;
	private String op;
	private String title;
	private String content;
	private String[] cosigners;
	private boolean transferable;
	private boolean allSignsRequired;

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public long getHeight() {
		return height;
	}

	public void setHeight(long height) {
		this.height = height;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getSigner() {
		return signer;
	}

	public void setSigner(String signer) {
		this.signer = signer;
	}

	public String getProofId() {
		return proofId;
	}

	public void setProofId(String proofId) {
		this.proofId = proofId;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String[] getCosigners() {
		return cosigners;
	}

	public void setCosigners(String[] cosigners) {
		this.cosigners = cosigners;
	}

	public boolean isTransferable() {
		return transferable;
	}

	public void setTransferable(boolean transferable) {
		this.transferable = transferable;
	}

	public boolean isAllSignsRequired() {
		return allSignsRequired;
	}

	public void setAllSignsRequired(boolean allSignsRequired) {
		this.allSignsRequired = allSignsRequired;
	}
}
