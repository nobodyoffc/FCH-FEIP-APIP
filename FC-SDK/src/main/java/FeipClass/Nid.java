package FeipClass;

public class Nid {
	private String nameId;
    private String name;
	private String desc;
	private String oid;
	
	private String namer;
	private long birthTime;
	private long birthHeight;
	private long lastTime;
	private long lastHeight;
	private boolean active;

	public String getNameId() {
		return nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
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

	public String getNamer() {
		return namer;
	}

	public void setNamer(String namer) {
		this.namer = namer;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}
}
