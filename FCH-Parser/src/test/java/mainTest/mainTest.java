package mainTest;

//import com.xwc1125.chain5j.abi.datatypes.Address;

import keyTools.KeyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class mainTest {
	public static final int FILE_END = -1;
	public static final int WRONG = -2;
	public static final int HEADER_FORK = -3;
	public static final int REPEAT = -4;
	public static final int WAIT_MORE = 0;
	public static final String MAGIC = "f9beb4d9";

	static final Logger log = LoggerFactory.getLogger(mainTest.class);

	public static void main(String[] args) throws Exception {

//		////////////////////
//		esClient.esClient.StartClient sc = new esClient.esClient.StartClient();
//		sc.setParams("192.168.31.193", 9200);
//
//		ElasticsearchClient esClient.esClient = sc.getClientHttp();
//		// createIndex(client,"test");
//		HealthResponse ch = esClient.esClient.cat().health();
//		List<HealthRecord> vb = ch.valueBody();
//		System.out.println("ES Client was created. The cluster is: " + vb.get(0).cluster());
//		////////////////////
		
//		String hash = Hash.Sha256x2("test update.");
//		System.out.print(hash);
		String pk = "020a6be4ed72a3317bc8d148a2604a2b31c2d2c07405cacfcd175b68b9445ce42e";
		
		KeyTools.pubKeyToAtomAddr(pk);

		
		//date
		
		
		//	System.out.println("result:" + gson.toJson(result));

		//esClient.esClient.shutdown();
	}
	
}
