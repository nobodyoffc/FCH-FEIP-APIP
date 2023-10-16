package fc;
import fcTools.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;

public class TrxAddress {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        String publicKeyHex = "02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67";
        String tronAddress = publicKeyToTrxAddress(publicKeyHex);
        System.out.println("Generated TRX Address: " + tronAddress);
    }

    public static String publicKeyToTrxAddress(String pubKey) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] pubKeyBytes = Hex.decode(pubKey);
        byte[] hashed = sha256(pubKeyBytes);
        byte[] ripemd160Hash = ripemd160(hashed);
        byte[] rawAddress = new byte[ripemd160Hash.length + 1];
        rawAddress[0] = 0x41;  // Version for mainnet
        System.arraycopy(ripemd160Hash, 0, rawAddress, 1, ripemd160Hash.length);

        byte[] checkSum = Arrays.copyOfRange(sha256(sha256(rawAddress)), 0, 4);
        byte[] fullRawAddress = new byte[rawAddress.length + checkSum.length];
        System.arraycopy(rawAddress, 0, fullRawAddress, 0, rawAddress.length);
        System.arraycopy(checkSum, 0, fullRawAddress, rawAddress.length, checkSum.length);

        return Base58.encode(fullRawAddress);
    }

    public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256", BouncyCastleProvider.PROVIDER_NAME);
        return digest.digest(data);
    }

    public static byte[] ripemd160(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest digest = MessageDigest.getInstance("RIPEMD160", BouncyCastleProvider.PROVIDER_NAME);
        return digest.digest(data);
    }
}


