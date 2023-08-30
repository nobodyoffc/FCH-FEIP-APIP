package reward;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import esTools.EsTools;
import esTools.Sort;
import fcTools.ParseTools;
import fchClass.CashMark;
import fchClass.OpReturn;
import fchClass.TxHas;
import feipClass.FcInfo;
import fileTools.JsonFileTools;
import menu.Inputer;
import menu.Menu;
import order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import service.Params;
import startAPIP.StartAPIP;
import startFEIP.FileParser;
import walletTools.SendTo;

import java.io.*;
import java.util.*;

import static constants.Constants.*;
import static constants.IndicesNames.ORDER;
import static constants.IndicesNames.TX_HAS;
import static constants.Strings.*;
import static reward.RewardManager.getLastRewardInfo;
import static startAPIP.StartAPIP.serviceName;

public class Rewarder {
    private static final Logger log = LoggerFactory.getLogger(Rewarder.class);
    private final ElasticsearchClient esClient;
    private final Jedis jedis;
    private String account;
    private String lastOrderId;
    private long incomeT=0;
    private long paidSum;
    private Map<String, Long> pendingMap;
    private final int recover4Decimal = 10000;

    public Rewarder(ElasticsearchClient esClient, Jedis jedis) {
        this.esClient = esClient;
        this.jedis = jedis;
    }
    public RewardReturn doReward(){
        long reservedFee = 100000;
        RewardReturn rewardReturn = new RewardReturn();

        lastOrderId = getLastOrderId(esClient);

        try {
            account = jedis.hget(jedis.hget(CONFIG,SERVICE_NAME)+"_"+ PARAMS_ON_CHAIN, ACCOUNT);
        }catch (Exception ignore){}
        if(account ==null){
            rewardReturn.setCode(2);
            rewardReturn.setMsg("Get account failed. Check the service parameters in redis.");
            return rewardReturn;
        }

        Map<String, RewardInfo> unpaidRewardInfoMap = getUnpaidRewardInfoMap(esClient);

        checkPayment(esClient,unpaidRewardInfoMap);

        if(incomeT==0){
            incomeT = makeIncomeT(lastOrderId,esClient);
        }else{
            System.out.println("Set income: "+incomeT);
            long sum = makeIncomeT(lastOrderId,esClient);
            if(sum<incomeT){
                System.out.println("Notice: New order sum is "+sum+", while you are paying "+incomeT+".");
            }
            if(lastOrderId==null){
                lastOrderId = Objects.requireNonNull(getLastRewardInfo(esClient)).getRewardId();
                if(lastOrderId==null){
                    rewardReturn.setCode(2);
                    rewardReturn.setMsg("Get last order id failed.");
                    return rewardReturn;
                }
            }
        }

        if(incomeT==0){
            rewardReturn.setCode(2);
            rewardReturn.setMsg("No income.");
            log.debug("No income.");
            return rewardReturn;
        }

        RewardParams rewardParams = getRewardParams(jedis);

        if(rewardParams == null){
            rewardReturn.setCode(3);
            rewardReturn.setMsg("Get reward parameters failed. Check redis.");
            log.debug("Get reward parameters failed. Check redis.");
            return rewardReturn;
        }


        incomeT -= reservedFee; // for tx fee
        RewardInfo rewardInfo = makeRewardInfo(incomeT, rewardParams, jedis);
        if(rewardInfo==null){
            rewardReturn.setCode(4);
            rewardReturn.setMsg("Make rewardInfo wrong.");
            log.debug("Make rewardInfo wrong.");
            return rewardReturn;
        }

        log.debug("Made a rewardInfo. The sum of payment is {}.",calcSumPay(rewardInfo));

        AffairMaker affairMaker = new AffairMaker(account, rewardInfo,esClient,jedis);

        String affairSignTxJson = affairMaker.makeAffair();

        pendingMap = affairMaker.getPendingMapFromRedis();
        if(pendingMap!=null && !pendingMap.isEmpty()) {
            if (!backUpPending()) log.debug("Backup pendingMap failed.");
        }
        if(!makeSignTxAffairHtml(affairSignTxJson)){
            rewardReturn.setCode(5);
            rewardReturn.setMsg("Save affairSignTxJson to tomcat directory failed. Check tomcat directory.");
            return rewardReturn;
        }

        if(!backUpRewardInfo(rewardInfo,esClient)){
            rewardReturn.setCode(6);
            rewardReturn.setMsg("BackUp payment failed. Check ES.");
            return rewardReturn;
        }
        rewardReturn.setCode(0);
        return rewardReturn;
    }

    private boolean backUpPending() {
        Map<String ,String > pendingStrMap = new HashMap<>();
        for(String key: pendingMap.keySet()){
            String amountStr = String.valueOf(pendingMap.get(key));
            pendingStrMap.put(key,amountStr);
        }
        try{
            jedis.hmset(REWARD_PENDING_MAP,pendingStrMap);
            return true;
        }catch (Exception e){
            log.error("Write pending map into redis wrong.");
            return false;
        }
    }

    private void checkPayment(ElasticsearchClient esClient, Map<String, RewardInfo> unpaidRewardInfoMap)  {

        System.out.println("Check payment.");
        if(unpaidRewardInfoMap==null || unpaidRewardInfoMap.size()==0)return;

        SearchResponse<OpReturn> result;
        try {
            result = esClient.search(s -> s
                            .index(OPRETURN)
                            .query(q -> q.term(t -> t.field(SIGNER).value(account)))
                            .size(200)
                            .sort(so -> so
                                    .field(f -> f.field(HEIGHT)
                                            .order(SortOrder.Desc)))
                    , OpReturn.class);
        } catch (IOException e) {
            log.debug("Get OpReturn list wrong. Check ES.");
            return;
        }

        if(result == null){
            log.debug("Get OpReturn list of " + account +" failed. Check ES.");
            return;
        }
        if(result.hits().hits().size() == 0) {
            log.debug("No OpReturn list of " + account +".");
            return;
        }

        FcInfo feip;
        RewardData rewardData;
        Gson gson = new Gson();
        List<Hit<OpReturn>> hitList = result.hits().hits();
        OpReturn opReturn;

        for(Hit<OpReturn> hit : hitList){
             opReturn = hit.source();
            if(opReturn==null)continue;
            String txId = opReturn.getTxId();
            try {
                feip = FileParser.parseFeip(opReturn);
            }catch (Exception e){
                System.out.println(txId+" isn't reward.");
                continue;
            }
            
            if(feip!=null && "FBBP".equals(feip.getType())&&"1".equals(feip.getSn())){
                try {
                    rewardData = gson.fromJson(gson.toJson(feip.getData()), RewardData.class);
                }catch (Exception e){
                    continue;
                }
                RewardInfo rewardInfo = unpaidRewardInfoMap.get(rewardData.getRewardId());
                if( rewardInfo!=null) {

                    RewardState rewardState = checkPaymentState(esClient,txId,rewardInfo);

                    updateRewardInfo(esClient,txId,rewardData.getRewardId(),rewardState);

                    unpaidRewardInfoMap.remove(rewardData.getRewardId());
                    log.debug("Find reward just paid: {}",rewardData.getRewardId());
                }
                if(unpaidRewardInfoMap.size()==0){
                    log.debug("All reward paid.");
                    return;
                }
            }
        }
        if(unpaidRewardInfoMap.size()!=0) {
            System.out.println(unpaidRewardInfoMap.size() + " unpaid rewards: ");
            for (String id : unpaidRewardInfoMap.keySet()) {
                RewardInfo re = unpaidRewardInfoMap.get(id);
                String time = ParseTools.convertTimestampToDate(re.getTime());
                double amount = (double) re.getRewardT() /FchToSatoshi;
                System.out.println(time +" "+ amount+"f "+re.getRewardId());
            }
        }
    }

    private RewardState checkPaymentState(ElasticsearchClient esClient, String txId, RewardInfo rewardInfo) {

        Map<String, SendTo> sendToMapWithoutDust = makeNoDustSendToMap(rewardInfo);
        Map<String, SendTo> sentMap = calcSentMap(esClient,txId);
        return getPaymentState(sendToMapWithoutDust,sentMap);
    }

    private RewardState getPaymentState(Map<String, SendTo> sendToMapWithoutDust, Map<String, SendTo> sentMap) {
        for(String fid:sendToMapWithoutDust.keySet()){
            double sendToAmount = sendToMapWithoutDust.get(fid).getAmount();
            double sentAmount = sentMap.get(fid).getAmount();
            if(sentAmount!=sendToAmount){
                System.out.println("Difference found. Owe to send: "+sendToAmount+" Paid:"+sentAmount);
                return RewardState.paidRevised;
            }
        }
        return RewardState.paid;
    }

    private Map<String, SendTo> calcSentMap(ElasticsearchClient esClient, String txId) {
        GetResponse<TxHas> result = null;
        try {
            result = esClient.get(g -> g.index(TX_HAS).id(txId), TxHas.class);
        } catch (IOException e) {
            log.debug("Get reward sent txHas failed.");
        }
        TxHas txHas = null;
        if(result!=null) txHas=result.source();

        if(txHas==null)return null;

        Map<String, SendTo> sentToMap = new HashMap<>();
        for(CashMark cashMark: txHas.getOutMarks()){
            SendTo sendTo = new SendTo();
            sendTo.setFid(cashMark.getFid());
            sendTo.setAmount((double)cashMark.getValue()/FchToSatoshi);
            sentToMap.put(cashMark.getFid(),sendTo);
        }
        return sentToMap;
    }

    private Map<String, SendTo> makeNoDustSendToMap(RewardInfo rewardInfo) {
        Map<String, SendTo> sendToMap = AffairMaker.makeSendToMap(rewardInfo);
        sendToMap.entrySet().removeIf(entry -> entry.getValue().getAmount() < MinPayValue);
        return sendToMap;
    }


    private void updateRewardInfo(ElasticsearchClient esClient, String txId, String rewardId, RewardState rewardState) {

        try {
            Map<String, JsonData> paramMap = new HashMap<>();
            paramMap.put("sendTxId",JsonData.of(txId));
            paramMap.put("state",JsonData.of(rewardState.name()));

            esClient.update(u->u
                            .index(StartAPIP.getNameOfService(jedis,REWARD))
                            .id(rewardId)
                            .script(s->s
                                    .inline(in->in
                                            .source("ctx._source.state = params.state; ctx._source.txId = params.sendTxId")
                                            .params(paramMap))
                                    )
                    ,Void.class);
        } catch (IOException e) {
            log.debug("Update reward info wrong.",e);
        }
    }


    public static RewardParams getRewardParams(Jedis jedis) {
        RewardParams rewardParams = new RewardParams();

        try {
            Map<String, String> shareMap = jedis.hgetAll(serviceName+"_"+BUILDER_SHARE_MAP);
            rewardParams.setBuilderShareMap(shareMap);
            if(shareMap.isEmpty())return null;
        }catch (Exception e){
            System.out.println("Get builder's shares from redis failed. It's required for rewarding.");
            return null;
        }

        try{
            Map<String, String> costMap = jedis.hgetAll(serviceName+"_"+COST_MAP);
            rewardParams.setCostMap(costMap);

            rewardParams.setOrderViaShare(jedis.hget(serviceName+"_"+ PARAMS_ON_CHAIN,ORDER_VIA_SHARE));

            rewardParams.setConsumeViaShare(jedis.hget(serviceName+"_"+ PARAMS_ON_CHAIN,CONSUME_VIA_SHARE));

        }catch (Exception ignore){}
        return rewardParams;
    }

    public String getLastOrderId(ElasticsearchClient esClient) {
        SearchResponse<RewardInfo> result;
        try {
            result = esClient.search(s -> s
                            .index(StartAPIP.getNameOfService(jedis,REWARD))
                            .size(1)
                            .sort(so -> so.field(f -> f
                                    .field(TIME)
                                    .order(SortOrder.Desc)))
                    , RewardInfo.class);
        } catch (IOException e) {
            log.debug("Read last reward info from ES wrong.");
            return null;
        }

        if(result.hits().hits().size()==0){
            log.debug("No reward info found.");
            return null;
        }

        return result.hits().hits().get(0).id();
    }

    private Map<String, RewardInfo> getUnpaidRewardInfoMap(ElasticsearchClient esClient) {
        SearchResponse<RewardInfo> result;
        try {
            result = esClient.search(s -> s
                            .index(StartAPIP.getNameOfService(jedis,REWARD))
                            .query(q->q
                                    .term(t->t
                                            .field(STATE)
                                            .value(UNPAID)))
                            .size(200)
                            .sort(so -> so.field(f -> f
                                    .field(BEST_HEIGHT)
                                    .order(SortOrder.Desc)))
                    , RewardInfo.class);
        } catch (IOException e) {
            log.debug("Read unpaid reward info from ES wrong.");
            return null;
        }

        Map<String,RewardInfo> unpaidRewardInfoMap = new HashMap<>();

        if(result.hits().hits().size()==0){
            log.debug("No unpaid reward info found.");
            return null;
        }

        for(Hit<RewardInfo> hit: result.hits().hits()){
            RewardInfo re = hit.source();
            if(re!=null)unpaidRewardInfoMap.put(re.getRewardId(),re);
        }

        return unpaidRewardInfoMap;
    }

    public long makeIncomeT(String lastOrderId, ElasticsearchClient esClient) {
        List<SortOptions> sortOptionsList = Sort.makeHeightTxIndexSort();

        long sum = 0;
        SearchResponse<Order> result;
        try {
            result = esClient.search(s -> s
                            .index(StartAPIP.getNameOfService(jedis, ORDER))
                            .sort(sortOptionsList)
                            .size(EsTools.READ_MAX)
                    , Order.class);
        } catch (Exception e) {
            log.error("Get order list wrong.",e);
            return 0;
        }

        if(result==null||result.hits().hits().isEmpty()){
            log.debug("No any order. Check ES.");
            return 0;
        }
        List<Hit<Order>> hitList = result.hits().hits();
        List<String> last = hitList.get(hitList.size() - 1).sort();

        for(Hit<Order> hit : hitList){
            Order order = hit.source();

            if(order==null)continue;

            if(lastOrderId!=null && lastOrderId.equals(order.getOrderId())){
                this.lastOrderId = hitList.get(0).source().getOrderId();
                return sum;
            }
            sum += order.getAmount();
        }

        while (hitList.size()>=EsTools.READ_MAX) {
            try {
                List<String> finalLast = last;
                result = esClient.search(s -> s
                                .index(StartAPIP.getNameOfService(jedis, ORDER))
                                .sort(sortOptionsList)
                                .size(EsTools.READ_MAX)
                                .searchAfter(finalLast)
                        , Order.class);
            } catch (IOException e) {
                log.error("Get order list wrong.", e);
                return 0;
            }
            if(result==null||result.hits().hits().size()==0){
                log.debug("No any order. Check ES.");
                return 0;
            }
            hitList = result.hits().hits();
            last = hitList.get(hitList.size() - 1).sort();

            for(Hit<Order> hit : hitList){
                Order order = hit.source();
                if(order==null)continue;
                if(lastOrderId!=null && lastOrderId.equals(order.getOrderId())){
                    this.lastOrderId = hitList.get(0).source().getOrderId();
                    return sum;
                }
                sum += order.getAmount();
            }
        }

        if(hitList.size()>0){
            this.lastOrderId = hitList.get(0).source().getOrderId();
        }
        return sum;
    }

    public RewardInfo makeRewardInfo(long incomeT, RewardParams rewardParams, Jedis jedis) {

        RewardInfo rewardInfo = new RewardInfo();

        ArrayList<Payment> builderRewardList= new ArrayList<>();
        ArrayList<Payment> orderViaRewardList= new ArrayList<>();
        ArrayList<Payment> consumeViaRewardList= new ArrayList<>();
        ArrayList<Payment> costList= new ArrayList<>();

        Map<String, String> orderViaMap;
        Map<String, String> consumeViaMap;
        Map<String, String> builderShareMap;
        Map<String, String> costMap;
        try {
            orderViaMap = jedis.hgetAll(serviceName+"_"+ORDER_VIA);
            consumeViaMap = jedis.hgetAll(serviceName+"_"+CONSUME_VIA);
            builderShareMap = jedis.hgetAll(serviceName+"_"+BUILDER_SHARE_MAP);
            costMap = jedis.hgetAll(serviceName+"_"+COST_MAP);
        }catch (Exception e){
            log.error("Get {},{},{} or {} from redis wrong.", serviceName+"_"+ORDER_VIA, serviceName+"_"+CONSUME_VIA, serviceName+"_"+BUILDER_SHARE_MAP, serviceName+"_"+COST_MAP,e);
            return null;
        }

        Integer orderViaShare= parseViaShare(rewardParams, serviceName+"_"+ORDER_VIA);
        Integer consumeViaShare = parseViaShare(rewardParams, serviceName+"_"+CONSUME_VIA);
        if (orderViaShare<0) return null;
        if (consumeViaShare<0) return null;

        orderViaRewardList = makeViaPayList(orderViaMap, orderViaShare, serviceName+"_"+ORDER_VIA);
        consumeViaRewardList = makeViaPayList(consumeViaMap, consumeViaShare, serviceName+"_"+CONSUME_VIA);

        costList = makeCostPayList(costMap,incomeT);
        builderRewardList = makeBuilderPayList(builderShareMap,incomeT);

        rewardInfo.setOrderViaList(orderViaRewardList);
        rewardInfo.setConsumeViaList(consumeViaRewardList);
        rewardInfo.setBuilderList(builderRewardList);
        rewardInfo.setCostList(costList);

        rewardInfo.setRewardT(paidSum);
        rewardInfo.setState(RewardState.unpaid);
        rewardInfo.setRewardId(lastOrderId);

        rewardInfo.setTime(System.currentTimeMillis());
        rewardInfo.setBestHeight(jedis.get(BEST_HEIGHT));

        return rewardInfo;
    }

    public long calcSumPay(RewardInfo rewardInfo){
        long sum = 0;
        ArrayList<Payment> consumeList = rewardInfo.getConsumeViaList();
        ArrayList<Payment> orderList = rewardInfo.getOrderViaList();
        ArrayList<Payment> costList = rewardInfo.getCostList();
        ArrayList<Payment> builderList = rewardInfo.getBuilderList();
        ArrayList<Payment> all = new ArrayList<>();
        all.addAll(consumeList);
        all.addAll(orderList);
        all.addAll(costList);
        all.addAll(builderList);
        for(Payment payment: all){
            sum+=payment.getAmount();
        }
        return sum;
    }

    private ArrayList<Payment> makeCostPayList(Map<String, String> costMap, long income) {
        long costTotal = 0;
        Map<String,Long> costAmountMap = new HashMap<>();
        for(String fid: costMap.keySet()) {
            long amount;
            try {
                amount = (long) (Float.parseFloat(costMap.get(fid))*FchToSatoshi);
            } catch (Exception e) {
                log.error("Get cost of {} from redis wrong.", fid, e);
                return null;
            }
            costTotal += amount;
            costAmountMap.put(fid,amount);
        }
        int payPercent = (int) (recover4Decimal*(income-paidSum)/costTotal);
        if(payPercent>recover4Decimal)payPercent=recover4Decimal;
        return payCost(costAmountMap,payPercent);
    }
    private ArrayList<Payment> payCost(Map<String, Long> costAmountMap, int payPercent) {
        ArrayList<Payment> costList = new ArrayList<>();
        for (String fid : costAmountMap.keySet()) {
            Long amount = costAmountMap.get(fid);
            Payment payDetail = new Payment();
            payDetail.setFid(fid);
            payDetail.setFixed(amount);
            long finalPay = amount*payPercent/recover4Decimal;
            payDetail.setAmount(finalPay);
            paidSum+=finalPay;
            costList.add(payDetail);
        }
        return costList;
    }


    private ArrayList<Payment> makeBuilderPayList(Map<String, String> builderShareMap, long incomeT) {
        long builderSum = incomeT-paidSum;
        ArrayList<Payment> builderList = new ArrayList<>();
        for (String builder : builderShareMap.keySet()) {
            int share;
            try {
                share = (int) (Float.parseFloat(builderShareMap.get(builder))*recover4Decimal);
            }catch (Exception ignore){
                log.error("Get builder share of {} from redis wrong.",builder);
                return null;
            }
            long amount = builderSum*share/recover4Decimal;

            Payment payDetail = new Payment();
            payDetail.setFid(builder);
            payDetail.setShare(share);
            payDetail.setAmount(amount);
            builderList.add(payDetail);
        }
        paidSum += builderSum;
        return builderList;
    }

    private ArrayList<Payment> makeViaPayList(Map<String, String> viaMap, Integer viaShare, String orderVia) {
        ArrayList<Payment> viaPayDetailList = new ArrayList<>();
        for(String via: viaMap.keySet()){
            long amount;
            try {
                amount = viaShare *Long.parseLong(viaMap.get(via))/recover4Decimal;
                Payment payDetail = new Payment();
                payDetail.setFid(via);
                payDetail.setAmount(amount);
                payDetail.setShare(viaShare);
                viaPayDetailList.add(payDetail);

                paidSum += amount;

            }catch (Exception e){
                log.debug("Make {} of {} wrong.",via,orderVia,e);
            }
        }
        return viaPayDetailList;
    }

    private Integer parseViaShare(RewardParams rewardParams, String orderVia) {
        int viaShare;
        try {
            viaShare = (int) (Float.parseFloat(rewardParams.getOrderViaShare())*recover4Decimal);
        }catch (Exception ignore){
            log.error("Parse {} from redis wrong.",orderVia+"Share");
            return -1;
        }
        return viaShare;
    }

    private boolean makeSignTxAffairHtml(String signTxAffairJson) {
        String tomcatBashPath = jedis.hget(CONFIG, TOMCAT_BASE_PATH);
        if (!tomcatBashPath.endsWith("/")) tomcatBashPath += "/";
        String directoryPath = tomcatBashPath + REWARD;
        String fileName = REWARD_HTML_FILE;

        try {
            // Create the directory if it doesn't exist
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                boolean success = directory.mkdirs();
                if (!success) {
                    log.debug("Failed to create directory: " + directoryPath);
                    return false;
                }
            }

            // Create the file if it doesn't exist
            File file = new File(directory, fileName);
            if (!file.exists()) {
                boolean success = file.createNewFile();
                if (!success) {
                    log.debug("Failed to create file: " + fileName);
                    return false;
                }
            }

            // Write data to the file
            return makeHtml(signTxAffairJson, file);
        } catch (IOException e) {
            log.error("An error occurred when writing affair to file: " + e.getMessage());
        }
        return false;
    }


        private boolean makeHtml(String jsonString,File file) {
        String htmlString = "<html>\n" +
                "<head>\n" +
                "    <title>JSON Copier</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p id='jsonText'>" + jsonString + "</p>\n" +
                "    <button onclick='copyToClipboard()'>Copy</button>\n" +
                "    <script>\n" +
                "        function copyToClipboard() {\n" +
                "            var text = document.getElementById('jsonText').innerText;\n" +
                "            var el = document.createElement('textarea');\n" +
                "            el.value = text;\n" +
                "            el.setAttribute('readonly', '');\n" +
                "            el.style = {position: 'absolute', left: '-9999px'};\n" +
                "            document.body.appendChild(el);\n" +
                "            el.select();\n" +
                "            document.execCommand('copy');\n" +
                "            document.body.removeChild(el);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(htmlString);
        } catch (IOException e) {
            log.error("Write signTxAffairJson into html wrong. Check tomcat.");
            return false;
        }
        log.debug("SignTxAffairJson was wrote into html.");
        return true;
    }

    private boolean backUpRewardInfo(RewardInfo rewardInfo, ElasticsearchClient esClient) {

        try {
            esClient.index(i->i.index(StartAPIP.getNameOfService(jedis,REWARD)).id(rewardInfo.getRewardId()).document(rewardInfo));
        } catch (IOException e) {
            log.error("Backup rewardInfo wrong. Check ES.",e);
            return false;
        }
        log.debug("Backup rewardInfo into ES success. BestHeight.");

        JsonFileTools.writeObjectToJsonFile(rewardInfo,REWARD_HISTORY_FILE,true,RewardInfo.class);

        log.debug("Backup rewardInfo into "+REWARD_HISTORY_FILE+" success. BestHeight {}.",rewardInfo.getBestHeight());
        return true;
    }

    public void setRewardParameters(Jedis jedis,BufferedReader br) {

        System.out.println("Set reward parameters. Input numbers like '1.23456789' for an amount of FCH or '0.1234' for a share which means '12.34%'.");
        RewardParams rewardParams = getRewardParams(jedis);

        if(rewardParams==null)rewardParams = new RewardParams();
        Params params = StartAPIP.service.getParams();
        Double share;
        if(params!=null&&params.getConsumeViaShare()==null){
            System.out.println("Set consumeViaShare");
            share = Inputer.inputGoodShare(br);
            String consumeViaShare;
            if (share != null) {
                consumeViaShare = String.valueOf(share);
                rewardParams.setConsumeViaShare(consumeViaShare);
            }
        }

        if(params!=null&&params.getOrderViaShare()==null) {
            System.out.println("Set orderViaShare");
            share = Inputer.inputGoodShare(br);
            String orderViaShare;
            if (share != null) {
                orderViaShare = String.valueOf(share);
                rewardParams.setOrderViaShare(orderViaShare);
            }
        }

        Map<String,String> costMap = Inputer.inputGoodFidValueStrMap(br, serviceName+"_"+COST_MAP,false);
        if(costMap!=null)rewardParams.setCostMap(costMap);

        Map<String, String> builderShareMap;
        while(true) {
            builderShareMap = Inputer.inputGoodFidValueStrMap(br, serviceName + "_" + BUILDER_SHARE_MAP,true);

            if(builderShareMap==null ||builderShareMap.isEmpty()){
                System.out.println("BuilderShareMap can't be empty.");
                continue;
            }

            if(!Menu.isFullShareMap(builderShareMap)) continue;

            rewardParams.setBuilderShareMap(builderShareMap);
            break;
        }

        writeRewardParamsToRedis(rewardParams,jedis);

        log.debug("Reward parameters were set.");
    }

    private void writeRewardParamsToRedis(RewardParams rewardParams, Jedis jedis) {

        ParseTools.gsonPrint(rewardParams);

        try{
            if(rewardParams.getOrderViaShare()!=null)jedis.hset(serviceName+"_"+ PARAMS_ON_CHAIN,ORDER_VIA_SHARE,rewardParams.getOrderViaShare());
            if(rewardParams.getConsumeViaShare()!=null)jedis.hset(serviceName+"_"+ PARAMS_ON_CHAIN,CONSUME_VIA_SHARE,rewardParams.getConsumeViaShare());
            if(!rewardParams.getBuilderShareMap().isEmpty())jedis.hmset(serviceName+"_"+BUILDER_SHARE_MAP,rewardParams.getBuilderShareMap());
            if(rewardParams.getCostMap()!=null&&!rewardParams.getCostMap().isEmpty())jedis.hmset(serviceName+"_"+COST_MAP, rewardParams.getCostMap());
        }catch (Exception e){
            log.error("Write rewardParams into redis wrong.",e);
        }
    }

    public void setIncomeT(long incomeT) {
        this.incomeT = incomeT;
    }

    public String getLastOrderId() {
        return lastOrderId;
    }

    public void setLastOrderId(String lastOrderId) {
        this.lastOrderId = lastOrderId;
    }
}
