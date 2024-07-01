package main;

import apipClass.ResponseBody;
import apipClass.Sort;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fchClass.Cash;
import jakarta.json.Json;
import javaTools.BytesTools;
import javaTools.JsonTools;
import org.bitcoinj.core.ECKey;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import esTools.EsTools;
import esTools.NewEsClient;
import config.ConfigAPIP;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.SignatureException;
import java.util.*;

import static constants.IndicesNames.CASH;

public class mainTest {
    public static void main(String[] args) throws IOException {
        List<Cash> cashList = new ArrayList<>();
        Cash cash = new Cash();
        cash.setIssuer("me");
        cash.setCashId("1");
        Cash cash1 = new Cash();
        cash1.setIssuer("you");
        cash1.setCashId("2");
        cashList.add(cash1);
        cashList.add(cash);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setCode(0);
        responseBody.setMessage("OK");
        responseBody.setData(cashList);
        String resStr = JsonTools.getString(responseBody);

        ResponseBody responseBody1 = new Gson().fromJson(resStr,ResponseBody.class);
//        String cashListStr = JsonTools.getString(cashList);
        List<Cash> newCashList = getList(responseBody1.getData(), Cash.class);
        JsonTools.gsonPrint(newCashList);

        List<Cash> newCashList1 =objectToList(cashList,Cash.class);
        JsonTools.gsonPrint(newCashList1);

        Map<String, Cash> map = listToMap(newCashList, "cashId");
//        String mapStr = JsonTools.getString(map);
        Map<String, Cash> map1 = objectToMap(map, String.class, Cash.class);
        JsonTools.gsonPrint(map1);

//        List<Cash> newCashList2 = getCashList(cashListStr);
//        JsonTools.gsonPrint(newCashList2);
    }

//    public static <K, T> Map<K, T> objectToMap(String jsonString, Class<K> kClass, Class<T> tClass) {
//        Gson gson = new Gson();
//        Type type = TypeToken.getParameterized(Map.class, kClass, tClass).getType();
//        return gson.fromJson(jsonString, type);
//    }

    public static <K, T> Map<K, T> objectToMap(Object obj, Class<K> kClass, Class<T> tClass) {
        Gson gson = new Gson();
        Type type = TypeToken.getParameterized(Map.class, kClass, tClass).getType();
        String jsonString = gson.toJson(obj);
        Map<K, T> tempMap = gson.fromJson(jsonString, type);
        return new HashMap<>(tempMap);
    }

    public static <T> List<T> objectToList(Object obj, Class<T> tClass) {
        Gson gson = new Gson();
        Type type = TypeToken.getParameterized(ArrayList.class, tClass).getType();
        String jsonString = gson.toJson(obj);
        List<T> tempList = gson.fromJson(jsonString, type);
        return new ArrayList<>(tempList);
    }

    public static <T, K> Map<K, T> listToMap(List<T> list, String keyFieldName) {
        Map<K, T> resultMap = new HashMap<>();
        try {
            if (list != null && !list.isEmpty()) {
                Field keyField = list.get(0).getClass().getDeclaredField(keyFieldName);
                keyField.setAccessible(true);

                for (T item : list) {
                    @SuppressWarnings("unchecked")
                    K key = (K) keyField.get(item);
                    resultMap.put(key, item);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
    public static <T> List<T> getList(Object responseData, Class<T> clazz) {
        Type type = TypeToken.getParameterized(ArrayList.class, clazz).getType();
        Gson gson = new Gson();
        String json = gson.toJson(responseData);
        return gson.fromJson(json, type);
    }
    public static List<Cash> getCashList(Object responseData) {
        Type t = new TypeToken<ArrayList<Cash>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(responseData), t);
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
        JsonTools.gsonPrint(addrSet);
        return new ArrayList<>(addrSet);
    }

    @Test
    public void testSort(){
        Sort sort = new Sort();
        sort.setField("id");
        sort.setOrder("asc");
        JsonTools.gsonPrint(sort);

        Sort sort1 = new Sort();
        sort1.setField("addr");
        sort1.setOrder("desc");

        ArrayList<Sort> sortL = new ArrayList<>();
        sortL.add(sort);
        sortL.add(sort1);

        JsonTools.gsonPrint(sortL);
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
