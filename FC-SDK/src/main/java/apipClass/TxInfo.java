package apipClass;

import fchClass.CashMark;
import fchClass.Tx;
import fchClass.TxHas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxInfo {
    private String id;		//txid,hash of tx
    private int version;		//version
    private long lockTime;	//locktime
    private long blockTime;		//blockTime
    private String blockId;		//block ID, hash of block head
    private int txIndex;		//the index of this tx in the block
    private String coinbase;	//string of the coinbase script
    private int outCount;		//number of outputs
    private int inCount;		//number of inputs
    private long height;		//block height of the block

    private String opReBrief; 	//Former 30 bytes of OP_RETURN data in String.

    //calculated
    private long inValueT;		//total amount of inputs
    private long outValueT;		//total amount of outputs
    private long fee;		//tx fee

    private long cdd;

    private ArrayList<CashMark> spentCashes;
    private  ArrayList<CashMark> issuedCashes;

    public static List<TxInfo> mergeTxAndTxHas(List<Tx> txList, List<TxHas> txHasList) {
        List<TxInfo> result = new ArrayList<>();
        Map<String, Tx> txMap = new HashMap<>();

        for (Tx tx : txList) {
            txMap.put(tx.getTxId(), tx);
        }

        for (TxHas txHas : txHasList) {
            Tx tx = txMap.get(txHas.getTxId());

            if (tx != null) {
                TxInfo txInfo = new TxInfo();
                txInfo.setId(tx.getTxId());
                txInfo.setHeight(txHas.getHeight());
                txInfo.setSpentCashes(txHas.getInMarks());
                txInfo.setIssuedCashes(txHas.getOutMarks());
                txInfo.setVersion(tx.getVersion());
                txInfo.setLockTime(tx.getLockTime());
                txInfo.setBlockTime(tx.getBlockTime());
                txInfo.setBlockId(tx.getBlockId());
                txInfo.setTxIndex(tx.getTxIndex());
                txInfo.setCoinbase(tx.getCoinbase());
                txInfo.setOutCount(tx.getOutCount());
                txInfo.setInCount(tx.getInCount());
                txInfo.setOpReBrief(tx.getOpReBrief());
                txInfo.setInValueT(tx.getInValueT());
                txInfo.setOutValueT(tx.getOutValueT());
                txInfo.setFee(tx.getFee());
                txInfo.setCdd(tx.getCdd());
                result.add(txInfo);
            }
        }

        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    public String getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(String coinbase) {
        this.coinbase = coinbase;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }

    public int getInCount() {
        return inCount;
    }

    public void setInCount(int inCount) {
        this.inCount = inCount;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getOpReBrief() {
        return opReBrief;
    }

    public void setOpReBrief(String opReBrief) {
        this.opReBrief = opReBrief;
    }

    public long getInValueT() {
        return inValueT;
    }

    public void setInValueT(long inValueT) {
        this.inValueT = inValueT;
    }

    public long getOutValueT() {
        return outValueT;
    }

    public void setOutValueT(long outValueT) {
        this.outValueT = outValueT;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getCdd() {
        return cdd;
    }

    public void setCdd(long cdd) {
        this.cdd = cdd;
    }

    public ArrayList<CashMark> getSpentCashes() {
        return spentCashes;
    }

    public void setSpentCashes(ArrayList<CashMark> spentCashes) {
        this.spentCashes = spentCashes;
    }

    public ArrayList<CashMark> getIssuedCashes() {
        return issuedCashes;
    }

    public void setIssuedCashes(ArrayList<CashMark> issuedCashes) {
        this.issuedCashes = issuedCashes;
    }
}
