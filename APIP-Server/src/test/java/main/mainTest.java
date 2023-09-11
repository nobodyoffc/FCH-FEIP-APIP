package main;

import apipClass.Sort;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import fchClass.Cash;
import javaTools.BytesTools;
import fcTools.ParseTools;
import org.bitcoinj.core.ECKey;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import esTools.EsTools;
import esTools.NewEsClient;
import config.ConfigAPIP;

import java.io.IOException;
import java.security.SignatureException;
import java.util.*;

import static constants.IndicesNames.CASH;

public class mainTest {
    public static void main(String[] args) throws IOException {
        NewEsClient newEsClient = new NewEsClient();
        ElasticsearchClient esClient = null;
        ConfigAPIP configAPIP = new ConfigAPIP();
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0) System.out.println("Es IP is null. Config first.");
            esClient = newEsClient.getEsClientSilent(configAPIP,new Jedis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        InfoResponse info = esClient.info();
        System.out.println(info.toString());

        //RollBacker.readEffectedAddresses(esClient,1517963);

    }

    private static ArrayList<String> readEffectedAddresses(ElasticsearchClient esClient, long lastHeight) throws IOException {
        Set<String> addrSet = new HashSet<>();
        int size = EsTools.READ_MAX;
        SearchResponse<Cash> response = esClient.search(s -> s.index(CASH)
                        .size(size)
                        .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                        .trackTotalHits(t->t.enabled(true))
                        .query(q -> q.bool(b -> b
                                .should(m -> m.range(r -> r.field("spendHeight").gt(JsonData.of(lastHeight))))
                                .should(m1 -> m1.range(r1 -> r1.field("birthHeight").gt(JsonData.of(lastHeight))))))
                , Cash.class);
        for(Hit<Cash> item: response.hits().hits()){
            if (item.source() != null) {
                addrSet.add(item.source().getOwner());
            }
        }
        int hitSize = response.hits().hits().size();
        List<String> last;
        //TODO
        System.out.println("LastHeight: "+ lastHeight);
        System.out.println("Total hits: "+response.hits().total());

        last= response.hits().hits().get(hitSize - 1).sort();
        while(hitSize>=size){
            List<String> finalLast = last;
            response = esClient.search(s -> s.index(CASH)
                            .size(size)
                            .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                            .searchAfter(finalLast)
                            .trackTotalHits(t->t.enabled(true))
                            .query(q -> q.bool(b -> b
                                    .should(m -> m.range(r -> r.field("spendHeight").gt(JsonData.of(lastHeight))))
                                    .should(m1 -> m1.range(r1 -> r1.field("birthHeight").gt(JsonData.of(lastHeight))))))
                    , Cash.class);
            for(Hit<Cash>item: response.hits().hits()){
                if (item.source() != null) {
                    addrSet.add(item.source().getOwner());
                }
            }
            hitSize = response.hits().hits().size();
            last = response.hits().hits().get(hitSize - 1).sort();
        }
        ParseTools.gsonPrint(addrSet);
        return new ArrayList<>(addrSet);
    }

    @Test
    public void testSort(){
        Sort sort = new Sort();
        sort.setField("id");
        sort.setOrder("asc");
        ParseTools.gsonPrint(sort);

        Sort sort1 = new Sort();
        sort1.setField("addr");
        sort1.setOrder("desc");

        ArrayList<Sort> sortL = new ArrayList<>();
        sortL.add(sort);
        sortL.add(sort1);

        ParseTools.gsonPrint(sortL);
    }

    @Test
    public void unescape(){
        String raw = "IOJ9LAxHfjQ8f4vke/mJ7ghxIytsJY54X8OuGQJm/wvmE4CYrMyOg6NycPbcdNTKDZUcK8I/wzaI9yi3QWthCFs\\u003d\\u003d";
        String nice = raw.replaceAll("u003d","=");
        String nice1 = nice.replace("\\","");

        raw = raw.replace("\\u003d","=");
        System.out.println(raw);





        ECKey eckey = new ECKey();

    }

    @Test
    public void sign() throws SignatureException {
        String addr = "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK";
        String pubKey = "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a";
        String message = "{\"url\":\"http://localhost:8080/APIPserver/connect\",\"time\":1677570126827}";
        String signature = "H66Di4UHi38+NLRM9bdLCE/AXYkBRL8+m47J2LHQHIy6br56pBGA/lEETH7y/dizBZ5furvlMg63qLyRzLNpJLg\u003d";
        byte[] pubKeyBytes = BytesTools.hexToByteArray (pubKey);
        ECKey ecKey = ECKey.fromPublicOnly(pubKeyBytes);

        String signPubKey = ECKey.signedMessageToKey(message, signature).getPublicKeyAsHex();
        System.out.println("sign public key: "+ signPubKey);

        // ecKey.verify();
    }
}
