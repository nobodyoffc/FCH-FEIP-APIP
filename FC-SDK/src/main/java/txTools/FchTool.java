package txTools;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.SchnorrSignature;
import org.bitcoinj.fch.FchMainNetwork;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.WNafL2RMultiplier;
import org.bouncycastle.util.encoders.Base64;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * @param fee
     * @return
     */
    public static String createTransactionSign(List<TxInput> inputs, List<TxOutput> outputs, String opReturn, String returnAddr, long fee) {

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

            ECKey eckey = ECKey.fromPrivate(input.getPriKey32());//added

            ecKeys.add(eckey);
            UTXO utxo = new UTXO(Sha256Hash.wrap(input.getTxId()), input.getIndex(), Coin.valueOf(input.getAmount()), 0, false, ScriptBuilder.createP2PKHOutputScript(eckey));
            TransactionOutPoint outPoint = new TransactionOutPoint(FchMainNetwork.MAINNETWORK, utxo.getIndex(), utxo.getHash());
            transaction.addSignedInput(outPoint, utxo.getScript(), eckey, Transaction.SigHash.ALL, true);
        }
        if ((totalOutput + fee) > totalMoney) {
            throw new RuntimeException("input is not enough");
        }

        if (returnAddr != null) {
            transaction.addOutput(Coin.valueOf(totalMoney - totalOutput - fee), Address.fromBase58(FchMainNetwork.MAINNETWORK, returnAddr));
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

    /**
     * 随机私钥
     *
     * @param secret
     * @return
     */
    public static IdInfo createRandomIdInfo(String secret) {

        return IdInfo.genRandomIdInfo();
    }

    /**
     * 公钥转地址
     *
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

    public static long calcMinFee(int inputsize, int outputsize, String openreturn, String opreturnAddr, long fee) {

        List<TxInput> txInputs = new ArrayList<>();
        for (int i = 0; i < inputsize; ++i) {

            TxInput input = new TxInput();
            input.setPriKey32(Base58.decode("KxhPaZzFT1S48C4mmZsBiAvxyAEE1E5zcnFKD93Zc69ENpchjxra"));
            input.setIndex(0);
            input.setTxId("4a6bef758ae46c4610e5970e75d87effb8630eb3c8d2401008b78fc73f86d41e");
            input.setAmount(20000000);
            txInputs.add(input);
        }
        List<TxOutput> txOutputs = new ArrayList<>();
        for (int i = 0; i < outputsize; ++i) {

            TxOutput output = new TxOutput();
            output.setAddress("FBmgfrbzRiJNTPnjgknRxqVU2CmKQFnKM4");
            output.setAmount(1);
            txOutputs.add(output);
        }
        String sig = createTransactionSign(txInputs, txOutputs, openreturn, opreturnAddr, 1000000);
        byte[] sigBytes = Utils.HEX.decode(sig);
        return sigBytes.length;
    }

    //**eceis


    public static byte[] generateKey(String pubkey) {

        IdInfo info = new IdInfo(Base58.decode("L1WkwqiJgkPoYdjrs7tcikRj5hjwFebiTUChvxwubuSohpAaDzjP"));
        BigInteger pk = info.getECKey().getPrivKey();
        ECKey pubECKey = ECKey.fromPublicOnly(Utils.HEX.decode(pubkey));
        WNafL2RMultiplier mu = new WNafL2RMultiplier();
        org.bouncycastle.math.ec.ECPoint newECPoint = mu.multiply(pubECKey.getPubKeyPoint(), pk);
        byte[] xCood = newECPoint.getXCoord().getEncoded();
        Digest digest = new SHA512Digest();
        digest.update(xCood, 0, xCood.length);
        byte[] r = new byte[digest.getDigestSize()];
        digest.doFinal(r, 0);
        System.out.println(Utils.HEX.encode(r));
        return r;
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


    public static String encodeBase64Str(String str) {
        try {
            byte[] strBytes = Base64.encode(str.getBytes("utf-8"));
            return new String(strBytes, "utf-8");
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public static String decodeBase64Str(String str) {
        try {
            byte[] strBytes = Base64.decode(str.getBytes("utf-8"));
            return new String(strBytes, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        byte[] r = generateKey("023e0098dbd6126b140a160073d3ab1f94bff109f144d211c4054759e2fe2e7f86");
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
}
