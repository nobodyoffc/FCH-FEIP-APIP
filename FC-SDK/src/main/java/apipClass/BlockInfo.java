package apipClass;

import fchClass.Block;
import fchClass.BlockHas;
import fchClass.TxMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockInfo {
    // Block properties
    private long size;
    private long height;
    private String version;
    private String preId;
    private String merkleRoot;
    private long time;
    private long bits;
    private long nonce;
    private int txCount;
    private long inValueT;
    private long outValueT;
    private long fee;
    private long cdd;

    // BlockHas properties
    private String blockId;
    private ArrayList<TxMark> txList;

    // Getters and setters for all properties


    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPreId() {
        return preId;
    }

    public void setPreId(String preId) {
        this.preId = preId;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBits() {
        return bits;
    }

    public void setBits(long bits) {
        this.bits = bits;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
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

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public ArrayList<TxMark> getTxList() {
        return txList;
    }

    public void setTxList(ArrayList<TxMark> txList) {
        this.txList = txList;
    }

    // Method to merge lists of Block and BlockHas into a list of BlockInfo
    public static List<BlockInfo> mergeBlockAndBlockHas(List<Block> blockList, List<BlockHas> blockHasList) {
        ArrayList<BlockInfo> blockInfoList = new ArrayList<>();
        Map<String,BlockHas> blockHasMap = new HashMap<>();

        for (BlockHas blockHas : blockHasList)
            blockHasMap.put(blockHas.getBlockId(),blockHas);

        for (Block block : blockList) {
            BlockInfo blockInfo = new BlockInfo();
            blockInfo.setSize(block.getSize());
            blockInfo.setHeight(block.getHeight());
            blockInfo.setVersion(block.getVersion());
            blockInfo.setPreId(block.getPreBlockId());
            blockInfo.setMerkleRoot(block.getMerkleRoot());
            blockInfo.setTime(block.getTime());
            blockInfo.setBits(block.getBits());
            blockInfo.setNonce(block.getNonce());
            blockInfo.setTxCount(block.getTxCount());
            blockInfo.setInValueT(block.getInValueT());
            blockInfo.setOutValueT(block.getOutValueT());
            blockInfo.setFee(block.getFee());
            blockInfo.setCdd(block.getCdd());
            blockInfo.setBlockId(block.getBlockId());

            blockInfo.setTxList(blockHasMap.get(block.getBlockId()).getTxMarks());

            blockInfoList.add(blockInfo);
        }
        return blockInfoList;
    }
}

