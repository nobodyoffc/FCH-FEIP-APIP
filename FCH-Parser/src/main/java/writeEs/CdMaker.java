package writeEs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import fchClass.Address;
import fchClass.Block;
import parser.WeightMethod;
import servers.EsTools;

import java.io.IOException;
import java.util.*;

import static constants.IndicesNames.ADDRESS;
import static constants.IndicesNames.CASH;

public class CdMaker {

	public void makeUtxoCd(ElasticsearchClient esClient, Block bestBlock)
			throws ElasticsearchException, IOException, InterruptedException {

		long bestBlockTime = bestBlock.getTime();

		System.out.println("Make all cd of UTXOs...");

		UpdateByQueryResponse response = esClient.updateByQuery(u -> u
				.conflicts(Conflicts.Proceed)
				.timeout(Time.of(t->t.time("1800s")))
				.index(CASH)
				.query(q -> q.bool(b -> b
						.filter(f -> f.term(t -> t.field("valid").value(true)))))
				.script(s -> s.inline(i1 -> i1.source(
								"ctx._source.cd = (long)(((long)((params.bestBlockTime - ctx._source.birthTime)/86400)*ctx._source.value)/100000000)")
						.params("bestBlockTime", JsonData.of(bestBlockTime)))));
		System.out.println(
				response.updated()
						+" utxo updated within "
						+response.took()/1000
						+" seconds. Version confilcts: "
						+response.versionConflicts());
	}

	public void makeAddrCd(ElasticsearchClient esClient) throws Exception {

		System.out.println("Make all cd of Addresses...");

		SearchResponse<Address> response = esClient.search(
				s -> s.index(ADDRESS).size(EsTools.READ_MAX).sort(sort -> sort.field(f -> f.field("fid"))),
				Address.class);

		ArrayList<Address> addrOldList = getResultAddrList(response);
		Map<String,Address> addrOldMap = new HashMap<>();
		for(Address addr : addrOldList){
			addrOldMap.put(addr.getFid(),addr);
		}

		Map<String,Long> addrNewCdMap = makeAddrCdList(esClient, addrOldList);
		Map<String, Long> addrNewWeightMap = makeWeight(addrNewCdMap, addrOldMap);
		updateAddrMap(esClient, addrNewCdMap, addrNewWeightMap);

		while (true) {
			if (response.hits().hits().size() < EsTools.READ_MAX)
				break;
			Hit<Address> last = response.hits().hits().get(response.hits().hits().size() - 1);
			String lastId = last.id();
			response = esClient.search(s -> s.index(ADDRESS).size(EsTools.READ_MAX)
					.sort(sort -> sort.field(f -> f.field("fid"))).searchAfter(lastId), Address.class);

			addrOldList = getResultAddrList(response);
			addrOldMap = new HashMap<>();
			for(Address addr : addrOldList){
				addrOldMap.put(addr.getFid(),addr);
			}

			addrNewCdMap = makeAddrCdList(esClient, addrOldList);
			addrNewWeightMap = makeWeight(addrNewCdMap, addrOldMap);
			updateAddrMap(esClient, addrNewCdMap, addrNewWeightMap);
		}
	}

	private Map<String, Long> makeWeight(Map<String, Long> addrNewCdMap, Map<String, Address> addrOldMap) {
		Map<String,Long> addrCdGrowMap = new HashMap<>();

		for(String id: addrNewCdMap.keySet()){
			long cdGrow = addrNewCdMap.get(id)-addrOldMap.get(id).getCd();
			addrCdGrowMap.put(id,cdGrow);
		}

		Map<String,Long> addrWeightMap = new HashMap<>();
		for(String id: addrNewCdMap.keySet()){
			long newWeight =(long) (addrOldMap.get(id).getWeight()+(addrCdGrowMap.get(id)*WeightMethod.cdPercentInWeight)/100);
			addrWeightMap.put(id, newWeight);
		}
		return addrWeightMap;
	}

	private ArrayList<Address> getResultAddrList(SearchResponse<Address> response) {
		ArrayList<Address> addrList = new ArrayList<Address>();
		for (Hit<Address> hit : response.hits().hits()) {
			addrList.add(hit.source());
		}
		return addrList;
	}

	private Map<String, Long> makeAddrCdList(ElasticsearchClient esClient, ArrayList<Address> addrOldList)
			throws ElasticsearchException, IOException {

		List<FieldValue> fieldValueList = new ArrayList<FieldValue>();
		for (Address addr : addrOldList) {
			fieldValueList.add(FieldValue.of(addr.getFid()));
		}

		SearchResponse<Address> response = esClient.search(
				s -> s.index(CASH).size(0).query(q -> q.term(t -> t.field("valid").value(true)))
						.aggregations("filterByAddr",
								a -> a.filter(f -> f.terms(t -> t.field("addr").terms(t1 -> t1.value(fieldValueList))))
										.aggregations("termByAddr",
												a1 -> a1.terms(t3 -> t3.field("addr").size(addrOldList.size()))
														.aggregations("cdSum", a2 -> a2.sum(su -> su.field("cd"))))),
				Address.class);

		Map<String, Long> addrCdMap = new HashMap<String, Long>();

		List<StringTermsBucket> utxoBuckets = response.aggregations().get("filterByAddr").filter().aggregations()
				.get("termByAddr").sterms().buckets().array();

		for (StringTermsBucket bucket : utxoBuckets) {
			String addr = bucket.key();
			long value1 = (long) bucket.aggregations().get("cdSum").sum().value();
			addrCdMap.put(addr, value1);
		}
		return addrCdMap;
	}

	private void updateAddrMap(ElasticsearchClient esClient, Map<String, Long> addrNewCdMap, Map<String, Long> addrNewWeightMap) throws Exception {
		// TODO Auto-generated method stub
		Set<String> addrSet = addrNewCdMap.keySet();

		BulkRequest.Builder br = new BulkRequest.Builder();

		for (String addr : addrSet) {
			Map<String, Long> updateMap = new HashMap<String, Long>();
			long cd = addrNewCdMap.get(addr);
			long weight = addrNewWeightMap.get(addr);
			updateMap.put("cd", cd);
			updateMap.put("weight",weight);
			br.operations(o -> o.update(u -> u.index(ADDRESS).id(addr).action(a -> a.doc(updateMap))));
		}

		if(addrNewCdMap.size()>0)
			EsTools.bulkWithBuilder(esClient, br);
	}


}
