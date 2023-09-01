package eccAes256K1P7;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

public class Test {
    public static void main(String[] args) throws Exception {

        Gson gson = new Gson();

        System.out.println("----------------------");
        System.out.println("Encode: ");
        System.out.println("    message: UTF-8");
        System.out.println("    key: Hex char[]");
        System.out.println("    ciphertext: Base64");
        System.out.println("----------------------");

        String msg = "hello world!";
        System.out.println("msg: "+msg);

        // ECC Test
        System.out.println("----------------------");
        System.out.println("Basic Test");
        System.out.println("----------------------");
        System.out.println("AsyOneWay:");
        System.out.println("----------");

        EccAes256K1P7 ecc = new EccAes256K1P7();

        EccAesDataByte eccAesDataByte;
        EccAesData eccAesData = new EccAesData();

        String pubKeyB = "02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67";
        eccAesData.setMsg(msg);
        eccAesData.setPubKeyB(pubKeyB);
        eccAesData.setType(EccAesType.AsyOneWay);
        eccAesDataByte = EccAesDataByte.fromEccAesData(eccAesData);

        // Encrypt with new keys
        ecc.encrypt(eccAesDataByte);

        char[] symKey;
        System.out.println("SymKey: "+HexFormat.of().formatHex(eccAesDataByte.getSymKey()));
        eccAesDataByte.clearSymKey();
        eccAesData = EccAesData.fromEccAesDataByte(eccAesDataByte);
        System.out.println("Encrypted with a new key pair:"+ gson.toJson(eccAesData));

        //Decrypt with new key
        String priKeyB = "ee72e6dd4047ef7f4c9886059cbab42eaab08afe7799cbc0539269ee7e2ec30c";
        eccAesDataByte.setMsg(null);
        eccAesDataByte.setPriKeyB(HexFormat.of().parseHex(priKeyB));

        ecc.decrypt(eccAesDataByte);
        eccAesDataByte.clearSymKey();
        System.out.println("Decrypted from bytes:"+ gson.toJson(EccAesData.fromEccAesDataByte(eccAesDataByte)));

        eccAesData= EccAesData.fromEccAesDataByte(eccAesDataByte);
        eccAesData.setPriKeyB(priKeyB.toCharArray());
        ecc.decrypt(eccAesData);
        System.out.println("Decrypted from String and char array:"+gson.toJson(eccAesData));

        System.out.println("EccAes JSON without symKey:");
        eccAesData.clearSymKey();
        System.out.println(gson.toJson(eccAesData));


        System.out.println("----------------------");
        System.out.println("AsyTwoWay:");
        System.out.println("----------");

        String pubKeyA = "03f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e2";
        String priKeyA = "46f27f1fc4c6371d8b78e8bd8bcd31c50da5edbe2fce1c68c4b25b710cab5ab3";

        eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.AsyTwoWay);
        eccAesData.setMsg(msg);
        eccAesData.setPubKeyB(pubKeyB);
        ecc.encrypt(eccAesData);
        System.out.println("Lack priKeyA: "+gson.toJson(eccAesData));

        eccAesData.setPriKeyA(priKeyA.toCharArray());

        ecc.encrypt(eccAesData);
        System.out.println("Encrypt: "+gson.toJson(eccAesData));

        eccAesData.setPriKeyB(priKeyB.toCharArray());
        eccAesData.setMsg(null);
        ecc.decrypt(eccAesData);
        System.out.println("Decrypt by private Key B: "+gson.toJson(eccAesData));
        eccAesData.setPriKeyA(priKeyA.toCharArray());
        eccAesData.setMsg(null);
        ecc.decrypt(eccAesData);
        System.out.println("Decrypt by private Key A: "+gson.toJson(eccAesData));

        System.out.println("----------------------");
        System.out.println("SymKey:");
        System.out.println("----------");
        eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.SymKey);

        String symKeyStr = "3b7ca1c4925c597083bb94c8e1582a621e4e72510780aa31ef0a769a406c2870";
        symKey = symKeyStr.toCharArray();
        eccAesData.setSymKey(symKey);
        ecc.encrypt(eccAesData);
        System.out.println("Lack msg: "+gson.toJson(eccAesData));

        eccAesData.setMsg(msg);
        eccAesData.setSymKey(symKey);
        ecc.encrypt(eccAesData);
        System.out.println("SymKey encrypt: "+gson.toJson(eccAesData));

        eccAesData.setMsg(null);
        eccAesData.setSymKey(symKey);
        ecc.decrypt(eccAesData);
        System.out.println("SymKey decrypt: "+gson.toJson(eccAesData));

        System.out.println("----------------------");
        System.out.println("Password:");
        System.out.println("----------");

        eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.Password);
        eccAesData.setMsg(msg);
        eccAesData.setSymKey(symKey);
        String passwordStr = "password马云！";
        char[] password = passwordStr.toCharArray();
        eccAesData.setPassword(password);

        System.out.println("password:"+String.valueOf(password));
        ecc.encrypt(eccAesData);
        System.out.println("Password encrypt: \n"+gson.toJson(eccAesData));

        eccAesData.setMsg(null);
        eccAesData.setSymKey(null);
        password = "password马云！".toCharArray();
        eccAesData.setPassword(password);

        ecc.decrypt(eccAesData);
        System.out.println("Password decrypt: \n"+ gson.toJson(eccAesData));
        System.out.println("----------------------");
        System.out.println("----------------------");
        System.out.println("Test Json");
        System.out.println("----------------------");
        System.out.println("AsyOneWay json:");
        System.out.println("----------");

        eccAesData = new EccAesData();

        eccAesData.setType(EccAesType.AsyOneWay);
        eccAesData.setMsg(msg);
        eccAesData.setPubKeyB(pubKeyB);
        String oneWayJson0 = gson.toJson(eccAesData);

        System.out.println("OneWayJson0: "+oneWayJson0);
        String encOneWayJson0 = ecc.encryptAsyOneWayJson(oneWayJson0);
        checkResult(eccAesData,"Encrypted: \n"+ encOneWayJson0);


        EccAesData eccAesData1 = ecc.decryptAsyJson(encOneWayJson0, priKeyB.toCharArray());
        checkResult(eccAesData,"Decrypted:\n"+eccAesData1.toJson());

        System.out.println("----------");

        System.out.println("AsyTwoWay json:");
        System.out.println("----------");
        eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.AsyTwoWay);
        eccAesData.setMsg(msg);
        eccAesData.setPubKeyB(pubKeyB);
        String twoWayJson1 = gson.toJson(eccAesData);
        System.out.println("TwoWayJson1:"+twoWayJson1);

        String encTwoWayJson1 = ecc.encryptAsyTwoWayJson(twoWayJson1,priKeyA.toCharArray());
        checkResult(eccAesData,"Encrypted: \n"+ encTwoWayJson1);
        eccAesData1 = ecc.decryptAsyJson(encTwoWayJson1, priKeyB.toCharArray());
        checkResult(eccAesData,"Decrypted:\n"+eccAesData1.toJson());

        System.out.println("----------");

        System.out.println("SymKey json:");
        System.out.println("----------");
        eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.SymKey);
        eccAesData.setMsg(msg);
        eccAesData.setSymKey(symKey);

        String symKeyJson1 = gson.toJson(eccAesData);
        System.out.println("SymKeyJson1:"+symKeyJson1);

        String encSymKeyJson1 = ecc.encryptWithSymKeyJson(symKeyJson1,symKey);
        checkResult(eccAesData,"Encrypted: \n"+ encSymKeyJson1);

        String decSymKeyJson = ecc.decryptWithSymKey(encSymKeyJson1, symKey);
        checkResult(eccAesData,"Decrypted:\n"+decSymKeyJson);
        System.out.println("----------");

        System.out.println("Password json:");
        System.out.println("----------");
        eccAesData = new EccAesData();
        eccAesData.setType(EccAesType.Password);
        eccAesData.setMsg(msg);
        eccAesData.setPassword(passwordStr.toCharArray());

        String passwordDataJson1 = gson.toJson(eccAesData);
        System.out.println("PasswordJson1:"+passwordDataJson1 );

        String encPasswordJson1 = ecc.encryptWithPasswordJson(passwordDataJson1 ,passwordStr.toCharArray());
        checkResult(eccAesData,"Encrypted: \n"+ encPasswordJson1);

        String decPasswordJson = ecc.decryptWithPasswordJson(encPasswordJson1, passwordStr.toCharArray());
        checkResult(eccAesData,"Decrypted:\n"+decPasswordJson);
        System.out.println("----------------------");

        System.out.println("----------------------");
        System.out.println("Test Constructor");
        System.out.println("----------------------");

        System.out.println("AsyOneWay encrypt Constructor:");
        System.out.println("----------");
        eccAesData = new EccAesData(EccAesType.AsyOneWay,msg,pubKeyB);
        ecc.encrypt(eccAesData);
        System.out.println(gson.toJson(eccAesData));

        System.out.println("----------");
        System.out.println("AsyTwoWay encrypt Constructor:");
        System.out.println("----------");

        eccAesData = new EccAesData(EccAesType.AsyTwoWay,msg,pubKeyB,priKeyA.toCharArray());
        ecc.encrypt(eccAesData);
        System.out.println(gson.toJson(eccAesData));
        System.out.println("----------");
        System.out.println("SymKey encrypt Constructor:");
        System.out.println("----------");
        symKey = symKeyStr.toCharArray();
        eccAesData = new EccAesData(EccAesType.SymKey,msg,symKey);
        ecc.encrypt(eccAesData);
        System.out.println(gson.toJson(eccAesData));
        System.out.println("----------");
        System.out.println("Password encrypt Constructor:");
        System.out.println("----------");
        eccAesData = new EccAesData(EccAesType.Password,msg,password);
        ecc.encrypt(eccAesData);
        System.out.println(gson.toJson(eccAesData));
        System.out.println("----------");
        System.out.println("Asy Decrypt Constructor:");
        System.out.println("----------");
        String cipher = "yu7qzwXoEeKwRsCT/fLxaA==";
        String iv = "988a330ab28e61fa01471bf13ce6cc7d";
        String sum = "346a8033";
        eccAesData = new EccAesData(EccAesType.AsyOneWay,pubKeyA,pubKeyB,iv,cipher,sum,priKeyB.toCharArray());
        ecc.decrypt(eccAesData);
        System.out.println(gson.toJson(eccAesData));
        System.out.println("----------");
        System.out.println("Sym Decrypt Constructor:");
        System.out.println("----------");
        cipher = "6f20f3ukM3ol0KRJHACb0w==";
        iv = "862dc48880b515d589851df25827fbcf";
        sum = "befc5792";
        eccAesData = new EccAesData(EccAesType.SymKey,iv,cipher,sum,symKey);
        ecc.decrypt(eccAesData);
        System.out.println(gson.toJson(eccAesData));
        System.out.println("----------------------");
        System.out.println("Bundle test");
        System.out.println("----------------------");
        System.out.println("String");
        System.out.println("----------");
        System.out.println("AsyOneWay bundle test");
        System.out.println("----------");

        System.out.println("msg:"+msg+",pubKeyB:"+pubKeyB);
        String bundle = ecc.encryptAsyOneWayBundle(msg,pubKeyB);
        System.out.println("Cipher bundle: "+bundle);
        String msgBundle = ecc.decryptAsyOneWayBundle(bundle, priKeyB.toCharArray());
        System.out.println("Msg from bundle:"+ msgBundle);

        System.out.println("----------------------");
        System.out.println("AsyTwoWay bundle test");
        System.out.println("----------");

        bundle = ecc.encryptAsyTwoWayBundle(msg,pubKeyB,priKeyA.toCharArray());
        System.out.println("Cipher bundle: "+bundle);
        msgBundle = ecc.decryptAsyTwoWayBundle(bundle, pubKeyA,priKeyB.toCharArray());
        System.out.println("Msg from PriKeyB:"+ msgBundle);
        msgBundle = ecc.decryptAsyTwoWayBundle(bundle, pubKeyB,priKeyA.toCharArray());
        System.out.println("Msg from PriKeyA:"+ msgBundle);

        System.out.println("----------------------");
        System.out.println("SymKey bundle test");
        System.out.println("----------");

        bundle = ecc.encryptSymKeyBundle(msg,symKey);
        System.out.println("Cipher bundle: "+bundle);
        msgBundle = ecc.decryptSymKeyBundle(bundle, symKey);
        System.out.println("Msg from bundle:"+ msgBundle);
        System.out.println("----------------------");

        System.out.println("byte[]");
        System.out.println("----------");
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        byte[] pubKeyBBytes = HexFormat.of().parseHex(pubKeyB);
        byte[] priKeyBBytes = HexFormat.of().parseHex(priKeyB);
        byte[] pubKeyABytes = HexFormat.of().parseHex(pubKeyA);
        byte[] priKeyABytes = HexFormat.of().parseHex(priKeyA);


        System.out.println("AsyOneWay bundle test");
        System.out.println("----------");

        byte[] bundleBytes = ecc.encryptAsyOneWayBundle(msgBytes,pubKeyBBytes);
        System.out.println("Cipher bundle: "+Base64.getEncoder().encodeToString(bundleBytes));
        byte[] msgBundleBytes = ecc.decryptAsyOneWayBundle(bundleBytes, priKeyBBytes);
        System.out.println("Msg from bundle:"+ new String(msgBundleBytes));

        System.out.println("----------------------");
        System.out.println("AsyTwoWay bundle test");
        System.out.println("----------");

        //Reload sensitive parameters
        priKeyBBytes = HexFormat.of().parseHex(priKeyB);
        priKeyABytes = HexFormat.of().parseHex(priKeyA);

        bundleBytes = ecc.encryptAsyTwoWayBundle(msgBytes, pubKeyBBytes, priKeyABytes);
        System.out.println("Cipher bundle: "+ Base64.getEncoder().encodeToString(bundleBytes));
        msgBundleBytes = ecc.decryptAsyTwoWayBundle(bundleBytes, pubKeyABytes, priKeyBBytes);
        System.out.println("Msg from PriKeyB:"+ new String(msgBundleBytes));

        //Reload sensitive parameters
        priKeyBBytes = HexFormat.of().parseHex(priKeyB);
        priKeyABytes = HexFormat.of().parseHex(priKeyA);
        msgBundleBytes = ecc.decryptAsyTwoWayBundle(bundleBytes, pubKeyBBytes,priKeyABytes);
        System.out.println("Msg from PriKeyA:"+ new String(msgBundleBytes));

        System.out.println("----------------------");
        System.out.println("SymKey bundle test");
        System.out.println("----------");

        byte[] symKeyBytes = HexFormat.of().parseHex(symKeyStr);
        bundleBytes = ecc.encryptSymKeyBundle(msgBytes,symKeyBytes);
        System.out.println("Cipher bundle: "+Base64.getEncoder().encodeToString(bundleBytes));

        //Reload sensitive parameters
        symKeyBytes = HexFormat.of().parseHex(symKeyStr);
        msgBundleBytes = ecc.decryptSymKeyBundle(bundleBytes, symKeyBytes);
        System.out.println("Msg from bundle:"+ new String(msgBundleBytes));
    }

    private static void checkResult(EccAesData eccAesData, String s) {
        if(eccAesData.getError()!=null){
            System.out.println(eccAesData.getError());
        }else{
            System.out.println(s);
        }
    }
}
