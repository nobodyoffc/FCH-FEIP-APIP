package service;

public class OpReturn {
	public final String type = "FEIP";
	public  final short sn = 5;
	public  final short ver = 2;
	public  final String name = "service";
	public  final String pid = "";
	
	private Data data;
	
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}
}
