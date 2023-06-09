package service;

public class Params {

	private String urlHead;
	private String currency;
	private String account;
	private String pricePerKBytes;
	private String pricePerRequest;
	private String minPayment;
	private String sessionDays;

	public String getPricePerKBytes() {
		return pricePerKBytes;
	}

	public void setPricePerKBytes(String pricePerKBytes) {
		this.pricePerKBytes = pricePerKBytes;
	}

	public String getUrlHead() {
		return urlHead;
	}
	public void setUrlHead(String urlHead) {
		this.urlHead = urlHead;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPricePerRequest() {
		return pricePerRequest;
	}
	public void setPricePerRequest(String pricePerRequest) {
		this.pricePerRequest = pricePerRequest;
	}
	public String getMinPayment() {
		return minPayment;
	}
	public void setMinPayment(String minPayment) {
		this.minPayment = minPayment;
	}

	public String getSessionDays() {
		return sessionDays;
	}

	public void setSessionDays(String sessionDays) {
		this.sessionDays = sessionDays;
	}
}
