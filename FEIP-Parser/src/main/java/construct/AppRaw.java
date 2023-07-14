package construct;

import feipClass.App;

public class AppRaw {
	
	private String aid;
	private String op;
	private String stdName;
	private String[] localNames;
	private String desc;
	private String[] types;
	private String[] urls;
	private App.Download[] downloads;
	private String[] waiters;
	private String[] protocols;
	private String[] codes;
	private String[] services;
	private int rate;
	private String closeStatement;
	
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getStdName() {
		return stdName;
	}
	public void setStdName(String stdName) {
		this.stdName = stdName;
	}
	public String[] getLocalNames() {
		return localNames;
	}
	public void setLocalNames(String[] localNames) {
		this.localNames = localNames;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String[] getUrls() {
		return urls;
	}
	public void setUrls(String[] urls) {
		this.urls = urls;
	}
	public String[] getWaiters() {
		return waiters;
	}
	public void setWaiters(String[] waiters) {
		this.waiters = waiters;
	}
	public String[] getProtocols() {
		return protocols;
	}
	public void setProtocols(String[] protocols) {
		this.protocols = protocols;
	}
	public String[] getServices() {
		return services;
	}
	public void setServices(String[] services) {
		this.services = services;
	}
	public String[] getTypes() {
		return types;
	}
	public void setTypes(String[] types) {
		this.types = types;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public String[] getCodes() {
		return codes;
	}
	public void setCodes(String[] codes) {
		this.codes = codes;
	}
	public String getCloseStatement() {
		return closeStatement;
	}
	public void setCloseStatement(String closeStatement) {
		this.closeStatement = closeStatement;
	}
	public App.Download[] getDownloads() {
		return downloads;
	}
	public void setDownloads(App.Download[] downloads) {
		this.downloads = downloads;
	}
}
