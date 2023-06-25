package startAPIP;

import EccAes256K1P7.Aes256CbcP7;
import api.Constant;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import FeipClass.Service;
import fcTools.ParseTools;
import menu.Menu;
import order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import servers.NewEsClient;
import service.Managing;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static api.Constant.UserDir;


public class StartAPIP {

	private static final Logger log = LoggerFactory.getLogger(StartAPIP.class);
	private static final NewEsClient newEsClient = new NewEsClient();

	public static void main(String[] args)throws Exception{

		log.info("Start.");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		boolean end = false;
		ElasticsearchClient esClient = null;

		ConfigAPIP configAPIP = new ConfigAPIP();
		configAPIP.setConfigFilePath();
		configAPIP = configAPIP.getClassInstanceFromFile(br,ConfigAPIP.class);
		if (configAPIP.getEsIp() == null||configAPIP.getTomcatBasePath()==null ||configAPIP.getOpReturnFilePath()==null) configAPIP.config(br);

		Jedis jedis = getJedis(configAPIP, br);
		if(jedis==null){
			log.debug("Redis is not ready.");
			br.readLine();
			return;
		}

		while(!end) {
			jedis.set(RedisKeys.AvatarBasePath,configAPIP.getAvatarBasePath());
			jedis.set(RedisKeys.AvatarPngPath,configAPIP.getAvatarPngPath());
			jedis.set(RedisKeys.ConfigFilePath,System.getProperty(UserDir));

			if(esClient==null)esClient = getElasticsearchClient(br, esClient, configAPIP, jedis);

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();
			menuItemList.add("Manage Service");
			menuItemList.add("Recreate Order Index");
			menuItemList.add("List APIs and Set nPrice");
			menuItemList.add("List Indices in ES");
			menuItemList.add("Switch mempool scanner");
			menuItemList.add("Set windowTime");
			menuItemList.add("Switch free get APIs");
			menuItemList.add("Find Users");
			menuItemList.add("How to buy this service?");
			menuItemList.add("Get ES indices list");
			menuItemList.add("config");

			menu.add(menuItemList);
			System.out.println(" << APIP manager>> \n");
			menu.show();
			int choice = menu.choose(br);

			switch(choice) {
				case 1: //Manage service
					Managing serviceManager= new Managing();
					serviceManager.menu(esClient, br,jedis);
					break;
				case 2:
					recreateOrderIndex(esClient);
					break;

				case 3:
					setNPrices(jedis, br);
					break;
				case 4:
					listIndices();
					br.readLine();
					break;

				case 5:
					System.out.println("scanMempool: "+configAPIP.isScanMempool()+". Switch it? 'y' to switch:");
					String confirm = br.readLine();

					if("y".equals(confirm)){
						configAPIP.switchScanMempool();
						configAPIP.writeConfigToFile();
					}
					log.debug("scanMempool: "+configAPIP.isScanMempool());
					break;
				case 6: //Set windowTime
					String windowTimeStr = jedis.get(RedisKeys.WindowTime);
					if(windowTimeStr==null) windowTimeStr="not set yet";
					System.out.println("WindowTime is "+windowTimeStr+". Input a long integer to set it in millisecond. Any other to cancel:");
					windowTimeStr = br.readLine();
					try{
						long windowTime = Long.parseLong(windowTimeStr);
						jedis.set(RedisKeys.WindowTime,windowTimeStr);
						log.debug("The windowTime was set to "+jedis.get(RedisKeys.WindowTime));
						br.readLine();
					}catch (Exception e){
						System.out.println("It's not a integer. ");
						break;
					}
					break;
				case 7: //Switch free get APIs
					boolean allowFreeGet = Boolean.parseBoolean(jedis.get(RedisKeys.AllowFreeGet));

					System.out.println("Allow free get: "+allowFreeGet+". Change it? 'y' to change, others to keep it:");

					String str = br.readLine();
					if(!"y".equals(str))break;

					if(allowFreeGet){
						jedis.set(RedisKeys.AllowFreeGet,"false");
						System.out.println("AllowFreeGet is false now.");
					}else{
						jedis.set(RedisKeys.AllowFreeGet,"true");
						System.out.println("AllowFreeGet is true now.");
					}

					br.readLine();
					break;
				case 8: //Find users
					findUsers(br);
					break;
				case 9:
					System.out.println("Anyone can send a freecash TX with following json in Op_Return to buy your service:" +
							"\n--------");
					String sidStr = new Jedis().get("service");
					if(sidStr==null){
						System.out.println("No service yet.");
						break;
					}
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					Service service = gson.fromJson(sidStr, Service.class);
					System.out.println(gson.toJson(Order.getJsonBuyOrder(service.getSid()))+
							"\n--------" +
							"\nMake sure the 'sid' is your service id. " +
							"\nAny key to continue...");
					br.readLine();
					break;
				case 10:
					System.out.println("----");
					for(Constant.Indices in : Constant.Indices.values()){
						System.out.println(in.sn()+". "+in.name());
					}
					System.out.println("----");
					br.readLine();
					break;
				case 11:
					configAPIP.config(br);
					break;
				case 0:
					if (esClient != null) newEsClient.shutdownClient();
					jedis.close();
					System.out.println("Exited, see you again.");
					end = true;
					break;
				default:
					break;
			}
		}
		newEsClient.shutdownClient();
		br.close();
	}

	private static void listIndices() {
		for (Constant.Indices value : Constant.Indices.values()) {
			System.out.println(value.sn()+". "+value.name());
		}
	}

	public static ElasticsearchClient getElasticsearchClient(BufferedReader br, ElasticsearchClient esClient, ConfigAPIP configAPIP, Jedis jedis) throws IOException {
		if(configAPIP.getEsUsername()==null){
			System.out.println("Create ES client without SSL? 'y' to confirm:");
			String input = br.readLine();
			if("y".equals(input)) {
				esClient = newEsClient.getClientHttp(configAPIP.getEsIp(), configAPIP.getEsPort());
			}else{
				System.out.println("Input ES username: ");
				input = br.readLine();
				configAPIP.setEsUsername(input);
			}
		}
		String password = null;
		if(esClient ==null){
			System.out.println("Input the password of " + configAPIP.getEsUsername()+" 'h' to create without SSL:");
			password = br.readLine();
			if("h".equals(password)){
				configAPIP.setEsUsername(null);
				configAPIP.writeConfigToFile();
				//configAPIP.copyConfigFileIntoTomcat();
				esClient = newEsClient.getClientHttp(configAPIP.getEsIp(), configAPIP.getEsPort());
			}else {
				try {
					esClient = newEsClient.getClientHttps(configAPIP.getEsIp(), configAPIP.getEsPort(), configAPIP.getEsUsername(), password);

					if (esClient != null) {
						setEncryptedEsPassword(password, configAPIP, jedis);
					}else{
						log.debug("Create SSL ES client failed. Check ES and Config.json.");
					}
				} catch (Exception e) {
					log.debug("Create SSL ES client failed. Check ES and Config.json.");
					e.printStackTrace();
				}
			}
		}
		return esClient;
	}

	public static void setEncryptedEsPassword(String password, ConfigAPIP configAPIP,Jedis jedis) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

		if(configAPIP.getRandomSymKeyHex()==null){
			configAPIP.setSymKey();
			configAPIP.writeConfigToFile();
		}

		String esPasswordCipher = Aes256CbcP7.encrypt(password,configAPIP.getRandomSymKeyHex());

		jedis.set(RedisKeys.EsPasswordCypher,esPasswordCipher);
		System.out.println("Your ES password is encrypted and saved locally.");
		log.debug("ES password is encrypted and saved locally.");
	}

	private static void setNPrices(Jedis jedis, BufferedReader br) throws IOException {
		Map<Integer, String> apiMap = loadAPIs();
		showAllAPIs(apiMap);
		while (true) {
			System.out.println("Input:" +
					"\n\t'a' to set all nPrices," +
					"\n\t'one' to set all nPrices by 1," +
					"\n\t'zero' to set all nPrices by 0," +
					"\n\tan integer to set the corresponding API," +
					"\n\tor 'q' to quit. ");
			String str = br.readLine();
			if ("".equals(str)) str = br.readLine();
			if (str.equals("q")) return;
			if (str.equals("a")) {
				setAllNPrices(apiMap, jedis, br);
				System.out.println("Done.");
				return;
			}
			if (str.equals("one")) {
				for (int i = 0; i < apiMap.size(); i++) {
					jedis.hset(RedisKeys.NPrice, apiMap.get(i + 1), "1");
				}
				System.out.println("Done.");
				return;
			}
			if (str.equals("zero")) {
				for (int i = 0; i < apiMap.size(); i++) {
					jedis.hset(RedisKeys.NPrice, apiMap.get(i + 1), "0");
				}
				System.out.println("Done.");
				return;
			}
			try {
				int i = Integer.parseInt(str);
				if (i > apiMap.size()) {
					System.out.println("The integer should be no bigger than " + apiMap.size());
					continue;
				} else {
					setNPrice(i, apiMap, jedis, br);
					System.out.println("Done.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Wrong input.");
			}
		}
	}

	private static void setAllNPrices(Map<Integer, String> apiMap, Jedis jedis, BufferedReader br) throws IOException {
		for (int i : apiMap.keySet()) {
			setNPrice(i, apiMap, jedis, br);
		}
	}

	private static void setNPrice(int i, Map<Integer, String> apiMap, Jedis jedis, BufferedReader br) throws IOException {
		String apiName = apiMap.get(i);
		while (true) {
			System.out.println("Input the multiple number of API " + apiName + ":");
			String str = br.readLine();
			try {
				int n = Integer.parseInt(str);
				jedis.hset(RedisKeys.NPrice, apiName, String.valueOf(n));
				return;
			} catch (Exception e) {
				System.out.println("Wong input.");
				return;
			}
		}
	}

	private static void showAllAPIs(Map<Integer, String> apiMap) {
		System.out.println("API list:");
		for (int i = 1; i <= apiMap.size(); i++) {
			System.out.println(i + ". " + apiMap.get(i));
		}
	}

	private static Map<Integer, String> loadAPIs() {

		ArrayList<String> apiList = Constant.apiList;


		Map<Integer, String> apiMap = new HashMap<Integer, String>();
		for (int i = 0; i < apiList.size(); i++) {
			apiMap.put(i + 1, apiList.get(i));
		}
		return apiMap;
	}

	private static void recreateOrderIndex(ElasticsearchClient esClient) throws InterruptedException {

		if(esClient==null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}
		try {
			DeleteIndexResponse req = esClient.indices().delete(c -> c.index("order"));

			if(req.acknowledged()) {
				log.debug("Index order was deleted.");
			}
		}catch(ElasticsearchException | IOException e) {
			log.debug("Deleting index order failed.",e);
		}

		TimeUnit.SECONDS.sleep(2);

		String orderJsonStr = "{\"mappings\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"fromAddr\":{\"type\":\"wildcard\"},\"toAddr\":{\"type\":\"wildcard\"},\"amount\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"txid\":{\"type\":\"keyword\"},\"txIndex\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"}}}}";
		InputStream orderJsonStrIs = new ByteArrayInputStream(orderJsonStr.getBytes());
		try {
			CreateIndexResponse req = esClient.indices().create(c -> c.index("order").withJson(orderJsonStrIs));
			orderJsonStrIs.close();
			System.out.println(req.toString());
			if(req.acknowledged()) {
				log.debug("Index order was created.");
			}else {
				log.debug("Creating index order failed.");
				return;
			}
		}catch(ElasticsearchException | IOException e) {
			log.debug("Creating index order failed.",e);
			return;
		}
	}

	public static Jedis getJedis(ConfigAPIP configAPIP, BufferedReader br) throws IOException {
		// TODO Auto-generated method stub

		if (configAPIP.getRedisPort() == 0 || configAPIP.getRedisIp() == null) configAPIP.setRedisIp(br);

		Jedis jedis = new Jedis(configAPIP.getRedisIp(), configAPIP.getRedisPort());
		//jedis.auth("xxxx");

		int count = 0;

		while(true) {
			try {
				String ping = jedis.ping();
				if (ping.equals("PONG")) {
					System.out.println("Redis is ready.");
					jedis.set("esIp", configAPIP.getEsIp());
					jedis.set("esPort", String.valueOf(configAPIP.getEsPort()));
					return jedis;
				}else {
					log.debug("Failed to startup redis.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			count++;
			if(count==3) {
				System.out.println("Check your redis server.");
				return null;
			}
			configAPIP.setRedisIp(br);
		}
	}

	public static void findUsers(BufferedReader br) throws IOException {
		System.out.println("Input user's fch address or session name. Press enter to list all users:");
		String str = br.readLine();

		Jedis jedis0Common = new Jedis();
		Jedis jedis1Session = new Jedis();
		jedis1Session.select(1);

		if("".equals(str)){
			Set<String> addrSet = jedis0Common.hkeys(RedisKeys.AddrSessionName);
			for(String addr: addrSet){
				UserAPIP user = getUser(addr,jedis0Common,jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}
		}else{
			if(jedis0Common.hget(RedisKeys.AddrSessionName,str)!=null){
				UserAPIP user = getUser(str, jedis0Common, jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}else if(jedis1Session.hgetAll(str)!=null){
				UserAPIP user = getUser(jedis1Session.hget(str,"addr"), jedis0Common, jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}
		}

		br.readLine();
	}

	private static UserAPIP getUser(String addr, Jedis jedis0Common, Jedis jedis1Session) {
		UserAPIP user = new UserAPIP();
		user.setAddress(addr);
		user.setBalance(jedis0Common.hget(RedisKeys.Balance,addr));
		String sessionName = jedis0Common.hget(RedisKeys.AddrSessionName,addr);
		user.setSessionName(sessionName);
		user.setSessionKey(jedis1Session.hget(sessionName,"sessionKey"));

		long timestamp = System.currentTimeMillis() + jedis1Session.expireTime(sessionName); // example timestamp in milliseconds
		Date date = new Date(timestamp); // create a new date object from the timestamp

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // define the date format
		String formattedDate = sdf.format(date); // format the date object to a string

		user.setExpireAt(formattedDate);

		return user;
	}
}

