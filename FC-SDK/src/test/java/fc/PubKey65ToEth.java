package fc;

import co.elastic.clients.json.JsonpUtils;
import keyTools.KeyTools;

import java.util.HexFormat;

public class PubKey65ToEth {
    public static void main(String[] args) {
        String pubKey33 = "039e677a89154d341a32430e355c5b76b64744fdd52a0502bd203f97eeba15ff0f";
        String pubKey65 = "047f82b9c287b6e7b2e8a6790e8b728644f4e9d7b817add1d18f42104e74f1e1c95e1fede24de9c2777a21191d0f6e8a472bb9a1b46b4a916871ca64be3b4e72e3";//KeyTools.recoverPK33ToPK65(pubKey33);
        System.out.println("Original:"+pubKey33);
        System.out.println("Recovered:"+pubKey65);
        String pubKey33New = KeyTools.compressPK65ToPK33(HexFormat.of().parseHex(pubKey65));
        System.out.println("Compressed:"+pubKey33New);
        String pubKey65New = KeyTools.recoverPK33ToPK65(pubKey33New);
        System.out.println("Recovered again:"+pubKey65New);
        System.out.println("Eth from 04:"+KeyTools.pubKeyToEthAddr(pubKey65New));
        System.out.println("Eth from 03:"+KeyTools.pubKeyToEthAddr(pubKey33New));


        String wangShiPriKey = "L5DDxf3PkFwi1jArqYokpTsntthLvhDYg44FXyTSgdTx3XEFR1iB";
        System.out.println("Wangshi priKey: "+ KeyTools.getPriKey32(wangShiPriKey));
        String wangShiPubKey = KeyTools.priKeyToPubKey(wangShiPriKey);
        System.out.println("Wangshi pubKey:"+wangShiPubKey);
        System.out.println("Wangshi pubKey 04:"+KeyTools.recoverPK33ToPK65(wangShiPubKey));
        System.out.println("Wangshi eth:"+ KeyTools.pubKeyToEthAddr(wangShiPubKey));

        String gpt47PubKey = "049d61b19deffd5a60ba844af492ec2cc44449c5697b3269197035766f7faa8e8a7f3b9d56a784d9045190cfef324e8f559fae324f3c29b61421f34d450342c2dc";
        String gpt47Eth = KeyTools.pubKeyToEthAddr(gpt47PubKey);
        System.out.println("Gpt47 eth:"+gpt47Eth);


        String pk = "02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67";
//        String pk = "02812e37281122d0ca4d386a0eaf03664011193427b27ad062028d2a8a7e743a86";
        String trxAddr = KeyTools.pubKeyToTrxAddr(pk);
        System.out.println(trxAddr);
    }

}
