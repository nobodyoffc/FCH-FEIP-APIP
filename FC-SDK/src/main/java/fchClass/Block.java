package fchClass;

public class Block {
	//from block head
	private Long size;		//block size
	private Long height;		//block height
	private String version;		//version
	private String preBlockId;	//previous block hash
	private String merkleRoot;	//merkle tree root
	private Long time;		//block timestamp
	private Long bits;		//The current difficulty target
	private Long nonce;		//nonce
	private Integer txCount;		//number of TXs included

	//calculated
	private String blockId;		//block hash
	private long inValueT;		//total amount of all inputs values in satoshi
	private long outValueT;		//total amount of all outputs values in satoshi
	private long fee;		//total amount of tx fee in satoshi
	private long cdd;		//total amount of coindays destroyed
	
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public Long getHeight() {
		return height;
	}
	public void setHeight(Long height) {
		this.height = height;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getPreBlockId() {
		return preBlockId;
	}
	public void setPreBlockId(String preBlockId) {
		this.preBlockId = preBlockId;
	}
	public String getMerkleRoot() {
		return merkleRoot;
	}
	public void setMerkleRoot(String merkleRoot) {
		this.merkleRoot = merkleRoot;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Long getBits() {
		return bits;
	}
	public void setBits(Long bits) {
		this.bits = bits;
	}
	public Long getNonce() {
		return nonce;
	}
	public void setNonce(Long nonce) {
		this.nonce = nonce;
	}
	public Integer getTxCount() {
		return txCount;
	}
	public void setTxCount(Integer txCount) {
		this.txCount = txCount;
	}
	public String getBlockId() {
		return blockId;
	}
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	public long getInValueT() {
		return inValueT;
	}
	public void setInValueT(long inValueT) {
		this.inValueT = inValueT;
	}
	public long getOutValueT() {
		return outValueT;
	}
	public void setOutValueT(long outValueT) {
		this.outValueT = outValueT;
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