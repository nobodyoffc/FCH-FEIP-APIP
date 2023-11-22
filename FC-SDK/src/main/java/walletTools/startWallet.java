package walletTools;

import apipClass.ApipParamsForClient;
import apipClass.CidInfo;
import apipClient.*;
import constants.Constants;
import constants.Strings;
import cryptoTools.Hash;
import eccAes256K1P7.EccAes256K1P7;
import fchClass.Address;
import fchClass.Cash;
import fchClass.P2SH;
import fipaClass.Signature;
import javaTools.BytesTools;
import javaTools.JsonTools;
import keyTools.KeyTools;
import menu.Inputer;
import menu.Menu;
import org.bitcoinj.core.ECKey;
import txTools.FchTool;
import txTools.RawTxParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SignatureException;
import java.util.*;

import static apipClient.StartApipClient.setting;
import static constants.Constants.FchToSatoshi;
import static constants.Strings.newCashMapKey;
import static constants.Strings.spendCashMapKey;
import static txTools.FchTool.createTransactionSign;


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
        menuItemList.add("MultiSign");
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
                case 9 -> multiSign(sessionKey,symKey,br);
                case 12 -> setting(sessionKey, symKey, br);
                case 0 -> {
                    BytesTools.clearByteArray(sessionKey);
                    return;
                }
            }
        }
    }

    private static void multiSign(byte[] sessionKey,byte[] symKey,  BufferedReader br) {
        Menu menu = new Menu();
        menu.add("Create multisig FID")
                .add("Show multisig FID")
                .add("Creat a multisig raw TX")
                .add("Sign a multisig raw TX")
                .add("Build the final multisig TX")
        ;
        while (true) {
            System.out.println(" << Multi Signature>>");

            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> createFid(sessionKey,br);
                case 2 -> showFid(sessionKey,br);
                case 3 -> createTx(sessionKey,br);
                case 4 -> signTx(symKey, br);
                case 5 -> buildSignedTx(br);
                case 0 -> {
                    BytesTools.clearByteArray(sessionKey);
                    return;
                }
            }
        }
    }

    private static void buildSignedTx(BufferedReader br) {
        String[] signedData = Inputer.inputStringArray(br,"Input the signed data. Enter to end:",0);

        Map<String,List<byte[]>> fidSigListMap= new HashMap<>();
        byte[] rawTx=null;
        P2SH p2sh=null;

        for(String dataJson:signedData){
            try {
                System.out.println(dataJson);

                MultiSigData multiSignData = MultiSigData.fromJson(dataJson);

                if(p2sh == null
                        && multiSignData.getP2SH()!=null){
                    p2sh = multiSignData.getP2SH();
                }

                if(rawTx == null
                        && multiSignData.getRawTx()!=null
                        && multiSignData.getRawTx().length>0){
                    rawTx = multiSignData.getRawTx();
                }
                //TODO
                JsonTools.gsonPrint(multiSignData.getFidSigMap());

                fidSigListMap.putAll(multiSignData.getFidSigMap());

            }catch (Exception ignored){}
        }
        if(rawTx==null||p2sh==null)return;

        JsonTools.gsonPrint(HexFormat.of().formatHex(rawTx));
        JsonTools.gsonPrint(fidSigListMap);
        JsonTools.gsonPrint(p2sh);

        String signedTx = FchTool.buildSchnorrMultiSignTx(rawTx, fidSigListMap, p2sh);
        System.out.println(signedTx);
        Menu.anyKeyToContinue(br);
    }

    private static void signTx(byte[] symKey, BufferedReader br) {

        byte[] priKey;
        while(true) {
            System.out.println("Sign with Apip Buyer? y/n:");
            String input = Inputer.inputString(br);
            if ("y".equals(input))
                priKey = EccAes256K1P7.decryptKeyWithSymKey(initApipParamsForClient.getApipBuyerPriKeyCipher(), symKey);
            else {
                try {
                    priKey = KeyTools.inputCipherGetPriKey(br);
                }catch (Exception e){
                    System.out.println("Wrong input. Try again.");
                    continue;
                }
            }

            if (priKey == null) {
                System.out.println("Get priKey wrong");
                return;
            }

            System.out.println("Input the unsigned data json string: ");
            String multiSignDataJson = Inputer.inputStringMultiLine(br);
            showRawTxInfo(multiSignDataJson,br);
            System.out.println("Multisig data signed by " + KeyTools.priKeyToFid(priKey)+":");
            Menu.printUnderline(60);
            System.out.println(FchTool.signSchnorrMultiSignTx(multiSignDataJson, priKey));
            BytesTools.clearByteArray(priKey);
            Menu.printUnderline(60);
            Menu.anyKeyToContinue(br);

            input = Inputer.inputString(br,"Sign with another priKey?y/n");
            if(!"y".endsWith(input)){
                BytesTools.clearByteArray(priKey);
                return;
            }
        }
    }

    private static void showRawTxInfo(String multiSignDataJson, BufferedReader br) {
        MultiSigData multiSigData = MultiSigData.fromJson(multiSignDataJson);

        byte[] rawTx = multiSigData.getRawTx();
        Map<String, Object> result;
        try {
            result = RawTxParser.parseRawTxBytes(rawTx);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        List<Cash> spendCashList = (List<Cash>) result.get(spendCashMapKey);
        List<Cash> issuredCashList = (List<Cash>) result.get(newCashMapKey);
        String msg = (String) result.get(Strings.OPRETURN);

        Map<String,Cash> spendCashMap= new HashMap<>();
        for(Cash cash:multiSigData.getCashList())spendCashMap.put(cash.getCashId(),cash);

        System.out.println("You are spending:");
        Menu.printUnderline(120);
        System.out.print(Menu.formatString("cashId",68));
        System.out.print(Menu.formatString("owner",38));
        System.out.println(Menu.formatString("fch",20));
        Menu.printUnderline(120);
        for(Cash cash:spendCashList){
            Cash niceCash = spendCashMap.get(cash.getCashId());
            if(niceCash==null) {
                System.out.println("Warning： The cash " + cash.getCashId() + "in the rawTx is unfounded.");
                return;
            }
            System.out.print(Menu.formatString(niceCash.getCashId(),68));
            System.out.print(Menu.formatString(niceCash.getOwner(),38));
            System.out.println(Menu.formatString(String.valueOf(niceCash.getValue()/FchToSatoshi),20));
        }
        Menu.printUnderline(120);
        Menu.anyKeyToContinue(br);
        System.out.println("You are paying:");
        Menu.printUnderline(120);
        System.out.print(Menu.formatString("FID",38));
        System.out.println(Menu.formatString("fch",20));
        Menu.printUnderline(120);
        for(Cash cash:issuredCashList){
            System.out.print(Menu.formatString(cash.getOwner(),38));
            System.out.println(Menu.formatString(String.valueOf(cash.getValue()/FchToSatoshi),20));
        }
        Menu.anyKeyToContinue(br);

        if(msg!=null) {
            System.out.println("The message in OP_RETURN is: \n" + msg);
            Menu.printUnderline(120);
            Menu.printUnderline(120);
            Menu.anyKeyToContinue(br);
        }
    }

    private static void createTx(byte[] sessionKey, BufferedReader br) {

        String fid = Inputer.inputGoodFid(br,"Input the multisig fid:");
        ApipClient apipClient = BlockchainAPIs.p2shByIdsPost(initApipParamsForClient.getUrlHead(), new String[]{fid}, initApipParamsForClient.getVia(), sessionKey);

        if(apipClient==null || apipClient.checkResponse()!=0){
            System.out.println(JsonTools.getNiceString(apipClient.getResponseBody()));
            return;
        }
        Map<String, P2SH> p2shMap = ApipDataGetter.getP2SHMap(apipClient.getResponseBody().getData());
        P2SH p2sh = p2shMap.get(fid);
        if(p2sh==null){
            System.out.println(fid + " is not found.");
            return;
        }

        System.out.println(JsonTools.getNiceString(p2sh));

        List<SendTo> sendToList = SendTo.inputSendToList(br);
        String msg = Inputer.inputString(br,"Input the message for OpReturn. Enter to ignore:");
        int msgLen;
        if("".equals(msg)){
            msg=null;
            msgLen = 0;
        }else msgLen = msg.getBytes().length;
        double sum=0;
        for(SendTo sendTo : sendToList){
            sum+=sendTo.getAmount();
        }

        long fee = FchTool.calcFeeMultiSign(0,sendToList.size(),msgLen, p2sh.getM(),p2sh.getN());
        double feeDouble = fee/FchToSatoshi;
        apipClient = WalletAPIs.cashValidForPayPost(initApipParamsForClient.getUrlHead(), fid, sum+feeDouble, initApipParamsForClient.getVia(), sessionKey);

        if(apipClient==null || apipClient.checkResponse()!=0){
            System.out.println(JsonTools.getNiceString(apipClient.getResponseBody()));
            return;
        }

        List<Cash> cashList = ApipDataGetter.getCashList(apipClient.getResponseBody().getData());

        JsonTools.gsonPrint(cashList);

        byte[] rawTx = FchTool.createMultiSignRawTx(cashList, sendToList, msg, p2sh);

        MultiSigData multiSignData = new MultiSigData(rawTx,p2sh,cashList);

        System.out.println("Multisig data unsigned:");
        Menu.printUnderline(10);
        System.out.println(multiSignData.toJson());
        Menu.printUnderline(10);

        System.out.println("Next step: sign it separately with the priKeys of: ");
        Menu.printUnderline(10);
        for(String fid1:p2sh.getFids()) System.out.println(fid1);
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }

    private static void createFid(byte[] sessionKey, BufferedReader br) {
        String[] fids = Inputer.inputFidArray(br,"Input FIDs. Enter to end:",0);
        if(fids.length>16){
            System.out.println("The FIDs can not be more than 16.");
            return;
        }
        int m = Inputer.inputInteger(br,"How many signatures is required? ",16);

        ApipClient apipClient = BlockchainAPIs.fidByIdsPost(initApipParamsForClient.getUrlHead(), fids, initApipParamsForClient.getVia(), sessionKey);
        if(apipClient==null || apipClient.checkResponse()!=0){
            System.out.println(JsonTools.getNiceString(apipClient.getResponseBody()));
            return;
        }

        Map<String, Address> fidMap = ApipDataGetter.getAddressMap(apipClient.getResponseBody().getData());

        List<byte[]> pubKeyList = new ArrayList<>();
        for(String fid:fids){
            String pubKey = fidMap.get(fid).getPubKey();
            pubKeyList.add(HexFormat.of().parseHex(pubKey));
        }

        P2SH p2SH = FchTool.genMultiP2sh(pubKeyList, 2);

        String mFid = p2SH.getFid();

        Menu.printUnderline(10);
        System.out.println("The multisig information is: \n"+JsonTools.getNiceString(p2SH));
        System.out.println("It's generated from :");
        for(String fid:fids){
            System.out.println(fid);
        }
        Menu.printUnderline(10);
        System.out.println("Your multisig FID: \n"+p2SH.getFid());
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }

    private static void showFid(byte[] sessionKey, BufferedReader br) {
        String fid = Inputer.inputGoodFid(br,"Input the multisig FID:");
        if(!fid.startsWith("3")){
            System.out.println("A multisig FID should start with '3'.");
            return;
        }
        System.out.println("Requesting APIP from "+ initApipParamsForClient.getUrlHead());
        ApipClient apipClient = BlockchainAPIs.p2shByIdsPost(initApipParamsForClient.getUrlHead(), new String[]{fid}, initApipParamsForClient.getVia(), sessionKey);
        if(apipClient==null || apipClient.checkResponse()!=0){
            System.out.println(JsonTools.getNiceString(apipClient.getResponseBody()));
            return;
        }
        Map<String, P2SH> p2shMap = ApipDataGetter.getP2SHMap(apipClient.getResponseBody().getData());
        P2SH p2sh = p2shMap.get(fid);
        if(p2sh==null){
            System.out.println(fid + " is not found.");
            return;
        }
        Menu.printUnderline(10);
        System.out.println("Multisig:");
        System.out.println(JsonTools.getNiceString(p2sh));
        Menu.printUnderline(10);
        System.out.println("The members:");

        apipClient = IdentityAPIs.cidInfoByIdsPost(initApipParamsForClient.getUrlHead(), p2sh.getFids(), initApipParamsForClient.getVia(), sessionKey);
        if(apipClient==null || apipClient.checkResponse()!=0){
            System.out.println(JsonTools.getNiceString(apipClient.getResponseBody()));
            return;
        }
        Map<String, CidInfo> cidInfoMap = ApipDataGetter.getCidInfoMap(apipClient.getResponseBody().getData());
        System.out.println(JsonTools.getNiceString(cidInfoMap));
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
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
        System.out.println(JsonTools.getNiceString(addrMap));
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }
    private static void priKeySwap(byte[] symKey, BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = KeyTools.inputCipherGetPriKey(br);
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
            priKey = KeyTools.inputCipherGetPriKey(br);
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

        System.out.println("Getting cashes from "+urlHead+" ...");
        ApipClient apipClient = walletAPIs.cashValidForPayPost(urlHead, sender, sum+((double) fee /FchToSatoshi), null, sessionKey);
        List<Cash> cashList = ApipDataGetter.getCashList(apipClient.getResponseBody().getData());

        String txSigned = createTransactionSign(cashList, priKey, sendToList, msg);

        System.out.println("Signed tx:");
        Menu.printUnderline(10);
        System.out.println(txSigned);
        Menu.printUnderline(10);

        System.out.println("Broadcast with "+urlHead+" ...");
        apipClient = walletAPIs.broadcastTxPost(urlHead,txSigned, initApipParamsForClient.getVia(), sessionKey);
        if(apipClient.checkResponse()!=0){
            System.out.println(apipClient.getCode()+": "+apipClient.getMessage());
            if(apipClient.getResponseBody().getData()!=null) System.out.println(apipClient.getResponseBody().getData());
            return;
        }

        System.out.println("Sent Tx:");
        Menu.printUnderline(10);
        System.out.println((String)apipClient.getResponseBody().getData());
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
    }

    private static void signMsgEcdsa(byte[] symKey,BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = KeyTools.inputCipherGetPriKey(br);
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
        System.out.println("Signature:");
        Menu.printUnderline(10);
        System.out.println(signature.toJsonAsyShortNice());
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
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
            System.out.println("Check result:");
            Menu.printUnderline(10);
            System.out.println(signature.getFid().equals(keyTools.KeyTools.pubKeyToFchAddr(signPubKey)));
            Menu.printUnderline(10);
            Menu.anyKeyToContinue(br);
        } catch (SignatureException e) {
            System.out.println("Check signature wrong."+e.getMessage());
        }
    }

    private static void signMsgSchnorr(byte[] symKey, BufferedReader br) {
        byte[] priKey;
        System.out.println("By a new FID? 'y' to confirm, others to use the local priKey:");
        String input = Inputer.inputString(br);
        if("y".equals(input)) {
            priKey = KeyTools.inputCipherGetPriKey(br);
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

        System.out.println("Signature:");
        Menu.printUnderline(10);
        System.out.println(signature.toJsonAsyShortNice());
        Menu.printUnderline(10);
        Menu.anyKeyToContinue(br);
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

            System.out.println("Result:");
            Menu.printUnderline(10);
            System.out.println(WalletTools.schnorrMsgVerify(signature.getMsg(), signature.getSign(), signature.getFid()));
            Menu.printUnderline(10);
            Menu.anyKeyToContinue(br);
        } catch (IOException e) {
            System.out.println("Checking signature wrong." + e.getMessage());
        }
    }
}
