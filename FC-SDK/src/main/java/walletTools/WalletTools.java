package walletTools;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import com.googlecode.jsonrpc4j.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import constants.*;
import cryptoTools.Hash;
import esTools.EsTools;
import fcTools.SchnorrSignature;
import fchClass.Block;
import javaTools.JsonTools;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.VarInt;
import txTools.FchTool;
import fchClass.Cash;

import freecashRPC.FcRpcMethods;
import freecashRPC.NewFcRpcClient;
import javaTools.BytesTools;
import redis.clients.jedis.Jedis;
import txTools.TxInput;
import txTools.TxOutput;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static constants.Constants.FchToSatoshi;
import static txTools.FchTool.createTransactionSign;

public class WalletTools {
    static final double Million = 100000000d;
    public static final int DEFAULT_CASH_COUNT = 200;

    private static JsonRpcHttpClient createFcRpcClient() {

        System.out.println("Create esClient test");
        JsonRpcHttpClient fcClient = null;
        try {
            String rpcIp = "127.0.0.1";
            int rpcPort = 8332;
            String rpcUser = "user";
            String password = "password";
            System.out.println("Create FcRpcClient.");
            NewFcRpcClient newFcRpcClient = new NewFcRpcClient(rpcIp, rpcPort,rpcUser,password);
            fcClient = newFcRpcClient.getClientSilent();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fcClient;
    }

    public static String makeOpCdTx(ElasticsearchClient esClient, JsonRpcHttpClient fcClient, String fromAddr, String toAddr, String opReturn, long cd) throws Throwable {

        // Add more transaction inputs as necessary
        float payToAddr = 100000f;
        List<Cash> cashList = getCashListForCd(esClient,fromAddr,cd);
        if(cashList==null)return "Get cashes failed.";
        if(cashList.size()==0)return "No cashes for required cd.";

        CashToInputsResult cashToInputsResult = cashListToInputs(cashList);
        
        List<Map<String, Object>> inputs = cashToInputsResult.getInputs();
        long inputSum = cashToInputsResult.getValueSum();
        long cdSum = cashToInputsResult.getCdSum();

        Map<String, Object> outputs = new LinkedHashMap<>();

        if(!"".equals(toAddr)&&!toAddr.equals(fromAddr)) outputs.put(toAddr, payToAddr/Million);
        int opLength=0;
        if(opReturn!=null && !"".equals(opReturn)) {
           String opReturnHex = BytesTools.bytesToHexStringBE(opReturn.getBytes(StandardCharsets.UTF_8));
            outputs.put("data", opReturnHex);
            opLength = (int)opReturnHex.length()/2;
        }

        //
        long fee = FchTool.calcFee(inputs.size(),outputs.size(),opLength);
        long change = 0;
        double outValue;
        if(!"".equals(toAddr)&&!toAddr.equals(fromAddr)) {
            change =(long) (inputSum-payToAddr-fee);
            outValue = change/Million;
            outputs.put(fromAddr,outValue);
        }else {
            outValue = (inputSum-fee)/Million;
            outputs.put(fromAddr,outValue);
        }


        JsonTools.gsonPrint(outputs);
        System.out.println("spent cashes: "+inputs.size());
        System.out.println("total input: "+inputSum/Million);
        System.out.println("spent FCH:"+(inputSum-change-fee)/Million);
        System.out.println("cdd:"+cdSum);
        System.out.println("fee:"+fee/Million);

        return FcRpcMethods.createRawTx(fcClient, inputs, outputs);
    }

    private static CashToInputsResult cashListToInputs(List<Cash> cashList) {
        if(cashList==null)return null;
        List<Map<String, Object>> inputs = new LinkedList<>();
        long inputSum = 0;
        long cdSum = 0;
        for(Cash cash:cashList){
            Map<String, Object> transactionInput = new LinkedHashMap<>();
            transactionInput.put("txid", cash.getBirthTxId());
            transactionInput.put("vout", cash.getBirthIndex());
            inputs.add(transactionInput);
            inputSum = inputSum + cash.getValue();
            cdSum = cdSum + cash.getCd();
        }
        CashToInputsResult cashToInputsResult = new CashToInputsResult();
        cashToInputsResult.setInputs(inputs);
        cashToInputsResult.setValueSum(inputSum);
        cashToInputsResult.setCdSum(cdSum);

        return cashToInputsResult;
    }

    public static List<Cash> getCashListForCd(ElasticsearchClient esClient, String addr, long cd) throws IOException {

        List<Cash> cashList = new ArrayList<>();
        SearchResponse<Cash> result = esClient.search(s -> s.index("cash")
                .query(q ->q.bool(b->b.must(m->m
                                .term(t -> t.field("owner").value(addr))
                        ).must(m1->m1.term(t1->t1.field("valid").value(true))))
                )
                .trackTotalHits(tr->tr.enabled(true))
                .aggregations("sum",a->a.sum(s1->s1.field("cd")))
                .sort(s1->s1.field(f->f.field("cd").order(SortOrder.Asc).field("value").order(SortOrder.Asc)))
                .size(100), Cash.class);

        if(result==null)return null;

        long sum = (long)result.aggregations().get("sum").sum().value();

        if(sum<cd)return cashList;

        List<Hit<Cash>> hitList = result.hits().hits();
        if(hitList.size()==0)return cashList;

        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            cashList.add(cash);
        }

        checkUnconfirmed(addr,cashList);

        List<Cash> meetList = new ArrayList<>();
        long adding = 0;
        for(Cash cash:cashList){
            adding+=cash.getCd();
            meetList.add(cash);
            if (adding>cd)break;
        }
        if(adding<cd){
            System.out.println("More the 100 cashes was required. Merge small cashes first please.");
            return null;
        }
        return meetList;
    }


    public static String[] getSpendingCashId(String addr) {
        Gson gson = new Gson();
        try(Jedis jedis3Mempool = new Jedis()) {
            jedis3Mempool.select(Constants.RedisDb3Mempool);
            String spendCashIdStr = jedis3Mempool.hget(addr, FieldNames.SPEND_CASHES);
            if (spendCashIdStr != null) {
                return gson.fromJson(spendCashIdStr, String[].class);
            }
        }
        return null;
    }

    public static List<Cash> getIssuingCashList(String addr) {
        List<Cash> issuingCashList = new ArrayList<>();
        Gson gson = new Gson();
        try(Jedis jedis3Mempool = new Jedis()) {
            jedis3Mempool.select(Constants.RedisDb3Mempool);
            String newCashIdStr = jedis3Mempool.hget(addr, FieldNames.NEW_CASHES);
            if (newCashIdStr != null) {
                String[] newCashIdList = gson.fromJson(newCashIdStr, String[].class);
                for (String cashId : newCashIdList) {
                    Cash cash = gson.fromJson(jedis3Mempool.hget(FieldNames.NEW_CASHES, cashId), Cash.class);
                    if (cash != null) issuingCashList.add(cash);
                }
            }
        }
        if(issuingCashList.size()==0)return null;
        return issuingCashList;
    }


    public static void checkUnconfirmed(String addr, List<Cash> meetList) {
        Gson gson = new Gson();
        try(Jedis jedis3Mempool = new Jedis()) {
            jedis3Mempool.select(Constants.RedisDb3Mempool);
            String spendCashIdStr = jedis3Mempool.hget(addr, FieldNames.SPEND_CASHES);
            if (spendCashIdStr != null) {
                String[] spendCashIds = gson.fromJson(spendCashIdStr, String[].class);
                Iterator<Cash> iter = meetList.iterator();
                while (iter.hasNext()) {
                    Cash cash = iter.next();
                    for (String id : spendCashIds) {
                        if (id.equals(cash.getCashId())) {
                            iter.remove();
                            break;
                        }
                    }
                }
            }

            String newCashIdStr = jedis3Mempool.hget(addr, FieldNames.NEW_CASHES);
            if (newCashIdStr != null) {
                String[] newCashIds = gson.fromJson(newCashIdStr, String[].class);
                for (String id : newCashIds) {
                    Cash cash = gson.fromJson(jedis3Mempool.hget(FieldNames.NEW_CASHES, id), Cash.class);
                    if (cash != null) meetList.add(cash);
                }
            }
        }
    }

    public static String splitCashes(ElasticsearchClient esClient, JsonRpcHttpClient fcClient, String addr, int inCount, int outCount) throws Throwable {

        System.out.println("get cash list.");
        SearchResponse<Cash> result = esClient.search(s -> s.index("cash")
                .query(q ->q.bool(b->b.must(m->m
                                .term(t -> t.field("owner").value(addr))
                        ).must(m1->m1.term(t1->t1.field("valid").value(true))))
                )
                .trackTotalHits(tr->tr.enabled(true))
                .aggregations("cdSum",a->a.sum(s1->s1.field("cd")))
                .aggregations("valueSum",a->a.sum(s1->s1.field("value")))
                .sort(s1->s1.field(f->f.field("cd").order(SortOrder.Asc).field("value").order(SortOrder.Asc)))
                .size(inCount), Cash.class);

        if(result==null) return addr;

        long cdSum = (long)result.aggregations().get("cdSum").sum().value();
        long valueSum = (long)result.aggregations().get("valueSum").sum().value();
        System.out.println(cdSum + " "+ valueSum);

        List<Hit<Cash>> hitList = result.hits().hits();
        int size = hitList.size();
        if(size==0) return addr;

        System.out.println(size);

        List<Cash> cashList = new ArrayList<>();
        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            cashList.add(cash);
        }

        checkUnconfirmed(addr,cashList);

        CashToInputsResult inputResult = cashListToInputs(cashList);

        List<Map<String, Object>> inputs = inputResult.getInputs();

        Map<String, Object> outputs = splitToOutputs(addr,outCount, inputResult);

        return FcRpcMethods.createRawTx(fcClient, inputs, outputs);

    }

    private static Map<String, Object> splitToOutputs(String addr,int outCount, CashToInputsResult inputResult) throws Throwable {
        if(outCount>100){
            System.out.println("Error, outCount > 100.");
        }
        long valueSum= inputResult.getValueSum();
        long valueEach = valueSum/outCount;
        if(valueEach<100000){
            outCount = (int)(valueSum/100000);
            valueEach = 100000;
            if(outCount<1){
                System.out.println("Error, value of each output < 0.001.");
                return null;
            }
        }
        Map<String,Object> outputs = new LinkedHashMap<>();


        List<Map<String, Object>> inputs = inputResult.getInputs();
        long fee = FchTool.calcFee(inputs.size(), outputs.size(), 0);
        outputs.put(addr,(valueSum-valueEach*(outCount-1)-fee)/Million);

        return outputs;
    }

    public static CashListReturn getCashForCd(String addrRequested, long cd,ElasticsearchClient esClient) throws IOException {
        CashListReturn cashListReturn;

        cashListReturn = getCdFromCashList(cd, addrRequested, esClient);

        if(cashListReturn.getCashList() != null)return cashListReturn;

        int code = cashListReturn.getCode();
        String msg = cashListReturn.getMsg();

        cashListReturn = getCdFromOneCash(addrRequested, cd, esClient);

        if(cashListReturn.getCashList()==null || cashListReturn.getCashList().isEmpty()){
            cashListReturn.setCode(code);
            cashListReturn.setMsg(msg);
        }

        return cashListReturn;
    }
    private static CashListReturn getCdFromOneCash(String addrRequested, long cd, ElasticsearchClient esClient) throws IOException {
        String index = IndicesNames.CASH;
        CashListReturn cashListReturn = new CashListReturn();

        List<FieldValue> spendingCashIdList;
        String[] spendingCashIds = getSpendingCashId(addrRequested);
        if(spendingCashIds!=null) {
            spendingCashIdList = new ArrayList<>();
            for(String id:spendingCashIds){
                spendingCashIdList.add(FieldValue.of(id));
            }
        } else {
            spendingCashIdList = null;
        }
        SearchResponse<Cash> result;
        if(spendingCashIdList!=null) {
            result = esClient.search(s -> s.index(index)
                    .query(q -> q.bool(b -> b
                                    .must(m -> m.term(t -> t.field(FieldNames.OWNER).value(addrRequested)))
                                    .must(m1 -> m1.term(t1 -> t1.field(FieldNames.VALID).value(true)))
                                    .must(m2 -> m2.range(r1 -> r1.field(FieldNames.CD).gte(JsonData.of(cd))))
                                    .mustNot(m3 -> m3.terms(t2 -> t2.field(FieldNames.CASH_ID).terms(t3 -> t3.value(spendingCashIdList))))
                            )
                    )
                    .trackTotalHits(tr -> tr.enabled(true))
                    .sort(s1 -> s1.field(f -> f.field(FieldNames.CD).order(SortOrder.Asc)))
                    .size(1), Cash.class);
        }else {
            result = esClient.search(s -> s.index(index)
                    .query(q -> q.bool(b -> b
                                    .must(m -> m.term(t -> t.field(FieldNames.OWNER).value(addrRequested)))
                                    .must(m1 -> m1.term(t1 -> t1.field(FieldNames.VALID).value(true)))
                                    .must(m2 -> m2.range(r1 -> r1.field(FieldNames.CD).gte(JsonData.of(cd))))
                            )
                    )
                    .trackTotalHits(tr -> tr.enabled(true))
                    .sort(s1 -> s1.field(f -> f.field(FieldNames.CD).order(SortOrder.Asc)))
                    .size(1), Cash.class);
        }

        List<Cash> cashList = new ArrayList<>();

        List<Hit<Cash>> hitList = result.hits().hits();

        if(hitList==null || hitList.size()==0)return cashListReturn;

        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            cashList.add(cash);
        }

        checkUnconfirmed(addrRequested,cashList);

        if(result.hits().total() == null)return cashListReturn;
        cashListReturn.setTotal(result.hits().total().value());
        cashListReturn.setCashList(cashList);
        return cashListReturn;
    }
    private static CashListReturn getCdFromCashList(long cd, String addrRequested, ElasticsearchClient esClient) throws IOException {
        String index = IndicesNames.CASH;
        CashListReturn cashListReturn = new CashListReturn();

        List<FieldValue> spendingCashIdList;
        String[] spendingCashIds = getSpendingCashId(addrRequested);
        if(spendingCashIds!=null) {
            spendingCashIdList = new ArrayList<>();
            for(String id:spendingCashIds){
                spendingCashIdList.add(FieldValue.of(id));
            }
        } else {
            spendingCashIdList = null;
        }
        SearchResponse<Cash> result;
        if(spendingCashIdList!=null) {
            result = esClient.search(s -> s.index(index)
                    .query(q -> q.bool(b -> b
                                    .must(m -> m.term(t -> t.field(FieldNames.OWNER).value(addrRequested)))
                                    .must(m1 -> m1.term(t1 -> t1.field(FieldNames.VALID).value(true)))
                                    .mustNot(m2 -> m2.terms(t2 -> t2.field(FieldNames.CASH_ID).terms(t3 -> t3.value(spendingCashIdList))))
                            )
                    )
                    .trackTotalHits(tr -> tr.enabled(true))
                    .aggregations("sum", a -> a.sum(s1 -> s1.field(FieldNames.CD)))
                    .sort(s1 -> s1.field(f -> f.field(FieldNames.CD).order(SortOrder.Desc)))
                    .size(DEFAULT_CASH_COUNT), Cash.class);
        }else {
            result = esClient.search(s -> s.index(index)
                    .query(q -> q.bool(b -> b
                                    .must(m -> m.term(t -> t.field(FieldNames.OWNER).value(addrRequested)))
                                    .must(m1 -> m1.term(t1 -> t1.field(FieldNames.VALID).value(true)))
                            )
                    )
                    .trackTotalHits(tr -> tr.enabled(true))
                    .aggregations("sum", a -> a.sum(s1 -> s1.field(FieldNames.CD)))
                    .sort(s1 -> s1.field(f -> f.field(FieldNames.CD).order(SortOrder.Desc)))
                    .size(DEFAULT_CASH_COUNT), Cash.class);
        }

        if(result==null){
            cashListReturn.setCode(1);
            cashListReturn.setMsg("Can't get cashes.");
            return cashListReturn;
        }

        long sumCd =(long)result.aggregations().get("sum").sum().value();

        if(sumCd<cd){
            cashListReturn.setCode(2);
            cashListReturn.setMsg("No enough cd balance: "+sumCd+ " cd");
            return cashListReturn;
        }

        assert result.hits().total() != null;
        cashListReturn.setTotal(result.hits().total().value());

        List<Hit<Cash>> hitList = result.hits().hits();
        if(hitList.size()==0){
            cashListReturn.setCode(3);
            cashListReturn.setMsg("Get cashes failed.");
            return cashListReturn;
        }

        List<Cash> cashList = new ArrayList<>();

        sumCd=0;
        long bestHeight = getBestHeight(esClient);
        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();

            //Remove immature cashes
            if(cash==null)continue;
            if(cash.getIssuer().equals(Values.COINBASE)&& cash.getBirthHeight()>(bestHeight-Constants.ONE_DAY_BLOCKS*10))
                continue;

            cashList.add(cash);
            sumCd+=cash.getCd();
            if(sumCd>cd)break;
        }

        if(sumCd<cd){
            cashListReturn.setCode(4);
            cashListReturn.setMsg("Can not get enough cd from "+DEFAULT_CASH_COUNT+" cashes. Merge cashes with small cd first please.");
            return cashListReturn;
        }
        cashListReturn.setCashList(cashList);
        return cashListReturn;
    }
    public static CashListReturn getCashListForPay(long value, String addrRequested, ElasticsearchClient esClient) {

        CashListReturn cashListReturn = new CashListReturn();

        String index = IndicesNames.CASH;

        SearchResponse<Cash> result;
        try {
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
            searchBuilder.index(index);
            searchBuilder.trackTotalHits(tr->tr.enabled(true));
            searchBuilder.sort(s1->s1.field(f->f.field(FieldNames.CD).order(SortOrder.Asc)));
            searchBuilder.size(DEFAULT_CASH_COUNT);

            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            boolQueryBuilder.must(m->m.term(t -> t.field(FieldNames.OWNER).value(addrRequested)));
            boolQueryBuilder.must(m1->m1.term(t1->t1.field(FieldNames.VALID).value(true)));

            searchBuilder.query(q->q.bool(boolQueryBuilder.build()));

            result = esClient.search(searchBuilder.build(),Cash.class);

        } catch (IOException e) {
            cashListReturn.setCode(1);
            cashListReturn.setMsg("Can't get cashes. Check ES.");
            return cashListReturn;
        }

        if(result==null){
            cashListReturn.setCode(1);
            cashListReturn.setMsg("Can't get cashes.Check ES.");
            return cashListReturn;
        }

        assert result.hits().total() != null;
        long total = result.hits().total().value();

        long sum = 0;//(long)result.aggregations().get(FieldNames.SUM).sum().value();

        List<Cash> cashList = new ArrayList<>();
        List<Hit<Cash>> hitList = result.hits().hits();

        long bestHeight = getBestHeight(esClient);

        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            if(cash==null)continue;
            if(cash.getIssuer().equals(Values.COINBASE)&& cash.getBirthHeight()>(bestHeight-Constants.ONE_DAY_BLOCKS *10))
                continue;
            cashList.add(cash);
            sum+=cash.getValue();
        }


        List<Cash> issuingCashList = getIssuingCashList(addrRequested);
        if(issuingCashList!=null && issuingCashList.size()>0) {
            for (Cash cash : issuingCashList) {
                cashList.add(cash);
                sum += cash.getValue();
                total++;
            }
        }

        String[] spendingCashIds = getSpendingCashId(addrRequested);
        if (spendingCashIds != null) {
            for (String id : spendingCashIds) {
                Iterator<Cash> iter = cashList.iterator();
                while (iter.hasNext()) {
                    Cash cash = iter.next();
                    if (id.equals(cash.getCashId())) {
                        sum -= cash.getValue();
                        iter.remove();
                        total--;
                        break;
                    }
                }
            }
        }

        if(sum<value){
            cashListReturn.setCode(2);
            cashListReturn.setMsg("No enough balance: "+sum/Constants.FchToSatoshi+ " fch");
            return cashListReturn;
        }

        List<Cash> meetList = new ArrayList<>();
        long adding = 0;
        long fee=0;
        for(Cash cash:cashList){
            adding+=cash.getValue();
            meetList.add(cash);
            //Add tx fee
            fee = 10 + meetList.size() * 141L;
            if (adding>value+fee)break;
        }
        if(adding<value+fee){
            cashListReturn.setCode(4);
            cashListReturn.setMsg("Can't get enough amount from 100 cashes. Merge cashes with small cd first. "+adding/Million + "f can be paid."+ "Request "+ value/Million+". Fee "+fee/Million);
            return cashListReturn;
        }
        cashListReturn.setTotal(total);
        cashListReturn.setCashList(meetList);
        return cashListReturn;
    }

    public static CashListReturn getCashList(long value,long cd,int outputNum,int opReturnLength, String addrRequested, ElasticsearchClient esClient) {

        CashListReturn cashListReturn = new CashListReturn();

        String index = IndicesNames.CASH;

        SearchResponse<Cash> result;
        try {
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
            searchBuilder.index(index);
            searchBuilder.trackTotalHits(tr->tr.enabled(true));
            searchBuilder.sort(s1->s1.field(f->f.field(FieldNames.CD).order(SortOrder.Asc)));
            searchBuilder.size(DEFAULT_CASH_COUNT);

            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            boolQueryBuilder.must(m->m.term(t -> t.field(FieldNames.OWNER).value(addrRequested)));
            boolQueryBuilder.must(m1->m1.term(t1->t1.field(FieldNames.VALID).value(true)));

            searchBuilder.query(q->q.bool(boolQueryBuilder.build()));

            result = esClient.search(searchBuilder.build(),Cash.class);

        } catch (IOException e) {
            cashListReturn.setCode(1);
            cashListReturn.setMsg("Can't get cashes. Check ES.");
            return cashListReturn;
        }

        if(result==null){
            cashListReturn.setCode(1);
            cashListReturn.setMsg("Can't get cashes.Check ES.");
            return cashListReturn;
        }

        assert result.hits().total() != null;
        long total = result.hits().total().value();

        long valueSum = 0;//(long)result.aggregations().get(FieldNames.SUM).valueSum().value();
        long cdSum = 0;
        long fee = 0;


        List<Cash> cashList = new ArrayList<>();
        List<Hit<Cash>> hitList = result.hits().hits();

        long bestHeight = getBestHeight(esClient);

        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            if(cash==null)continue;
            if(cash.getIssuer()!=null && cash.getIssuer().equals(Values.COINBASE)&& cash.getBirthHeight()>(bestHeight-Constants.ONE_DAY_BLOCKS *10))
                continue;
            cashList.add(cash);
        }


        List<Cash> issuingCashList = getIssuingCashList(addrRequested);
        if(issuingCashList!=null && issuingCashList.size()>0) {
            for (Cash cash : issuingCashList) {
                cashList.add(cash);
            }
        }

        String[] spendingCashIds = getSpendingCashId(addrRequested);
        if (spendingCashIds != null) {
            for (String id : spendingCashIds) {
                Iterator<Cash> iter = cashList.iterator();
                while (iter.hasNext()) {
                    Cash cash = iter.next();
                    if (id.equals(cash.getCashId())) {
                        iter.remove();
                        break;
                    }
                }
            }
        }

        List<Cash> meetList = new ArrayList<>();
        boolean done = false;
        for(Cash cash : cashList){
            meetList.add(cash);
            valueSum+=cash.getValue();
            cdSum += cash.getCd();
            fee = calcTxSize(cashList.size(),outputNum,opReturnLength);

            if(valueSum>(value+fee) && cdSum > cd) {
                done = true;
                break;
            }
        }

        if(!done){
            cashListReturn.setCode(4);
            cashListReturn.setMsg("Can't meet the conditions.");
            return cashListReturn;
        }
        cashListReturn.setTotal(total);
        cashListReturn.setCashList(meetList);

        return cashListReturn;
    }

    public static long calcTxSize(int inputNum, int outputNum, int opReturnBytesLen) {

        long baseLength = 10;
        long inputLength = 141 * (long) inputNum;
        long outputLength = 34 * (long) (outputNum + 1); // Include change output

        int opReturnLen = 0;
        if (opReturnBytesLen != 0)
            opReturnLen = calcOpReturnLen(opReturnBytesLen);

        return baseLength + inputLength + outputLength + opReturnLen;
    }

    private static int calcOpReturnLen(int opReturnBytesLen) {
        int dataLen;
        if (opReturnBytesLen < 76) {
            dataLen = opReturnBytesLen + 1;
        } else if (opReturnBytesLen < 256) {
            dataLen = opReturnBytesLen + 2;
        } else dataLen = opReturnBytesLen + 3;
        int scriptLen;
        scriptLen = (dataLen + 1) + VarInt.sizeOf(dataLen + 1);
        int amountLen = 8;
        return scriptLen + amountLen;
    }

    private static long getBestHeight(ElasticsearchClient esClient) {
        long bestHeight = 0;
        try {
            Block bestBlock = EsTools.getBestBlock(esClient);
            if(bestBlock!=null)
                bestHeight=bestBlock.getHeight();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bestHeight;
    }

    private static void makeUnconfirmedFilter(List<String> spendCashIdList, BoolQuery.Builder boolQueryBuilder) {
        List<FieldValue> valueList = new ArrayList<>();
        for (String v : spendCashIdList) {
            if (v.isBlank()) continue;
            valueList.add(FieldValue.of(v));
        }

        TermsQuery tQuery = TermsQuery.of(t -> t
                .field(Strings.CASH_ID)
                .terms(t1 -> t1
                        .value(valueList)
                ));

        boolQueryBuilder.filter(new Query.Builder().terms(tQuery).build());
    }

    public static List<TxOutput> sendToToTxOutputList(List<SendTo> sendToList) {
        List<TxOutput> outputList = new ArrayList<>();
        for (SendTo sendTo : sendToList){
            TxOutput txOutput = new TxOutput();
            txOutput.setAddress(sendTo.getFid());
            txOutput.setAmount((long) (sendTo.getAmount()*FchToSatoshi));
            outputList.add(txOutput);
        }
        return outputList;
    }

    public static List<TxInput> cashToInputList(List<Cash> cashList, byte[] priKey) {
        List<TxInput> inputList = new ArrayList<>();
        JsonTools.gsonPrint(cashList);
        for (Cash cash : cashList){
            TxInput txInput = new TxInput();
            txInput.setIndex(cash.getBirthIndex());
            txInput.setAmount(cash.getValue());
            txInput.setTxId(cash.getBirthTxId());
            txInput.setPriKey32(priKey);
            inputList.add(txInput);
        }
        return inputList;
    }

    public static String schnorrMsgSign(String msg, byte[]priKey){
        ECKey ecKey = ECKey.fromPrivate(priKey);
        BigInteger priKeyBigInteger = ecKey.getPrivKey();
        byte[] pubKey = ecKey.getPubKey();
        byte[] msgHash = Hash.Sha256x2(msg.getBytes());
        byte[] sign = SchnorrSignature.schnorr_sign(msgHash, priKeyBigInteger);
        byte[] pkSign = BytesTools.bytesMerger(pubKey,sign);
        return Base64.encodeBytes(pkSign);
    }

    public static boolean schnorrMsgVerify(String msg, String pubSign, String fid) throws IOException {
        byte[] msgHash = Hash.Sha256x2(msg.getBytes());
        byte[] pubSignBytes = java.util.Base64.getDecoder().decode(pubSign);
        byte[] pubKey = Arrays.copyOf(pubSignBytes,33);
        if(!fid.equals(KeyTools.pubKeyToFchAddr(HexFormat.of().formatHex(pubKey))))return false;
        byte[] sign = Arrays.copyOfRange(pubSignBytes,33,pubSignBytes.length);
        return SchnorrSignature.schnorr_verify(msgHash,pubKey, sign);
    }
}
