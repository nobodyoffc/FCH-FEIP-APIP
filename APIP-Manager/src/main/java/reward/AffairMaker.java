package reward;

import FipaClass.Affair;
import FipaClass.DataSignTx;
import FipaClass.Op;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.Gson;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import walletTools.*;
import fchClass.Cash;
import feipClass.Feip;

import java.util.*;

import static constants.Constants.*;
import static constants.Strings.*;

public class AffairMaker {

    private RewardInfo rewardInfo;
    private String account;
    private DataForOffLineTx dataForOffLineTx;
    private List<Cash> meetCashList;
    private String msg;
    private final Gson gson = new Gson();

    private Affair affairReward = new Affair();
    private DataSignTx dataSignTx = new DataSignTx();

    private final ElasticsearchClient esClient;
    private final Jedis jedis;
    private Map<String, Long> pendingMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(AffairMaker.class);

    public AffairMaker(String account, RewardInfo rewardInfo, ElasticsearchClient esClient, Jedis jedis) {
        this.rewardInfo = rewardInfo;
        this.account = account;
        this.esClient = esClient;
        this.jedis = jedis;
        getPendingMapFromRedis();
    }
    public Map<String, Long> getPendingMapFromRedis() {
        Map<String, String> pendingStrMap = jedis.hgetAll(REWARD_PENDING_MAP);
        for(String key: pendingStrMap.keySet()){
            Long amount =Long.parseLong( pendingStrMap.get(key));
            pendingMap.put(key,amount);
        }
        return pendingMap;
    }

    public String makeAffair(){

        Feip feip = new Feip();
        feip.setType(FBBP);
        feip.setSn("1");
        feip.setVer("1");

        RewardData rewardData = new RewardData();

        rewardData.setOp(REWARD);
        rewardData.setRewardId(rewardInfo.getRewardId());
        feip.setData(rewardData);

        msg = gson.toJson(feip);

        long rewardT = rewardInfo.getRewardT();

        CashListReturn cashListReturn = WalletTools.getCashListForPay(rewardT,account,esClient);

        if(cashListReturn.getCode()>0){
            log.debug(cashListReturn.getMsg());
            return null;
        }

        List<Cash> cashList = cashListReturn.getCashList();
        if (cashList==null || cashList.size()==0){
            return null;
        }

        HashMap<String,SendTo> sendToMap = makeSendToMap(rewardInfo);

        pendingDust(sendToMap);

        addQualifiedPendingToPay(sendToMap);

        DataForOffLineTx dataForOffLineTx = new DataForOffLineTx();
        dataForOffLineTx.setFromFid(account);
        dataForOffLineTx.setSendToList(new ArrayList<>(sendToMap.values()));
        dataForOffLineTx.setCd(0);
        dataForOffLineTx.setMsg(msg);

        String rawTxStr = CryptoSigner.makeRawTxForCs(dataForOffLineTx,cashList);

        dataSignTx.setUnsignedTxCs(rawTxStr);
        dataSignTx.setAlg(ALG_SIGN_TX_BY_CRYPTO_SIGN);

        affairReward.setFid(account);
        affairReward.setOp(Op.sign);
        affairReward.setData(dataSignTx);

        return ParseTools.gsonString(affairReward);
    }

    private void addQualifiedPendingToPay(HashMap<String, SendTo> sendToMap) {
        for(String key: pendingMap.keySet()){
            double amount = (double) pendingMap.get(key) /FchToSatoshi;
            SendTo sendTo = new SendTo();
            if (amount >= MinPayValue){
                if(sendToMap.get(key)!=null){
                    sendTo = sendToMap.get(key);
                    sendTo.setFid(key);
                    sendTo.setAmount(sendTo.getAmount()+amount);
                }else {
                    sendTo.setFid(key);
                    sendTo.setAmount(amount);
                }
                sendToMap.put(key,sendTo);
                pendingMap.remove(key);
            }
        }
    }

    private void pendingDust(HashMap<String, SendTo> sendToMap) {
        Iterator<Map.Entry<String, SendTo>> iterator = sendToMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SendTo> entry = iterator.next();
            String key = entry.getKey();
            SendTo sendTo = entry.getValue();
            String fid = sendTo.getFid();
            double amount = sendTo.getAmount();
            if (amount < MinPayValue) {
                addToPending(fid, (long) (amount*FchToSatoshi));
                iterator.remove();
            }
        }
    }

    public static HashMap<String,SendTo> makeSendToMap(RewardInfo rewardInfo) {
        HashMap<String,SendTo> sendToMap= new HashMap<>();

        ArrayList<Payment> buildList = rewardInfo.getBuilderList();
        ArrayList<Payment> costList = rewardInfo.getCostList();
        ArrayList<Payment> consumeViaList = rewardInfo.getConsumeViaList();
        ArrayList<Payment> orderViaList = rewardInfo.getOrderViaList();

        makePayDetailListIntoSendToMap(sendToMap,orderViaList);
        makePayDetailListIntoSendToMap(sendToMap,consumeViaList);
        makePayDetailListIntoSendToMap(sendToMap,costList);
        makePayDetailListIntoSendToMap(sendToMap,buildList);

        return sendToMap;
    }

    public static void makePayDetailListIntoSendToMap(HashMap<String,SendTo> sendToMap,ArrayList<Payment> payDetailList) {

        for(Payment payDetail:payDetailList){
            SendTo sendTo = new SendTo();
            String fid = payDetail.getFid();
            sendTo.setFid(fid);
            double amount = (double) payDetail.getAmount() /FchToSatoshi;
            if(sendToMap.get(fid)!=null){
                amount = amount+ sendToMap.get(fid).getAmount();
                sendTo.setAmount(ParseTools.roundDouble8(amount));
            }else{
                sendTo.setAmount(ParseTools.roundDouble8(amount));
            }
            sendToMap.put(sendTo.getFid(),sendTo);
        }
    }

    private void addToPending(String fid, Long amount) {
        Long pendingValue = 0L;
        try{
            pendingValue = Long.parseLong(jedis.hget(REWARD_PENDING_MAP, fid));
        }catch (Exception ignore){}
        if(pendingMap.get(fid)!=null) pendingValue += pendingMap.get(fid);
        pendingMap.put(fid,pendingValue+ amount);
    }

    public RewardInfo getRewardInfo() {
        return rewardInfo;
    }

    public void setRewardInfo(RewardInfo rewardInfo) {
        this.rewardInfo = rewardInfo;
    }

    public Affair getAffairReward() {
        return affairReward;
    }

    public void setAffairReward(Affair affairReward) {
        this.affairReward = affairReward;
    }

    public DataSignTx getDataSignTx() {
        return dataSignTx;
    }

    public void setDataSignTx(DataSignTx dataSignTx) {
        this.dataSignTx = dataSignTx;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public DataForOffLineTx getDataForOffLineTx() {
        return dataForOffLineTx;
    }

    public void setDataForOffLineTx(DataForOffLineTx dataForOffLineTx) {
        this.dataForOffLineTx = dataForOffLineTx;
    }

    public List<Cash> getMeetCashList() {
        return meetCashList;
    }

    public void setMeetCashList(List<Cash> meetCashList) {
        this.meetCashList = meetCashList;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Long> getPendingMap() {
        return pendingMap;
    }

    public void setPendingMap(Map<String, Long> pendingMap) {
        this.pendingMap = pendingMap;
    }
}
