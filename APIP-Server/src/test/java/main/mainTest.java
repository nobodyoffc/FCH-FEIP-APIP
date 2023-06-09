package main;

import AesEcc.AES256;
import AesEcc.Envelop;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import FchClass.Cash;
import javaTools.BytesTools;
import keyTools.KeyTools;
import fcTools.ParseTools;
import fc_dsl.Sort;
import org.bitcoinj.core.ECKey;
import org.junit.Test;
import servers.EsTools;
import servers.NewEsClient;
import startAPIP.ConfigAPIP;
import startFCH.IndicesFCH;

import java.io.IOException;
import java.security.SignatureException;
import java.util.*;

public class mainTest {
    public static void main(String[] args) throws IOException {
        NewEsClient newEsClient = new NewEsClient();
        ElasticsearchClient esClient = null;
        ConfigAPIP configAPIP = new ConfigAPIP();
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);
            if (configAPIP.getEsIp() == null||configAPIP.getEsPort()==0) System.out.println("Es IP is null. Config first.");
            esClient = newEsClient.checkEsClient(esClient, configAPIP);
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
        SearchResponse<Cash> response = esClient.search(s -> s.index(IndicesFCH.CashIndex)
                        .size(size)
                        .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                        .trackTotalHits(t->t.enabled(true))
                        .query(q -> q.bool(b -> b
                                .should(m -> m.range(r -> r.field("spendHeight").gt(JsonData.of(lastHeight))))
                                .should(m1 -> m1.range(r1 -> r1.field("birthHeight").gt(JsonData.of(lastHeight))))))
                , Cash.class);
        for(Hit<Cash> item: response.hits().hits()){
            if (item.source() != null) {
                addrSet.add(item.source().getFid());
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
            response = esClient.search(s -> s.index(IndicesFCH.CashIndex)
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
                    addrSet.add(item.source().getFid());
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


    public void decrypt() throws Exception {
        String priKey = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        String msg = "test";
        String secKey = KeyTools.getPriKey32(priKey);
        Envelop env = new Envelop();
        env.setEciesSecKey(secKey);

        String cypher = env.encryptKey(msg);
        System.out.println("test cypher: "+cypher.length()+"\n"+cypher);
        System.out.println("decrypted:"+env.decryptKey(cypher));

        //
        String cd = cypher.substring(64, cypher.length());
        byte[] pk65Bytes = ("04"+cypher.substring(0,64)).getBytes();
        String pk33 = KeyTools.compressPK65ToPK33(pk65Bytes);
        String cypherJs = pk33+cd;
        Base64.Encoder encoder = Base64.getEncoder();
        System.out.println("pk33+cd: "+cypherJs.length()+"\n"+ cypherJs);

        byte  []cypherJsBytes = BytesTools.hexToByteArray(cypherJs);
        String cypherJsStr = new String(encoder.encode(cypherJsBytes));
        System.out.println("cypherJsStr: "+cypherJsStr.length()+"\n"+cypherJsStr);
        //

        String cypherFreeSign = "A9ynhf2BsBq3xOQzXCUwLgFA+kR7bE81qi2JZWt6svTQHQJgMOAg7IuLOc9BCc4Gn1bPc+7dRIWFph8JxQxKhtd86peBErFNZ/NAC7KPKVH2VL0Z6xf3U6vytnZm0SV1uQ==";        Base64.Decoder decoder = Base64.getDecoder();
        byte[] cpfsBytes = decoder.decode(cypherFreeSign);
        String cpfsHex = AES256.byteToHexString(cpfsBytes);
        System.out.println("cpfsHex: "+cpfsHex.length()+"\n"+cpfsHex);
        String cpfsPubKey = cpfsHex.substring(0, 66);


        System.out.println("pubkey33: "+cpfsPubKey);
        String cpfsFullPubKey = KeyTools.recoverPK33ToPK65(cpfsPubKey);
        System.out.println("pubKey65: "+cpfsFullPubKey);
        String newCypher = cpfsFullPubKey.substring(2, 130) + cpfsHex.substring(66, cpfsHex.length());
        System.out.println("newCypher: "+newCypher.length()+"\n"+newCypher);
       // env.decryptKey(newCypher);
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
