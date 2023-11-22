package publish;

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

public class PublishRollbacker {

	public boolean rollback(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		error = rollbackStatementAndNid(esClient,lastHeight);
		error = rollbackProof(esClient,lastHeight);

		return error;
	}

	public boolean rollbackStatementAndNid(ElasticsearchClient esClient, long lastHeight) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		List<String> indexList = new ArrayList<String>();
		indexList.add(IndicesNames.STATEMENT);
		indexList.add(IndicesNames.NID);

		esClient.deleteByQuery(d->d.index(indexList).query(q->q.range(r->r.field("birthHeight").gt(JsonData.of(lastHeight)))));
		
		TimeUnit.SECONDS.sleep(3);
		
		return false;
	}
	
	private boolean rollbackProof(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub
		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedProofs(esClient,lastHeight);
		ArrayList<String> itemIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");


		if(itemIdList==null||itemIdList.isEmpty())return error;
		System.out.println("If Rollbacking is interrupted, reparse all effected ids of index 'proof': ");
		JsonTools.gsonPrint(itemIdList);
		deleteEffectedItems(esClient, IndicesNames.PROOF, itemIdList);
		if(histIdList==null||histIdList.isEmpty())return error;
		deleteRolledHists(esClient, IndicesNames.PROOF_HISTORY,histIdList);

		TimeUnit.SECONDS.sleep(3);

		List<ProofHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesNames.PROOF_HISTORY,"gid",itemIdList, ProofHistory.class);

		reparseProof(esClient,reparseHistList);

		return error;
	}

	private Map<String, ArrayList<String>> getEffectedProofs(ElasticsearchClient esClient,long height) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<ProofHistory> resultSearch = esClient.search(s->s
				.index(IndicesNames.PROOF_HISTORY)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(height)))),ProofHistory.class);

		Set<String> itemSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<ProofHistory> hit: resultSearch.hits().hits()) {

			ProofHistory item = hit.source();
			if(item.getOp().equals("create")) {
				itemSet.add(item.getTxId());
			}else {
				itemSet.add(item.getProofId());
			}
			histList.add(hit.id());
		}


		ArrayList<String> itemList = new ArrayList<String>(itemSet);

		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);

		return resultMap;
	}

	private void reparseProof(ElasticsearchClient esClient, List<ProofHistory> reparseHistList) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(ProofHistory proofHist: reparseHistList) {
			new PublishParser().parseProof(esClient, proofHist);
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
