package mainTest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import FchClass.Address;
import fcTools.ParseTools;
import servers.NewEsClient;
import startFCH.IndicesFCH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

class EsTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        NewEsClient newEsClient = new NewEsClient();
        //ConfigBase configBase = new ConfigBase(br);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ElasticsearchClient esClient = null;
        esClient = newEsClient.getClientHttp("127.0.0.1", 9200);

        long lastHeight = 30;
        int size = 2;

        SearchResponse<Address> response = esClient.search(s -> s
                        .index(IndicesFCH.AddressIndex)
                        .size(size)
                        .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                        .query(q -> q.range(r -> r.field("lastHeight").gt(JsonData.of(lastHeight))))
                , Address.class);
        List<Hit<Address>> hitList = response.hits().hits();
        if (hitList.size() == 0) return;
        ArrayList<String> addrAllList = new ArrayList<String>();
        for (Hit<Address> hit : hitList) {
            addrAllList.add(hit.source().getFid());
        }
        while (true) {
            if (hitList.size() < size) break;
            response = esClient.search(s -> s
                            .index(IndicesFCH.AddressIndex)
                            .size(size)
                            .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                            .searchAfter(addrAllList.get(addrAllList.size() - 1))
                            .query(q -> q.range(r -> r.field("lastHeight").gt(JsonData.of(lastHeight))))
                    , Address.class);
            hitList = response.hits().hits();
            for (Hit<Address> hit : hitList) {
                addrAllList.add(hit.source().getFid());
            }
        }

        ParseTools.gsonPrint(addrAllList);
    }
}
