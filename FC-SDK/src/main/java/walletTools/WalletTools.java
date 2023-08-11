package walletTools;

//import APIP0V1_OpenAPI.Replier;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import constants.IndicesNames;
import fcTools.ParseTools;
import fchClass.Cash;
import freecashRPC.FcRpcMethods;
import freecashRPC.NewFcRpcClient;
import freecashRPC.SignResult;
import javaTools.BytesTools;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WalletTools {
    static final double Million = 100000000d;
//    public static void main(String[] args) throws Throwable {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//        System.out.println("Input the sending address: ");
//        String fromAddr = br.readLine();
//        if("".equals(fromAddr))return;
////
////        System.out.println("Input the receiving address: ");
////        String toAddr = br.readLine();
////
////        System.out.println("Input the text being wrote into OP_RETURN: ");
////        String opReturn = br.readLine();
//
//        NewEsClient newEsClient = new NewEsClient();
//        System.out.println("Create esClient.");
//        ElasticsearchClient esClient = newEsClient.getClientHttp("127.0.0.1", 9200);
//        JsonRpcHttpClient fcClient = createFcRpcClient();
//
////        long cd = 1;
//
////        String unsignedRawTx = makeOpCdTx(esClient, fcClient, fromAddr, toAddr, opReturn, cd);
////        SignResult signedRawTxResult = FcRpcMethods.signRawTxWithWallet(fcClient, unsignedRawTx);
////
////        String sighedRawTx = signedRawTxResult.getHex();
////
////        System.out.println("Sent. "+FcRpcMethods.sendTx(fcClient,sighedRawTx));
//        splitCashes(esClient,fcClient,fromAddr,100,20);
//
//        newEsClient.shutdownClient();
//        br.close();
//    }

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
        long fee = calcFee(inputs,outputs,opLength);
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


        ParseTools.gsonPrint(outputs);
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

    private static long calcFee(List<Map<String, Object>> inputs, Map<String, Object> outputs, int opLength) {
        long priceInSatoshi =1;
        int inputNum = inputs.size();
        int outputNum = outputs.size();
        long length = 0 ;
        if(opLength==0) {
            length = 6+ (32+4+3+66+66+4)*inputNum +(8+1+25+4)*(outputNum+1);
        }else{
            length= 6+ (32+4+3+66+66+4)*inputNum +(8+1+25+4)*(outputNum+1)+ opLength+11;
        }

        long fee = priceInSatoshi*length;
        if(fee<546)fee=546;
        return fee;
    }

    public static List<Cash> getCashListForCd(ElasticsearchClient esClient, String addr, long cd) throws IOException {

        List<Cash> cashList = new ArrayList<>();
        SearchResponse<Cash> result = esClient.search(s -> s.index("cash")
                .query(q ->q.bool(b->b.must(m->m
                                .term(t -> t.field("fid").value(addr))
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

    public static void checkUnconfirmed(String addr, List<Cash> meetList) {
        Gson gson = new Gson();
        Jedis jedis3Mempool = new Jedis();
        jedis3Mempool.select(3);
        String spendCashIdStr = jedis3Mempool.hget(addr,"spendCashes");
        if(spendCashIdStr!=null){
            String[] spendCashIdList = gson.fromJson(spendCashIdStr, String[].class);
            Iterator<Cash> iter = meetList.iterator();
            while(iter.hasNext()){
                Cash cash = iter.next();
                for(String id : spendCashIdList){
                    if(id.equals(cash.getCashId()))iter.remove();
                    break;
                }
            }
        }

        String newCashIdStr = jedis3Mempool.hget(addr,"newCashes");
        if(newCashIdStr!=null){
            String[] newCashIdList = gson.fromJson(newCashIdStr, String[].class);
            for(String id :newCashIdList){
                Cash cash = gson.fromJson(jedis3Mempool.hget("newCashes",id),Cash.class);
                if(cash!=null)meetList.add(cash);
            }
        }
    }

    public static void splitCashes(ElasticsearchClient esClient, JsonRpcHttpClient fcClient,String addr, int inCount, int outCount) throws Throwable {


        System.out.println("get cash list.");
        SearchResponse<Cash> result = esClient.search(s -> s.index("cash")
                .query(q ->q.bool(b->b.must(m->m
                                .term(t -> t.field("fid").value(addr))
                        ).must(m1->m1.term(t1->t1.field("valid").value(true))))
                )
                .trackTotalHits(tr->tr.enabled(true))
                .aggregations("cdSum",a->a.sum(s1->s1.field("cd")))
                .aggregations("valueSum",a->a.sum(s1->s1.field("value")))
                .sort(s1->s1.field(f->f.field("cd").order(SortOrder.Asc).field("value").order(SortOrder.Asc)))
                .size(inCount), Cash.class);

        System.out.println(result.hits().total());

        if(result==null)return;

        long cdSum = (long)result.aggregations().get("cdSum").sum().value();
        long valueSum = (long)result.aggregations().get("valueSum").sum().value();
        System.out.println(cdSum + " "+ valueSum);

        List<Hit<Cash>> hitList = result.hits().hits();
        int size = hitList.size();
        if(size==0)return;

        System.out.println(size);

        List<Cash> cashList = new ArrayList<>();
        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            //TODO
            ParseTools.gsonPrint(cash);
            cashList.add(cash);
        }
//TODO
        ParseTools.gsonPrint(cashList);

        //checkUnconfirmed(addr,cashList);

        CashToInputsResult inputResult = cashListToInputs(cashList);

        //TODO
        ParseTools.gsonPrint(inputResult);

        List<Map<String, Object>> inputs = inputResult.getInputs();

        Map<String, Object> outputs = splitToOutputs(addr,outCount, inputResult);

        //TODO
        ParseTools.gsonPrint(outputs);

        String unsignedRawTx = FcRpcMethods.createRawTx(fcClient, inputs, outputs);

        SignResult signedRawTxResult = FcRpcMethods.signRawTxWithWallet(fcClient, unsignedRawTx);

        String sighedRawTx = signedRawTxResult.getHex();

        //System.out.println("Sent. "+FcRpcMethods.sendTx(fcClient,sighedRawTx));

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

//        for(int i=0;i<outCount-1;i++){
//            outputs.put(addr,valueEach/Million);
//        }

        List<Map<String, Object>> inputs = inputResult.getInputs();
        long fee = calcFee(inputs, outputs, 0);
        outputs.put(addr,(valueSum-valueEach*(outCount-1)-fee)/Million);

        ParseTools.gsonPrint(outputs);
//        System.out.println("spent cashes: "+inputs.size());
//        System.out.println("total input: "+inputSum/Million);
//        System.out.println("spent FCH:"+(inputSum-change-fee)/Million);
//        System.out.println("cdd:"+cdSum);
//        System.out.println("fee:"+fee/Million);


//        String unsignedRawTx = FcRpcMethods.createRawTx(fcClient, inputs, outputs);
//
//        SignResult signedRawTxResult = FcRpcMethods.signRawTxWithWallet(fcClient, unsignedRawTx);
//
//        String sighedRawTx = signedRawTxResult.getHex();
//
//        String result = FcRpcMethods.sendTx(fcClient, sighedRawTx);
//
//        System.out.println("Sent. "+result);

        return outputs;
    }

    public static CashListReturn getCashForCd(String addrRequested, long cd,ElasticsearchClient esClient) throws IOException {
        CashListReturn cashListReturn = new CashListReturn();

        cashListReturn = getCdFromOneCash(addrRequested, cd, esClient);

        if(cashListReturn.getCashList()!=null &&cashListReturn.getCashList().size()==1)return cashListReturn;

        return getCdfromCashList(cd, addrRequested, esClient);
    }
    private static CashListReturn getCdFromOneCash(String addrRequested, long cd, ElasticsearchClient esClient) throws IOException {
        String index = IndicesNames.CASH;
        CashListReturn cashListReturn = new CashListReturn();
        SearchResponse<Cash> result = esClient.search(s -> s.index(index)
                .query(q ->q.bool(b->b
                                .must(m->m.term(t -> t.field("fid").value(addrRequested)))
                                .must(m1->m1.term(t1->t1.field("valid").value(true)))
                                .must(m2->m2.range(r1->r1.field("cd").gte(JsonData.of(cd))))
                        )
                )
                .trackTotalHits(tr->tr.enabled(true))
                .aggregations("sum",a->a.sum(s1->s1.field("cd")))
                .sort(s1->s1.field(f->f.field("cd").order(SortOrder.Asc)))
                .size(1), Cash.class);

        Cash cash =null;
        try {
            cash = result.hits().hits().get(0).source();
        }catch (Exception e){
            return null;
        }
        cashListReturn.setTotal(result.hits().total().value());
        List<Cash> cashList = new ArrayList<>();
        cashList.add(cash);
        cashListReturn.setCashList(cashList);
        return cashListReturn;
    }
    private static CashListReturn getCdfromCashList(long cd, String addrRequested, ElasticsearchClient esClient) throws IOException {
        String index = IndicesNames.CASH;
        CashListReturn cashListReturn = new CashListReturn();

        SearchResponse<Cash> result = esClient.search(s -> s.index(index)
                .query(q ->q.bool(b->b
                                .must(m->m.term(t -> t.field("fid").value(addrRequested)))
                                .must(m1->m1.term(t1->t1.field("valid").value(true)))
                                .must(m2->m2.range(r1->r1.field("cd").lte(JsonData.of(cd))))
                        )
                )
                .trackTotalHits(tr->tr.enabled(true))
                .aggregations("sum",a->a.sum(s1->s1.field("cd")))
                .sort(s1->s1.field(f->f.field("cd").order(SortOrder.Desc)))
                .size(100), Cash.class);

        if(result==null){
            cashListReturn.setCode(1);
            cashListReturn.setMsg("Can't get cashes.");
            return cashListReturn;
        }

        long sum = (long)result.aggregations().get("sum").sum().value();

        if(sum<cd){
            cashListReturn.setCode(2);
            cashListReturn.setMsg("No enough cd balance: "+sum+ " cd");
            return cashListReturn;
        }

        cashListReturn.setTotal(result.hits().total().value());

        List<Hit<Cash>> hitList = result.hits().hits();
        if(hitList.size()==0){
            cashListReturn.setCode(3);
            cashListReturn.setMsg("Get cashes failed.");
            return null;
        }

        List<Cash> cashList = new ArrayList<>();

        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            cashList.add(cash);
        }

        checkUnconfirmed(addrRequested,cashList);

        List<Cash> meetList = new ArrayList<>();
        long adding = 0;
        for(Cash cash:cashList){
            adding+=cash.getCd();
            meetList.add(cash);
            if (adding>cd)break;
        }

        if(adding<cd){
            cashListReturn.setCode(4);
            cashListReturn.setMsg("Can't get enough cd from 100 cashes. Merge cashes with small cd first please.");
            return null;
        }

        cashListReturn.setCashList(meetList);
        return cashListReturn;
    }

    public static CashListReturn getCashListForPay(long value, String addrRequested, ElasticsearchClient esClient) {

        CashListReturn cashListReturn = new CashListReturn();

        String index = IndicesNames.CASH;

        SearchResponse<Cash> result = null;
        try {
            result = esClient.search(s -> s.index(index)
                    .query(q ->q.bool(b->b
                                    .must(m->m.term(t -> t.field("fid").value(addrRequested)))
                                    .must(m1->m1.term(t1->t1.field("valid").value(true)))
    //                                .must(m2->m2.range(r1->r1.field("value").lte(JsonData.of(value))))
                            )
                    )
                    .trackTotalHits(tr->tr.enabled(true))
                    .aggregations("sum",a->a.sum(s1->s1.field("value")))
                    .sort(s1->s1.field(f->f.field("cd").order(SortOrder.Asc)))
                    .size(100), Cash.class);
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

        cashListReturn.setTotal(result.hits().total().value());

        long sum = (long)result.aggregations().get("sum").sum().value();

        if(sum<value){
            cashListReturn.setCode(2);
            cashListReturn.setMsg("No enough balance: "+sum+ " fch");
            return cashListReturn;
        }

        List<Hit<Cash>> hitList = result.hits().hits();
        if(hitList.size()==0){
            cashListReturn.setCode(3);
            cashListReturn.setMsg("Get cashes failed.");
            return cashListReturn;
        }

        List<Cash> cashList = new ArrayList<>();

        for(Hit<Cash> hit : hitList){
            Cash cash = hit.source();
            cashList.add(cash);
        }

        checkUnconfirmed(addrRequested,cashList);

        List<Cash> meetList = new ArrayList<>();
        long adding = 0;
        for(Cash cash:cashList){
            adding+=cash.getValue();
            meetList.add(cash);
            if (adding>value)break;
        }
        if(adding<value){
            cashListReturn.setCode(4);
            cashListReturn.setMsg("Can't get enough amount from 100 cashes. Merge cashes with small cd first please. "+adding/Million + "f can be paid.");
            return cashListReturn;
        }

        cashListReturn.setCashList(meetList);
        return cashListReturn;
    }
}
