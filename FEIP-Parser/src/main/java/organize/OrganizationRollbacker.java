package organize;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import constants.IndicesNames;
import esTools.EsTools;
import javaTools.JsonTools;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class OrganizationRollbacker {

	public boolean rollback(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		error = rollbackGroup(esClient,lastHeight);
		error = rollbackTeam(esClient,lastHeight);
		
		return error;
		
	}
	
	private boolean rollbackGroup(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedGroups(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");
		
		
		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If Rollbacking is interrupted, reparse all effected ids of index 'group': ");
		JsonTools.gsonPrint(itemIdList);
		deleteEffectedItems(esClient, IndicesNames.GROUP, itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesNames.GROUP_HISTORY,histIdList);
		
		TimeUnit.SECONDS.sleep(3);
		
		List<GroupHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesNames.GROUP_HISTORY,"gid",itemIdList, GroupHistory.class);
		
		reparseGroup(esClient,reparseHistList);
		
		return error;
	}

	private Map<String, ArrayList<String>> getEffectedGroups(ElasticsearchClient esClient,long height) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<GroupHistory> resultSearch = esClient.search(s->s
				.index(IndicesNames.GROUP_HISTORY)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(height)))),GroupHistory.class);
		
		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<GroupHistory> hit: resultSearch.hits().hits()) {
			
			GroupHistory item = hit.source();
			if(item.getOp().equals("create")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getGid());
			}
			histList.add(hit.id());
		}
		

		ArrayList<String> itemList = new ArrayList<String>(itemSet);
		
		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);
		
		return resultMap;
	}
	
	private void reparseGroup(ElasticsearchClient esClient, List<GroupHistory> reparseHistList) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(GroupHistory groupHist: reparseHistList) {
			new OrganizationParser().parseGroup(esClient, groupHist);
		}
	}
	
	private boolean rollbackTeam(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedTeams(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");
		
		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If Rollbacking is interrupted, reparse all effected ids of index 'team': ");
		JsonTools.gsonPrint(itemIdList);
		
		TimeUnit.SECONDS.sleep(10);
		
		deleteEffectedItems(esClient, IndicesNames.TEAM, itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesNames.TEAM_HISTORY,histIdList);

		TimeUnit.SECONDS.sleep(3);
		
		List<TeamHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesNames.TEAM_HISTORY,"tid",itemIdList, TeamHistory.class);
		
		reparseTeam(esClient,reparseHistList);
		
		return error;
	}

	private Map<String, ArrayList<String>> getEffectedTeams(ElasticsearchClient esClient,long height) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<TeamHistory> resultSearch = esClient.search(s->s
				.index(IndicesNames.TEAM_HISTORY)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(height)))),TeamHistory.class);
		
		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<TeamHistory> hit: resultSearch.hits().hits()) {
			
			TeamHistory item = hit.source();
			if(item.getOp().equals("create")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getTid());
			}
			histList.add(hit.id());
		}
		

		ArrayList<String> itemList = new ArrayList<String>(itemSet);
		
		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);
		
		return resultMap;
	}
	
	private void reparseTeam(ElasticsearchClient esClient, List<TeamHistory> reparseHistList) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(TeamHistory teamHist: reparseHistList) {
			new OrganizationParser().parseTeam(esClient, teamHist);
		}
	}

	private void deleteEffectedItems(ElasticsearchClient esClient,String index, ArrayList<String> itemIdList) throws Exception {
		// TODO Auto-generated method stub
		EsTools.bulkDeleteList(esClient, index, itemIdList);
	}

	private void deleteRolledHists(ElasticsearchClient esClient, String index, ArrayList<String> histIdList) throws Exception {
		// TODO Auto-generated method stub
		EsTools.bulkDeleteList(esClient, index, histIdList);
	}
}
