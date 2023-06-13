package parser;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import FchClass.Cash;
import FchClass.Tx;
import javaTools.BytesTools;
import keyTools.KeyTools;
import fcTools.ParseTools;
import servers.EsTools;
import servers.NewEsClient;
import startFCH.ConfigFCH;
import startFCH.IndicesFCH;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static fcTools.ParseTools.parseVarint;

public class RawTxParser {
    static String spendCashMapKey = "spentCashMap";
    static String newCashMapKey = "newCashMap";

//    public static void main(String[] args) throws Exception {
//        String txHex = "020000000288fb3f1dec2201a614b3b4ba6d6cfe9030b888b4915e00e2c588da6450662ca70100000064414aa17004d314d8024d4489ae343dd7456926cfd930028a243c9c7cf8f48b1d84d0d286a03d7a03b4264b5fa315fe0087d1342d554d5182a112924cd732673972412103f1af10342bfac3b06f2088e1340941d70e27aa8adecdfe24f6f1ba1e334c6eaaffffffff88fb3f1dec2201a614b3b4ba6d6cfe9030b888b4915e00e2c588da6450662ca700000000644173a5defd95d171ac91f74276a6c9a0fc8f6a263d9f0b43304f56df687ffa30c8c9b8b8bb7afd9ab1ea530855b88bae5a11d9fd1983b3daeec415ab324d1b29b2412103f1af10342bfac3b06f2088e1340941d70e27aa8adecdfe24f6f1ba1e334c6eaaffffffff0200ca9a3b000000001976a914bff35cb6b032194a8cb6fb85578054a0378db03d88ac8d016ccf000000001976a914bff35cb6b032194a8cb6fb85578054a0378db03d88ac00000000";
//        String txid = "1679b78c8f4c5cdc57f269612b0293190db8a1e4a890d4c427ba6008f395c66d";
//        byte[] txBytes = BytesTools.hexToByteArray(txHex);
////        System.out.println("Txid: " + txid);
////        System.out.println("rawTx BE:" + BytesTools.bytesToHexStringBE(txBytes));
////        System.out.println("Txid caculated is :" + BytesTools.bytesToHexStringLE(Hash.Sha256x2(txBytes)));
////        System.out.println("rawTx inverted BE: " + BytesTools.bytesToHexStringBE(BytesTools.invertArray(txBytes)));
////        System.out.println("Txid caculated and converted is :" + BytesTools.bytesToHexStringBE(Hash.Sha256x2(BytesTools.invertArray(txBytes))));
////        parseUnconfirmedTxBytes(txBytes, txid);
//
//        ElasticsearchClient esClient = getEsclient();
//        TxInMempool txInPool = parseMempoolTx(esClient, txHex, txid);
//        ParseTools.gsonPrint(txInPool);
//    }

    private static ElasticsearchClient getEsclient() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ConfigFCH configFCH = new ConfigFCH();
        NewEsClient newEsClient;
        newEsClient = new NewEsClient();
        ElasticsearchClient esClient = null;
        configFCH= configFCH.getClassInstanceFromFile(br, ConfigFCH.class);
        return newEsClient.checkEsClient(esClient, configFCH);
    }

    public static TxInMempool parseMempoolTx(ElasticsearchClient esClient,String txHex, String txid) throws Exception {
        Map<String, Map<String, Cash>> cashMapMap = parseUnconfirmedTxHex(txHex, txid);
        ArrayList<Cash> inCashList = makeInCashMap(esClient, cashMapMap.get(spendCashMapKey),cashMapMap.get(newCashMapKey));
        ArrayList<Cash> outCashList = new ArrayList<>(cashMapMap.get(newCashMapKey).values());

        for(Cash cash:inCashList) cash.setSpendTxId(txid);
        for(Cash cash:outCashList) cash.setBirthTxId(txid);

        Tx tx = makeTx(txid,inCashList,outCashList);
        TxInMempool txInMempool = new TxInMempool();
        txInMempool.setTx(tx);
        txInMempool.setInCashList(inCashList);
        txInMempool.setOutCashList(outCashList);
        return txInMempool;
    }

    private static Tx makeTx(String txid, ArrayList<Cash> inCashList, ArrayList<Cash> outCashList) {
        Tx tx = new Tx();
        tx.setTxId(txid);
        tx.setInCount(inCashList.size());
        long inValueT =0;
        for(Cash cash:inCashList){
            inValueT = inValueT + cash.getValue();
        }
        tx.setInValueT(inValueT);
        tx.setOutCount(outCashList.size());
        long outValueT=0;
        for(Cash cash:outCashList){
            outValueT = outValueT + cash.getValue();
        }
        tx.setOutValueT(outValueT);
        tx.setFee(inValueT-outValueT);
        return tx;
    }

    public static Map<String, Map<String, Cash>> parseUnconfirmedTxHex(String txHex, String txid) throws Exception {
        byte[] txBytes = BytesTools.hexToByteArray(txHex);
        return parseUnconfirmedTxBytes(txBytes,txid);
    }
    public static Map<String, Map<String, Cash>> parseUnconfirmedTxBytes(byte[] txBytes, String txid) throws Exception {

        ByteArrayInputStream blockInputStream = new ByteArrayInputStream(txBytes);

        // Read tx version/读取交易的版本
        byte[] b4Version = new byte[4];
        blockInputStream.read(b4Version);

        // Read inputs /读输入
        // ParseTxOutResult parseTxOutResult
        Map<String, Cash> spendCashMap = parseInput(blockInputStream,txid);

        // Read outputs /读输出
        // Parse Outputs./解析输出。
        Map<String, Cash> newCashMap = parseOut(blockInputStream, txid);

        Map<String, Map<String,Cash>> cashMapMap = new HashMap<>();
        cashMapMap.put(spendCashMapKey,spendCashMap);
        cashMapMap.put(newCashMapKey,newCashMap);
        return cashMapMap;
    }
    private static Map<String, Cash> parseInput(ByteArrayInputStream rawTxInputStream,String txid) throws IOException {

        // Get input count./获得输入数量
        ParseTools.VarintResult varintParseResult;
        varintParseResult = parseVarint(rawTxInputStream);
        long inputCount = varintParseResult.number;

        // Read inputs /读输入
        Map<String,Cash> spendCashMap = new HashMap<>();

        for (int j = 0; j < inputCount; j++) {
            Cash spentCash = new Cash();
            spentCash.setSpendTxId(txid);
            // Read preTXHash and preOutIndex./读取前交易哈希和输出索引。
            byte[] b36PreTxIdAndIndex = new byte[32 + 4];
            rawTxInputStream.read(b36PreTxIdAndIndex);
            String cashId =ParseTools.calcTxoIdFromBytes(b36PreTxIdAndIndex);
            spentCash.setCashId(cashId);

            // Read the length of script./读脚本长度。
            varintParseResult = ParseTools.parseVarint(rawTxInputStream);
            long scriptCount = varintParseResult.number;

            // Get script./获取脚本。
            byte[] bvScript = new byte[(int) scriptCount];
            rawTxInputStream.read(bvScript);
            spentCash.setUnlockScript(BytesTools.bytesToHexStringBE(bvScript));

            // Parse sigHash.
            // 解析sigHash。
            int sigLen = Byte.toUnsignedInt(bvScript[0]);// Length of signature;
            // Skip signature/跳过签名。
            byte sigHash = bvScript[sigLen];// 交易类型标志
            switch (sigHash) {
                case 0x41:
                    spentCash.setSigHash("ALL");
                    break;
                case 0x42:
                    spentCash.setSigHash("NONE");
                    break;
                case 0x43:
                    spentCash.setSigHash("SINGLE");
                    break;
                case (byte) 0xc1:
                    spentCash.setSigHash("ALLIANYONECANPAY");
                    break;
                case (byte) 0xc2:
                    spentCash.setSigHash("NONEIANYONECANPAY");
                    break;
                case (byte) 0xc3:
                    spentCash.setSigHash("SINGLEIANYONECANPAY");
                    break;
                default:
                    spentCash.setSigHash(null);
            }

            // Get sequence./获取sequence。
            byte[] b4Sequence = new byte[4];
            rawTxInputStream.read(b4Sequence);

            spentCash.setSequence(BytesTools.bytesToHexStringBE(b4Sequence));
            spendCashMap.put(cashId,spentCash);
        }
        return spendCashMap;
    }
    private static Map<String, Cash> parseOut(ByteArrayInputStream rawTxInputStream, String txid) throws IOException {
        Map<String,Cash> rawNewCashMap = new HashMap<>();

        // Parse output count.
        // 解析输出数量。
        ParseTools.VarintResult varintParseResult = new ParseTools.VarintResult();
        varintParseResult =ParseTools.parseVarint(rawTxInputStream);
        long outputCount = varintParseResult.number;

        // Starting operators in output script.
        // 输出脚本中的起始操作码。
        final byte OP_DUP = (byte) 0x76;
        final byte OP_HASH160 = (byte) 0xa9;
        final byte OP_RETURN = (byte) 0x6a;
        byte b1Script = 0x00; // For get the first byte of output script./接收脚本中的第一个字节。

        for(int j = 0; j<outputCount;j++) {
            // Start one output.
            // 开始解析一个输出。
            Cash newCash = new Cash();
            newCash.setBirthTxId(txid);
            newCash.setBirthIndex(j);
            String cashId = ParseTools.calcTxoId(txid, j);
            newCash.setCashId(cashId);

            // Read the value of this output in satoshi.
            // 读取该输出的金额，以聪为单位。
            byte[] b8Value = new byte[8];
            rawTxInputStream.read(b8Value);
            newCash.setValue(BytesTools.bytes8ToLong(b8Value, true));

            // Parse the length of script.
            // 解析脚本长度。
            varintParseResult = ParseTools.parseVarint(rawTxInputStream);
            long scriptSize = varintParseResult.number;
            byte[] b2 = varintParseResult.rawBytes;

            byte[] bScript = new byte[(int) scriptSize];
            rawTxInputStream.read(bScript);

            b1Script = bScript[0];

            switch (b1Script) {
                case OP_DUP -> {
                    newCash.setType("P2PKH");
                    newCash.setLockScript(BytesTools.bytesToHexStringBE(bScript));
                    byte[] hash160Bytes = Arrays.copyOfRange(bScript, 3, 23);
                    newCash.setFid(KeyTools.hash160ToFCHAddr(hash160Bytes));
                }
                case OP_RETURN -> {
                    newCash.setType("OP_RETURN");
                    newCash.setFid("OpReturn");
                    newCash.setValid(false);
                }
                case OP_HASH160 -> {
                    newCash.setType("P2SH");
                    newCash.setLockScript(BytesTools.bytesToHexStringBE(bScript));
                    byte[] hash160Bytes1 = Arrays.copyOfRange(bScript, 2, 22);
                    newCash.setFid(KeyTools.hash160ToMultiAddr(hash160Bytes1));
                }
                default -> {
                    newCash.setType("Unknown");
                    newCash.setLockScript(BytesTools.bytesToHexStringBE(bScript));
                    newCash.setFid("Unknown");
                }
            }

            // Add block and tx information to output./给输出添加区块和交易信息。
            // Add information where it from/添加来源信息
            newCash.setValid(true);

            // Add this output to List.
            // 添加输出到列表。
            rawNewCashMap.put(cashId,newCash);
        }
        return rawNewCashMap;
    }
    public static ArrayList<Cash> makeInCashMap(ElasticsearchClient esClient, Map<String, Cash> rawInCashMap,Map<String, Cash> outCashMap) throws Exception {
        ArrayList<String> inIdList = new ArrayList<>(rawInCashMap.keySet());
        Map<String, Cash> esInCashMap = getInCashListFromEs(esClient, inIdList);
        return mergeInCash(esInCashMap, rawInCashMap,outCashMap);
    }
    private static Map<String, Cash> getInCashListFromEs(ElasticsearchClient esClient, List<String> inIdList) throws Exception {
        if(inIdList==null || inIdList.size()==0)return null;
        EsTools.MgetResult<Cash> result = EsTools.getMultiByIdList(esClient, IndicesFCH.CashIndex, inIdList, Cash.class);
        List<Cash> cashList = result.getResultList();
        List<String> missList = result.getMissList();
        Map<String,Cash> cashMap = new HashMap<>();
        for(Cash cash:cashList) cashMap.put(cash.getCashId(), cash);
        for(String cashId:missList){
            cashMap.put(cashId,null);
        }
        return cashMap;
    }
    private static ArrayList<Cash> mergeInCash(Map<String, Cash> esCashMap, Map<String, Cash> rawCashMap,Map<String, Cash> outCashMap) {
        ArrayList<Cash> inCashList = new ArrayList<>();
        for(String id:rawCashMap.keySet()){
            Cash cash;
            if(esCashMap.get(id)==null){
                cash = outCashMap.get(id);
                if(cash == null) {
                    System.out.println("Cash "+id+" missed. Check if FCH-Parser is running.");
                    continue;
                }
                cash.setUnlockScript(rawCashMap.get(id).getUnlockScript());
                cash.setSequence(rawCashMap.get(id).getSequence());
                cash.setSigHash(rawCashMap.get(id).getSigHash());
                inCashList.add(cash);
            }else {
                cash = esCashMap.get(id);
                if(cash == null) {
                    System.out.println("Cash "+id+" missed. Check if FCH-Parser is running.");
                    continue;
                }
                cash.setUnlockScript(rawCashMap.get(id).getUnlockScript());
                cash.setSequence(rawCashMap.get(id).getSequence());
                cash.setSigHash(rawCashMap.get(id).getSigHash());
                inCashList.add(cash);
            }
        }
        return inCashList;
    }

}
