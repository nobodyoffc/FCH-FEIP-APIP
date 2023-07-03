import api.Constant;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import fcTools.ParseTools;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import servers.ConfigBase;
import servers.NewEsClient;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class StartCidCashManager {

	private static final Logger log = LoggerFactory.getLogger(StartCidCashManager.class);

	static NewEsClient newEsClient = new NewEsClient();
	public static void main(String[] args) throws Exception {
		log.debug("FchParser is start...");
		ElasticsearchClient esClient = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ConfigBase configBase = new ConfigBase();

		boolean end = false;

		Jedis jedis = new Jedis("127.0.0.1",6379);

		if(jedis==null){
			log.debug("Redis is not ready.");
			br.readLine();
			return;
		}

		while(!end) {

			configBase= configBase.getClassInstanceFromFile(br, ConfigBase.class);
			if (configBase.getEsIp() == null ) configBase.config(br);
			esClient = newEsClient.checkEsClient(esClient, configBase);
			if (esClient == null) {
				log.debug("Creating ES client failed.");
				newEsClient.shutdownClient();
				return;
			}

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();
			menuItemList.add("Recreate Order Index");
			menuItemList.add("Set price");
			menuItemList.add("Set windowTime");
			menuItemList.add("Find Users");
			menuItemList.add("Config");

			menu.add(menuItemList);
			System.out.println(" << Cid.Cash manager>> \n");
			menu.show();
			int choice = menu.choose(br);

			switch(choice) {
				case 1:
					recreateOrderIndex(esClient);
					break;
				case 2:
					setNPrices(jedis, br);
					break;

				case 3:
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
				case 4:
					findUsers(br);
					break;

				case 5:
					configBase.config(br);
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

