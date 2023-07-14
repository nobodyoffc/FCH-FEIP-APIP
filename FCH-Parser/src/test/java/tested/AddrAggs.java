package tested;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.IndicesNames;
import fchClass.Address;
import config.ConfigBase;
import servers.EsTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static tested.TestRollback.getEsClient;

public class AddrAggs {
    static Map<String,Long> diffMap = new HashMap<>();
    static Map<String,Long> negMap = new HashMap<>();
    static Map<String,Long> neg1Map = new HashMap<>();
    static int count = 0;
    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ElasticsearchClient esClient = getEsClient(new ConfigBase());

        System.out.println(esClient.info());
//        long lastHeight = 1616401;
//        rollback(esClient,lastHeight);
        getAllAddresses(esClient);
    }

    public static void getAllAddresses(ElasticsearchClient esClient) throws Exception {
        int size = EsTools.READ_MAX;
        System.out.println("begin...");
        SearchResponse<Address> response = esClient.search(
                s -> s.index(IndicesNames.ADDRESS).size(size).sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Asc))),
                Address.class);
        int hitSize = response.hits().hits().size();
        List<String> last = response.hits().hits().get(hitSize-1).sort();

        for (Hit<Address>hit:response.hits().hits()){
            System.out.println(hit.source().getFid());
        }

        while (hitSize>=size) {
            List<String> finalLast = last;
            response = esClient.search(
                    s -> s.index(IndicesNames.ADDRESS).size(size).sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Asc))).searchAfter(finalLast),
                    Address.class);
            hitSize = response.hits().hits().size();
            last = response.hits().hits().get(hitSize-1).sort();

            for (Hit<Address>hit:response.hits().hits()){
                System.out.println(hit.source().getFid());
            }
        }
    }
    public static void makeAddrBalance(ElasticsearchClient esClient) throws Exception {

        System.out.println("Make all balance of Addresses...");

        SearchResponse<Address> response = esClient.search(
                s -> s.index(IndicesNames.ADDRESS).size(EsTools.READ_MAX).sort(sort -> sort.field(f -> f.field("id"))),
                Address.class);

        ArrayList<Address> addrOldList = getResultAddrList(response);
        //TODO
        for(Address addr:addrOldList){
            if(addr.getBalance()<0)negMap.put(addr.getFid(),addr.getBalance());
        }
        Map<String, Long> addrNewMap = makeAddrList(esClient, addrOldList);
        //TODO
        checkBalance(esClient,addrNewMap);
        updateAddrMap(esClient, addrNewMap);

        while (true) {
            if (response.hits().hits().size() < EsTools.READ_MAX)
                break;
            Hit<Address> last = response.hits().hits().get(response.hits().hits().size() - 1);
            String lastId = last.id();
            response = esClient.search(s -> s.index(IndicesNames.ADDRESS).size(EsTools.READ_MAX)
                    .sort(sort -> sort.field(f -> f.field("id"))).searchAfter(lastId), Address.class);

            addrOldList = getResultAddrList(response);
            //TODO
            for(Address addr:addrOldList){
                if(addr.getBalance()<0)negMap.put(addr.getFid(),addr.getBalance());
            }
            addrNewMap = makeAddrList(esClient, addrOldList);

            //TODO
            checkBalance(esClient,addrNewMap);
            updateAddrMap(esClient, addrNewMap);
        }
    }

    private static void checkBalance(ElasticsearchClient esClient, Map<String, Long> addrNewMap) throws IOException {
        ArrayList<String> addrList = new ArrayList<>(addrNewMap.keySet());
        MgetResponse<Address> result = esClient.mget(m -> m.index(IndicesNames.ADDRESS).ids(addrList), Address.class);
        List<MultiGetResponseItem<Address>> docs = result.docs();
        for(MultiGetResponseItem<Address> doc:docs){
            if(doc.result().source().getBalance()!= addrNewMap.get(doc.result().source().getFid())){
                diffMap.put(doc.result().source().getFid(),doc.result().source().getBalance());
            };
            if(doc.result().source().getBalance()<0)neg1Map.put(doc.result().source().getFid(),doc.result().source().getBalance());
        }
    }

    private static void updateAddrMap(ElasticsearchClient esClient, Map<String, Long> addrNewMap) throws Exception {
        // TODO Auto-generated method stub
        Set<String> addrSet = addrNewMap.keySet();
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (String addr : addrSet) {
            Map<String, Long> updateMap = new HashMap<String, Long>();
            updateMap.put("balance", addrNewMap.get(addr));
            br.operations(o -> o.update(u -> u.index(IndicesNames.ADDRESS).id(addr).action(a -> a.doc(updateMap))));
        }
        BulkResponse result = EsTools.bulkWithBuilder(esClient, br);
        for(BulkResponseItem r : result.items()){
            if(!r.result().equals("noop"))count++;
        }
    }

    private static Map<String, Long> makeAddrList(ElasticsearchClient esClient, ArrayList<Address> addrOldList)
            throws ElasticsearchException, IOException {

        List<FieldValue> fieldValueList = new ArrayList<FieldValue>();
        for (Address addr : addrOldList) {
            fieldValueList.add(FieldValue.of(addr.getFid()));
        }

        SearchResponse<Address> response = esClient.search(
                s -> s.index(IndicesNames.CASH).size(0).query(q -> q.term(t -> t.field("valid").value(true)))
                        .aggregations("filterByAddr",
                                a -> a.filter(f -> f.terms(t -> t.field("addr").terms(t1 -> t1.value(fieldValueList))))
                                        .aggregations("termByAddr",
                                                a1 -> a1.terms(t3 -> t3.field("addr").size(addrOldList.size()))
                                                        .aggregations("valueSum", a2 -> a2.sum(su -> su.field("value"))))),
                Address.class);

        Map<String, Long> addrCdMap = new HashMap<String, Long>();

        List<StringTermsBucket> utxoBuckets = response.aggregations().get("filterByAddr").filter().aggregations()
                .get("termByAddr").sterms().buckets().array();

        for (StringTermsBucket bucket : utxoBuckets) {
            String addr = bucket.key();
            long value1 = (long) bucket.aggregations().get("valueSum").sum().value();
            addrCdMap.put(addr, value1);
        }
        return addrCdMap;
    }

    private static ArrayList<Address> getResultAddrList(SearchResponse<Address> response) {
        ArrayList<Address> addrList = new ArrayList<Address>();
        for (Hit<Address> hit : response.hits().hits()) {
            addrList.add(hit.source());
        }
        return addrList;
    }
}
