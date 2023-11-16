package walletTools;

import apipClass.ApipParamsForClient;
import apipClient.ApipClient;
import apipClient.ApipDataGetter;
import apipClient.WalletAPIs;
import constants.Constants;
import cryptoTools.Hash;
import fcTools.ParseTools;
import fchClass.Cash;
import fipaClass.Signature;
import javaTools.BytesTools;
import keyTools.KeyTools;
import menu.Inputer;
import menu.Menu;
import org.bitcoinj.core.ECKey;
import txTools.FchTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SignatureException;
import java.util.*;

import static apipClient.StartApipClient.setting;
import static constants.Constants.FchToSatoshi;
import static walletTools.WalletTools.schnorrTxSign;

public class startWallet {

    static ApipParamsForClient initApipParamsForClient;
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        byte[] sessionKey;
        byte[] symKey;
        while (true) {
            Menu.printUnderline(20);
            System.out.println("\nWelcome to the Freecash Wallet Tools with APIP Client.");
            Menu.printUnderline(20);
            System.out.println("Confirm or set your password...");
            byte[] passwordBytes = Inputer.getPasswordBytes(br);
            symKey = Hash.Sha256x2(passwordBytes);
            try {
                initApipParamsForClient = ApipParamsForClient.checkApipParams(br, passwordBytes.clone());
                if (initApipParamsForClient == null) return;
                sessionKey = initApipParamsForClient.decryptSessionKey(Hash.Sha256x2(passwordBytes));
                if (sessionKey == null) continue;
                BytesTools.clearByteArray(passwordBytes);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Wrong password, try again.");
            }
        }

        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();
        menuItemList.add("SendTxSchnorr");
        menuItemList.add("SignMsgEcdsa");
        menuItemList.add("VerifyMsgEcdsa");
        menuItemList.add("SignMsgSchnorr");
        menuItemList.add("VerifyMsgSchnorr");
        menuItemList.add("PriKeySwap");
        menuItemList.add("PubKeySwap");
        menuItemList.add("AddressSwap");
        menuItemList.add("Settings");

        menu.add(menuItemList);

        while (true) {
            System.out.println(" << APIP Client>>");
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> sendTxSchnorr(symKey, br);
                case 2 -> signMsgEcdsa(symKey,br);
                case 3 -> verifyMsgEcdsa(br);
                case 4 -> signMsgSchnorr(symKey, br);
                case 5 -> verifyMsgSchnorr(br);
                case 6 -> priKeySwap(symKey, br);
                case 7 -> pubKeySwap(br);
                case 8 -> addressSwap(br);
                case 12 -> setting(sessionKey, symKey, br);
                case 0 -> {
                    BytesTools.clearByteArray(sessionKey);
                    return;
                }
            }
        }
    }

    private static void pubKeySwap(BufferedReader br) throws Exception {
        System.out.println("Input the public key:");
        String input = Inputer.inputString(br);
        String pubKey33 = KeyTools.getPubKey33(input);
        Menu.printUnderline(10);
        System.out.println("* PubKey 33 bytes compressed hex:\n"+pubKey33);
        System.out.println("* PubKey 65 bytes uncompressed hex:\n"+KeyTools.recoverPK33ToPK65(pubKey33));
        System.out.println("* PubKey WIF uncompressed:\n"+KeyTools.getPubKeyWifUncompressed(pubKey33));
        System.out.println("* PubKey WIF compressed with ver 0:\n"+ KeyTools.getPubKeyWifCompressedWithVer0(pubKey33));
        System.out.println("* PubKey WIF compressed without ver:\n"+KeyTools.getPubKeyWifCompressedWithoutVer(pubKey33));
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }

    private static void addressSwap(BufferedReader br) throws Exception {
        System.out.println("Input the address or public key:");
        String input = Inputer.inputString(br);

        Map<String, String> addrMap = new HashMap<>();
        String pubKey;

        if (input.startsWith("F") || input.startsWith("1") || input.startsWith("D") || input.startsWith("L")) {
            byte[] hash160 = KeyTools.addrToHash160(input);
            addrMap = KeyTools.hash160ToAddresses(hash160);
        } else {
            pubKey = KeyTools.getPubKey33(input);
            addrMap = KeyTools.pubKeyToAddresses(pubKey);
        }
        Menu.printUnderline(10);
        System.out.println(ParseTools.gsonString(addrMap));
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }
    private static void priKeySwap(byte[] symKey, BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = ApipParamsForClient.inputCipherGetPriKey(br);
            if(priKey==null)return;
        }else priKey = initApipParamsForClient.decryptApipBuyerPriKey(initApipParamsForClient.getApipBuyerPriKeyCipher(),symKey);

        String sender = KeyTools.priKeyToFid(priKey);
        Menu.printUnderline(10);
        System.out.println("The sender is :"+ sender);

        System.out.println("* PriKey 32 bytes:");
        String priKey32 = HexFormat.of().formatHex(priKey);
        System.out.println(priKey32);
        System.out.println("* PriKey WIF:");
        System.out.println(KeyTools.priKey32To37(priKey32));
        System.out.println("* PriKey WIF compressed:");
        System.out.println(KeyTools.priKey32To38(priKey32));
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }

    private static void sendTxSchnorr(byte[] symKey, BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = ApipParamsForClient.inputCipherGetPriKey(br);
            if(priKey==null)return;
        }else priKey = initApipParamsForClient.decryptApipBuyerPriKey(initApipParamsForClient.getApipBuyerPriKeyCipher(),symKey);

        String sender = KeyTools.priKeyToFid(priKey);
        System.out.println("The sender is :"+ sender);

        List<SendTo> sendToList = SendTo.inputSendToList(br);
        double sum = 0;
        for(SendTo sendTo : sendToList) sum += sendTo.getAmount();

        WalletAPIs walletAPIs = new WalletAPIs();
        String urlHead = Constants.UrlHead_CID_CASH;

        System.out.println("Input the opreturn message. Enter to ignore:");
        String msg = Inputer.inputString(br);

        long fee = FchTool.calcFee(0, sendToList.size(), msg.length());

        byte[] sessionKey = initApipParamsForClient.decryptSessionKey(symKey);
        ApipClient apipClient = walletAPIs.cashValidForPayPost(urlHead, sender, sum+((double) fee /FchToSatoshi), null, sessionKey);

        List<Cash> cashList = ApipDataGetter.<Cash>getCashList(apipClient);

        String txSigned = schnorrTxSign(cashList, sendToList, msg, priKey);
        System.out.println("Signed Tx:");
        System.out.println(txSigned);
    }

    private static void signMsgEcdsa(byte[] symKey,BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = ApipParamsForClient.inputCipherGetPriKey(br);
            if(priKey==null)return;
        }else priKey = initApipParamsForClient.decryptApipBuyerPriKey(initApipParamsForClient.getApipBuyerPriKeyCipher(),symKey);

        String signer = KeyTools.priKeyToFid(priKey);
        System.out.println("The signer is :"+ signer);

        ECKey ecKey = ECKey.fromPrivate(priKey);

        System.out.println("Input the message to be sign. Enter to exit:");
        String msg = Inputer.inputString(br);
        if("".equals(msg))return;

        String sign = ecKey.signMessage(msg);
        Signature signature = new Signature(signer,msg,sign,Constants.EcdsaBtcMsg_No1_NrC7);
        System.out.println(signature.toJsonAsyShort());
    }
    private static void verifyMsgEcdsa(BufferedReader br) {
        System.out.println("Input the signature:");
        String input = Inputer.inputStringMultiLine(br);
        if(input==null)return;

        Signature signature = Signature.parseSignature(input);
        if(signature==null){
            System.out.println("Parse signature wrong.");
            return;
        }

        String signPubKey = null;
        try {
            signPubKey = ECKey.signedMessageToKey(signature.getMsg(), signature.getSign()).getPublicKeyAsHex();
            System.out.println(signature.getFid().equals(keyTools.KeyTools.pubKeyToFchAddr(signPubKey)));
        } catch (SignatureException e) {
            System.out.println("Check signature wrong."+e.getMessage());
        }
    }

    private static void signMsgSchnorr(byte[] symKey, BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = ApipParamsForClient.inputCipherGetPriKey(br);
            if(priKey==null)return;
        }else priKey = initApipParamsForClient.decryptApipBuyerPriKey(initApipParamsForClient.getApipBuyerPriKeyCipher(),symKey);

        String signer = KeyTools.priKeyToFid(priKey);
        System.out.println("The signer is :"+ signer);

        ECKey ecKey = ECKey.fromPrivate(priKey);

        System.out.println("Input the message to be sign. Enter to exit:");
        String msg = Inputer.inputString(br);
        if("".equals(msg))return;

        String sign = WalletTools.schnorrMsgSign(msg,priKey);
        Signature signature = new Signature(signer,msg,sign,Constants.EcdsaBtcMsg_No1_NrC7);
        System.out.println(signature.toJsonAsyShort());
    }

    private static void verifyMsgSchnorr( BufferedReader br) {
        System.out.println("Input the signature:");
        String input = Inputer.inputStringMultiLine(br);
        if (input == null) return;

        Signature signature = Signature.parseSignature(input);
        if (signature == null) {
            System.out.println("Parse signature wrong.");
            return;
        }

        try {
            System.out.println(WalletTools.schnorrMsgVerify(signature.getMsg(), signature.getSign(), signature.getFid()));
        } catch (IOException e) {
            System.out.println("Check signature wrong." + e.getMessage());
        }
    }
}
