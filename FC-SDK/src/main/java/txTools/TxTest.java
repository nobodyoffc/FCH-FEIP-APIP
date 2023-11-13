package txTools;

import keyTools.KeyTools;
import org.bitcoinj.core.*;
import org.bitcoinj.fch.FchMainNetwork;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;


import static constants.Constants.FchToSatoshi;
import static txTools.FchTool.createTransactionSign;

public class TxTest {

    public static void main(String[] args) {

        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK);
        int inputIndex =0;
        String priKey = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";

        String priKey32 = KeyTools.getPriKey32(priKey);
        System.out.println("priKey32:"+priKey32);
        byte[]priKeyBytes = HexFormat.of().parseHex(priKey32);
        ECKey ecKey = ECKey.fromPrivate(priKeyBytes);
        System.out.println("PubKey:"+ecKey.getPublicKeyAsHex());
        System.out.println("Address:"+ecKey.toAddress(FchMainNetwork.MAINNETWORK));

        Address address = Address.fromKey(FchMainNetwork.MAINNETWORK,ecKey);
        String addr = address.toBase58();
        System.out.println("addr to:"+addr);
        Script script = ScriptBuilder.createOutputScript(address);


        Transaction.SigHash sigHash = Transaction.SigHash.ALL;
        boolean anyoneCanPay = false;
        Coin value= Coin.valueOf(100000000);

        List<TxInput> inputs = new ArrayList<>();
        TxInput txInput = new TxInput();
        txInput.setAmount(2*FchToSatoshi);
        txInput.setIndex(0);
        txInput.setPriKey32(priKeyBytes);
        txInput.setTxId("6a8ee1015faedaf31d2742c204ad34120426e656dcffbcaca74b919ce81f8e44");
        inputs.add(txInput);

        List<TxOutput> outputs = new ArrayList<>();
        TxOutput txOutput = new TxOutput();
        txOutput.setAddress(addr);
        txOutput.setAmount((long)(0.9*FchToSatoshi));
        outputs.add(txOutput);


        String opreturn = "text";
        long fee=1000;
        String signed = createTransactionSign(inputs, outputs, opreturn, addr, fee);
        System.out.println(signed);

//        SchnorrSignature sign = transaction.calculateSchnorrSignature(inputIndex, ecKey, script.getProgram(), value, sigHash, anyoneCanPay);
//        System.out.println(HexFormat.of().formatHex(sign.getSignature()));
//        transaction.calculateSignature(inputIndex,ecKey,redeemScript,sigHash,anyoneCanPay);



    }

    private static void testVarInt() {
        System.out.println("0:"+VarInt.sizeOf(0));
        System.out.println("1:"+VarInt.sizeOf(1));
        System.out.println("253:"+VarInt.sizeOf(253));
        System.out.println("65536:"+VarInt.sizeOf(65536));
        System.out.println("4294967296:"+VarInt.sizeOf(4294967296L));


        long inputNum=1;
        long outputNum=1;
        long opLen=4;
        long length = 10 + (long) 141 * inputNum + (long) 34 * (outputNum + 1) + (opLen + VarInt.sizeOf(opLen) + 1 + VarInt.sizeOf(opLen + VarInt.sizeOf(opLen) + 1) + 8);
        System.out.println("4:"+length);
    }
}
