package txTools;

import apipClass.ApipParamsForClient;
import apipClient.ApipClient;
import apipClient.WalletAPIs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.Constants;
import cryptoTools.Hash;
import fcTools.ParseTools;
import fchClass.Cash;
import fipaClass.Signature;
import javaTools.BytesTools;
import keyTools.KeyTools;
import menu.Inputer;
import org.bitcoinj.core.*;
import org.bitcoinj.fch.FchMainNetwork;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import walletTools.SendTo;
import walletTools.WalletTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;


import static constants.Constants.FchToSatoshi;
import static txTools.FchTool.createTransactionSign;

public class TxTest {

    public static void main(String[] args) throws IOException {
        Transaction transaction = new Transaction(FchMainNetwork.MAINNETWORK);
        String priKey = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        String priKey32 = KeyTools.getPriKey32(priKey);
        if(priKey32==null)return;
        byte[]priKeyBytes = HexFormat.of().parseHex(priKey32);
        ECKey ecKey = ECKey.fromPrivate(priKeyBytes);
        String pubkey = ecKey.getPublicKeyAsHex();
        String fid = KeyTools.pubKeyToFchAddr(pubkey);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Confirm or set your password...");
        byte[] passwordBytes = Inputer.getPasswordBytes(br);
        byte[] symKey = Hash.Sha256x2(passwordBytes);
        byte[] sessionKey = new byte[0];
        try {
            ApipParamsForClient initApipParamsForClient = ApipParamsForClient.checkApipParams(br, passwordBytes.clone());
            if(initApipParamsForClient ==null)return;
            sessionKey = initApipParamsForClient.decryptSessionKey(Hash.Sha256x2(passwordBytes));
            if(sessionKey ==null)return;
            BytesTools.clearByteArray(passwordBytes);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Wrong password, try again.");
        }

        WalletAPIs walletAPIs = new WalletAPIs();
        String urlHead = Constants.UrlHead_CID_CASH;

        List<SendTo>sendToList = new ArrayList<>();
        SendTo sendTo = new SendTo();
        sendTo.setFid(fid);
        sendTo.setAmount(0.1);
        sendToList.add(sendTo);

        String msg = "hi";

        long fee = FchTool.calcFee(0, sendToList.size(), msg.length());

        ApipClient apipClient = walletAPIs.cashValidForPayPost(urlHead, fid, 0.1+((double) fee /FchToSatoshi), null, sessionKey);

        Object responseData = apipClient.getResponseBody().getData();
        Type t = new TypeToken<ArrayList<Cash>>() {}.getType();
        Gson gson = new Gson();
        List<Cash> cashList = new Gson().fromJson(gson.toJson(responseData), t);

        String txSigned = WalletTools.schnorrTxSign(cashList, sendToList, msg, priKeyBytes);
        System.out.println(txSigned);
    }

    private static void schnorrMsgTest() throws IOException {
        String priKey = "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        String priKey32 = KeyTools.getPriKey32(priKey);
        if(priKey32==null)return;
        byte[]priKeyBytes = HexFormat.of().parseHex(priKey32);
        ECKey ecKey = ECKey.fromPrivate(priKeyBytes);
        String pubkey = ecKey.getPublicKeyAsHex();
        String fid = KeyTools.pubKeyToFchAddr(pubkey);
        String msg = "hello";
        System.out.println(msg);
        String sign = WalletTools.schnorrMsgSign(msg,priKeyBytes);
        System.out.println("sign:"+sign);

        boolean verify = WalletTools.schnorrMsgVerify(msg,sign,fid);
        System.out.println("verify '"+msg+"':"+verify);
        verify = WalletTools.schnorrMsgVerify(msg+" ",sign,fid);
        System.out.println("verify '"+msg+" "+"':"+verify);
        Signature signature = new Signature(fid,msg,sign, Constants.Schnorr_No1_NrC7);
        System.out.println(ParseTools.gsonString(signature));
    }

    public static void schnorrTxTest() {
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
