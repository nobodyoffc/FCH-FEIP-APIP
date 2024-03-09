package publish;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import constants.IndicesNames;
import esTools.EsTools;
import feipClass.ProofHistory;
import feipClass.TokenHistory;
import feipClass.TokenHolder;
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
		error = rollbackToken(esClient,lastHeight);

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

	private boolean rollbackToken(ElasticsearchClient esClient, long lastHeight) throws Exception {
		// TODO Auto-generated method stub

		boolean error = false;
		Map<String, ArrayList<String>> resultMap = getEffectedTokens(esClient,lastHeight);
		ArrayList<String> tokenIdList = resultMap.get("itemIdList");
		ArrayList<String> histIdList = resultMap.get("histIdList");


		if(tokenIdList==null||tokenIdList.isEmpty())return error;
		System.out.println("If Rollback is interrupted, reparse all effected ids of index 'token': ");
		JsonTools.gsonPrint(tokenIdList);
		deleteEffectedItems(esClient, IndicesNames.TOKEN, tokenIdList);

		if(histIdList==null||histIdList.isEmpty())return error;

		deleteRolledHists(esClient, IndicesNames.TOKEN_HISTORY,histIdList);

		TimeUnit.SECONDS.sleep(3);

		List<TokenHistory>reparseHistList = EsTools.getHistsForReparse(esClient, IndicesNames.TOKEN_HISTORY,"tokenId",tokenIdList, TokenHistory.class);

		deleteEffectedTokenHolders(esClient,tokenIdList);

		TimeUnit.SECONDS.sleep(3);

		reparseToken(esClient,reparseHistList);

		return error;
	}

	private Map<String, ArrayList<String>> getEffectedTokens(ElasticsearchClient esClient,long height) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		SearchResponse<TokenHistory> resultSearch = esClient.search(s->s
				.index(IndicesNames.TOKEN_HISTORY)
				.query(q->q
						.range(r->r
								.field("height")
								.gt(JsonData.of(height)))),TokenHistory.class);

		Set<String> tokenIdSet = new HashSet<String>();
		ArrayList<String> histList = new ArrayList<String>();

		for(Hit<TokenHistory> hit: resultSearch.hits().hits()) {

			TokenHistory item = hit.source();
			if(item.getOp().equals("deploy")) {
				tokenIdSet.add(item.getTxId());
			}else {
				tokenIdSet.add(item.getTokenId());
			}
			histList.add(hit.id());
		}


		ArrayList<String> itemList = new ArrayList<String>(tokenIdSet);

		Map<String,ArrayList<String>> resultMap = new HashMap<String,ArrayList<String>>();
		resultMap.put("itemIdList", itemList);
		resultMap.put("histIdList", histList);

		return resultMap;
	}

	private void deleteEffectedTokenHolders(ElasticsearchClient esClient,List<String> tokenIdList) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		List<FieldValue> fieldValueList = new ArrayList<>();
		tokenIdList.forEach(tokenId->fieldValueList.add(FieldValue.of(tokenId)));

		esClient.deleteByQuery(d->d.index(IndicesNames.TOKEN_HOLDER)
				.query(q->q
						.terms(t->t
								.field("tokenId")
								.terms(ts->ts.value(fieldValueList))
						)));
	}

	private void reparseToken(ElasticsearchClient esClient, List<TokenHistory> reparseHistList) throws Exception {
		// TODO Auto-generated method stub
		if(reparseHistList==null)return;
		for(TokenHistory tokenHist: reparseHistList) {
			try {
				new PublishParser().parseToken(esClient, tokenHist);
			}catch (NumberFormatException ignore){}
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
