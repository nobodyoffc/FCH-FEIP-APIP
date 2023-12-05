package txTools;

import apipClass.ApipParamsForClient;
import apipClient.ApipClient;
import apipClient.ApipDataGetter;
import apipClient.WalletAPIs;
import com.google.common.base.Preconditions;
import fcTools.FchMainNetwork;
import fcTools.ParseTools;
import fchClass.Cash;
import javaTools.JsonTools;
import keyTools.KeyTools;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.SchnorrSignature;
import org.bitcoinj.script.*;
import walletTools.SendTo;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

import static constants.Constants.FchToSatoshi;
import static txTools.FchTool.createTransactionSign;

public class TxBuilder {

    public static Transaction buildLockedTx(){
        Transaction transaction = new Transaction(new FchMainNetwork());

        ScriptBuilder scriptBuilder = new ScriptBuilder();
        byte[] hash = KeyTools.addrToHash160("FKi3bRKUPUbUfQuzxT9CfbYwT7m4KEu13R");
        Script script = scriptBuilder.op(169).data(hash).op(135).build();
        return transaction;
    }

    public static String createTransactionSign(List<Cash> inputs,byte[] priKey, List<SendTo> outputs, String opReturn, String returnAddr, long fee) {

        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK);

        long totalMoney = 0;
        long totalOutput = 0;

//        List<ECKey> ecKeys = new ArrayList<>();
        ECKey eckey = ECKey.fromPrivate(priKey);

        for (SendTo output : outputs) {
            totalOutput += output.getAmount();
            transaction.addOutput(Coin.valueOf(ParseTools.fchToSatoshi(output.getAmount())), Address.fromBase58(org.bitcoinj.fch.FchMainNetwork.MAINNETWORK, output.getFid()));
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

            Script lockScript = new Script(HexFormat.of().parseHex(input.getLockScript()));

            UTXO utxo = new UTXO(Sha256Hash.wrap(input.getBirthTxId()),
                    input.getBirthIndex(), Coin.valueOf(input.getValue()),
                    0,
                    false,
                    lockScript);

            TransactionOutPoint outPoint = new TransactionOutPoint(org.bitcoinj.fch.FchMainNetwork.MAINNETWORK, utxo.getIndex(), utxo.getHash());
            TransactionInput unsignedInput = new TransactionInput(new FchMainNetwork(), transaction, new byte[0], outPoint);
            transaction.addInput(unsignedInput);
        }

        if ((totalOutput + fee) > totalMoney) {
            throw new RuntimeException("input is not enough");
        }

        if (returnAddr != null) {
            transaction.addOutput(Coin.valueOf(totalMoney - totalOutput - fee), Address.fromBase58(org.bitcoinj.fch.FchMainNetwork.MAINNETWORK, returnAddr));
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

    public static Script createP2PKHOutputScript(byte[] hash) {
        Preconditions.checkArgument(hash.length == 20);
        ScriptBuilder builder = new ScriptBuilder();
        builder.op(118);
        builder.op(169);
        builder.data(hash);
        builder.op(136);
        builder.op(172);
        return builder.build();
    }

    public static String sendTxForMsgByAPIP(ApipParamsForClient apipParams, byte[] symKey, byte[] priKey, List<SendTo> sendToList, String msg) {

        byte[] sessionKey = apipParams.decryptSessionKey(symKey.clone());

        String sender = KeyTools.priKeyToFid(priKey);

        double sum = 0;
        int sendToSize = 0;
        if(sendToList!=null && !sendToList.isEmpty()) {
            sendToSize = sendToList.size();
            for (SendTo sendTo : sendToList) sum += sendTo.getAmount();
        }

        long fee = FchTool.calcFee(0, sendToSize, msg.length());

        String urlHead=apipParams.getUrlHead();
        System.out.println("Getting cashes from "+urlHead+" ...");
        ApipClient apipClient = WalletAPIs.cashValidForPayPost(urlHead, sender, sum+((double) fee /FchToSatoshi), apipParams.getVia(), sessionKey);
        if(apipClient.checkResponse()!=0){
            System.out.println("Failed to get cashes."+apipClient.getMessage()+apipClient.getResponseBody().getData());
            JsonTools.gsonPrint(apipClient);
            return apipClient.getMessage();
        }

        List<Cash> cashList = ApipDataGetter.getCashList(apipClient.getResponseBody().getData());

        String txSigned = FchTool.createTransactionSign(cashList, priKey, sendToList, msg);

        System.out.println("Broadcast with "+urlHead+" ...");
        apipClient = WalletAPIs.broadcastTxPost(urlHead,txSigned, apipParams.getVia(), sessionKey);
        if(apipClient.checkResponse()!=0){
            System.out.println(apipClient.getCode()+": "+apipClient.getMessage());
            if(apipClient.getResponseBody().getData()!=null) System.out.println(apipClient.getResponseBody().getData());
            return apipClient.getMessage();
        }

        return (String)apipClient.getResponseBody().getData();
    }
}