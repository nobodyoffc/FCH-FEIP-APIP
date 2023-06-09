package FeipClass;

public class Mail {
	private String mailId;
    private String alg;
	private String ciphertextSend;
	private String ciphertextReci;
	private String textId;
	
	private String sender;
	private String recipient;
	private long birthTime;
	private long birthHeight;
	private long lastHeight;
	private boolean active;


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
	public String getMailId() {
		return mailId;
	}
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getTextId() {
		return textId;
	}
	public void setTextId(String textId) {
		this.textId = textId;
	}
	
	
}
