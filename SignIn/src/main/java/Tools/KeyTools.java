package Tools;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Bech32;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class KeyTools {
    public static boolean isValidFchAddr(String addr) {
        // TODO Auto-generated method stub

        byte[] addrBytes = Base58.decode(addr);

        
        byte[] suffix = new byte[4];
        byte[] addrNaked = new byte[21];

        System.arraycopy(addrBytes, 0, addrNaked, 0, 21);
        System.arraycopy(addrBytes, 21, suffix, 0, 4);

        byte[] hash = Sha256Hash.hashTwice(addrNaked);


        byte[] hash4 = new byte[4];
        System.arraycopy(hash, 0, hash4, 0, 4);

        if (addrNaked[0] == (byte) 0x23 && Arrays.equals(suffix, hash4)) {
            return true;
        }
        return false;
    }

    public static Map<String, String> pkToAddresses(String pubkey) {
        String fchAddr = KeyTools.pubKeyToFchAddr(pubkey);
        String btcAddr = KeyTools.pubKeyToBtcAddr(pubkey);
        String ethAddr = KeyTools.pubKeyToEthAddr(pubkey);
        String ltcAddr = KeyTools.pubKeyToLtcAddr(pubkey);
        String dogeAddr = KeyTools.pubKeyToDogeAddr(pubkey);
        String trxAddr = KeyTools.pubKeyToTrxAddr(pubkey);

        Map<String, String> map = new HashMap<String, String>();
        map.put("fchAddr", fchAddr);
        map.put("btcAddr", btcAddr);
        map.put("ethAddr", ethAddr);
        map.put("ltcAddr", ltcAddr);
        map.put("dogeAddr", dogeAddr);
        map.put("trxAddr", trxAddr);

        return map;
    }

    public static String parsePkFromUnlockScript(String hexScript) {
        byte[] bScript = SymSign.hexToByteArray(hexScript);
        
        int sigLen = Byte.toUnsignedInt(bScript[0]);//Length of signature;
        //Skip signature/跳过签名。
        //Read pubKey./读公钥
        byte pubkeyLenB = bScript[sigLen + 1]; //公钥长度
        int pubkeyLen = Byte.toUnsignedInt(pubkeyLenB);
        byte[] pubKeyBytes = new byte[pubkeyLen];
        System.arraycopy(bScript, sigLen + 2, pubKeyBytes, 0, pubkeyLen);
        return SymSign.bytesToHex(pubKeyBytes);
    }

    public static String recoverPK33ToPK65(String PK33) {

        BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
        BigInteger e = new BigInteger("3", 16);
        BigInteger one = new BigInteger("1", 16);
        BigInteger two = new BigInteger("2", 16);
        BigInteger four = new BigInteger("4", 16);
        BigInteger seven = new BigInteger("7", 16);
        String prefix = PK33.substring(0, 2);

        if (prefix.equals("02") || prefix.equals("03")) {
            BigInteger x = new BigInteger(PK33.substring(2), 16);

            BigInteger ySq = (x.modPow(e, p).add(seven)).mod(p);
            BigInteger y = ySq.modPow(p.add(one).divide(four), p);

            if (!(y.mod(two).equals(new BigInteger(prefix, 16).mod(two)))) {
                y = p.subtract(y);
            }

            return "04" + PK33.substring(2) + SymSign.bytesToHex(y.toByteArray());
        } else return null;
    }

    public static byte[] recoverPK33ToPK65(byte[] PK33) {
        String str = SymSign.bytesToHex(PK33);
        return SymSign.hexToByteArray(recoverPK33ToPK65(str));
    }

    public static String compressPk65To33(String pk64_65) throws Exception {
        String publicKey = null;
        if (pk64_65.length() == 130) {
            publicKey = pk64_65.substring(2, pk64_65.length());
        } else if (pk64_65.length() == 128) {
            publicKey = pk64_65;
        } else {
            throw new Exception("public key is invalid");
        }
        String keyX = publicKey.substring(0, publicKey.length() / 2);
        String keyY = publicKey.substring(publicKey.length() / 2, publicKey.length());
        String y_d = keyY.substring(keyY.length() - 1);
        String header;
        if ((Integer.parseInt(y_d, 16) & 1) == 0) {
            header = "02";
        } else {
            header = "03";
        }
        String pk33 = header + keyX;
        return pk33;
    }

    public static String compressPK65ToPK33(byte[] bytesPK65) {
        byte[] pk33 = new byte[33];
        byte[] y = new byte[32];
        System.arraycopy(bytesPK65, 1, pk33, 1, 32);
        System.arraycopy(bytesPK65, 33, y, 0, 32);
        BigInteger Y = new BigInteger(y);
        BigInteger TWO = new BigInteger("2");
        BigInteger ZERO = new BigInteger("0");
        if (Y.mod(TWO) == ZERO) {
            pk33[0] = 0x02;
        } else {
            pk33[0] = 0x03;
        }
        String PK33 = bytesToHex(invertArray(pk33));
        return PK33;
    }

    public static String hash160ToFCHAddr(String hash160Hex) {

        byte[] b = SymSign.hexToByteArray(hash160Hex);

        byte[] d = {0x23};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToFCHAddr(byte[] hash160Bytes) {

        byte[] prefixForFch = {0x23};
        byte[] hash160WithPrefix = new byte[21];
        System.arraycopy(prefixForFch, 0, hash160WithPrefix, 0, 1);
        System.arraycopy(hash160Bytes, 0, hash160WithPrefix, 1, 20);


        byte[] hashWithPrefix = Sha256Hash.hashTwice(hash160WithPrefix);
        byte[] checkHash = new byte[4];
        System.arraycopy(hashWithPrefix, 0, checkHash, 0, 4);
        byte[] addrRaw = bytesMerger(hash160WithPrefix, checkHash);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToBTCAddr(String hash160Hex) {

        byte[] b = SymSign.hexToByteArray(hash160Hex);

        byte[] d = {0x00};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToBTCAddr(byte[] hash160Bytes) {
        byte[] d = {0x00};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToDOGEAddr(String hash160Hex) {

        byte[] b = SymSign.hexToByteArray(hash160Hex);

        byte[] d = {0x1e};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        
        return Base58.encode(addrRaw);
    }

    public static String hash160ToDOGEAddr(byte[] hash160Bytes) {
        byte[] d = {0x1e};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToLTCAddr(String hash160Hex) {

        byte[] b = SymSign.hexToByteArray(hash160Hex);

        byte[] d = {0x30};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToLTCAddr(byte[] hash160Bytes) {

        byte[] d = {0x30};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToTRXAddr(byte[] hash160Bytes) {

        byte[] d = {0x41};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToMultiAddr(byte[] hash160Bytes) {
        byte[] d = {0x05};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = Sha256Hash.hashTwice(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String pubKeyToFchAddr(String a) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToFCHAddr(h);
        return address;
    }

    public static String pubKeyToFchAddr(byte[] a) {
        byte[] b = Sha256Hash.hash(a);
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToFCHAddr(h);
    }

    public static String pubKeyToMultiSigAddr(String a) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToMultiAddr(h);
    }

    public static String pubKeyToMultiSigAddr(byte[] a) {
        byte[] b = Sha256Hash.hash(a);
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToMultiAddr(h);
    }

    public static String pubKeyToBtcAddr(String a) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToBTCAddr(h);
    }

    public static String pubKeyToBtcAddr(byte[] a) {
        byte[] b = Sha256Hash.hash(a);
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToBTCAddr(h);
        return address;
    }

    public static String pubKeyToTrxAddr(String a) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToTRXAddr(h);
        return address;
    }

    public static String pubKeyToTrxAddr(byte[] a) {
        byte[] b = Sha256Hash.hash(a);
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToTRXAddr(h);
        return address;
    }

    public static String pubKeyToDogeAddr(String a) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToDOGEAddr(h);
        return address;
    }

    public static String pubKeyToDogeAddr(byte[] a) {
        byte[] b = Sha256Hash.hash(a);
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToDOGEAddr(h);
        return address;
    }

    public static String pubKeyToLtcAddr(String a) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToLTCAddr(h);
        return address;
    }

    public static String pubKeyToLtcAddr(byte[] a) {
        byte[] b = Sha256Hash.hash(a);
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToLTCAddr(h);
        return address;
    }

    public static String pubKeyToEthAddr(String a) {

        String pubKey65;
        if (a.length() == 130) {
            pubKey65 = a;
        } else {
            pubKey65 = recoverPK33ToPK65(a);
        }

        String pubKey63 = pubKey65.substring(2);

        byte[] pubKey63Bytes = SymSign.hexToByteArray(pubKey63);
        byte[] pukHash63Hash = SHA.sha3(pubKey63Bytes);
        
        
        String fullHash = SymSign.bytesToHex(pukHash63Hash);
        String address = "0x" + fullHash.substring(24);

        return address;
    }

    public static String pubKeyToEthAddr(byte[] b) {
        String a = SymSign.bytesToHex(b);

        String pubKey65 = recoverPK33ToPK65(a);

        String pubKey63 = pubKey65.substring(2);

        byte[] pubKey63Bytes = SymSign.hexToByteArray(pubKey63);
        byte[] pukHash63Hash = SHA.sha3(pubKey63Bytes);
        String fullHash = SymSign.bytesToHex(pukHash63Hash);
        String address = "0x" + fullHash.substring(24);

        return address;
    }

    public static String pubKeyToAtomAddr(String a) {
        byte[] sha256 = Sha256Hash.hash(SymSign.hexToByteArray(a));
        byte[] ripemd160 = SHA.Ripemd160(sha256);
        String bech32Addr = Bech32.encode("cosmos", ripemd160);
        return bech32Addr;
    }

    public static String scriptToMultiAddr(String script) {
        byte[] b = Sha256Hash.hash(SymSign.hexToByteArray(script));
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToMultiAddr(h);
        return address;
    }

    public static String priKeyToPubKey(String priKey) {
        // TODO Auto-generated method stub
        //私钥如果长度为38字节，则为压缩格式。构成为：前缀80+32位私钥+压缩标志01+4位校验位。
        byte[] priKey32Bytes = new byte[32];
        byte[] priKeyBytes;
        byte[] suffix;
        byte[] priKeyForHash;
        byte[] hash;
        byte[] hash4;

        int len = priKey.length();

        switch (len) {
            case 64:
                priKey32Bytes = SymSign.hexToByteArray(priKey);
                break;
            case 52:
                if (!(priKey.substring(0, 1).equals("L") || priKey.substring(0, 1).equals("K"))) {
                    System.out.println("It's not a private key.");
                    return null;
                }
                priKeyBytes = Base58.decode(priKey);

                suffix = new byte[4];
                priKeyForHash = new byte[34];

                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 34);
                System.arraycopy(priKeyBytes, 34, suffix, 0, 4);

                hash = Sha256Hash.hashTwice(priKeyForHash);

                hash4 = new byte[4];
                System.arraycopy(hash, 0, hash4, 0, 4);

                if (!Arrays.equals(suffix, hash4)) {
                    return null;
                }
                if (priKeyForHash[0] != (byte) 0x80) {
                    return null;
                }
                priKey32Bytes = new byte[32];
                System.arraycopy(priKeyForHash, 1, priKey32Bytes, 0, 32);
                break;
            case 51:
                if (!priKey.substring(0, 1).equals("5")) {
                    System.out.println("It's not a private key.");
                    return null;
                }

                priKeyBytes = Base58.decode(priKey);

                suffix = new byte[4];
                priKeyForHash = new byte[33];

                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 33);
                System.arraycopy(priKeyBytes, 33, suffix, 0, 4);

                hash = Sha256Hash.hashTwice(priKeyForHash);

                hash4 = new byte[4];
                System.arraycopy(hash, 0, hash4, 0, 4);

                if (!Arrays.equals(suffix, hash4)) {
                    return null;
                }
                if (priKeyForHash[0] != (byte) 0x80) {
                    return null;
                }
                priKey32Bytes = new byte[32];
                System.arraycopy(priKeyForHash, 1, priKey32Bytes, 0, 32);
                break;
            default:
                System.out.println("It's not a private key.");
                return null;
        }

        ECKey eckey = ECKey.fromPrivate(priKey32Bytes);

        String pubkey = SymSign.bytesToHex(eckey.getPubKey());

        return pubkey;
    }

    public static String getPriKey32(String priKey) {
        byte[] priKey32Bytes = new byte[32];
        byte[] priKeyBytes;
        byte[] suffix;
        byte[] priKeyForHash;
        byte[] hash;
        byte[] hash4;
        int len = priKey.length();

        switch (len) {
            case 64:
                priKey32Bytes = SymSign.hexToByteArray(priKey);
                break;
            case 52:
                if (!(priKey.substring(0, 1).equals("L") || priKey.substring(0, 1).equals("K"))) {
                    System.out.println("It's not a private key.");
                    return null;
                }
                priKeyBytes = Base58.decode(priKey);

                suffix = new byte[4];
                priKeyForHash = new byte[34];

                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 34);
                System.arraycopy(priKeyBytes, 34, suffix, 0, 4);

                hash = Sha256Hash.hashTwice(priKeyForHash);

                hash4 = new byte[4];
                System.arraycopy(hash, 0, hash4, 0, 4);

                if (!Arrays.equals(suffix, hash4)) {
                    return null;
                }
                if (priKeyForHash[0] != (byte) 0x80) {
                    return null;
                }
                priKey32Bytes = new byte[32];
                System.arraycopy(priKeyForHash, 1, priKey32Bytes, 0, 32);
                break;
            case 51:
                if (!priKey.substring(0, 1).equals("5")) {
                    System.out.println("It's not a private key.");
                    return null;
                }

                priKeyBytes = Base58.decode(priKey);

                suffix = new byte[4];
                priKeyForHash = new byte[33];

                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 33);
                System.arraycopy(priKeyBytes, 33, suffix, 0, 4);

                hash = Sha256Hash.hashTwice(priKeyForHash);

                hash4 = new byte[4];
                System.arraycopy(hash, 0, hash4, 0, 4);

                if (!Arrays.equals(suffix, hash4)) {
                    return null;
                }
                if (priKeyForHash[0] != (byte) 0x80) {
                    return null;
                }
                priKey32Bytes = new byte[32];
                System.arraycopy(priKeyForHash, 1, priKey32Bytes, 0, 32);
                break;
            default:
                System.out.println("It's not a private key.");
                return null;
        }

        return SymSign.bytesToHex(priKey32Bytes);
    }

    public static boolean checkSum(String str) {
        byte[] strBytes;
        byte[] suffix;
        byte[] hash;
        byte[] hash4 = new byte[4];

        strBytes = SymSign.hexToByteArray(str);
        int len = str.length();

        suffix = new byte[4];
        byte[] strNake = new byte[len - 4];

        System.arraycopy(strBytes, 0, strNake, 0, len - 4);
        System.arraycopy(strBytes, len - 4, suffix, 0, 4);

        hash = Sha256Hash.hashTwice(strNake);
        System.arraycopy(hash, 0, hash4, 0, 4);

        if (Arrays.equals(suffix, hash4)) {
            return true;
        } else return false;
    }

    public static boolean isValidPubKey(String puk) {
        // TODO Auto-generated method stub
        String prefix = "";
        if (puk.length() > 2) prefix = puk.substring(0, 2);
        if (puk.length() == 66) {
            if (prefix.equals("02") || prefix.equals("03")) return true;
        } else if (puk.length() == 130) {
            if (prefix.equals("04")) return true;
        }
        return false;
    }

    public static byte[] bytesMerger(byte[] a, byte[] b) {
        byte[] ab = new byte[a.length+ b.length];

        System.arraycopy(a,0,ab,0, a.length);
        System.arraycopy(b,0,ab, a.length, b.length);

        return ab;
    }

    public static byte[] hexToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static byte[] invertArray(byte[] a) {
        byte[] b = new byte[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[b.length - i - 1];
        }
        return b;
    }
}




