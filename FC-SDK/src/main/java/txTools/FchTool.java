package txTools;

import com.google.common.base.Preconditions;
import constants.Constants;
import fcTools.ParseTools;
import fchClass.Cash;
import fchClass.P2SH;
import javaTools.BytesTools;
import keyTools.KeyTools;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.SchnorrSignature;
import org.bitcoinj.fch.FchMainNetwork;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import walletTools.MultiSigData;
import walletTools.SendTo;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.*;

import static org.bitcoinj.script.ScriptBuilder.createMultiSigInputScriptBytes;

/**
 * 工具类
 */
public class FchTool {

    static {
        fixKeyLength();
        Security.addProvider(new BouncyCastleProvider());

    }

    public static void fixKeyLength() {
        String errorString = "Failed manually overriding key-length permissions.";
        int newMaxKeyLength;
        try {
            if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
                Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
                Constructor con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissionCollection = con.newInstance();
                Field f = c.getDeclaredField("all_allowed");
                f.setAccessible(true);
                f.setBoolean(allPermissionCollection, true);

                c = Class.forName("javax.crypto.CryptoPermissions");
                con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissions = con.newInstance();
                f = c.getDeclaredField("perms");
                f.setAccessible(true);
                ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

                c = Class.forName("javax.crypto.JceSecurityManager");
                f = c.getDeclaredField("defaultPolicy");
                f.setAccessible(true);
                Field mf = Field.class.getDeclaredField("modifiers");
                mf.setAccessible(true);
                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                f.set(null, allPermissions);

                newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
            }
        } catch (Exception e) {
            throw new RuntimeException(errorString, e);
        }
        if (newMaxKeyLength < 256)
            throw new RuntimeException(errorString); // hack failed
    }

    /**
     * 创建签名
     *
     * @param inputs
     * @param outputs
     * @param opReturn
     * @param returnAddr
     * @return
     */
    public static String createTransactionSign(List<TxInput> inputs, List<TxOutput> outputs, String opReturn, String returnAddr) {

        long fee = FchTool.calcFee(inputs.size(), outputs.size()+1, opReturn.getBytes().length);

        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK);

        long totalMoney = 0;
        long totalOutput = 0;

        List<ECKey> ecKeys = new ArrayList<>();
        for (TxOutput output : outputs) {
            totalOutput += output.getAmount();
            transaction.addOutput(Coin.valueOf(output.getAmount()), Address.fromBase58(FchMainNetwork.MAINNETWORK, output.getAddress()));
        }

        if (opReturn != null && !"".equals(opReturn)) {
            try {
                Script opreturnScript = ScriptBuilder.createOpReturnScript(opReturn.getBytes(StandardCharsets.UTF_8));
                transaction.addOutput(Coin.ZERO, opreturnScript);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (TxInput input : inputs) {
            totalMoney += input.getAmount();

            ECKey eckey = ECKey.fromPrivate(input.getPriKey32());

            ecKeys.add(eckey);
            UTXO utxo = new UTXO(Sha256Hash.wrap(input.getTxId()), input.getIndex(), Coin.valueOf(input.getAmount()), 0, false, ScriptBuilder.createP2PKHOutputScript(eckey));
            TransactionOutPoint outPoint = new TransactionOutPoint(FchMainNetwork.MAINNETWORK, utxo.getIndex(), utxo.getHash());
            TransactionInput unsignedInput = new TransactionInput(new fcTools.FchMainNetwork(), transaction, new byte[0], outPoint);
            transaction.addInput(unsignedInput);
        }
        if ((totalOutput + fee) > totalMoney) {
            throw new RuntimeException("input is not enough");
        }
        long change = totalMoney - totalOutput - fee;
        if (returnAddr != null && change > Constants.DustInSatoshi) {
            transaction.addOutput(Coin.valueOf(change), Address.fromBase58(FchMainNetwork.MAINNETWORK, returnAddr));
        }


        for (int i = 0; i < inputs.size(); ++i) {
            TxInput input = inputs.get(i);
            ECKey eckey = ecKeys.get(i);
            Script script = ScriptBuilder.createP2PKHOutputScript(eckey);
            SchnorrSignature signature = transaction.calculateSchnorrSignature(i, eckey, script.getProgram(), Coin.valueOf(input.getAmount()), Transaction.SigHash.ALL, false);
            Script schnorr = ScriptBuilder.createSchnorrInputScript(signature, eckey);
            transaction.getInput(i).setScriptSig(schnorr);
        }

        byte[] signResult = transaction.bitcoinSerialize();
        return Utils.HEX.encode(signResult);
    }

    public static String createTransactionSign(List<Cash> inputs, byte[] priKey, List<SendTo> outputs, String opReturn) {

        String changeToFid=inputs.get(0).getOwner();

        long fee;
        if(opReturn!=null){
            fee = FchTool.calcFee(inputs.size(), outputs.size(), opReturn.getBytes().length);
        }else fee = FchTool.calcFee(inputs.size(), outputs.size(), 0);

        Transaction transaction = new Transaction(fcTools.FchMainNetwork.MAINNETWORK);

        long totalMoney = 0;
        long totalOutput = 0;

        ECKey eckey = ECKey.fromPrivate(priKey);

        for (SendTo output : outputs) {
            long value = ParseTools.fchToSatoshi(output.getAmount());
            totalOutput += value;
            transaction.addOutput(Coin.valueOf(value), Address.fromBase58(org.bitcoinj.fch.FchMainNetwork.MAINNETWORK, output.getFid()));
        }

        if (opReturn != null && !"".equals(opReturn)) {
            try {
                Script opreturnScript = ScriptBuilder.createOpReturnScript(opReturn.getBytes(StandardCharsets.UTF_8));
                transaction.addOutput(Coin.ZERO, opreturnScript);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (Cash input : inputs) {
            totalMoney += input.getValue();
            TransactionOutPoint outPoint = new TransactionOutPoint(FchMainNetwork.MAINNETWORK, input.getBirthIndex(), Sha256Hash.wrap(input.getBirthTxId()));
            TransactionInput unsignedInput = new TransactionInput(new fcTools.FchMainNetwork(), transaction, new byte[0], outPoint);
            transaction.addInput(unsignedInput);
        }

        if ((totalOutput + fee) > totalMoney) {
            throw new RuntimeException("input is not enough");
        }
        long change = totalMoney - totalOutput - fee;
        if(change > Constants.DustInSatoshi) {
            transaction.addOutput(Coin.valueOf(change), Address.fromBase58(FchMainNetwork.MAINNETWORK, changeToFid));
        }

        for (int i = 0; i < inputs.size(); ++i) {
            Cash input = inputs.get(i);
            Script script = ScriptBuilder.createP2PKHOutputScript(eckey);
            SchnorrSignature signature = transaction.calculateSchnorrSignature(i, eckey, script.getProgram(), Coin.valueOf(input.getValue()), Transaction.SigHash.ALL, false);
            Script schnorr = ScriptBuilder.createSchnorrInputScript(signature, eckey);
            transaction.getInput(i).setScriptSig(schnorr);
        }

        byte[] signResult = transaction.bitcoinSerialize();
        return Utils.HEX.encode(signResult);
    }

    public static P2SH genMultiP2sh(List<byte[]> pubKeyList, int m)  {
        List<ECKey> keys = new ArrayList<>();
        for (byte[] bytes : pubKeyList) {
            ECKey ecKey = ECKey.fromPublicOnly(bytes);
            keys.add(ecKey);
        }

        Script multiSigScript = ScriptBuilder.createMultiSigOutputScript(m, keys);

        byte[] redeemScriptBytes = multiSigScript.getProgram();

        P2SH p2sh;
        try {
            p2sh = P2SH.parseP2shRedeemScript(HexFormat.of().formatHex(redeemScriptBytes));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return p2sh;
    }

    public static byte[] createMultiSignRawTx(List<Cash> inputs, List<SendTo> outputs, String opReturn, P2SH p2SH) {

        String changeToFid=inputs.get(0).getOwner();
        if(!changeToFid.startsWith("3"))
            throw new RuntimeException("It's not a multisig address.");;

        long fee;
        if(opReturn!=null){
            fee = FchTool.calcFeeMultiSign(inputs.size(), outputs.size(), opReturn.getBytes().length,p2SH.getM(),p2SH.getN());
        }else fee = FchTool.calcFeeMultiSign(inputs.size(), outputs.size(), 0,p2SH.getM(),p2SH.getN());

        Transaction transaction = new Transaction(fcTools.FchMainNetwork.MAINNETWORK);

        long totalMoney = 0;
        long totalOutput = 0;

        for (SendTo output : outputs) {
            long value = ParseTools.fchToSatoshi(output.getAmount());
            totalOutput += value;
            transaction.addOutput(Coin.valueOf(value), Address.fromBase58(org.bitcoinj.fch.FchMainNetwork.MAINNETWORK, output.getFid()));
        }

        if (opReturn != null && !"".equals(opReturn)) {
            try {
                Script opreturnScript = ScriptBuilder.createOpReturnScript(opReturn.getBytes(StandardCharsets.UTF_8));
                transaction.addOutput(Coin.ZERO, opreturnScript);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (Cash input : inputs) {
            totalMoney += input.getValue();
            TransactionOutPoint outPoint = new TransactionOutPoint(FchMainNetwork.MAINNETWORK, input.getBirthIndex(), Sha256Hash.wrap(input.getBirthTxId()));
            TransactionInput unsignedInput = new TransactionInput(new fcTools.FchMainNetwork(), transaction, new byte[0], outPoint);
            transaction.addInput(unsignedInput);
        }

        if ((totalOutput + fee) > totalMoney) {
            throw new RuntimeException("input is not enough");
        }
        long change = totalMoney - totalOutput - fee;
        if(change > Constants.DustInSatoshi) {
            transaction.addOutput(Coin.valueOf(change), Address.fromBase58(FchMainNetwork.MAINNETWORK, changeToFid));
        }

        return transaction.bitcoinSerialize();
    }

    public static String signSchnorrMultiSignTx(String multiSignDataJson, byte[] priKey) {
        MultiSigData multiSignData = MultiSigData.fromJson(multiSignDataJson);
        return signSchnorrMultiSignTx(multiSignData,priKey).toJson();
    }

        public static MultiSigData signSchnorrMultiSignTx(MultiSigData multiSignData, byte[] priKey){

        byte[]rawTx = multiSignData.getRawTx();
        byte[]redeemScript = HexFormat.of().parseHex(multiSignData.getP2SH().getRedeemScript());
        List<Cash> cashList = multiSignData.getCashList();

        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK,rawTx);
        List<TransactionInput> inputs = transaction.getInputs();

        ECKey ecKey = ECKey.fromPrivate(priKey);
        BigInteger priKeyBigInteger = ecKey.getPrivKey();
        List<byte[]> sigList = new ArrayList<>();
        for (int i = 0; i < inputs.size(); ++i) {
            Script script = new Script(redeemScript);
            Sha256Hash hash = transaction.hashForSignatureWitness(i,script, Coin.valueOf(cashList.get(i).getValue()), Transaction.SigHash.ALL, false);
            byte[] sig = SchnorrSignature.schnorr_sign(hash.getBytes(),priKeyBigInteger);
            sigList.add(sig);
        }

        String fid = KeyTools.priKeyToFid(priKey);
        if(multiSignData.getFidSigMap()==null) {
            Map<String, List<byte[]>> fidSigListMap = new HashMap<>();
            multiSignData.setFidSigMap(fidSigListMap);
        }
        multiSignData.getFidSigMap().put(fid,sigList);
        return multiSignData;
    }

    public static boolean rawTxSigVerify(byte[] rawTx,byte[] pubKey,byte[]sig,int inputIndex,long inputValue,byte[] redeemScript){
        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK,rawTx);
        Script script = new Script(redeemScript);
        Sha256Hash hash = transaction.hashForSignatureWitness(inputIndex,script, Coin.valueOf(inputValue), Transaction.SigHash.ALL, false);
        return SchnorrSignature.schnorr_verify(hash.getBytes(), pubKey,sig);
    }

    public static String buildSchnorrMultiSignTx(byte[] rawTx, Map<String,List<byte[]>> sigListMap, P2SH p2sh){

        if(sigListMap.size()>p2sh.getM())
            sigListMap= dropRedundantSigs(sigListMap,p2sh.getM());

        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK,rawTx);

        for(int i= 0;i<transaction.getInputs().size();i++) {
            List<byte[]> sigListByTx = new ArrayList<>();
            for (String fid :p2sh.getFids()) {
                try {
                    byte[] sig = sigListMap.get(fid).get(i);
                    sigListByTx.add(sig);
                }catch (Exception ignore){}
            }

            Script inputScript = createSchnorrMultiSigInputScriptBytes(sigListByTx,HexFormat.of().parseHex(p2sh.getRedeemScript())); // Include all required signatures

            System.out.println(HexFormat.of().formatHex(inputScript.getProgram()));
            TransactionInput input = transaction.getInput(i);
            input.setScriptSig(inputScript);
        }

        byte[] signResult = transaction.bitcoinSerialize();
        return Utils.HEX.encode(signResult);
    }

    private static Map<String, List<byte[]>> dropRedundantSigs(Map<String, List<byte[]>> sigListMap, int m) {
        Map<String, List<byte[]>> newMap = new HashMap<>();
        int i=0;
        for(String key : sigListMap.keySet()){
            newMap.put(key,sigListMap.get(key));
            i++;
            if(i==m)return newMap;
        }
        return newMap;
    }

    public static Script createSchnorrMultiSigInputScriptBytes(List<byte[]> signatures ,@Nullable byte[] multisigProgramBytes) {
        Preconditions.checkArgument(signatures.size() <= 16);
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(0);
        Iterator var3 = signatures.iterator();
        byte[] sigHashAll = new byte[]{0x41};

        while(var3.hasNext()) {
            byte[] signature = (byte[])var3.next();
            builder.data(BytesTools.bytesMerger(signature,sigHashAll));
        }

        if (multisigProgramBytes != null) {
            builder.data(multisigProgramBytes);
        }

        return builder.build();
    }

    public static String createTimeLockedTransaction(List<Cash> inputs, byte[] priKey, List<SendTo> outputs,long lockUntil, String opReturn) {

        String changeToFid=inputs.get(0).getOwner();

        long fee;
        if(opReturn!=null){
            fee = FchTool.calcFee(inputs.size(), outputs.size(), opReturn.getBytes().length);
        }else fee = FchTool.calcFee(inputs.size(), outputs.size(), 0);

        Transaction transaction = new Transaction(fcTools.FchMainNetwork.MAINNETWORK);
//        transaction.setLockTime(nLockTime);

        long totalMoney = 0;
        long totalOutput = 0;

        ECKey eckey = ECKey.fromPrivate(priKey);

        for (SendTo output : outputs) {
            long value = ParseTools.fchToSatoshi(output.getAmount());
            byte[] pubKeyHash = KeyTools.addrToHash160(output.getFid());
            totalOutput += value;

            ScriptBuilder builder = new ScriptBuilder();

            builder.number(lockUntil)
                    .op(ScriptOpCodes.OP_CHECKLOCKTIMEVERIFY)
                    .op(ScriptOpCodes.OP_DROP);

            builder.op(ScriptOpCodes.OP_DUP)
                    .op(ScriptOpCodes.OP_HASH160)
                    .data(pubKeyHash)
                    .op(ScriptOpCodes.OP_EQUALVERIFY)
                    .op(ScriptOpCodes.OP_CHECKSIG);

            Script cltvScript = builder.build();

            transaction.addOutput(Coin.valueOf(value), cltvScript);
        }

        if (opReturn != null && !"".equals(opReturn)) {
            try {
                Script opreturnScript = ScriptBuilder.createOpReturnScript(opReturn.getBytes(StandardCharsets.UTF_8));
                transaction.addOutput(Coin.ZERO, opreturnScript);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (Cash input : inputs) {
            totalMoney += input.getValue();

            TransactionOutPoint outPoint = new TransactionOutPoint(FchMainNetwork.MAINNETWORK, input.getBirthIndex(), Sha256Hash.wrap(input.getBirthTxId()));
            TransactionInput unsignedInput = new TransactionInput(new fcTools.FchMainNetwork(), transaction, new byte[0], outPoint);
            transaction.addInput(unsignedInput);
        }

        if ((totalOutput + fee) > totalMoney) {
            throw new RuntimeException("input is not enough");
        }

        long change = totalMoney - totalOutput - fee;
        if(change > Constants.DustInSatoshi) {
            transaction.addOutput(Coin.valueOf(change), Address.fromBase58(FchMainNetwork.MAINNETWORK, changeToFid));
        }

        for (int i = 0; i < inputs.size(); ++i) {
            Cash input = inputs.get(i);
            Script script = ScriptBuilder.createP2PKHOutputScript(eckey);
            SchnorrSignature signature = transaction.calculateSchnorrSignature(i, eckey, script.getProgram(), Coin.valueOf(input.getValue()), Transaction.SigHash.ALL, false);

            Script schnorr = ScriptBuilder.createSchnorrInputScript(signature, eckey);
            transaction.getInput(i).setScriptSig(schnorr);
        }

        byte[] signResult = transaction.bitcoinSerialize();
        return Utils.HEX.encode(signResult);
    }

    /**
     * 随机私钥
     * @param secret
     * @return
     */
    public static IdInfo createRandomIdInfo(String secret) {
        return IdInfo.genRandomIdInfo();
    }

    /**
     * 公钥转地址
     * @param pukey
     * @return
     */
    public static String pubkeyToAddr(String pukey) {

        ECKey eckey = ECKey.fromPublicOnly(Utils.HEX.decode(pukey));
        return eckey.toAddress(FchMainNetwork.MAINNETWORK).toString();

    }

    /**
     * 通过wif创建私钥
     *
     * @param wifKey
     * @return
     */
    public static IdInfo createIdInfoFromWIFPrivateKey(byte[] wifKey) {

        return new IdInfo(wifKey);
    }

    /**
     * 消息签名
     *
     * @param msg
     * @param wifkey
     * @return
     */
    public static String signMsg(String msg, byte[] wifkey) {
        IdInfo idInfo = new IdInfo(wifkey);
        return idInfo.signMsg(msg);
    }

    public static String signFullMsg(String msg, byte[] wifkey) {
        IdInfo idInfo = new IdInfo(wifkey);
        return idInfo.signFullMessage(msg);
    }

    public static String signFullMsgJson(String msg, byte[] wifkey) {
        IdInfo idInfo = new IdInfo(wifkey);
        return idInfo.signFullMessageJson(msg);
    }

    /**
     * 签名验证
     *
     * @param msg
     * @return
     */
    public static boolean verifyFullMsg(String msg) {
        String args[] = msg.split("----");
        try {
            ECKey key = ECKey.signedMessageToKey(args[0], args[2]);
            Address targetAddr = key.toAddress(FchMainNetwork.MAINNETWORK);
            return args[1].equals(targetAddr.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean verifyFullMsgJson(String msg) {
        FchProtocol.SignMsg signMsg = FchProtocol.parseSignMsg(msg);
        try {
            ECKey key = ECKey.signedMessageToKey(signMsg.getMessage(), signMsg.getSignature());
            Address targetAddr = key.toAddress(FchMainNetwork.MAINNETWORK);
            return signMsg.getAddress().equals(targetAddr.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public static String msgHash(String msg) {
        try {
            byte[] data = msg.getBytes("UTF-8");
            return Utils.HEX.encode(Sha256Hash.hash(data));
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public static String msgFileHash(String path) {
        try {
            File f = new File(path);
            return Utils.HEX.encode(Sha256Hash.of(f).getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] aesCBCEncrypt(byte[] srcData, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] encData = cipher.doFinal(srcData);
        return encData;

    }

    public static byte[] aesCBCDecrypt(byte[] encData, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] decbbdt = cipher.doFinal(encData);
        return decbbdt;
    }

    public static long calcFee(int inputNum, int outputNum, int opReturnBytesLen) {
        long priceInSatoshi =1;
        long length = 0 ;
        if(opReturnBytesLen==0) {
            length = 10+ 141 * (long)inputNum + (long) 34 *(outputNum+1);
        }else{
            length= 10+ (long)141*inputNum + (long) 34 *(outputNum+1)+ (opReturnBytesLen+VarInt.sizeOf(opReturnBytesLen)+1+VarInt.sizeOf(opReturnBytesLen+VarInt.sizeOf(opReturnBytesLen)+1)+8);
        }
        return priceInSatoshi*length;
    }

    public static long calcFeeMultiSign(int inputNum, int outputNum, int opReturnBytesLen,int m, int n) {
        long priceInSatoshi =1;

        /*多签单个Input长度：
            基础字节40（preTxId 32，preIndex 4，sequence 4），
            可变脚本长度：？
            脚本：
                op_0    1
                签名：m * (1+64+1)     // length + pubKeyLength + sigHash ALL
                可变redeemScript 长度：？
                redeem script：
                    op_m    1
                    pubKeys    n * 33
                    op_n    1
                    OP_CHECKMULTISIG    1
         */

        long redeemScriptLength = 1+(n* 33L)+1+1;
        long redeemScriptVarInt = VarInt.sizeOf(redeemScriptLength);
        long scriptLength = 1+ (m* 66L) + redeemScriptVarInt + redeemScriptLength;
        long scriptVarInt = VarInt.sizeOf(scriptLength);
        long inputLength = 40+scriptVarInt+scriptLength;

        long length;
        if(opReturnBytesLen==0) {
            length = 10+ inputLength * inputNum + (long) 34 *(outputNum+1);
        }else{
            length= 10+ inputLength * inputNum + (long) 34 *(outputNum+1)+ (opReturnBytesLen+VarInt.sizeOf(opReturnBytesLen)+1+VarInt.sizeOf(opReturnBytesLen+VarInt.sizeOf(opReturnBytesLen)+1)+8);
        }
        return priceInSatoshi*length;
    }
}
