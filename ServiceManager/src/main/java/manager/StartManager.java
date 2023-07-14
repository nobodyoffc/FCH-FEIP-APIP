package manager;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.ConfigService;
import constants.ApiNames;
import constants.Constants;
import constants.IndicesNames;
import constants.Strings;
import fcTools.ParseTools;
import feipClass.Service;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redisTools.WriteRedis;
import servers.NewEsClient;
import servers.NewRedisClient;
import service.Managing;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class StartManager {

	private static final Logger log = LoggerFactory.getLogger(StartManager.class);
	private static final NewEsClient newEsClient = new NewEsClient();
	public static String SERVICE_NAME;

	public static void main(String[] args)throws Exception{

		log.info("Service manager start.");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		boolean end = false;
		ElasticsearchClient esClient = null;

		ConfigService configService = getConfigService(br);

		Jedis jedis = getJedis(br, configService);
		if (jedis == null) return;

		checkServiceName(br, jedis);

		String forbidFreeGetStr = jedis.get(SERVICE_NAME + Strings.FORBID_FREE_GET);
		if(forbidFreeGetStr==null)jedis.set(SERVICE_NAME + Strings.FORBID_FREE_GET,"false");

		if(jedis.hget(SERVICE_NAME + Strings.PARAMS, Strings.ACCOUNT)==null)setServiceParams(br, jedis);

		if(jedis.get(SERVICE_NAME + Strings.LISTEN_PATH)==null)setListenPath(br, jedis,configService);

		String windowTimeStr = jedis.get(SERVICE_NAME + Strings.WINDOW_TIME);
		if(windowTimeStr==null) {
			windowTimeStr="not set yet";
			setWindowTime(br, jedis,windowTimeStr);
		}

		while(!end) {
			if(esClient==null)esClient = newEsClient.getElasticSearchClient(br, esClient, configService, jedis);

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();
			menuItemList.add("Manage Service");
			menuItemList.add("Create Order Index");
			menuItemList.add("List APIs and Set nPrice");
			menuItemList.add("Set windowTime");
			menuItemList.add("Switch free get APIs");
			menuItemList.add("Find Users");
			menuItemList.add("How to buy this service?");
			menuItemList.add("Create ES index for Balance backup");
			menuItemList.add("Delete Order Index");
			menuItemList.add("config");

			menu.add(menuItemList);
			System.out.println(" << "+ SERVICE_NAME +" manager>> \n");
			menu.show();
			int choice = menu.choose(br);
			String mappingJsonStr;
			switch(choice) {
				case 1: //Manage service
					Managing serviceManager= new Managing();
					serviceManager.menu(esClient, br,jedis);
					break;
				case 2:
					order.Order.createOrderIndex(esClient, SERVICE_NAME + Constants.ORDER);
					break;

				case 3:
					setNPrices(jedis, br);
					break;

				case 4: //Set windowTime
					windowTimeStr = jedis.get(SERVICE_NAME + Strings.WINDOW_TIME);
					if(windowTimeStr==null) windowTimeStr="not set yet";
					setWindowTime(br, jedis,windowTimeStr);
					break;
				case 5: //Switch free get APIs

					boolean freeGetForbidden = Boolean.parseBoolean(forbidFreeGetStr);

					System.out.println("Forbid free get: "+freeGetForbidden+". Change it? 'y' to change, others to keep it:");

					String str = br.readLine();
					if(!"y".equals(str))break;

					if(freeGetForbidden){
						jedis.set(SERVICE_NAME + Strings.FORBID_FREE_GET,"false");
						System.out.println("ForbidFreeGet is false now.");
					}else{
						jedis.set(SERVICE_NAME + Strings.FORBID_FREE_GET,"true");
						System.out.println("ForbidFreeGet is true now.");
					}

					br.readLine();
					break;
				case 6: //Find users
					findUsers(br);
					break;
				case 7:
					String sidStr = jedis.get(SERVICE_NAME + Strings.SERVICE_OBJECT);
					if(sidStr==null){
						System.out.println("No service yet.");
						break;
					}
					System.out.println("Anyone can send a freecash TX with following json in Op_Return to buy your service:" +
							"\n--------");
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					Service service = gson.fromJson(sidStr, Service.class);
					System.out.println(gson.toJson(order.Order.getJsonBuyOrder(service.getSid()))+
							"\n--------" +
							"\nMake sure the 'sid' is your service id. " +
							"\nAny key to continue...");
					br.readLine();
					break;

				case 8:
					System.out.println("Create balance index for "+ SERVICE_NAME +". 'y' to recreat. Enter to ignore :");
					String input = br.readLine();
					if("y".equals(input)){
						try {
							CreateIndexResponse req = esClient.indices().create(c -> c.index(SERVICE_NAME + Strings.BALANCE));

							if(req.acknowledged()) {
								log.debug("Index {} was created.", SERVICE_NAME + Strings.BALANCE);
							}else {
								log.debug("Creating index {} failed.", SERVICE_NAME + Strings.BALANCE);
								return;
							}
						}catch(ElasticsearchException | IOException e) {
							log.debug("Creating index {} failed.", SERVICE_NAME + Strings.BALANCE,e);
							return;
						}
					}else{
						System.out.println("Ignored.");
					}
					break;
				case 9:
					order.Order.deleteOrderIndex(esClient, SERVICE_NAME + Constants.ORDER);
					break;
				case 10:
					configService.config(br);
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

	private static void setListenPath(BufferedReader br, Jedis jedis, ConfigService configService) throws IOException {

		String input;
		while(true) {
			System.out.println("Input the path to be listen when scanning new order. " +
					"\n'b' for block directory. " +
					"\n'o' for opreturn directory. " +
					"\nEnter to ignore:");
			input = br.readLine();
			if ("".equals(input)) return;
			if("b".equals(input)){
				jedis.set(SERVICE_NAME + Strings.LISTEN_PATH,configService.getBlockFilePath());
				return;
			}
			if("o".equals(input)){
				jedis.set(SERVICE_NAME + Strings.LISTEN_PATH,configService.getOpReturnFilePath());
				return;
			}

			File listenDir = new File(input);
			if (listenDir.isDirectory() && listenDir.exists()) {
				if (!input.endsWith("/")) input = input + "/";
				jedis.set(SERVICE_NAME + Strings.LISTEN_PATH,input);
				return;
			}else {
				System.out.println("This directory does not exist. Any key to input again.");
				br.readLine();
			}
		}
	}

	private static void setWindowTime(BufferedReader br, Jedis jedis, String windowTimeStr) throws IOException {

		System.out.println("WindowTime is "+windowTimeStr+". Input a long integer to set it in millisecond. Any other to cancel:");
		windowTimeStr = br.readLine();
		try{
			Long.parseLong(windowTimeStr);
			jedis.set(SERVICE_NAME + Strings.WINDOW_TIME,windowTimeStr);
			System.out.println("The windowTime was set to "+ jedis.get(SERVICE_NAME + Strings.WINDOW_TIME));
			log.debug("The windowTime was set to "+ jedis.get(SERVICE_NAME + Strings.WINDOW_TIME));
			br.readLine();
		}catch (Exception e){
			System.out.println("It's not a long integer. ");
		}
	}

	private static void setServiceParams(BufferedReader br, Jedis jedis) throws IOException {

		WriteRedis.setFid(SERVICE_NAME+ Strings.PARAMS, Strings.ACCOUNT,br,jedis);

		WriteRedis.setNumber(SERVICE_NAME+ Strings.PARAMS, Strings.MIN_PAYMENT,  br,jedis);

		WriteRedis.setNumber(SERVICE_NAME+ Strings.PARAMS, Strings.PRICE_PER_K_BYTES,  br,jedis);

		WriteRedis.setNumber(SERVICE_NAME+ Strings.PARAMS, Strings.PRICE_PER_REQUEST, br,jedis);
	}

	private static void checkServiceName(BufferedReader br, Jedis jedis) throws IOException {
		SERVICE_NAME = jedis.get(Strings.SERVICE_NAME);
		if(SERVICE_NAME==null){
			System.out.println("Set your service name: ");
			String input = br.readLine();
			if (!"".equals(input)) {
				SERVICE_NAME = input+"_";
				jedis.set(Strings.SERVICE_NAME,SERVICE_NAME);
			}
		}
	}

	private static Jedis getJedis(BufferedReader br, ConfigService configService) throws IOException {
		Jedis jedis = NewRedisClient.getJedis(configService, br);
		if(jedis==null){
			log.debug("Redis is not ready.");
			br.readLine();
			return null;
		}
		return jedis;
	}

	private static ConfigService getConfigService(BufferedReader br) throws IOException {
		ConfigService configService = new ConfigService();
		configService.setCurrentPathAsConfigFilePath();
		configService = configService.getClassInstanceFromFile(br, ConfigService.class);
		if (configService.getEsIp() == null) configService.config(br);
		return configService;
	}

	private static void listIndices() {
		for (IndicesNames.Indices value : IndicesNames.Indices.values()) {
			System.out.println(value.sn()+". "+value.name());
		}
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
					jedis.hset(SERVICE_NAME + Strings.N_PRICE, apiMap.get(i + 1), "1");
				}
				System.out.println("Done.");
				return;
			}
			if (str.equals("zero")) {
				for (int i = 0; i < apiMap.size(); i++) {
					jedis.hset(SERVICE_NAME + Strings.N_PRICE, apiMap.get(i + 1), "0");
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
			System.out.println("Input the multiple number of the API price" + apiName + ":");
			String str = br.readLine();
			try {
				int n = Integer.parseInt(str);
				jedis.hset(SERVICE_NAME + Strings.N_PRICE, apiName, String.valueOf(n));
				return;
			} catch (Exception e) {
				System.out.println("Wrong input.");
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

		ArrayList<String> apiList = ApiNames.apiList;

		Map<Integer, String> apiMap = new HashMap<Integer, String>();
		for (int i = 0; i < apiList.size(); i++) {
			apiMap.put(i + 1, apiList.get(i));
		}
		return apiMap;
	}

	private static void recreateOrderIndex(ElasticsearchClient esClient,String index,String mappingJsonStr) throws InterruptedException {

		if(esClient==null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}
		try {
			DeleteIndexResponse req = esClient.indices().delete(c -> c.index(index));

			if(req.acknowledged()) {
				log.debug("Index {} was deleted.",index);
			}
		}catch(ElasticsearchException | IOException e) {
			log.debug("Deleting index {} failed.",index,e);
		}

		TimeUnit.SECONDS.sleep(2);

		InputStream orderJsonStrIs = new ByteArrayInputStream(mappingJsonStr.getBytes());
		try {
			CreateIndexResponse req = esClient.indices().create(c -> c.index(index).withJson(orderJsonStrIs));
			orderJsonStrIs.close();
			System.out.println(req.toString());
			if(req.acknowledged()) {
				log.debug("Index {} was created.",index);
			}else {
				log.debug("Creating index {} failed.",index);
				return;
			}
		}catch(ElasticsearchException | IOException e) {
			log.debug("Creating index {} failed.",index,e);
			return;
		}
	}

	public static void findUsers(BufferedReader br) throws IOException {
		System.out.println("Input user's fch address or session name. Press enter to list all users:");
		String str = br.readLine();

		Jedis jedis0Common = new Jedis();
		Jedis jedis1Session = new Jedis();
		jedis1Session.select(1);

		if("".equals(str)){
			Set<String> addrSet = jedis0Common.hkeys(SERVICE_NAME + Strings.ADDR_SESSION_NAME);
			for(String addr: addrSet){
				User user = getUser(addr,jedis0Common,jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}
		}else{
			if(jedis0Common.hget(SERVICE_NAME + Strings.ADDR_SESSION_NAME,str)!=null){
				User user = getUser(str, jedis0Common, jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}else if(jedis1Session.hgetAll(str)!=null){
				User user = getUser(jedis1Session.hget(str,"addr"), jedis0Common, jedis1Session);
				System.out.println(ParseTools.gsonString(user));
			}
		}

		br.readLine();
	}

	private static User getUser(String addr, Jedis jedis0Common, Jedis jedis1Session) {
		User user = new User();
		user.setAddress(addr);
		user.setBalance(jedis0Common.hget(SERVICE_NAME + Strings.BALANCE,addr));
		String sessionName = jedis0Common.hget(SERVICE_NAME + Strings.ADDR_SESSION_NAME,addr);
		user.setSessionName(sessionName);
		user.setSessionKey(jedis1Session.hget(sessionName,"sessionKey"));

		long timestamp = System.currentTimeMillis() + jedis1Session.expireTime(sessionName); // example timestamp in milliseconds
		Date date = new Date(timestamp); // create a new date object from the timestamp

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // define the date format
		String formattedDate = sdf.format(date); // format the date object to a string

		user.setExpireAt(formattedDate);

		return user;
	}

	public static boolean isFreeGetForbidden(PrintWriter writer) {
		Jedis jedis = new Jedis();
		boolean forbidFreeGet = Boolean.parseBoolean(jedis.get(SERVICE_NAME + Strings.FORBID_FREE_GET));
		if(forbidFreeGet){
			writer.write("Sorry, the freeGet APIs were closed.");
			jedis.close();
			return true;
		}
		jedis.close();
		return false;
	}
}

