package startAPIP;

import balance.BalanceManager;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import config.ConfigAPIP;
import constants.Strings;
import esTools.NewEsClient;
import mempool.MempoolCleaner;
import mempool.MempoolScanner;
import appUtils.Menu;
import order.OrderManager;
import order.OrderScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import reward.RewardManager;
import config.RewardParams;
import reward.Rewarder;
import service.ApipService;
import service.Params;
import service.ServiceManager;
import webhook.Pusher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static constants.Values.FALSE;
import static constants.Values.TRUE;
import static constants.Strings.*;

public class StartAPIP {

	private static final Logger log = LoggerFactory.getLogger(StartAPIP.class);
	public static String serviceName;

	public static ApipService service;
	private static ElasticsearchClient esClient = null;
	private static MempoolScanner mempoolScanner =null;
	private static OrderScanner orderScanner=null;
	private static Pusher pusher = null;
	private static MempoolCleaner mempoolCleaner=null;
	private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static IndicesAPIP indicesAPIP;
	public static JedisPool jedisPool;


	public static void main(String[] args)throws Exception{

		log.info("Start.");
		Gson gson = new Gson();

		NewEsClient newEsClient = new NewEsClient();
		jedisPool = new JedisPool();

		ConfigAPIP configAPIP = new ConfigAPIP();
		configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);

		while(true) {

			try(Jedis jedis = jedisPool.getResource()){
				while (!configAPIP.loadConfigToRedis()) {
					configAPIP.config(br);
				}

				if (esClient == null){
					esClient = newEsClient.getElasticSearchClient(br, configAPIP, jedis);
				}

				serviceName = jedis.hget(CONFIG,SERVICE_NAME);
				try {
					service = gson.fromJson(jedis.get(serviceName + "_" + SERVICE), ApipService.class);
				}catch (Exception ignore){}

				if(configAPIP.getServiceName()==null||service==null||jedis.hget(CONFIG,SERVICE_NAME)==null){
					System.out.println("No service yet. Find your service.");
					boolean done = new ServiceManager(esClient,br,configAPIP).findService();
					if(!done){
						newEsClient.shutdownClient();
						if (mempoolScanner != null) mempoolScanner.shutdown();
						if (orderScanner != null) orderScanner.shutdown();
						if (mempoolCleaner != null) mempoolCleaner.shutdown();
						jedisPool.clear();
						jedisPool.close();
						br.close();
						return;
					}
				}else freshServiceFromEsToRedis(service,esClient,jedis,configAPIP);

				if(!jedis.exists(serviceName+"_"+Strings.N_PRICE)) Settings.setNPrices(br);

				indicesAPIP = new IndicesAPIP(esClient,br);
				indicesAPIP.checkApipIndices();
				indicesAPIP.checkSwapIndices();
			}

			if(orderScanner==null) startOrderScan(configAPIP, esClient);

			if(mempoolScanner==null) startMempoolScan(configAPIP, esClient);

			if(pusher == null) startPusher(configAPIP, esClient);

			checkServiceParams();

			checkPublicSessionKey();

			checkRewardParams();

			System.out.println();
			if(orderScanner!=null && orderScanner.isRunning().get()) System.out.println("Order scanner is running...");
			if(mempoolScanner!=null && mempoolScanner.getRunning().get()) System.out.println("Mempool scanner is running...");
			if(mempoolScanner!=null && mempoolCleaner.getRunning().get()) System.out.println("Mempool cleaner is running...");
			if(pusher!=null && pusher.isRunning().get()) System.out.println("Webhook pusher is running.");
			System.out.println();

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();

			menuItemList.add("Manage service");
			menuItemList.add("Manage order");
			menuItemList.add("Manage balance");
			menuItemList.add("Manage reward");
			menuItemList.add("Manage indices");
			menuItemList.add("Settings");

			menu.add(menuItemList);
			System.out.println(" << " + configAPIP.getServiceName() + " manager>> \n");
			menu.show();

			int choice = menu.choose(br);
			switch (choice) {
				case 1 -> new ServiceManager(esClient, br, configAPIP).menu();
				case 2 -> new OrderManager(esClient, br,orderScanner).menu();
				case 3 -> new BalanceManager(esClient, br).menu();
				case 4 -> manageReward(esClient);
				case 5 -> manageIndices();
				case 6 -> new Settings(br, configAPIP).menu();
				case 0 -> {
					if(orderScanner!=null && orderScanner.isRunning().get()) System.out.println("Order scanner is running.");
					if(mempoolScanner!=null && mempoolScanner.getRunning().get()) System.out.println("Mempool scanner is running.");
					if(mempoolCleaner!=null && mempoolCleaner.getRunning().get()) System.out.println("Mempool cleaner is running.");
					if(pusher!=null && pusher.isRunning().get()) System.out.println("Webhook pusher is running.");
					System.out.println("Do you want to quit? 'q' to quit.");
					String input = br.readLine();
					if("q".equals(input)) {
						if (mempoolScanner != null) mempoolScanner.shutdown();
						if (orderScanner != null) orderScanner.shutdown();
						if (mempoolCleaner != null) mempoolCleaner.shutdown();
						if (pusher!=null)pusher.shutdown();
						br.close();
						if(orderScanner==null ||!orderScanner.isRunning().get()) System.out.println("Order scanner is set to stop.");
						if(mempoolScanner==null|| !mempoolScanner.getRunning().get()) System.out.println("Mempool scanner is set to stop.");
						if(mempoolCleaner==null|| !mempoolCleaner.getRunning().get()) System.out.println("Mempool cleaner is set to stop.");
						if(pusher==null ||!pusher.isRunning().get()) System.out.println("Webhook pusher is set to stop.");
						System.out.println("Exited, see you again.");
						System.exit(0);
						return;
					}
				}
				default -> {}
			}
		}
	}

	private static void freshServiceFromEsToRedis(ApipService service, ElasticsearchClient esClient, Jedis jedis, ConfigAPIP configAPIP) {
		ApipService serviceNew = getServiceFromEsById(esClient,service.getSid());
		if(serviceNew==null)return;

		if(!serviceNew.getStdName().equals(service.getStdName())) {
			updateAllServiceNameInRedis(jedis, service.getStdName(),serviceNew.getStdName());
		}
		serviceName= serviceNew.getStdName();
		StartAPIP.service = serviceNew;
		updateServiceParamsInRedisAndConfig(configAPIP);
		setServiceToRedis(serviceName);
	}

	private static ApipService getServiceFromEsById(ElasticsearchClient esClient, String sid) {
		ApipService serviceNew = null;
		try {
			serviceNew = esClient.get(g -> g.index(SERVICE).id(sid), ApipService.class).source();
		} catch (IOException e) {
			log.error("Get service from Es wrong. Check Es.");
		}
		return serviceNew;
	}

	private static final String[] PARAMS_NAMES_IN_REDIS = {
			PARAMS_ON_CHAIN,
			CONSUME_VIA,
			FID_SESSION_NAME,
			FID_BALANCE,
			N_PRICE,
			ORDER_LAST_HEIGHT,
			BUILDER_SHARE_MAP,
			ORDER_LAST_BLOCK_ID,
			SERVICE
	};

	private static void updateAllServiceNameInRedis(Jedis jedis, String oldName, String newName) {
		for(String suffix : PARAMS_NAMES_IN_REDIS) {
			renameKey(jedis, oldName, newName, suffix);
		}
	}

	private static void renameKey(Jedis jedis, String oldName, String newName, String suffix) {
		try {
			jedis.rename(oldName + "_" + suffix, newName + "_" + suffix);
		}catch (Exception ignore){
			System.out.println(oldName +" no found.");
		}
	}


	private static void checkRewardParams() {
		RewardParams rewardParams = Rewarder.getRewardParams();
		if (rewardParams == null) {
			System.out.println("Reward parameters aren't set yet.");
			new Rewarder(esClient).setRewardParameters( br);
			Menu.anyKeyToContinue(br);
		}
	}

	private static void manageReward(ElasticsearchClient esClient) {
		RewardManager rewardManager = new RewardManager(esClient, StartAPIP.br);
		rewardManager.menu();
	}

	private static void checkPublicSessionKey() throws IOException {
		try(Jedis jedis = jedisPool.getResource()) {
			if (jedis.hget(serviceName + "_" + FID_SESSION_NAME, PUBLIC) == null) {
				System.out.println("Public sessionKey for getFreeService API is null. Set it? 'y' to set.");
				String input = StartAPIP.br.readLine();
				if ("y".equals(input)) {
					Settings.setPublicSessionKey(StartAPIP.br);
				}
			}
		}
	}

	private static void manageIndices() throws IOException, InterruptedException {
		indicesAPIP.menu();
	}

	private static void startMempoolClean(ConfigAPIP configAPIP, ElasticsearchClient esClient) {
		mempoolCleaner = new MempoolCleaner(configAPIP.getBlockFilePath(), esClient);
		log.debug("Clean mempool data in Redis...");
		Thread thread = new Thread(mempoolCleaner);
		thread.start();
	}

	private static void startOrderScan(ConfigAPIP configAPIP, ElasticsearchClient esClient) throws IOException {
//		String input;
//		System.out.println("Start order scanning? 'y' to start. Other to ignore");
//		input = StartAPIP.br.readLine();
//		if("y".equals(input)) {
		log.debug("Start order scanner...");
		String listenPath = configAPIP.getListenPath();

		orderScanner = new OrderScanner(listenPath,esClient);
		Thread thread2 = new Thread(orderScanner);
		thread2.start();
		log.debug("Order scanner is running.");
//		}
	}

	private static void startPusher(ConfigAPIP configAPIP, ElasticsearchClient esClient) throws IOException {
		String listenPath = configAPIP.getListenPath();

		pusher = new Pusher(listenPath, esClient);
		Thread thread3 = new Thread(pusher);
		thread3.start();

		log.debug("Webhook pusher is running.");
	}

	private static void startMempoolScan(ConfigAPIP configAPIP, ElasticsearchClient esClient) throws IOException {

//		System.out.println("Start mempool scanning? 'y' to start. Other to ignore");
//		String input = StartAPIP.br.readLine();
//		if("y".equals(input)) {
		startMempoolClean(configAPIP,esClient);

		mempoolScanner = new MempoolScanner(esClient);
		Thread thread1 = new Thread(mempoolScanner);
		thread1.start();
		log.debug("Mempool scanner is running.");
//		}
	}

	public static String getNameOfService(String name) {
		String finalName;
		try(Jedis jedis = StartAPIP.jedisPool.getResource()) {
			finalName = (jedis.hget(CONFIG, SERVICE_NAME) + "_" + name).toLowerCase();
		}
		return finalName;
	}

	private static void checkServiceParams() {
		try(Jedis jedis = jedisPool.getResource()) {
			if (jedis.hget(serviceName + "_" + Strings.PARAMS_ON_CHAIN, Strings.ACCOUNT) == null) {
				writeParamsToRedis();
				Menu.anyKeyToContinue(br);
			}
		}
	}

	private static void writeParamsToRedis() {

		Params params = service.getParams();
		try(Jedis jedis = jedisPool.getResource()) {
			jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, ACCOUNT, params.getAccount());
			jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, CURRENCY, params.getCurrency());
			jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, URL_HEAD, params.getUrlHead());
			if (params.getMinPayment() != null)
				jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, MIN_PAYMENT, params.getMinPayment());
			if (params.getPricePerKBytes() != null)
				jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, PRICE_PER_K_BYTES, params.getPricePerKBytes());
			if (params.getPricePerRequest() != null)
				jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, PRICE_PER_REQUEST, params.getPricePerRequest());
			if (params.getSessionDays() != null)
				jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, SESSION_DAYS, params.getSessionDays());
			if (params.getConsumeViaShare() != null)
				jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, CONSUME_VIA_SHARE, params.getConsumeViaShare());
			if (params.getOrderViaShare() != null)
				jedis.hset(StartAPIP.serviceName + "_" + PARAMS_ON_CHAIN, ORDER_VIA_SHARE, params.getOrderViaShare());

			if (params.getPricePerKBytes() == null || "0".equals(params.getPricePerKBytes())) {
				jedis.hset(CONFIG, PRICE, params.getPricePerRequest());
				jedis.hset(CONFIG, IS_PRICE_PER_REQUEST, FALSE);
			} else {
				jedis.hset(CONFIG, PRICE, params.getPricePerKBytes());
				jedis.hset(CONFIG, IS_PRICE_PER_REQUEST, TRUE);
			}
		}
		System.out.println("Service parameters has been wrote into redis.");
	}

	public static void updateServiceParamsInRedisAndConfig( ConfigAPIP configAPIP){
		serviceName = service.getStdName();

		setServiceToRedis(serviceName);
		writeParamsToRedis();

		configAPIP.setServiceName(serviceName);
		configAPIP.writeConfigToFile();
	}

	public static void setServiceToRedis(String serviceName) {
		Gson gson = new Gson();
		try(Jedis jedis = StartAPIP.jedisPool.getResource()) {
			jedis.set(serviceName + "_" + SERVICE, gson.toJson(service));
			jedis.hset(CONFIG, SERVICE_NAME, serviceName);
		}
	}
}

