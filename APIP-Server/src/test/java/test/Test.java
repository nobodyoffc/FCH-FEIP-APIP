package test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import fcTools.ParseTools;
import identity.CidHist;
import servers.EsTools;
import servers.NewEsClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        ElasticsearchClient esClient = null;
        String ip = "127.0.0.1";
        int port = 9200;
        NewEsClient newEsClient = new NewEsClient();
        esClient = newEsClient.getClientHttp(ip, port);
        System.out.println(esClient.info().toString());

        List<FieldValue> itemValueList = new ArrayList<FieldValue>();
        List<String> itemIdList = new ArrayList<>();
        itemIdList.add("FEk41Kqjar45fLDriztUDTUkdki7mmcjWK");

        for (String v : itemIdList) {
            itemValueList.add(FieldValue.of(v));
        }

        List<String> lastSort = new ArrayList<String>();
        String index="cid_history";
        String termsField = "signer";
//
//        List<SortOptions> soList = new ArrayList<>();
//
//
//
//        FieldSort fs1 = FieldSort.of(f->f.field("height").order(SortOrder.Asc));
//        SortOptions so1 = SortOptions.of(s->s.field(fs1));
//        soList.add(so1);
//
//        FieldSort fs2 = FieldSort.of(f->f.field("index").order(SortOrder.Asc));
//        SortOptions so2 = SortOptions.of(s->s.field(fs2));
//        soList.add(so2);
//
//
//
//
//        SearchResponse<CidHist> result = esClient.search(s -> s.index(index)
//                .query(q -> q.terms(t -> t.field(termsField).terms(t1 -> t1.value(itemValueList))))
//                .size(2)
//                .sort(soList)
//                , CidHist.class);
//
//        List<CidHist> cidHistList = new ArrayList<>();
//
//        for(Hit<CidHist> hit : result.hits().hits()){
//            cidHistList.add(hit.source());
//        }
//
//        ParseTools.gsonPrint(cidHistList);
//        ParseTools.gsonPrint(result.hits().hits().get(cidHistList.size()-1).sort());

        ArrayList<String> signerList = new ArrayList<>();
        signerList.add("FEk41Kqjar45fLDriztUDTUkdki7mmcjWK");
        List<CidHist> reparseList = EsTools.getHistsForReparse(esClient, "cid_history","signer",signerList, CidHist.class);
        ParseTools.gsonPrint(reparseList);
    }
}
