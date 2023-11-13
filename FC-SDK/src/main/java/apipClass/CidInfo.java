package apipClass;

import fchClass.Address;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import feipClass.Cid;
import fcTools.WeightMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.IndicesNames.ADDRESS;
import static constants.IndicesNames.CID;

public class CidInfo {
    private String fid;    //fch address
    private String cid;
    private String [] usedCids;
    private String pubKey;        //public key
    private String priKey;

    private long balance;        //value of fch in satoshi
    private long cash;        //Count of UTXO
    private long income;        //total amount of fch received in satoshi
    private long expend;        //total amount of fch pay in satoshi

    private long cd;        //CoinDays
    private long cdd;        //the total amount of coindays destroyed
    private long reputation;
    private long hot;
    private long weight;

    private String master;
    private  String guide;    //the address of the address which sent the first fch to this address
    private  String noticeFee;
    private  String[] homepages;

    private String btcAddr;    //the btc address
    private String ethAddr;    //the eth address
    private String ltcAddr;    //the ltc address
    private String dogeAddr;    //the doge address
    private String trxAddr;    //the doge address

    private long birthHeight;    //the height where this address got its first fch
    private long nameTime;
    private long lastHeight;     //the height where this address info changed latest. If roll back happened, lastHei point to the lastHeight before fork.

    public void reCalcWeight(){
        this.weight=WeightMethod.calcWeight(this.cd,this.cdd,this.reputation);
    }
    public static List<CidInfo> mergeCidInfoList(List<Cid> meetCidList, List<Address> meetAddrList) {
        List<CidInfo> cidInfoList = new ArrayList<>();
        Map<String,Cid> cidMap = new HashMap<>();

        if(meetCidList!=null&& !meetCidList.isEmpty()) {
            for (Cid cid : meetCidList) {
                cidMap.put(cid.getFid(), cid);
            }
        }

        if(meetAddrList==null || meetAddrList.isEmpty()) return null;

        for(Address addr : meetAddrList){
            CidInfo cidInfo = new CidInfo();
            String id = addr.getFid();
            setAddrToCidInfo(addr,cidInfo);
            Cid cid = cidMap.get(id);
            if(cid!=null) {
                setCidToCidInfo(cid,cidInfo);
                cidMap.remove(id);
            }
            cidInfoList.add(cidInfo);
        }

        for(String id: cidMap.keySet()){
            CidInfo cidInfo = new CidInfo();
            Cid cid = cidMap.get(id);
            setCidToCidInfo(cid, cidInfo);
            cidInfoList.add(cidInfo);
        }

        return cidInfoList;
    }

    public static CidInfo mergeCidInfo(Cid cid, Address addr) {

        CidInfo cidInfo = new CidInfo();
        if(addr!=null){
            setAddrToCidInfo(addr,cidInfo);
            if(cid!=null){
                setCidToCidInfo(cid,cidInfo);
                return cidInfo;
            }
        }
        return null;
    }

    private static void setAddrToCidInfo(Address addr, CidInfo cidInfo) {
        cidInfo.setFid(addr.getFid());
        cidInfo.setBalance(addr.getBalance());
        cidInfo.setBirthHeight(addr.getBirthHeight());
        cidInfo.setBtcAddr(addr.getBtcAddr());
        cidInfo.setCd(addr.getCd());
        cidInfo.setCdd(addr.getCdd());
        cidInfo.setWeight(addr.getWeight());

        cidInfo.setDogeAddr(addr.getDogeAddr());
        cidInfo.setEthAddr(addr.getEthAddr());
        cidInfo.setExpend(addr.getExpend());
        cidInfo.setGuide(addr.getGuide());
        cidInfo.setPubKey(addr.getPubKey());
        cidInfo.setIncome(addr.getIncome());
        cidInfo.setLastHeight(addr.getLastHeight());
        cidInfo.setTrxAddr(addr.getTrxAddr());
        cidInfo.setCash(addr.getCash());
        cidInfo.setLtcAddr(addr.getLtcAddr());
    }

    private static void setCidToCidInfo(Cid cid,CidInfo cidInfo) {
        cidInfo.setFid(cid.getFid());
        cidInfo.setCid(cid.getCid());
        cidInfo.setHomepages(cid.getHomepages());
        cidInfo.setHot(cid.getHot());
        cidInfo.setMaster(cid.getMaster());
        cidInfo.setNameTime(cid.getNameTime());
        cidInfo.setNoticeFee(cid.getNoticeFee());
        cidInfo.setPriKey(cid.getPriKey());
        cidInfo.setReputation(cid.getReputation());
        cidInfo.setUsedCids(cid.getUsedCids());
    }

//    public static List<Address> getAddrList(List<String> addrIdList) throws IOException {
//
//        MgetResponse<Address> result = esClient.mget(m -> m
//                .index(ADDRESS)
//                .ids(addrIdList), Address.class);
//        List<MultiGetResponseItem<Address>> itemList = result.docs();
//
//        List<Address> addrList = new ArrayList<>();
//
//        for(MultiGetResponseItem<Address> item : itemList){
//            if(!item.isFailure()) {
//                if(item.result().found())
//                    addrList.add(item.result().source());
//            }
//        }
//        return addrList;
//    }
//
//    public static List<Cid> getCidList(List<String> addrIdList) throws IOException {
//
//        MgetResponse<Cid> result = esClient.mget(m -> m
//                .index(CID)
//                .ids(addrIdList), Cid.class);
//        List<MultiGetResponseItem<Cid>> itemList = result.docs();
//
//        List<Cid> cidList = new ArrayList<>();
//
//        for(MultiGetResponseItem<Cid> item : itemList){
//            if(!item.isFailure()) {
//                if(item.result().found())
//                    cidList.add(item.result().source());
//            }
//        }
//        return cidList;
//    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String[] getUsedCids() {
        return usedCids;
    }

    public void setUsedCids(String[] usedCids) {
        this.usedCids = usedCids;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getCash() {
        return cash;
    }

    public void setCash(long cash) {
        this.cash = cash;
    }

    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getExpend() {
        return expend;
    }

    public void setExpend(long expend) {
        this.expend = expend;
    }

    public long getCd() {
        return cd;
    }

    public void setCd(long cd) {
        this.cd = cd;
    }

    public long getCdd() {
        return cdd;
    }

    public void setCdd(long cdd) {
        this.cdd = cdd;
    }

    public long getReputation() {
        return reputation;
    }

    public void setReputation(long reputation) {
        this.reputation = reputation;
    }

    public long getHot() {
        return hot;
    }

    public void setHot(long hot) {
        this.hot = hot;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getGuide() {
        return guide;
    }

    public void setGuide(String guide) {
        this.guide = guide;
    }

    public String getNoticeFee() {
        return noticeFee;
    }

    public void setNoticeFee(String noticeFee) {
        this.noticeFee = noticeFee;
    }

    public String[] getHomepages() {
        return homepages;
    }

    public void setHomepages(String[] homepages) {
        this.homepages = homepages;
    }

    public String getBtcAddr() {
        return btcAddr;
    }

    public void setBtcAddr(String btcAddr) {
        this.btcAddr = btcAddr;
    }

    public String getEthAddr() {
        return ethAddr;
    }

    public void setEthAddr(String ethAddr) {
        this.ethAddr = ethAddr;
    }

    public String getLtcAddr() {
        return ltcAddr;
    }

    public void setLtcAddr(String ltcAddr) {
        this.ltcAddr = ltcAddr;
    }

    public String getDogeAddr() {
        return dogeAddr;
    }

    public void setDogeAddr(String dogeAddr) {
        this.dogeAddr = dogeAddr;
    }

    public String getTrxAddr() {
        return trxAddr;
    }

    public void setTrxAddr(String trxAddr) {
        this.trxAddr = trxAddr;
    }

    public long getBirthHeight() {
        return birthHeight;
    }

    public void setBirthHeight(long birthHeight) {
        this.birthHeight = birthHeight;
    }

    public long getNameTime() {
        return nameTime;
    }

    public void setNameTime(long nameTime) {
        this.nameTime = nameTime;
    }

    public long getLastHeight() {
        return lastHeight;
    }

    public void setLastHeight(long lastHeight) {
        this.lastHeight = lastHeight;
    }
}