package FeipClass;

public class Mail {
	private String mailId;
    private String alg;
	private String cipher;
	private String cipherSend;
	private String cipherReci;
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
	public String getCipherSend() {
		return cipherSend;
	}
	public void setCipherSend(String cipherSend) {
		this.cipherSend = cipherSend;
	}
	public String getCipherReci() {
		return cipherReci;
	}
	public void setCipherReci(String cipherReci) {
		this.cipherReci = cipherReci;
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

	public String getCipher() {
		return cipher;
	}

	public void setCipher(String cipher) {
		this.cipher = cipher;
	}
}
