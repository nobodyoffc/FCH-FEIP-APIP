package service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.ConfigAPIP;
import constants.FieldNames;
import constants.Strings;
import fcTools.ParseTools;
import keyTools.KeyTools;
import menu.Menu;
import redis.clients.jedis.Jedis;
import redisTools.GetJedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static constants.Strings.*;


public class Managing {


	ApipService service;
	Params params = new Params();
	String serviceName;
	Jedis jedis = new Jedis();

	BufferedReader br;

	public Managing(String serviceName, ConfigAPIP configAPIP) throws IOException {
		service = checkService();
		this.serviceName=serviceName;
		if(service!=null) {
			params = service.getParams();
		}
	}

	public void menu(ElasticsearchClient esClient, BufferedReader br) throws IOException {

		this.br=br;
        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();
		menuItemList.add("Publish New Service");
        menuItemList.add("Find my service");
		menuItemList.add("Show my service");
        menuItemList.add("Update Existed Service");
        menuItemList.add("Reload service to redis");
        menuItemList.add("Stop Existed Service");
        menuItemList.add("Recover Stopped Service");
        menuItemList.add("Close Service Permanently");

        menu.add(menuItemList);
        menu.show();

        int choice = menu.choose(br);

		switch(choice) {
			case 1:
				publish(br, jedis);
				break;
			case 2:
				service = getService(jedis,esClient,br);
				params = service.getParams();
				break;
			case 3:
				if(jedis.get("service")==null){
					System.out.println("No service set yet.");
					break;
				}
				Gson gson = new Gson();
				ApipService service1 = gson.fromJson(jedis.get("service"), ApipService.class);
				ParseTools.gsonPrint(service1);
				br.readLine();
				break;
			case 4:
				update(esClient,br, jedis);
				break;
			case 5:
				reloadService(esClient,jedis);
				br.readLine();
				break;
			case 6:
				stop(esClient, br);
				break;
			case 7:
				recover(esClient, br);
				break;
			case 8:
				System.out.println("Do you really want to give up the service forever? y or n:");
				String delete = br.readLine();
				if (delete.equals("y")) {
					close(esClient, br);
				}
				break;
			case 0:
				return;
			default:
				break;
		}
	}

	private void reloadService(ElasticsearchClient esClient, Jedis jedis) throws IOException {
		String serviceStr = jedis.get("service");
		if(serviceStr==null){
			System.out.println("Service isn't set yet.");
		}else {
			service = new Gson().fromJson(serviceStr, ApipService.class);
			GetResponse<ApipService> r = esClient.get(g -> g.index("service").id(service.getSid()), ApipService.class);
			if(r.source()!=null) {
				String serviceJson = new Gson().toJson(r.source());
				jedis.set("service", serviceJson);
				jedis.set("sid", service.getSid());
				params = service.getParams();
				System.out.println("Service " + service.getSid() + " reloaded.");
			}else{
				System.out.println("No service found.");
			}
		}
	}

	public ApipService getService(Jedis jedis, ElasticsearchClient esClient, BufferedReader br) throws IOException {
		Gson gson = new Gson();
		String str;
		while(true) {
			System.out.println("Input the fch address of the owner:");
			str = br.readLine();
			if (str.equals("")) return null;
			try{
				if(!KeyTools.isValidFchAddr(str)) {
					System.out.println("It's not a valid Freecash address.");
				}else break;
			}catch (Exception e){
				System.out.println("Invalid input. Try again.");
			}
		}

		String finalStr = str;
		SearchResponse<ApipService> result = esClient.search(s -> s.index(SERVICE).query(q -> q.term(t -> t.field(FieldNames.OWNER).value(finalStr))), ApipService.class);

		List<Hit<ApipService>> hitList = result.hits().hits();
        ArrayList<ApipService> serviceList = new ArrayList<ApipService>();
        for(Hit<ApipService> hit:hitList){
			ApipService s = hit.source();
			if(s.isClosed())continue;
            serviceList.add(s);
        }
        int size = serviceList.size();
        if(size==0){
            System.out.println("No service found under this owner.");
			br.readLine();
            return null;
        }
        for(int i = 0;i<size;i++){
            service = serviceList.get(i);
            System.out.println((i+1) +". service \nname: "+ service.getStdName()+"\nsid: "+service.getSid());
        }
        if(size==1){
			writeParamsToRedis(jedis, br, gson);
			return service;
        }

		int choice = 0;
		while (true) {
			String input = br.readLine();
			try {
				choice = Integer.parseInt(input);
				break;
			}catch (Exception e){
				System.out.println("Input a integer please:");
			}
        }

        service= serviceList.get(choice-1);
        System.out.println(choice +". service name: "+ service.getStdName()+"sid: "+service.getSid());
        writeParamsToRedis(jedis,br,gson);
        return service;
	}

	private void writeParamsToRedis(Jedis jedis, BufferedReader br, Gson gson) throws IOException {
		jedis.set(serviceName+ Strings.SERVICE, gson.toJson(service));

		jedis.set(serviceName+ Strings.SID,service.getSid());
		params=service.getParams();
		jedis.hset(serviceName+ PARAMS,ACCOUNT,params.getAccount());
		jedis.hset(serviceName+ PARAMS,CURRENCY,params.getCurrency());
		jedis.hset(serviceName+ PARAMS,MIN_PAYMENT,params.getMinPayment());
		jedis.hset(serviceName+ PARAMS,PRICE_PER_REQUEST,params.getPricePerRequest());
		jedis.hset(serviceName+ PARAMS,PRICE_PER_K_BYTES,params.getPricePerKBytes());
		jedis.hset(serviceName+ PARAMS,SESSION_DAYS,params.getSessionDays());
		jedis.hset(serviceName+ PARAMS,URL_HEAD,params.getUrlHead());

		System.out.println("Service has been wrote into redis. Press enter to continue...");
		br.readLine();
	}

	public void publish(BufferedReader br, Jedis jedis) throws IOException {
		System.out.println("To publish a new service.");

		OpReturn opReturn = new OpReturn();

		Data data = new Data();

		data.setOp("publish");

		System.out.println("Input the English name of your service:");
		data.setStdName(br.readLine());

		String ask = "Input the local names of your service, if you want. Press enter to end :";
		String[] localNames = inputStringArray(br,ask,0);
		if(localNames.length!=0) data.setLocalNames(localNames);

		System.out.println("Input the description of your service if you want.Press enter to ignore:");
		String str = br.readLine();
		if(!str.equals(""))data.setDesc(str);

		String[] types = {"APIP","FEIP"};
		data.setTypes(types);

		ask = "Input the URLs of your service, if you want. Press enter to end :";
		String[] urls = inputStringArray(br,ask,0);
		if(urls.length!=0)data.setUrls(urls);

		ask = "Input the FCH address of the waiter for your service if you want. Press enter to ignore:";
		String[] waiters = inputStringArray(br,ask,0);
		if(waiters.length!=0)data.setWaiters(waiters);

		ask = "Input the PIDs of the PIDs your service using if you want. Press enter to end :";
		String[] protocols = inputStringArray(br,ask,64);
		if(protocols.length!=0)data.setProtocols(protocols);

		System.out.println("Input the head of the URL being requested for your service. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setUrlHead(str);

		System.out.println("Input the currency you acceptting for your service, if you need. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setCurrency(str);

		System.out.println("Input the account to recieve payments if you need. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setAccount(str);

		System.out.println("Input the price per request of your service if you need. Press enter to ignore:");
		float flo = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					flo = Float.valueOf(str);
					params.setPricePerRequest(String.valueOf(flo));
					break;
				}catch(Exception e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else break;
		}

		System.out.println("Input the price per KB of your service if you need. Press enter to ignore:");
		flo = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) try {
				flo = Float.parseFloat(str);
				params.setPricePerKBytes(String.valueOf(flo));
				break;
			} catch (Exception e) {
				System.out.println("It isn't a number. Input again:");
			}
			else break;
		}

		System.out.println("Input the minimum amount of payment for your service, if you need. Press enter to ignore:");
		flo = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					flo = Float.valueOf(str);
					params.setMinPayment(String.valueOf(flo));
					break;
				}catch(Exception e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else break;
		}

		System.out.println("Input the expiring days of session key of your service, if you need. Press enter to ignore:");
		int num = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					num = Integer.valueOf(str);
					params.setSessionDays(String.valueOf(num));
					break;
				}catch(Exception e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}else break;
		}


		data.setParams(params);
		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private String[] inputStringArray(BufferedReader br, String ask, int len) throws IOException {
		// TODO Auto-generated method stub

		System.out.println(ask);
		ArrayList<String> itemList = new ArrayList<String>();
		while(true) {
			String item = br.readLine();
			if(item.equals(""))break;
			if(len>0) {
				if(item.length()!=len) {
					System.out.println("The length does not match.");
					continue;
				}
			}
			itemList.add(item);
			System.out.println("Input next item if you want or enter to end:");
		}
		if(itemList.isEmpty())return new String [0];

		String[] items = itemList.toArray(new String[itemList.size()]);

		return items;
	}

	private void update(ElasticsearchClient esClient, BufferedReader br, Jedis jedis) throws IOException {
		System.out.println("To update the service information.");

//		System.out.println("Input the SID of your service:");
//		String sid;
//		while(true) {
//			sid = br.readLine();
//			if(sid.length()==64) {
//				break;
//			}
//			System.out.println("Illegal sid. Input again:");
//		}
		String sid = jedis.get("sid");

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}
		ApipService service = result.source();

		OpReturn opReturn = new OpReturn();

		Data data = new Data();

		data.setOp("update");
		data.setSid(sid);

		System.out.println("\nThe English name of your service: "+service.getStdName());
		System.out.println("Input the English name of your service if you want to change it, . Press enter to keep it:");
		String str = br.readLine();
		if(!str.equals("")) {
			data.setStdName(str);
		}else {
			data.setStdName(service.getStdName());
		}

		if(service.getLocalNames()!=null) {
			System.out.println("\nThe local names of your service: ");
			for(String item:service.getLocalNames()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo local names yet.");
		}
		String ask = "Input the local names of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] localNames = inputStringArray(br,ask,0);
		if(localNames.length!=0) {
			data.setLocalNames(localNames);
		}else {
			if(service.getLocalNames()!=null)
				data.setLocalNames(service.getLocalNames());
		}


		if(service.getDesc()!=null) {
			System.out.println("\nThe description of your service: "+service.getDesc());
		}else {
			System.out.println("\nNo description yet.");
		}
		System.out.println("Input the description of your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			data.setDesc(null);
		}else if(!str.equals("")) {
			data.setStdName(str);
		}else data.setStdName(service.getStdName());

		if(service.getUrls()!=null) {
			System.out.println("\nThe URLs of your service: ");
			for(String item:service.getUrls()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo URLs yet.");
		}
		ask = "Input the URLs of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] urls = inputStringArray(br,ask,0);
		if(urls.length!=0) {
			data.setUrls(urls);
		}else {
			if(service.getUrls()!=null)
				data.setUrls(service.getUrls());
		}

		if(service.getWaiters()!=null) {
			System.out.println("\nThe FCH addresses of the waiters of your service: ");
			for(String item:service.getUrls()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo FCH addresses of the waiters yet.");
		}
		ask = "Input the public Key of the waiters of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] waiters = inputStringArray(br,ask,0);
		if(urls.length!=0) {
			data.setWaiters(waiters);
		}else {
			if(service.getWaiters()!=null)
				data.setWaiters(service.getWaiters());
		}

		if(service.getProtocols()!=null) {
			System.out.println("\nThe PIDs of your service: ");
			for(String item:service.getProtocols()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo PIDs yet.");
		}
		ask = "Input the PIDs of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] protocols = inputStringArray(br,ask,64);
		if(protocols.length!=0) {
			data.setProtocols(protocols);
		}else {
			if(service.getProtocols()!=null)
				data.setProtocols(service.getProtocols());
		}

		Params params = service.getParams();

		if(params.getUrlHead()!=null) {
			System.out.println("\nThe head of the URL being requested for your service: "+params.getUrlHead());
		}else {
			System.out.println("\nNo head of the URL yet.");
		}
		System.out.println("Input the head of the URL being requested for your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setUrlHead(null);
		}else if(!str.equals("")) {
			params.setUrlHead(str);
		}

		if(params.getCurrency()!=null) {
			System.out.println("\nThe currency you accepting for your service: "+params.getCurrency());
		}else {
			System.out.println("\nNo currency yet.");
		}
		System.out.println("Input the currency you accepting for your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setCurrency(null);
		}else if(!str.equals("")) {
			params.setCurrency(str);
		}

		if(params.getAccount()!=null) {
			System.out.println("\nThe account to receive payments: "+params.getAccount());
		}else {
			System.out.println("\nNo local names yet.");
		}
		System.out.println("Input the account to receive payments if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setAccount(null);
		}else if(!str.equals("")) {
			params.setAccount(str);
		}


		System.out.println("\nThe price per request of your service: "+params.getPricePerRequest());
		System.out.println("Input the price per request of your service if you want to change it . Press enter to keep it:");
		float flo = 0;
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					flo = Float.parseFloat(str);
					params.setPricePerRequest(String.valueOf(flo));
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else break;
		}


		System.out.println("\nThe minimum amount of payment for your service: "+params.getMinPayment());
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Press enter to keep it:");
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					flo = Float.parseFloat(str);
					params.setMinPayment(String.valueOf(flo));
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else break;
		}

		System.out.println("\nThe expiring days of the session key of your service: "+params.getSessionDays());
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Press enter to keep it:");
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					int num = Integer.parseInt(str);
					params.setSessionDays(String.valueOf(num));
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}else break;
		}
		data.setParams(params);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void stop(ElasticsearchClient esClient,BufferedReader br) throws IOException {
		System.out.println("To stop the service.");

		System.out.println("Input the SID of your service:");
		String sid = br.readLine();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}

		OpReturn opReturn = new OpReturn();

		Data data = new Data();

		data.setOp("stop");
		data.setSid(sid);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void recover(ElasticsearchClient esClient,BufferedReader br) throws ElasticsearchException, IOException {
		System.out.println("To recover the service.");

		System.out.println("Input the SID of your service:");
		String sid = br.readLine();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}

		OpReturn opReturn = new OpReturn();

		Data data = new Data();

		data.setOp("recover");
		data.setSid(sid);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void close(ElasticsearchClient esClient,BufferedReader br) throws ElasticsearchException, IOException {
		System.out.println("To close the service.");

		System.out.println("Input the SID of your service:");
		String sid = br.readLine();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}

		OpReturn opReturn = new OpReturn();

		Data data = new Data();

		data.setOp("close");
		data.setSid(sid);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service or its master to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private ApipService checkService() {
		String serviceStr = jedis.get(SERVICE);
		if(serviceStr==null){
			System.out.println("Service isn't set yet.");
			return null;
		}else {
			service = new Gson().fromJson(serviceStr, ApipService.class);

		}
		return service;
	}
}
