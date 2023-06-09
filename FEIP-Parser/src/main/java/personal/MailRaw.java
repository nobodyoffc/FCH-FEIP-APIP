package personal;

public class MailRaw {

	private String op;
	private String mailId;
    private String alg;
	private String msg;
	private String ciphertextSend;
	private String ciphertextReci;
	private String textId;
	
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getMailId() {
		return mailId;
	}
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
	}
	public String getCiphertextSend() {
		return ciphertextSend;
	}
	public void setCiphertextSend(String ciphertextSend) {
		this.ciphertextSend = ciphertextSend;
	}
	public String getCiphertextReci() {
		return ciphertextReci;
	}
	public void setCiphertextReci(String ciphertextReci) {
		this.ciphertextReci = ciphertextReci;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getTextId() {
		return textId;
	}
	public void setTextId(String textId) {
		this.textId = textId;
	}
	
	
}
