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
import constants.OpNames;
import feipClass.ServiceData;
import fcTools.ParseTools;
import keyTools.KeyTools;
import menu.Inputer;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import startAPIP.StartAPIP;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static constants.Strings.*;


public class ServiceManager {
	private static final Logger log = LoggerFactory.getLogger(ServiceManager.class);
	Params params = new Params();
	Jedis jedis;
	ElasticsearchClient esClient;
	BufferedReader br;
	ConfigAPIP configAPIP;

	public ServiceManager(ElasticsearchClient esClient, Jedis jedis, BufferedReader br, ConfigAPIP configAPIP)  {

		if(StartAPIP.service!=null) {
			params = StartAPIP.service.getParams();
		}
		this.jedis = jedis;
		this.br = br;
		this.esClient = esClient;
		this.configAPIP = configAPIP;
	}

	public void menu() throws IOException {
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

		while (true){
			menu.show();

			int choice = menu.choose(br);

			switch (choice) {
				case 1 -> publish(br, jedis);
				case 2 -> findService();
				case 3 -> showService();
				case 4 -> update(esClient, br);
				case 5 -> reloadServiceFromRedis(esClient, jedis, br);
				case 6 -> stop(esClient, br);
				case 7 -> recover(esClient, br);
				case 8 -> close(esClient, br);
				case 0 -> {
					return;
				}
				default -> {
				}
			}
		}
	}

	private void showService() {
		if (jedis.get(StartAPIP.serviceName+"_"+SERVICE) == null) {
			System.out.println("No service set yet.");
			return;
		}
		Gson gson = new Gson();
		ApipService service1 = gson.fromJson(jedis.get(StartAPIP.serviceName+"_"+SERVICE), ApipService.class);
		ParseTools.gsonPrint(service1);
	}

	public boolean findService() throws IOException {
		StartAPIP.service = getServiceFromEsByOwner(esClient, br);
		if(StartAPIP.service == null){
			System.out.println("No service found. Publish one? 'y' to publish. Any other key to quit");
			String input = br.readLine();
			if("y".equals(input)) {
				publish(br, jedis);
				System.out.println("Publish your service and try again.");
			}
			return false;
		}
		params = StartAPIP.service.getParams();
		StartAPIP.updateServiceParamsInRedisAndConfig(jedis,configAPIP);
		return true;
	}

	private void reloadServiceFromRedis(ElasticsearchClient esClient, Jedis jedis, BufferedReader br) throws IOException {
		if (Menu.askIfNotToDo("Reload the service from ES? ", br)) return;

		String serviceStr = jedis.get(StartAPIP.serviceName+"_"+SERVICE);
		if(serviceStr==null){
			System.out.println("Service isn't set yet.");
		}else {
			StartAPIP.service = new Gson().fromJson(serviceStr, ApipService.class);
			GetResponse<ApipService> r = esClient.get(g -> g.index(SERVICE).id(StartAPIP.service.getSid()), ApipService.class);
			if(r.source()!=null) {
				String serviceJson = new Gson().toJson(r.source());
				jedis.set(StartAPIP.serviceName+"_"+SERVICE, serviceJson);
				params = StartAPIP.service.getParams();
				System.out.println("Service " + StartAPIP.service.getSid() + " reloaded.");
			}else{
				System.out.println("No service found.");
			}
		}
	}

	public ApipService getServiceFromEsByOwner(ElasticsearchClient esClient, BufferedReader br) throws IOException {
		String str;
		while(true) {
			System.out.println("Input the fch address of the owner. 'q' to quit:");
			str = br.readLine();
			if ("q".equals(str)) return null;
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
			StartAPIP.service = serviceList.get(i);
            System.out.println((i+1) +". service \nname: "+ StartAPIP.service.getStdName()+"\nsid: "+StartAPIP.service.getSid());
        }
        if(size==1){
			return StartAPIP.service;
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

		StartAPIP.service= serviceList.get(choice-1);
        System.out.println(choice +". service name: "+ StartAPIP.service.getStdName()+"sid: "+StartAPIP.service.getSid());
        return StartAPIP.service;
	}

	public void publish(BufferedReader br, Jedis jedis) throws IOException {
		if (Menu.askIfNotToDo("Get the OpReturn text to publish a new service?", br)) return;

		Feip5 feip5 = new Feip5();

//		Data data = new Data();

		ServiceData data = new ServiceData();
		data.setOp(OpNames.PUBLISH);
		String[] types = {"APIP","FEIP"};
		data.setTypes(types);
		data.inputServicePublish(br);
		inputServiceApipParams(br);

		data.setParams(params);
		feip5.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(feip5));
		System.out.println();
	}

	private void inputServiceApipParams(BufferedReader br) throws IOException {
		setUrlHead(br);

		setCurrency(br);

		setAccount(br);

		setPricePerRequest(br);

		setPricePerKB(br);

		setMinPayment(br);

		setSessionKey(br);

		params.setConsumeViaShare(Inputer.inputShare(br, CONSUME_VIA_SHARE));

		params.setOrderViaShare(Inputer.inputShare(br,ORDER_VIA_SHARE));
	}

	private void setUrlHead(BufferedReader br) throws IOException {
		String str;
		System.out.println("Input the head of the URL being requested for your service. Enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setUrlHead(str);
	}

	private void setCurrency(BufferedReader br) throws IOException {
		String str;
		System.out.println("Input the currency you accepting for your service, if you need. Enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setCurrency(str);
	}

	private void setAccount(BufferedReader br) throws IOException {
		String str;
		System.out.println("Input the account to receive payments if you need. Enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setAccount(str);
	}

	private void setPricePerRequest(BufferedReader br) throws IOException {
		String str;
		System.out.println("Input the price per request of your service if you need. Enter to ignore:");
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
	}

	private void setPricePerKB(BufferedReader br) throws IOException {
		String str;
		float flo;
		System.out.println("Input the price per KB of your service if you need. Enter to ignore:");
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
	}

	private void setMinPayment(BufferedReader br) throws IOException {
		float flo;
		String str;
		System.out.println("Input the minimum amount of payment for your service, if you need. Enter to ignore:");
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
	}

	private void setSessionKey(BufferedReader br) throws IOException {
		String str;
		System.out.println("Input the expiring days of session key of your service, if you need. Enter to ignore:");
		int num = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					num = Integer.parseInt(str);
					params.setSessionDays(String.valueOf(num));
					break;
				}catch(Exception e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}else break;
		}
	}


	private void update(ElasticsearchClient esClient, BufferedReader br) throws IOException {

		if (Menu.askIfNotToDo("Get the OpReturn text to update your service? ", br)) return;

		String sid = StartAPIP.service.getSid();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}
		ApipService service = result.source();

		if(service == null){
			log.debug("Got service from ES failed.");
			return;
		}

		Feip5 opReturn = new Feip5();

		ServiceData data = new ServiceData();

		data.setOp(OpNames.UPDATE);
		data.setSid(sid);

		updateStdName(br, service, data);

		updateLocalNames(br, service, data);

		updateDesc(br, service, data);

		updateUrls(br, service, data);

		updateWaiters(br, service, data);

		updatePids(br, service, data);

		Params params = service.getParams();
		if(params==null){
			log.debug("Service.params is null.");
			return;
		}

		updateParamsUrlHead(br, params);

		updateParamsCurrency(br, params);

		updateParamsAccount(br, params);

		updateParamsPricePerRequest(br, params);

		updateParamsMinPayment(br, params);

		updateParamsExpireDays(br, params);

		System.out.println("\nThe ConsumeViaShare of your service: "+ params.getConsumeViaShare());
		params.setConsumeViaShare(updateViaShare(br,params, CONSUME_VIA_SHARE));

		System.out.println("\nThe OrderViaShare of your service: "+ params.getOrderViaShare());
		params.setOrderViaShare(updateViaShare(br,params, ORDER_VIA_SHARE));

		data.setParams(params);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private String updateViaShare(BufferedReader br, Params params, String viaShare) {
		String str;
		System.out.println("Input "+viaShare+" your service if you want to change it. Enter to keep it:");
		while(true) {
			str = Inputer.inputString(br);
			if("".equals(str)) return null;
			if(ParseTools.isGoodShare(str)) {
				try {
					double num = Double.parseDouble(str);
					num = ParseTools.roundDouble4(num);
					return String.valueOf(num);
				} catch (NumberFormatException e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else{
				System.out.println("A share should be a number with no more than 4 decimal digits.");
			}
		}
	}

	private void updateOrderViaShare(BufferedReader br, Params params) {
		String str;
		System.out.println("\nThe consumeViaShare of your service: "+ params.getOrderViaShare());
		System.out.println("Input consumeViaShare your service if you want to change it. Enter to keep it:");
		while(true) {
			str = Inputer.inputString(br);
			if(!"".equals(str)) {
				if(ParseTools.isGoodShare(str)) {
					try {
						double num = Double.parseDouble(str);
						num = ParseTools.roundDouble4(num);
						params.setOrderViaShare(String.valueOf(num));
						break;
					} catch (NumberFormatException e) {
						System.out.println("It isn't a number. Input again:");
					}
				}else{
					System.out.println("A share should be a ");
				}
			}else break;
		}
	}

	private static void updateParamsExpireDays(BufferedReader br, Params params) throws IOException {
		String str;
		System.out.println("\nThe expiring days of the session key of your service: "+ params.getSessionDays());
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Enter to keep it:");
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
	}

	private static void updateParamsMinPayment(BufferedReader br, Params params) throws IOException {
		String str;
		float flo;
		System.out.println("\nThe minimum amount of payment for your service: "+ params.getMinPayment());
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Enter to keep it:");
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
	}

	private static void updateParamsPricePerRequest(BufferedReader br, Params params) throws IOException {
		String str;
		System.out.println("\nThe price per request of your service: "+ params.getPricePerRequest());
		System.out.println("Input the price per request of your service if you want to change it . Enter to keep it:");
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
	}

	private static void updateParamsAccount(BufferedReader br, Params params) throws IOException {
		String str;
		if(params.getAccount()!=null) {
			System.out.println("\nThe account to receive payments: "+ params.getAccount());
		}else {
			System.out.println("\nNo local names yet.");
		}
		System.out.println("Input the account to receive payments if you want to change it . Enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setAccount(null);
		}else if(!str.equals("")) {
			params.setAccount(str);
		}
	}

	private static void updateParamsCurrency(BufferedReader br, Params params) throws IOException {
		String str;
		if(params.getCurrency()!=null) {
			System.out.println("\nThe currency you accepting for your service: "+ params.getCurrency());
		}else {
			System.out.println("\nNo currency yet.");
		}
		System.out.println("Input the currency you accepting for your service if you want to change it . Enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setCurrency(null);
		}else if(!str.equals("")) {
			params.setCurrency(str);
		}
	}

	private static void updateParamsUrlHead(BufferedReader br, Params params) throws IOException {
		String str;
		if(params.getUrlHead()!=null) {
			System.out.println("\nThe head of the URL being requested for your service: "+ params.getUrlHead());
		}else {
			System.out.println("\nNo head of the URL yet.");
		}
		System.out.println("Input the head of the URL being requested for your service if you want to change it . Enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setUrlHead(null);
		}else if(!str.equals("")) {
			params.setUrlHead(str);
		}
	}

	private void updatePids(BufferedReader br, ApipService service, ServiceData data) throws IOException {
		String ask;
		if(service.getProtocols()!=null) {
			System.out.println("\nThe PIDs of your service: ");
			for(String item: service.getProtocols()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo PIDs yet.");
		}
		ask = "Input the PIDs of your service if you want to change it . Enter to keep it or 'd' to delete it:";
		String[] protocols = Inputer.inputStringArray(br,ask,64);
		if(protocols.length!=0) {
			data.setProtocols(protocols);
		}else {
			if(service.getProtocols()!=null)
				data.setProtocols(service.getProtocols());
		}
	}

	private void updateWaiters(BufferedReader br, ApipService service, ServiceData data) throws IOException {
		String ask;
		if(service.getWaiters()!=null) {
			System.out.println("\nThe FIDs of the waiters of your service: ");
			for(String item: service.getWaiters()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo FCH addresses of the waiters yet.");
		}
		ask = "Input the FIDs of the waiters of your service if you want to change it . Enter to keep it or 'd' to delete it:";
		String[] waiters = Inputer.inputStringArray(br,ask,0);
		if(waiters.length!=0) {
			data.setWaiters(waiters);
		}else {
			if(service.getWaiters()!=null)
				data.setWaiters(service.getWaiters());
		}
	}

	private String[] updateUrls(BufferedReader br, ApipService service, ServiceData data) throws IOException {
		String ask;
		ask = "Input the URLs of your service if you want to change it . Enter to keep it or 'd' to delete it:";
		String[] urls = Inputer.inputStringArray(br,ask,0);
		if(urls.length!=0) {
			data.setUrls(urls);
		}else {
			if(service.getUrls()!=null)
				data.setUrls(service.getUrls());
		}
		return urls;
	}

	private static void updateDesc(BufferedReader br, ApipService service, ServiceData data) throws IOException {
		String str;
		if(service.getDesc()!=null) {
			System.out.println("\nThe description of your service: "+ service.getDesc());
		}else {
			System.out.println("\nNo description yet.");
		}
		System.out.println("Input the description of your service if you want to change it . Enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			data.setDesc(null);
		}else if(!str.equals("")) {
			data.setStdName(str);
		}else data.setStdName(service.getStdName());

		if(service.getUrls()!=null) {
			System.out.println("\nThe URLs of your service: ");
			for(String item: service.getUrls()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo URLs yet.");
		}
	}

	private void updateLocalNames(BufferedReader br, ApipService service, ServiceData data) throws IOException {
		if(service.getLocalNames()!=null) {
			System.out.println("\nThe local names of your service: ");
			for(String item: service.getLocalNames()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo local names yet.");
		}
		String ask = "Input the local names of your service if you want to change it . Enter to keep it or 'd' to delete it:";
		String[] localNames = Inputer.inputStringArray(br,ask,0);
		if(localNames.length!=0) {
			data.setLocalNames(localNames);
		}else {
			if(service.getLocalNames()!=null)
				data.setLocalNames(service.getLocalNames());
		}
	}

	private static void updateStdName(BufferedReader br, ApipService service, ServiceData data) throws IOException {
		System.out.println("\nThe English name of your service: "+service.getStdName());
		System.out.println("Input the English name of your service if you want to change it. Enter to keep it:");
		String str = br.readLine();
		if("".equals(str)){
			data.setStdName(service.getStdName());
		}else{
			data.setStdName(str);
		}
	}

	private void stop(ElasticsearchClient esClient,BufferedReader br) throws IOException {
		if (Menu.askIfNotToDo("Get the OpReturn text to stop the service? ", br)) return;

		System.out.println("Input the SID of your service:");
		String sid = br.readLine();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}

		Feip5 opReturn = new Feip5();

		ServiceData data = new ServiceData();

		data.setOp("stop");
		data.setSid(sid);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void recover(ElasticsearchClient esClient,BufferedReader br) throws ElasticsearchException, IOException {
		if (Menu.askIfNotToDo("Get the OpReturn text to recover the service? ", br)) return;

		System.out.println("Input the SID of your service:");
		String sid = br.readLine();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}

		Feip5 opReturn = new Feip5();

		ServiceData data = new ServiceData();

		data.setOp("recover");
		data.setSid(sid);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void close(ElasticsearchClient esClient,BufferedReader br) throws ElasticsearchException, IOException {
		if (Menu.askIfNotToDo("Get the OpReturn text to CLOSE the service?!", br)) return;

		System.out.println("Input the SID of your service:");
		String sid = br.readLine();

		GetResponse<ApipService> result = esClient.get(g->g.index(SERVICE).id(sid), ApipService.class);

		if(!result.found()) {
			System.out.println("Service does not exist.");
			return;
		}

		Feip5 opReturn = new Feip5();

		ServiceData data = new ServiceData();

		data.setOp("close");
		data.setSid(sid);

		opReturn.setData(data);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service or its master to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

}
