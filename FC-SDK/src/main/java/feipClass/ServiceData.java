package feipClass;

import constants.OpNames;
import menu.Inputer;

import java.io.BufferedReader;
import java.util.Arrays;

public class ServiceData {
	private String sid;
	private String op;
	private String stdName;
	private String[] localNames;
	private String desc;
	private String[] types;
	private String[] urls;
	private String[] waiters;
	private String[] protocols;
	private String[] codes;
	private Object params;
	private int rate;
	private String closeStatement;

	public void inputServicePublish(BufferedReader br)  {

		inputStdName(br);

		inputLocalNames(br);

		inputDesc(br);

		inputUrls(br);

		inputWaiters(br);

		inputProtocols(br);

	}

	public void inputOp(BufferedReader br)  {
		System.out.println("Input the operation you want to do:");
		while (true) {
			String input = Inputer.inputString(br);
			if(OpNames.contains(input)) {
				setStdName(input);
				break;
			}else{
				System.out.println("It should be one of "+OpNames.showAll());
			}
		}
	}

	public void inputTypes(BufferedReader br)  {
		String ask = "Input the types of your service if you want. Enter to end :";
		String[] types = Inputer.inputStringArray(br,ask,0);
		if(types.length!=0) setTypes(types);
	}

	public void updateTypes(BufferedReader br)  {
		System.out.println("Types are: "+ Arrays.toString(types));
		inputTypes(br);
	}

	private void inputStdName(BufferedReader br) {
		System.out.println("Input the English name of your service:");
		setStdName(Inputer.inputString(br));
	}

	private void inputLocalNames(BufferedReader br)  {
		String ask = "Input the local names of your service, if you want. Enter to end :";
		String[] localNames = Inputer.inputStringArray(br,ask,0);
		if(localNames.length!=0) setLocalNames(localNames);
	}

	private void inputDesc(BufferedReader br)  {
		System.out.println("Input the description of your service if you want.Enter to ignore:");
		String str = Inputer.inputString(br);
		if(!str.equals("")) setDesc(str);
	}

	private void inputUrls(BufferedReader br){
		String ask;
		ask = "Input the URLs of your service, if you want. Enter to end :";
		String[] urls = Inputer.inputStringArray(br,ask,0);
		if(urls.length!=0) setUrls(urls);
	}

	private void inputWaiters(BufferedReader br) {
		String ask;
		ask = "Input the FCH address of the waiter for your service if you want. Enter to ignore:";
		String[] waiters = Inputer.inputStringArray(br,ask,0);
		if(waiters.length!=0) setWaiters(waiters);
	}

	private void inputProtocols(BufferedReader br) {
		String ask;
		ask = "Input the PIDs of the protocols your service using if you want. Enter to end :";
		String[] protocols = Inputer.inputStringArray(br,ask,64);
		if(protocols.length!=0) setProtocols(protocols);
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
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

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
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

	public Object getParams() {
		return params;
	}

	public void setParams(Object params) {
		this.params = params;
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


	public void updateServiceHead(BufferedReader br) {
		updateStdName(br);

		updateLocalNames(br);

		updateDesc(br);

		updateUrls(br);

		updateWaiters(br);

		updateProtocols(br);
	}

	private void updateProtocols(BufferedReader br) {
		System.out.println("Protocols are: "+ Arrays.toString(protocols));
		inputProtocols(br);
	}

	private void updateWaiters(BufferedReader br) {
		System.out.println("Waiters are: "+ Arrays.toString(waiters));
		inputWaiters(br);
	}

	private void updateUrls(BufferedReader br) {
		System.out.println("Urls are: "+ Arrays.toString(urls));
		inputUrls(br);
	}

	private void updateDesc(BufferedReader br) {
		System.out.println("StdName is: "+desc);
		inputDesc(br);
	}

	private void updateLocalNames(BufferedReader br) {
		System.out.println("LocalNames are: "+ localNames);
		inputLocalNames(br);
	}

	private void updateStdName(BufferedReader br) {
		System.out.println("StdName is: "+stdName);
		inputStdName(br);
	}
}
