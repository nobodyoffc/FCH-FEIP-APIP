package tools;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.fch.FchMainNetwork;


public class IdInfo {

    private byte[] privatekey;

    private String pubkey;

    private String address;// FID

    private ECKey ecKey;

    public IdInfo(byte[] privatekey) {
        NetworkParameters params = FchMainNetwork.MAINNETWORK;
//        byte[] bytesWif = Base58.decodeChecked(privatekey);
//        byte[] privateKeyBytes = new byte[32];
//        System.arraycopy(bytesWif, 1, privateKeyBytes, 0, 32);
//        ECKey eckey = ECKey.fromPrivate(privateKeyBytes);
        ECKey eckey = ECKey.fromPrivate(privatekey);
        init(eckey, CoinType.FCH);
    }

    public IdInfo(ECKey ecKey) {
        init(ecKey, CoinType.FCH);
    }

    private void init(ECKey ecKey, CoinType cointype) {
        this.ecKey = ecKey;
        this.privatekey = ecKey.getPrivKeyBytes();
        this.address = ecKey.toAddress(FchMainNetwork.MAINNETWORK).toString();
        this.pubkey = Utils.HEX.encode(ecKey.getPubKey());
    }

    public byte[] getPrivatekey() {
        return privatekey;
    }

    public String getPubkey() {
        return pubkey;
    }

    public String signMsg(String msg) {
        return ecKey.signMessage(msg);
    }

    public String signFullMessage(String msg) {
        return msg + "----" + address + "----" + signMsg(msg);
    }

    public String signFullMessageJson(String msg) {
        return FchProtocol.createSignMsg(msg, address, signMsg(msg));
    }

    public ECKey getECKey() {
        return ecKey;
    }

    public String getAddress() {
        return address;
    }

    public static IdInfo genRandomIdInfo() {
        ECKey ecKey = new ECKey();
        return new IdInfo(ecKey);
    }
}
