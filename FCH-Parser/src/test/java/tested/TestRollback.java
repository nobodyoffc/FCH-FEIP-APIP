package tested;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import FchClass.Address;
import FchClass.Cash;
import FchClass.OpReturn;
import javaTools.BytesTools;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.ChainParser;
import parser.OpReFileTools;
import servers.ConfigBase;
import servers.EsTools;
import servers.NewEsClient;
import startFCH.IndicesFCH;
import writeEs.RollBacker;

import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestRollback {

    private static final Logger log = LoggerFactory.getLogger(RollBacker.class);
    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ElasticsearchClient esClient = getEsClient(new ConfigBase());

        System.out.println(esClient.info());
        long lastHeight = 1694001;
//        rollback(esClient,lastHeight);

        ArrayList<String> addrList = readEffectedAddresses(esClient, lastHeight);
        ParseTools.gsonPrint(addrList);

        br.close();
    }
    public static boolean rollback(ElasticsearchClient esClient, long lastHeight) throws Exception {

        long bestHeight = getBestHeight(esClient);
        if(bestHeight==lastHeight) {
            System.out.println("The height you are rollbacking to is the best height:" +bestHeight );
            return true;
        }

        System.out.println("Rollback to : "+ lastHeight  + " ...");

        System.out.println("Recover spent cashes...");
        recoverStxoToUtxo(esClient, lastHeight);
        System.out.println("Cash recovered. Wait for 2 seconds...");
        TimeUnit.SECONDS.sleep(2);
        ArrayList<String> addrList = readEffectedAddresses(esClient, lastHeight);
        try {
            System.out.println("Delete blocks...");
            deleteBlocks(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete blockHas...");
            deleteBlockHas(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete TX...");
            deleteTxs(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete TxHas...");
            deleteTxHas(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete OpReturn...");
            deleteOpReturns(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete cash...");
            deleteUtxos(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete address...");
            deleteNewAddresses(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        try {
            System.out.println("Delete block mark...");
            deleteBlockMarks(esClient, lastHeight);
        } catch (Exception e) {
            log.error("Error when deleting in rollback",e);
            e.printStackTrace();
        }
        System.out.println("Data deleted. Wait for 2 seconds...");
        TimeUnit.SECONDS.sleep(2);
        System.out.println("Recover address...");
        Map<String, Map<String, Long>> aggsMaps = aggsTxoByAddrs(esClient,lastHeight, addrList);
        bulkUpdateAddr(esClient,aggsMaps,lastHeight);
        recordInOpReturnFile(lastHeight);
        System.out.println("Prepare parsing again. Wait for 2 seconds...");
        TimeUnit.SECONDS.sleep(2);
        return true;
    }

    public static long getBestHeight(ElasticsearchClient esClient) throws ElasticsearchException, IOException {
        SearchResponse<Void> response = esClient.search(s->s.index(IndicesFCH.BlockIndex).aggregations("bestHeight", a->a.max(m->m.field("height"))), void.class);
        long bestHeight = (long)response.aggregations().get("bestHeight").max().value();
        return bestHeight;
    }
    public static ElasticsearchClient getEsClient(ConfigBase config) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        ElasticsearchClient esClient = null;
        NewEsClient newEsClient = new NewEsClient();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        if (config == null || config.getEsIp() == null||config.getBlockFilePath() == null) {
            config = new ConfigBase();
            config.getClassInstanceFromFile(br,ConfigBase.class);
        }

        config.writeConfigToFile();
        String path = config.getBlockFilePath();

        esClient = newEsClient.checkEsClient(esClient, config);
        if (esClient == null) {
            newEsClient.shutdownClient();
            return null;
        }
        return esClient;
    }
    private static void recoverAddress(ElasticsearchClient esClient, long lastHeight) throws Exception {

        ArrayList<String> addrList = readAllAddrs(esClient,lastHeight);
        if (addrList == null) return;
        //TODO
        ParseTools.gsonPrint(addrList);

        Map<String, Map<String, Long>> aggsMaps = aggsTxoByAddrs(esClient,lastHeight, addrList);

        System.out.println();
        ParseTools.gsonPrint(aggsMaps);

        bulkUpdateAddr(esClient,aggsMaps,lastHeight);
    }

    private static ArrayList<String> readEffectedAddresses(ElasticsearchClient esClient, long lastHeight) throws IOException {
        Set<String> addrSet = new HashSet<>();
        int size = EsTools.READ_MAX;
        SearchResponse<Cash> response = esClient.search(s -> s.index(IndicesFCH.CashIndex)
                        .size(size)
                        .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                        .query(q -> q.bool(b -> b
                                .should(m -> m.range(r -> r.field("spendHeight").gt(JsonData.of(lastHeight))))
                                .should(m1 -> m1.range(r1 -> r1.field("birthHeight").gt(JsonData.of(lastHeight))))))
                , Cash.class);
        for(Hit<Cash>item: response.hits().hits()){
            if (item.source() != null) {
                addrSet.add(item.source().getFid());
            }
        }
        int hitSize = response.hits().hits().size();
        List<String> last = response.hits().hits().get(hitSize - 1).sort();
        while(hitSize>=size){
            List<String> finalLast = last;
            response = esClient.search(s -> s.index(IndicesFCH.CashIndex)
                            .size(size)
                            .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                            .searchAfter(finalLast)
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
        return new ArrayList<>(addrSet);
    }
    private static ArrayList<String> readAllAddrs(ElasticsearchClient esClient, long lastHeight) throws ElasticsearchException, IOException {

        int size = EsTools.READ_MAX;

        SearchResponse<Address> response = esClient.search(s -> s
                        .index(IndicesFCH.AddressIndex)
                        .size(size)
                        .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
                        .query(q -> q.range(r -> r.field("lastHeight").gt(JsonData.of(lastHeight))))
                , Address.class);
        List<Hit<Address>> hitList = response.hits().hits();
        if (hitList.size() == 0) return null;
        ArrayList<String> addrAllList = new ArrayList<String> ();
        for (Hit<Address> hit : hitList) {
            addrAllList.add(hit.source().getFid());
        }
        while (hitList.size() >= size) {
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
        return addrAllList;
    }

    private static Map<String, Map<String, Long>> aggsTxoByAddrs(ElasticsearchClient esClient, long lastHeight, List<String> addrAllList) throws ElasticsearchException, IOException {

        List<FieldValue> fieldValueList = new ArrayList<FieldValue>();

        Iterator<String> iter = addrAllList.iterator();
        while(iter.hasNext())
            fieldValueList.add(FieldValue.of(iter.next()));

        SearchResponse<Void> response = esClient.search(s->s
                        .index(IndicesFCH.CashIndex)
//                        .query(q->q.bool(b->b
//                                .should(m->m.range(r->r.field("spendHeight").lte(JsonData.of(lastHeight))))
//                                .should(m1->m1.range(r1->r1.field("birthHeight").lte(JsonData.of(lastHeight)))))
//                        )
                        .size(0)
                        .aggregations("addrFilterAggs",a->a
                                .filter(f->f.terms(t->t
                                        .field("addr")
                                        .terms(t1->t1
                                                .value(fieldValueList))))
                                .aggregations("utxoFilterAggs",a0->a0
                                        .filter(f1->f1.match(m->m.field("valid").query(true)))
                                        .aggregations("utxoAggs",a3->a3
                                                .terms(t2->t2
                                                        .field("addr")
                                                        .size(200000))
                                                .aggregations("utxoSum",t5->t5
                                                        .sum(s1->s1
                                                                .field("value"))))
                                )
                                .aggregations("stxoFilterAggs",a0->a0
                                        .filter(f1->f1.match(m->m.field("valid").query(false)))
                                        .aggregations("stxoAggs",a1->a1
                                                .terms(t2->t2
                                                        .field("addr")
                                                        .size(200000))
                                                .aggregations("stxoSum",t3->t3
                                                        .sum(s1->s1
                                                                .field("value")))
                                                .aggregations("cddSum",t4->t4
                                                        .sum(s1->s1
                                                                .field("cdd")))
                                        )
                                )
                        )
                , void.class);

        Map<String, Long> utxoSumMap = new HashMap<String, Long>();
        Map<String, Long> stxoSumMap = new HashMap<String, Long>();
        Map<String, Long> cddMap = new HashMap<String, Long>();
        Map<String, Long> utxoCountMap = new HashMap<String, Long>();

        List<StringTermsBucket> utxoBuckets = response.aggregations()
                .get("addrFilterAggs")
                .filter()
                .aggregations()
                .get("utxoFilterAggs")
                .filter()
                .aggregations()
                .get("utxoAggs")
                .sterms()
                .buckets().array();

        for (StringTermsBucket bucket: utxoBuckets) {
            String addr = bucket.key();
            long value1 = (long)bucket.aggregations().get("utxoSum").sum().value();
            utxoCountMap.put(addr, bucket.docCount());
            utxoSumMap.put(addr, value1);
        }

        List<StringTermsBucket> stxoBuckets = response.aggregations()
                .get("addrFilterAggs")
                .filter()
                .aggregations()
                .get("stxoFilterAggs")
                .filter()
                .aggregations()
                .get("stxoAggs")
                .sterms()
                .buckets().array();

        for (StringTermsBucket bucket: stxoBuckets) {
            String addr = bucket.key();
            long value1 = (long)bucket.aggregations().get("stxoSum").sum().value();
            stxoSumMap.put(addr, value1);
            long cddSum = (long)bucket.aggregations().get("cddSum").sum().value();
            cddMap.put(addr, cddSum);
        }

        Map<String,Map<String, Long>> resultMapMap = new HashMap<String,Map<String, Long>>();
        resultMapMap.put("utxoSum",utxoSumMap);
        resultMapMap.put("stxoSum",stxoSumMap);
        resultMapMap.put("cdd",cddMap);
        resultMapMap.put("utxoCount",utxoCountMap);
        return resultMapMap;
    }

    private static void bulkUpdateAddr(ElasticsearchClient esClient, Map<String, Map<String, Long>> aggsMaps,long lastHeight) throws ElasticsearchException, IOException {
        // TODO Auto-generated method stub


        Map<String, Long> utxoSumMap = aggsMaps.get("utxoSum");
        Map<String, Long> stxoSumMap = aggsMaps.get("stxoSum");
        Map<String, Long> stxoCddMap = aggsMaps.get("cdd");
        Map<String, Long> utxoCountMap = aggsMaps.get("utxoCount");

        Set<String> utxoAddrSet = utxoSumMap.keySet();
        Set<String> stxoAddrSet = stxoSumMap.keySet();
        Set<String> addrSet = new HashSet<String>();
        addrSet.addAll(utxoAddrSet);
        addrSet.addAll(stxoAddrSet);

        if(addrSet.isEmpty())return;

        BulkRequest.Builder br = new BulkRequest.Builder();

        for(String addr : addrSet) {

            Map<String,Object> updateMap = new HashMap<String,Object>();

            if(utxoSumMap.get(addr)!=null) {
                updateMap.put("balance", utxoSumMap.get(addr));
                updateMap.put("cash", utxoCountMap.get(addr));
            }else {
                updateMap.put("balance", 0);
                updateMap.put("cash", 0);
            }

            if(stxoSumMap.get(addr)!=null) {
                updateMap.put("expend", stxoSumMap.get(addr));
            }else {
                updateMap.put("expend", 0);
            }

            if(stxoCddMap.get(addr)!=null) {
                updateMap.put("cdd", stxoCddMap.get(addr));
            }else {
                updateMap.put("cdd", 0);
            }

            if(utxoSumMap.get(addr)!=null) {
                if(stxoSumMap.get(addr)!=null) {
                    updateMap.put("income", utxoSumMap.get(addr)+stxoSumMap.get(addr));
                }else {
                    updateMap.put("income", utxoSumMap.get(addr)+0);
                }
            }else {
                if(stxoSumMap.get(addr)!=null) {
                    updateMap.put("income", 0+stxoSumMap.get(addr));
                }
            }

            updateMap.put("lastHeight",lastHeight);
            br.operations(o1->o1.update(u->u
                    .index(IndicesFCH.AddressIndex)
                    .id(addr)
                    .action(a->a
                            .doc(updateMap)))
            );
        }
        br.timeout(t->t.time("600s"));
        esClient.bulk(br.build());
    }

    private static void recoverStxoToUtxo(ElasticsearchClient esClient, long lastHeight) throws Exception {
        esClient.updateByQuery(u->u
                .index(IndicesFCH.CashIndex)
                .query(q->q.bool(b->b
                        .must(m->m.range(r->r.field("spendHeight").gt(JsonData.of(lastHeight))))
                        .must(m1->m1.range(r1->r1.field("birthHeight").lte(JsonData.of(lastHeight))))))
                .script(s->s.inline(i->i.source(
                        "ctx._source.spendTime=0;"
                                + "ctx._source.spendTxId=null;"
                                + "ctx._source.spendHeight=0;"
                                + "ctx._source.spendIndex=0;"
                                + "ctx._source.unlockScript=null;"
                                + "ctx._source.sigHash=null;"
                                + "ctx._source.sequence=null;"
                                + "ctx._source.cdd=0;"
                                + "ctx._source.valid=true;"
                )))
        );
    }

    private static void deleteOpReturns(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.OpReturnIndex,"height",lastHeight);
    }
    private static void deleteBlocks(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.BlockIndex,"height",lastHeight);
    }
    private static void deleteBlockHas(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.BlockHasIndex,"height",lastHeight);
    }
    private static void deleteTxHas(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.TxHasIndex,"height",lastHeight);
    }
    private static void deleteTxs(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.TxIndex,"height",lastHeight);
    }
    private static void deleteUtxos(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.CashIndex,"birthHeight",lastHeight);
    }
    private static void deleteNewAddresses(ElasticsearchClient esClient, long lastHeight) throws Exception {
        deleteHeigherThan(esClient, IndicesFCH.AddressIndex,"birthHeight",lastHeight);
    }
    private static void deleteBlockMarks(ElasticsearchClient esClient, long lastHeight) throws IOException {
        esClient.deleteByQuery(d->d
                .index(IndicesFCH.BlockMarkIndex)
                .query(q->q
                        .bool(b->b
                                .should(s->s
                                        .range(r->r
                                                .field("height")
                                                .gt(JsonData.of(lastHeight))))
                                .should(s1->s1
                                        .range(r1->r1
                                                .field("orphanHeight")
                                                .gt(JsonData.of(lastHeight))))))
        );
    }
    private static void deleteHeigherThan(ElasticsearchClient esClient, String index, String rangeField,long lastHeight) throws Exception {

        esClient.deleteByQuery(d->d
                .index(index)
                .query(q->q
                        .range(r->r
                                .field(rangeField)
                                .gt(JsonData.of(lastHeight))))
        );

    }

    private static void recordInOpReturnFile(long lastHeight) throws IOException {

        String fileName = ChainParser.OpRefileName;
        File opFile;
        FileOutputStream opos;

        while(true) {
            opFile = new File(fileName);
            if(opFile.length()>251658240) {
                fileName =  OpReFileTools.getNextFile(fileName);
            }else break;
        }
        if(opFile.exists()) {
            opos = new FileOutputStream(opFile,true);
        }else {
            opos = new FileOutputStream(opFile);
        }

        OpReturn opRollBack = new OpReturn();//rollbackMarkInOpreturn
        opRollBack.setHeight(lastHeight);

        ArrayList<byte[]> opArrList = new ArrayList<byte[]>();
        opArrList.add(BytesTools.intToByteArray(40));
        opArrList.add("Rollback........................".getBytes());
        opArrList.add(BytesTools.longToBytes(opRollBack.getHeight()));

        opos.write(BytesTools.bytesMerger(opArrList));
        opos.flush();
        opos.close();
    }

//    private static ArrayList<String> readAllAddrs(ElasticsearchClient esClient, long lastHeight) throws ElasticsearchException, IOException {
//
//        int size = EsTools.READ_MAX;
//
//        SearchResponse<Address> response = esClient.search(s -> s
//                        .index(IndicesFCH.AddressIndex)
//                        .size(size)
//                        .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
//                        .query(q -> q.range(r -> r.field("lastHeight").gt(JsonData.of(lastHeight))))
//                , Address.class);
//        List<Hit<Address>> hitList = response.hits().hits();
//        if (hitList.size() == 0) return null;
//        ArrayList<String> addrAllList = new ArrayList<String> ();
//        for (Hit<Address> hit : hitList) {
//            addrAllList.add(hit.source().getId());
//        }
//        while (true) {
//            if (hitList.size() < size) break;
//            response = esClient.search(s -> s
//                            .index(IndicesFCH.AddressIndex)
//                            .size(size)
//                            .sort(s1 -> s1.field(f -> f.field("id").order(SortOrder.Asc)))
//                            .searchAfter(addrAllList.get(addrAllList.size() - 1))
//                            .query(q -> q.range(r -> r.field("lastHeight").gt(JsonData.of(lastHeight))))
//                    , Address.class);
//            hitList = response.hits().hits();
//            for (Hit<Address> hit : hitList) {
//                addrAllList.add(hit.source().getId());
//            }
//        }
//        return addrAllList;
//    }
//
//    private static Map<String, Map<String, Long>> aggsTxoByAddrs(ElasticsearchClient esClient, long lastHeight, List<String> addrAllList) throws ElasticsearchException, IOException {
//
//        List<FieldValue> fieldValueList = new ArrayList<FieldValue>();
//
//        Iterator<String> iter = addrAllList.iterator();
//        while(iter.hasNext())
//            fieldValueList.add(FieldValue.of(iter.next()));
//
//        SearchResponse<Void> response = esClient.search(s->s
//                        .index(IndicesFCH.CashIndex)
////                        .query(q->q.bool(b->b
////                                .should(m->m.range(r->r.field("spendHeight").lte(JsonData.of(lastHeight))))
////                                .should(m1->m1.range(r1->r1.field("birthHeight").lte(JsonData.of(lastHeight)))))
////                        )
//                        .size(0)
//                        .aggregations("addrFilterAggs",a->a
//                                .filter(f->f.terms(t->t
//                                        .field("addr")
//                                        .terms(t1->t1
//                                                .value(fieldValueList))))
//                                .aggregations("utxoFilterAggs",a0->a0
//                                        .filter(f1->f1.match(m->m.field("spendHeight").query(0)))
//                                        .aggregations("balanceAggs",a3->a3
//                                                .terms(t2->t2
//                                                        .field("addr")
//                                                        .size(200000))
//                                                .aggregations("balanceSum",t5->t5
//                                                        .sum(s1->s1
//                                                                .field("value"))))
//                                )
//                                .aggregations("stxoFilterAggs",a0->a0
//                                        .filter(f1->f1.range(r4->r4.field("spendHeight").gt(JsonData.of(0))))
//                                        .aggregations("expendAggs",a1->a1
//                                                .terms(t2->t2
//                                                        .field("addr")
//                                                        .size(200000))
//                                                .aggregations("spendSum",t3->t3
//                                                        .sum(s1->s1
//                                                                .field("value")))
//                                                .aggregations("cddSum",t4->t4
//                                                        .sum(s1->s1
//                                                                .field("cdd")))
//                                        )
//                                )
//                        )
//                , void.class);
//
//        Map<String, Long> balanceMap = new HashMap<String, Long>();
//        Map<String, Long> expendMap = new HashMap<String, Long>();
//        Map<String, Long> cddMap = new HashMap<String, Long>();
//        Map<String, Long> utxoCountMap = new HashMap<String, Long>();
//
//        List<StringTermsBucket> utxoBuckets = response.aggregations()
//                .get("addrFilterAggs")
//                .filter()
//                .aggregations()
//                .get("utxoFilterAggs")
//                .filter()
//                .aggregations()
//                .get("balanceAggs")
//                .sterms()
//                .buckets().array();
//
//        for (StringTermsBucket bucket: utxoBuckets) {
//            String addr = bucket.key();
//            long value1 = (long)bucket.aggregations().get("balanceSum").sum().value();
//            utxoCountMap.put(addr, bucket.docCount());
//            balanceMap.put(addr, value1);
//        }
//
//        List<StringTermsBucket> stxoBuckets = response.aggregations()
//                .get("addrFilterAggs")
//                .filter()
//                .aggregations()
//                .get("stxoFilterAggs")
//                .filter()
//                .aggregations()
//                .get("expendAggs")
//                .sterms()
//                .buckets().array();
//
//        for (StringTermsBucket bucket: stxoBuckets) {
//            String addr = bucket.key();
//            long value1 = (long)bucket.aggregations().get("spendSum").sum().value();
//            expendMap.put(addr, value1);
//            long cddSum = (long)bucket.aggregations().get("cddSum").sum().value();
//            cddMap.put(addr, cddSum);
//        }
//
//        Map<String,Map<String, Long>> resultMapMap = new HashMap<String,Map<String, Long>>();
//        resultMapMap.put("balance",balanceMap);
//        resultMapMap.put("spend",expendMap);
//        resultMapMap.put("cdd",cddMap);
//        resultMapMap.put("utxoCount",utxoCountMap);
//        return resultMapMap;
//    }
//
//    private static void bulkUpdateAddr(ElasticsearchClient esClient, Map<String, Map<String, Long>> aggsMaps) throws ElasticsearchException, IOException {
//        // TODO Auto-generated method stub
//
//
//        Map<String, Long> utxoSumMap = aggsMaps.get("balance");
//        Map<String, Long> stxoSumMap = aggsMaps.get("spend");
//        Map<String, Long> stxoCddMap = aggsMaps.get("cdd");
//        Map<String, Long> utxoCountMap = aggsMaps.get("utxoCount");
//
//        Set<String> ukset = utxoSumMap.keySet();
//        Set<String> skset = stxoSumMap.keySet();
//        Set<String> addrSet = new HashSet<String>();
//        addrSet.addAll(ukset);
//        addrSet.addAll(skset);
//
//        if(addrSet.isEmpty())return;
//
//        BulkRequest.Builder br = new BulkRequest.Builder();
//
//        for(String addr : addrSet) {
//
//            Map<String,Object> updateMap = new HashMap<String,Object>();
//
//            if(utxoSumMap.get(addr)!=null) {
//                updateMap.put("balance", utxoSumMap.get(addr));
//                updateMap.put("cash", utxoCountMap.get(addr));
//            }else {
//                updateMap.put("balance", 0);
//            }
//
//            if(stxoSumMap.get(addr)!=null) {
//                updateMap.put("expend", stxoSumMap.get(addr));
//            }else {
//                updateMap.put("expend", 0);
//            }
//
//            if(stxoCddMap.get(addr)!=null) {
//                updateMap.put("cdd", stxoCddMap.get(addr));
//            }else {
//                updateMap.put("cdd", 0);
//            }
//
//            if(utxoSumMap.get(addr)!=null) {
//                if(stxoSumMap.get(addr)!=null) {
//                    updateMap.put("income", utxoSumMap.get(addr)+stxoSumMap.get(addr));
//                }else {
//                    updateMap.put("income", utxoSumMap.get(addr)+0);
//                }
//            }else {
//                if(stxoSumMap.get(addr)!=null) {
//                    updateMap.put("income", 0+stxoSumMap.get(addr));
//                }
//            }
//
//            br.operations(o1->o1.update(u->u
//                    .index(IndicesFCH.AddressIndex)
//                    .id(addr)
//                    .action(a->a
//                            .doc(updateMap)))
//            );
//        }
//        br.timeout(t->t.time("600s"));
//        esClient.bulk(br.build());
//    }
}
