package personal;

public class ContactRaw {

	private String op;
	private String contactId;
	private String alg;
	private String ciphertext;
	

	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}
	public String getCiphertext() {
		return ciphertext;
	}
	public void setCiphertext(String ciphertext) {
		this.ciphertext = ciphertext;
	}
	
	
}
