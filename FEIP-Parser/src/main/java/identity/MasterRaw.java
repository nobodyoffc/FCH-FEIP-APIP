package identity;

public class MasterRaw {
	private String master;
	private String promise;
	private String cipherPriKey;
	private String alg;

	public String getCipherPriKey() {
		return cipherPriKey;
	}

	public void setCipherPriKey(String cipherPriKey) {
		this.cipherPriKey = cipherPriKey;
	}

	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}

	public String getMaster() {
		return master;
	}
	public void setMaster(String master) {
		this.master = master;
	}
	public String getPromise() {
		return promise;
	}
	public void setPromise(String promise) {
		this.promise = promise;
	}
}
