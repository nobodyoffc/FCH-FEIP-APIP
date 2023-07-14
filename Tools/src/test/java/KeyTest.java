import FcTools.KeyTools;


//问题可能出在私钥转换的时候
public class KeyTest {
    public static void main(String[] args) throws Exception {
        String pubKey33 = "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a";
        String pubKey65 = keyTools.KeyTools.recoverPK33ToPK65(pubKey33);
        System.out.println("chunhua:"+pubKey65);
        String newPubKey33 = KeyTools.compressPk65To33(pubKey65);
        System.out.println("chunhua:"+newPubKey33);
        System.out.println();

        String chunhuaPriKey = "L5DDxf3PkFwi1jArqYokpTsntthLvhDYg44FXyTSgdTx3XEFR1iB";//"L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8";
        String chunhuaPriKey32 = KeyTools.getPriKey32(chunhuaPriKey);
        System.out.println("chunhua priKey32:"+chunhuaPriKey32);
        String pubKeyNew = KeyTools.priKeyToPubKey(chunhuaPriKey32);
        System.out.println("chunhua pubKey: "+ pubKeyNew);
        System.out.println("chunhua eth:" + KeyTools.pubKeyToEthAddr(pubKeyNew));
        System.out.println();

        String cashPubKey = "039e677a89154d341a32430e355c5b76b64744fdd52a0502bd203f97eeba15ff0f";
        System.out.println("cash eth:" + KeyTools.pubKeyToEthAddr(cashPubKey));
        System.out.println();

        String wanshiPriKey = "L5DDxf3PkFwi1jArqYokpTsntthLvhDYg44FXyTSgdTx3XEFR1iB";
        String wanshiPriKey32 = KeyTools.getPriKey32(wanshiPriKey);
        System.out.println("wanshiPriKey32:"+wanshiPriKey32);
        //String wanshiPubKey = KeyTools.priKeyToPubKey(wanshiPriKey32);
        String wanshiPubKey = KeyTools.priKeyToPubKey(wanshiPriKey);
        System.out.println("wangshi pubKey: "+wanshiPubKey);
        System.out.println("wangshi fch:"+KeyTools.pubKeyToFchAddr(wanshiPubKey));
        System.out.println("wangshi eth: "+ KeyTools.pubKeyToEthAddr(wanshiPubKey));

        System.out.println();
        String xiaolouPriKey = "Kybd6FqL2xBEknFV2rcxvYsTZwqAbk99FyN3EBnWdi2M5UxiJL8A";
        String xiaolouPriKey32 = "46f27f1fc4c6371d8b78e8bd8bcd31c50da5edbe2fce1c68c4b25b710cab5ab3";
        String xiaolouPubKey = KeyTools.priKeyToPubKey(xiaolouPriKey);//"03f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e2";
        System.out.println("xiaolou 04PubKey:"+ KeyTools.recoverPK33ToPK65(KeyTools.priKeyToPubKey(xiaolouPriKey32)));
        System.out.println("xiaolou pubkey from b58:"+ xiaolouPubKey);
        System.out.println("xiaolou pubKey:"+KeyTools.priKeyToPubKey(xiaolouPriKey32));
        System.out.println("xiaolou eth: "+ KeyTools.pubKeyToEthAddr(xiaolouPubKey));
        System.out.println("xiaolou eth: "+ KeyTools.pubKeyToFchAddr(xiaolouPubKey));
    }
}
