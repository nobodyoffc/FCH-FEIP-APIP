package startAPIP;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.ConfigAPIP;
import constants.ApiNames;
import constants.IndicesNames;
import constants.Strings;
import fcTools.ParseTools;
import feipClass.Service;
import mempool.MempoolCleaner;
import mempool.MempoolScanner;
import menu.Menu;
import order.BalanceInfo;
import order.Order;
import order.OrderScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import servers.NewEsClient;
import service.ApipService;
import service.Managing;

import java.io.*;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static constants.Constants.*;
import static constants.Strings.*;


public class StartAPIP {

	private static final Logger log = LoggerFactory.getLogger(StartAPIP.class);
	private static final NewEsClient newEsClient = new NewEsClient();
	public static String serviceName;
	private static ElasticsearchClient esClient = null;
	static Jedis jedis = null;
	private static MempoolScanner mempoolScanner =null;
	private static OrderScanner orderScanner=null;
	private static MempoolCleaner mempoolCleaner=null;

	public static void main(String[] args)throws Exception{

		log.info("Start.");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		jedis = getJedis();

		ConfigAPIP configAPIP = getConfigApip(br,jedis);

		assert jedis != null;
		if(!jedis.exists(Strings.N_PRICE))setNPrices(jedis, br);

		String  orderMappingJsonStr = "{\"mappings\":{\"properties\":{\"amount\":{\"type\":\"long\"},\"cashId\":{\"type\":\"keyword\"},\"fromFid\":{\"type\":\"wildcard\"},\"height\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"toFid\":{\"type\":\"wildcard\"},\"txId\":{\"type\":\"keyword\"},\"txIndex\":{\"type\":\"long\"},\"txid\":{\"type\":\"keyword\"},\"via\":{\"type\":\"wildcard\"}}}}";
		String  balanceMappingJsonStr = "{\"mappings\":{\"properties\":{\"user\":{\"type\":\"text\"},\"consumeVia\":{\"type\":\"text\"},\"orderVia\":{\"type\":\"text\"},\"bestHeight\":{\"type\":\"keyword\"}}}}";

		while(true) {

			while (!configAPIP.loadConfigToRedis(jedis)) {
				configAPIP.config(br);
			}

			serviceName = configAPIP.getServiceName() + "_";

			if (esClient == null){
				esClient = newEsClient.getElasticSearchClient(br, configAPIP, jedis);
				startOrderScan(br, configAPIP,esClient);
				br.readLine();
				startMempoolScan(br,configAPIP,esClient);
			}
			checkServiceParams(br, esClient, configAPIP);

			String orderIndex = getIndexOfService(jedis, ORDER);
			if (noSuchIndex(esClient, orderIndex)) {
				createIndex(orderIndex,esClient,orderMappingJsonStr);
			}

			String balanceIndex = getIndexOfService(jedis, BALANCE);
			if (noSuchIndex(esClient, balanceIndex)) {
				createIndex(balanceIndex,esClient,balanceMappingJsonStr);
			}

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();
			menuItemList.add("Manage Service");
			menuItemList.add("Set windowTime");
			menuItemList.add("List APIs and Set nPrice");
			menuItemList.add("Set public sessionKey");
			menuItemList.add("List Indices in ES");
			menuItemList.add("Switch free get APIs");
			menuItemList.add("Find Users");
			menuItemList.add("Recreate order index");
			menuItemList.add("Recreate balance backup index");
			menuItemList.add("Backup user balance");
			menuItemList.add("Recover user balance");
			menuItemList.add("How to buy this service?");
			menuItemList.add("config");

			menu.add(menuItemList);
			System.out.println(" << " + configAPIP.getServiceName() + " manager>> \n");
			menu.show();
			int choice = menu.choose(br);
			switch (choice) {
				case 1 -> new Managing(serviceName, configAPIP).menu(esClient, br);
				case 2 -> setWindowTime(br, jedis, configAPIP);
				case 3 -> setNPrices(jedis, br);
				case 4 -> setPublicSessionKey(br, jedis);
				case 5 -> listIndices(br);
				case 6 -> switchForbidFreeGet(br, jedis, configAPIP);
				case 7 -> findUsers(br);
				case 8 -> reCreateOrderIndex(br, esClient, jedis, orderMappingJsonStr);
				case 9 -> recreateUserBalanceIndex(br, esClient, jedis,balanceMappingJsonStr);
				case 10 -> howToByService(br, jedis);
				case 11 -> BalanceInfo.backupUserBalanceToEs(esClient, jedis);
				case 12 -> BalanceInfo.recoverUserBalanceFromEs(esClient, jedis);
				case 13 -> configAndLoadToRedis(br, jedis, configAPIP);
				case 0 -> {
					newEsClient.shutdownClient();
					esClient.shutdown();
					if(mempoolScanner!=null)mempoolScanner.shutdown();
					if(orderScanner!=null)orderScanner.shutdown();
					if(mempoolCleaner!=null)mempoolCleaner.shutdown();
					jedis.close();
					System.out.println("Exited, see you again.");
					br.close();
					return;
				}
				default -> {}
			}
		}
	}

	private static void startMempoolClean(BufferedReader br,ConfigAPIP configAPIP) {
		mempoolCleaner = new MempoolCleaner(configAPIP.getBlockFilePath());
		log.debug("Clean mempool data in Redis...");
		Thread thread = new Thread(mempoolCleaner);
		thread.start();
	}

	private static void startOrderScan(BufferedReader br, ConfigAPIP configAPIP, ElasticsearchClient esClient) throws IOException {
		String input;
		System.out.println("Start order scanning? 'y' to start. Other to ignore");
		input = br.readLine();
		if("y".equals(input)) {
			log.debug("Start order scanner...");
			String listenPath = configAPIP.getListenPath();

			orderScanner = new OrderScanner(listenPath,esClient);
			Thread thread2 = new Thread(orderScanner);
			thread2.start();
			log.debug("Order scanner is running.");
		}
	}

	private static void startMempoolScan(BufferedReader br, ConfigAPIP configAPIP, ElasticsearchClient esClient) throws IOException {

		System.out.println("Start mempool scanning? 'y' to start. Other to ignore");
		String input = br.readLine();
		if("y".equals(input)) {
			startMempoolClean(br,configAPIP);

			mempoolScanner = new MempoolScanner(esClient);
			Thread thread1 = new Thread(mempoolScanner);
			thread1.start();
			log.debug("Mempool scanner is running.");
		}
	}

	private static void configAndLoadToRedis(BufferedReader br, Jedis jedis, ConfigAPIP configAPIP) throws IOException {
		configAPIP.config(br);
		configAPIP.loadConfigToRedis(jedis);
	}

	private static void reCreateOrderIndex(BufferedReader br, ElasticsearchClient esClient, Jedis jedis, String orderMappingJsonStr) throws InterruptedException, IOException {
		String index = getIndexOfService(jedis, ORDER);
		recreateIndex(index, esClient, orderMappingJsonStr);
		br.readLine();
	}

	private static void howToByService(BufferedReader br, Jedis jedis) throws IOException {
		System.out.println("Anyone can send a freecash TX with following json in Op_Return to buy your service:" +
				"\n--------");
		String sidStr = jedis.get(serviceName + SERVICE);
		if (sidStr == null) {
			System.out.println("No service yet.");
			return;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Service service = gson.fromJson(sidStr, Service.class);
		System.out.println(gson.toJson(Order.getJsonBuyOrder(service.getSid())) +
				"\n--------" +
				"\nMake sure the 'sid' is your service id. " +
				"\nAny key to continue...");
		br.readLine();
	}

	private static void switchForbidFreeGet(BufferedReader br, Jedis jedis, ConfigAPIP configAPIP) throws IOException {
		String freeGetForbidden;
		try {
			freeGetForbidden = jedis.hget(CONFIG, FORBID_FREE_GET);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Read forbidFreeGet failed.");
			return;
		}
		System.out.println("Forbid free get APIs: " + freeGetForbidden + ". Change it? 'y' to change, others to keep it:");
		String input = br.readLine();
		if (!("y".equals(input))) return;
		if (TRUE.equals(freeGetForbidden)) {
			jedis.hset(CONFIG, FORBID_FREE_GET, FALSE);
			configAPIP.setForbidFreeGet(false);
			System.out.println("ForbidFreeGet is false now.");
		} else if (FALSE.equals(freeGetForbidden)) {
			jedis.hset(CONFIG, FORBID_FREE_GET, TRUE);
			configAPIP.setForbidFreeGet(true);
			System.out.println("ForbidFreeGet is true now.");
		}
		configAPIP.writeConfigToFile();
		br.readLine();
	}

	private static void setPublicSessionKey(BufferedReader br, Jedis jedis) {
		setPublicSessionKey(jedis);
		String balance = jedis.hget(USER, PUBLIC);
		System.out.println("The balance of public session is: " + balance + ". Would you reset it? Input a number satoshi to set. Enter to skip.");
		while (true) {
			try {
				String num = br.readLine();
				Long.parseLong(num);
				jedis.hset(USER, PUBLIC, num);
				break;
			} catch (Exception ignore) {
				System.out.println("It's not a integer. Input again:");
			}
		}
	}

	private static void setWindowTime(BufferedReader br, Jedis jedis, ConfigAPIP configAPIP) throws IOException {
		String windowTimeStr = jedis.hget(CONFIG, Strings.WINDOW_TIME);
		if (windowTimeStr == null) {
			System.out.println("WindowTime is not set yet. Input a long integer to set it in millisecond. Any other to cancel:");
		}
		System.out.println("Input the windowTime: ");
		windowTimeStr = br.readLine();
		long windowTime;
		try {
			windowTime = Long.parseLong(windowTimeStr);
		} catch (Exception e) {
			System.out.println("It's not a integer. ");
			return;
		}
		jedis.hset(CONFIG, Strings.WINDOW_TIME, windowTimeStr);
		configAPIP.setWindowTime(windowTime);
		configAPIP.writeConfigToFile();
		log.debug("The windowTime was set to " + jedis.hget(CONFIG, Strings.WINDOW_TIME));
		br.readLine();
	}

	public static String getIndexOfService(Jedis jedis, String name) {
		return (jedis.hget(CONFIG,SERVICE_NAME)+"_"+name).toLowerCase();
	}

	private static boolean noSuchIndex(ElasticsearchClient esClient, String index) throws IOException {
		BooleanResponse result = esClient.indices().exists(e -> e.index(index));
		return !result.value();
	}

	private static Jedis getJedis() {
		Jedis jedis = new Jedis();
		try {
			jedis = new Jedis();
			if(!(jedis.ping().equals("PONG"))){
				log.debug("Redis is not ready.");
			}
		}catch (Exception e){
			e.printStackTrace();
			log.error("Create jedis wrong.");
		}
		return jedis;
	}

	private static void recreateUserBalanceIndex(BufferedReader br, ElasticsearchClient esClient, Jedis jedis, String balanceMappingJsonStr) throws IOException, InterruptedException {
		String index = getIndexOfService(jedis, BALANCE);
		recreateIndex(index, esClient,balanceMappingJsonStr);
		br.readLine();
	}

	private static void setPublicSessionKey(Jedis jedis) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		String sessionKey = HexFormat.of().formatHex(randomBytes);
		String oldSession = null;
		try{
			oldSession=jedis.hget(FID_SESSION_NAME,PUBLIC);
		}catch (Exception ignore){}

		jedis.hset(FID_SESSION_NAME,PUBLIC,sessionKey.substring(0,12));

		jedis.select(1);
		try{
			jedis.del(oldSession);
		}catch (Exception ignore){}

		jedis.hset(sessionKey.substring(0,12),SESSION_KEY,sessionKey);
		jedis.hset(sessionKey.substring(0,12),FID,PUBLIC);
		jedis.select(0);
		System.out.println("Public session key set into redis: "+sessionKey);
	}

	private static void checkServiceParams(BufferedReader br, ElasticsearchClient esClient, ConfigAPIP configAPIP) throws IOException {
		Jedis jedis = new Jedis();
		while(true) {
			if (jedis.hget(serviceName + Strings.PARAMS, Strings.ACCOUNT) == null) {
				System.out.println("Find your service and set it. ");
				Managing serviceManager= new Managing(serviceName,configAPIP );
				ApipService service = serviceManager.getService(jedis, esClient, br);
				if(service==null){
					serviceManager.publish(br,jedis);
				}
			}else break;
		}
	}

	private static ConfigAPIP getConfigApip(BufferedReader br,Jedis jedis) throws IOException {
		ConfigAPIP configApip = new ConfigAPIP();
		configApip = configApip.getClassInstanceFromFile(br, ConfigAPIP.class);
		if (configApip.getEsIp() == null) configAndLoadToRedis(br, jedis, configApip);
		return configApip;
	}

private static void listIndices(BufferedReader br) throws IOException {
	for (IndicesNames.Indices index : IndicesNames.Indices.values()) {
		System.out.println(index.sn()+". "+index.name().toLowerCase());
	}
	br.readLine();
}

	private static void setNPrices(Jedis jedis, BufferedReader br) throws IOException {
		Map<Integer, String> apiMap = loadAPIs();
		showAllAPIs(apiMap);
		while (true) {
			System.out.println("""
					Input:
					\t'a' to set all nPrices,
					\t'one' to set all nPrices by 1,
					\t'zero' to set all nPrices by 0,
					\tan integer to set the corresponding API,
					\tor 'q' to quit.\s""");
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
					jedis.hset(Strings.N_PRICE, apiMap.get(i + 1), "1");
				}
				System.out.println("Done.");
				return;
			}
			if (str.equals("zero")) {
				for (int i = 0; i < apiMap.size(); i++) {
					jedis.hset(Strings.N_PRICE, apiMap.get(i + 1), "0");
				}
				System.out.println("Done.");
				return;
			}
			try {
				int i = Integer.parseInt(str);
				if (i > apiMap.size()) {
					System.out.println("The integer should be no bigger than " + apiMap.size());
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
				jedis.hset(Strings.N_PRICE, apiName, String.valueOf(n));
				return;
			} catch (Exception e) {
				System.out.println("Wong input.");
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

		ArrayList<String> apiList = ApiNames.apiList;


		Map<Integer, String> apiMap = new HashMap<>();
		for (int i = 0; i < apiList.size(); i++) apiMap.put(i + 1, apiList.get(i));
		return apiMap;
	}

	private static void createIndex(String index, ElasticsearchClient esClient, String mappingJsonStr) {

		InputStream orderJsonStrIs = new ByteArrayInputStream(mappingJsonStr.getBytes());
		try {
			CreateIndexResponse req = esClient.indices().create(c -> c.index(index).withJson(orderJsonStrIs));
			orderJsonStrIs.close();
			System.out.println(req.toString());
			if(req.acknowledged()) {
				log.debug("Index {} was created.", index);
			}else {
				log.debug("Creating index {} failed.", index);
			}
		}catch(ElasticsearchException | IOException e) {
			log.debug("Creating index {} failed.", index,e);
		}
	}

	private static void recreateIndex(String index, ElasticsearchClient esClient, String mappingJsonStr) throws InterruptedException {

		if(esClient==null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}
		try {
			DeleteIndexResponse req = esClient.indices().delete(c -> c.index(index));

			if(req.acknowledged()) {
				log.debug("Index {} was deleted.", index);
			}
		}catch(ElasticsearchException | IOException e) {
			log.debug("Deleting index {} failed.", index,e);
		}

		TimeUnit.SECONDS.sleep(2);

		createIndex(index,esClient,mappingJsonStr);
	}

	public static void findUsers(BufferedReader br) throws IOException {
		System.out.println("Input user's fch address or session name. Press enter to list all users:");
		String str = br.readLine();

		Jedis jedis0Common = new Jedis();
		Jedis jedis1Session = new Jedis();

		jedis1Session.select(1);

		if("".equals(str)){
			Set<String> addrSet = jedis0Common.hkeys(Strings.FID_SESSION_NAME);
			for(String addr: addrSet){
				UserAPIP user = getUser(addr,jedis0Common,jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}
		}else{
			if(jedis0Common.hget(Strings.FID_SESSION_NAME,str)!=null){
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
		user.setBalance(jedis0Common.hget(Strings.USER,addr));
		String sessionName = jedis0Common.hget(Strings.FID_SESSION_NAME,addr);
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

