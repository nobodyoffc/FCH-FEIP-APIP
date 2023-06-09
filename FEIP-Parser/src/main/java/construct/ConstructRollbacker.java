package construct;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import fcTools.ParseTools;
import servers.EsTools;
import startFEIP.IndicesFEIP;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConstructRollbacker {

	public boolean rollback(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		error = rollbackFreeProtocol(esClient,lastHeight);
		error = rollbackService(esClient,lastHeight);
		error = rollbackApp(esClient,lastHeight);
		error = rollbackCode(esClient,lastHeight);
		
		return error;
	}

	private boolean rollbackFreeProtocol(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedFreeProtocols(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");
		
		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If rolling back is interrupted, reparse all effected ids of index 'protocol': ");
		ParseTools.gsonPrint(itemIdList);
		deleteEffectedItems(esClient, IndicesFEIP.ProtocolIndex, itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesFEIP.ProtocolHistIndex,histIdList);
		
		TimeUnit.SECONDS.sleep(2);
		
		List<ProtocolHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesFEIP.ProtocolHistIndex,"pid",itemIdList, ProtocolHistory.class);

		reparseFreeProtocol(esClient,reparseHistList);
		
		return error;
	}

	private Map<String, ArrayList<String>> getEffectedFreeProtocols(ElasticsearchClient esClient,long height) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<ProtocolHistory> resultSearch = esClient.search(s->s
				.index(IndicesFEIP.ProtocolHistIndex)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(height)))), ProtocolHistory.class);
		
		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<ProtocolHistory> hit: resultSearch.hits().hits()) {
			
			ProtocolHistory item = hit.source();
			if(item.getOp().equals("publish")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getPid());
			}
			histList.add(hit.id());
		}
		

		ArrayList<String> itemList = new ArrayList<String>(itemSet);
		
		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);
		
		return resultMap;
	}

	private void deleteEffectedItems(ElasticsearchClient esClient,String index, ArrayList<String> itemIdList) throws Exception {
		// TODO Auto-generated method stub
		EsTools.bulkDeleteList(esClient, index, itemIdList);
	}

	private void deleteRolledHists(ElasticsearchClient esClient, String index, ArrayList<String> histIdList) throws Exception {
		// TODO Auto-generated method stub
		EsTools.bulkDeleteList(esClient, index, histIdList);
	}
	
	private void reparseFreeProtocol(ElasticsearchClient esClient, List<ProtocolHistory> reparseHistList) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(ProtocolHistory freeProtocolHist: reparseHistList) {
			new ConstructParser().parseFreeProtocol(esClient, freeProtocolHist);
		}
	}

	private boolean rollbackService(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedServices(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");
		
		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If rolling back is interrupted, reparse all effected ids of index 'service': ");
		ParseTools.gsonPrint(itemIdList);
		deleteEffectedItems(esClient, IndicesFEIP.ServiceIndex,itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesFEIP.ServiceHistIndex,histIdList);
		TimeUnit.SECONDS.sleep(2);
		
		List<ServiceHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesFEIP.ServiceHistIndex,"sid",itemIdList,ServiceHistory.class);

		reparseService(esClient,reparseHistList);
		
		return error;
	}

	private Map<String, ArrayList<String>> getEffectedServices(ElasticsearchClient esClient, long lastHeight) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<ServiceHistory> resultSearch = esClient.search(s->s
				.index(IndicesFEIP.ServiceHistIndex)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(lastHeight)))),ServiceHistory.class);
		
		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<ServiceHistory> hit: resultSearch.hits().hits()) {
			
			ServiceHistory item = hit.source();
			if(item.getOp().equals("publish")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getSid());
			}
			histList.add(hit.id());
		}
		

		ArrayList<String> itemList = new ArrayList<String>(itemSet);
		
		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);
		
		return resultMap;
	}

	private void reparseService(ElasticsearchClient esClient, List<ServiceHistory> reparseHistList) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(ServiceHistory serviceHist: reparseHistList) {
			new ConstructParser().parseService(esClient, serviceHist);
		}
	}

	private boolean rollbackApp(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedApps(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");
		
		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If rolling back is interrupted, reparse all effected ids of index 'app': ");
		ParseTools.gsonPrint(itemIdList);
		deleteEffectedItems(esClient, IndicesFEIP.AppIndex,itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesFEIP.AppHistIndex,histIdList);
		TimeUnit.SECONDS.sleep(2);
		
		List<AppHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesFEIP.AppHistIndex,"aid",itemIdList,AppHistory.class);

		reparseApp(esClient,reparseHistList);
		
		return error;
	}

	private Map<String, ArrayList<String>> getEffectedApps(ElasticsearchClient esClient, long lastHeight) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<AppHistory> resultSearch = esClient.search(s->s
				.index(IndicesFEIP.AppHistIndex)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(lastHeight)))),AppHistory.class);
		
		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<AppHistory> hit: resultSearch.hits().hits()) {
			
			AppHistory item = hit.source();
			if(item.getOp().equals("publish")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getAid());
			}
			histList.add(hit.id());
		}
		

		ArrayList<String> itemList = new ArrayList<String>(itemSet);
		
		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);
		
		return resultMap;
	}

	private void reparseApp(ElasticsearchClient esClient, List<AppHistory> reparseHistList) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(AppHistory appHist: reparseHistList) {
			new ConstructParser().parseApp(esClient, appHist);
		}
	}

	private boolean rollbackCode(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedCodes(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");
		
		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If rolling back is interrupted, reparse all effected ids of index 'code': ");
		ParseTools.gsonPrint(itemIdList);
		deleteEffectedItems(esClient, IndicesFEIP.CodeIndex,itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesFEIP.CodeHistIndex,histIdList);
		TimeUnit.SECONDS.sleep(2);
		
		List<CodeHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesFEIP.CodeHistIndex,"codeId",itemIdList,CodeHistory.class);

		reparseCode(esClient,reparseHistList);
		
		return error;
	}

	private Map<String, ArrayList<String>> getEffectedCodes(ElasticsearchClient esClient, long lastHeight) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<CodeHistory> resultSearch = esClient.search(s->s
				.index(IndicesFEIP.CodeHistIndex)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(lastHeight)))),CodeHistory.class);
		
		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<CodeHistory> hit: resultSearch.hits().hits()) {
			
			CodeHistory item = hit.source();
			if(item.getOp().equals("publish")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getCodeId());
			}
			histList.add(hit.id());
		}
		

		ArrayList<String> itemList = new ArrayList<String>(itemSet);
		
		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);
		
		return resultMap;
	}

	private void reparseCode(ElasticsearchClient esClient, List<CodeHistory> reparseHistList) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(CodeHistory codeHist: reparseHistList) {
			new ConstructParser().parseCode(esClient, codeHist);
		}
	}

}
