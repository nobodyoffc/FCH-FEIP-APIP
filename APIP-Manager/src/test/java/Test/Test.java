package Test;

import AesEcc.AES128;
import AesEcc.ECIES;
import AesEcc.EXPList;
import com.googlecode.jsonrpc4j.Base64;
import cryptoTools.SHA;
import keyTools.KeyTools;
import redis.clients.jedis.Jedis;
import FeipClass.Service;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(System.getProperty("user.dir"));
		//getFiled();
		//jedisTest();
		//stringToNum();
//		String filePath = "/Users/liuchangyong/Desktop/协议/FIPA9V1_ECIES非对称密码算法(zh-CN).md";//"/Users/liuchangyong/Desktop/协议/FIPA8V1_AES256CBC密码算法(zh-CN).md";
//		shaFile(filePath);
		//aesEncrypt()
//		eciesEncrypt();
//		File directory = new File("/fc");
//		if (!directory.exists()) {
//			System.out.println(directory.mkdirs());
//		}
//		File configFile = new File("/fc/config.json");
//		try {
//			if (configFile.createNewFile()) {
//				System.out.println("File created successfully.");
//			} else {
//				System.out.println("File already exists.");
//			}
//		} catch (IOException e) {
//			System.out.println("An error occurred while creating the file.");
//			e.printStackTrace();
//		}
	}

	private static void eciesEncrypt() throws Exception {
		String message= "{\"data\":\"test\"}";
		String pubKey = "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a";
		String priKey = KeyTools.getPriKey32("L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8");
		System.out.println("priKey: "+priKey);

		EXPList.set_EXP_List();
		ECIES ecies = new ECIES();
		ecies.generateKeyPair(priKey);

		byte[] ciphertextBytes = ecies.encrypt(message);
		System.out.println("ciphertext:"+Base64.encodeBytes(ciphertextBytes));

		byte[] msgBytes = ecies.decrypt(ciphertextBytes);
		System.out.println(new String(msgBytes));
	}

	private static void aesEncrypt() throws Exception {
		String plainText= "{\"data\":\"test\"}";
		String symKey = "7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08";//"7904517bd0c5646aeb861b1475bc4d78";//"

//		String kee = new String (AES256.hexStringToBytes(symKey),"utf-8");
//		System.out.println(kee);
//		System.out.println(AES256.byteToHexString(kee.getBytes()));
//		System.out.println("new :"+AES256.encrypt(plainText,kee));

		String cyphertext = AES128.byteToHexString(AES128.encrypt(plainText.getBytes(), AES128.hexStringToBytes(symKey)));//aesEncrypt(plainText,AES256.hexStringToBytes(symKey));
		String cyphertextBase64 = Base64.encodeBytes(AES128.encrypt(plainText.getBytes(), AES128.hexStringToBytes(symKey)));//aesEncrypt(plainText,AES256.hexStringToBytes(symKey));

		System.out.println(cyphertext);
		System.out.println(cyphertextBase64);
		System.out.println(new String(AES128.decrypt(AES128.hexStringToBytes(cyphertext), AES128.hexStringToBytes(symKey))));

	}

	private static void shaFile(String filePath) throws IOException {
		System.out.println(SHA.Sha256x2(new File(filePath)));
	}


	private void aes(){

	}


	private static void getFiled() throws ClassNotFoundException {
		// TODO Auto-generated method stub

		Class<?> c = Class.forName("Service");

		Field[] f = c.getFields();

		for(Field fi:f) {
			System.out.println(fi);
		}
	}

	private static void stringToNum() {
		// TODO Auto-generated method stub
		String str = "0.01s";

		Float flo = Float.valueOf(str);

		System.out.println(flo);
	}

	private static void jedisTest() {
		// TODO Auto-generated method stub
		Jedis jedis = new Jedis();

		jedis.set("feip", "cid");

		System.out.println("feip:"+ jedis.get("feip"));

		Service service = new Service();

		service.setSid("fjasd");
		service.setStdName("test");

		HashMap<String, String> map = new HashMap <String,String>();

		map.put("cid", "carmx");
		map.put("addr", "ffffarmx");

		jedis.hmset("service",map);

		Set<String> keys = jedis.keys("*");
		for(String key:keys) {
			System.out.println(key);
		}

		jedis.close();
	}


}
