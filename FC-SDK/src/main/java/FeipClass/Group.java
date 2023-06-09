package FeipClass;

public class Group {
	private String gid;
	private String name;
	private String desc;
	
	private String[] namers;
	private String[] members;
	private long memberNum;
	private long birthTime;

	public long getMemberNum() {
		return memberNum;
	}

	public void setMemberNum(long memberNum) {
		this.memberNum = memberNum;
	}

	private long birthHeight;
	private String lastTxId;
	private long lastTime;
	private long lastHeight;
	private long cddToUpdate;
	private long tCdd;


	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public long getBirthTime() {
		return birthTime;
	}
	public void setBirthTime(long birthTime) {
		this.birthTime = birthTime;
	}
	public long getBirthHeight() {
		return birthHeight;
	}
	public void setBirthHeight(long birthHeight) {
		this.birthHeight = birthHeight;
	}
	public String getLastTxId() {
		return lastTxId;
	}
	public void setLastTxId(String lastTxId) {
		this.lastTxId = lastTxId;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	public long getLastHeight() {
		return lastHeight;
	}
	public void setLastHeight(long lastHeight) {
		this.lastHeight = lastHeight;
	}
	public long getCddToUpdate() {
		return cddToUpdate;
	}
	public void setCddToUpdate(long requiredCdd) {
		this.cddToUpdate = requiredCdd;
	}
	public long gettCdd() {
		return tCdd;
	}
	public void settCdd(long tCdd) {
		this.tCdd = tCdd;
	}
	public String[] getNamers() {
		return namers;
	}
	public void setNamers(String[] namers) {
		this.namers = namers;
	}
	public String[] getMembers() {
		return members;
	}
	public void setMembers(String[] members) {
		this.members = members;
	}
}
