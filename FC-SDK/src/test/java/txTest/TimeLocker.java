package txTest;

import fc.Hex;
import fcTools.ParseTools;
import fchClass.Cash;
import keyTools.KeyTools;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.SchnorrSignature;
import org.bitcoinj.fch.FchMainNetwork;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.Test;
import txTools.FchTool;
import txTools.RawTxParser;
import walletTools.SendTo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TimeLocker {

@Test
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
            builder.number(lockUntil).op(ScriptOpCodes.OP_CHECKLOCKTIMEVERIFY).op(ScriptOpCodes.OP_DROP);
            Script p2pkhScript = ScriptBuilder.createP2PKHOutputScript(pubKeyHash);
            builder.data(p2pkhScript.getProgram());
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

        transaction.addOutput(Coin.valueOf(totalMoney - totalOutput - fee), Address.fromBase58(FchMainNetwork.MAINNETWORK, changeToFid));


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
}
