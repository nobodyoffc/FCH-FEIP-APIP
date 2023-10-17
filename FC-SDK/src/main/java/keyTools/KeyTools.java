package keyTools;

import constants.Constants;
import cryptoTools.SHA;
import cryptoTools.Hash;
import fcTools.ParseTools;
import javaTools.BytesTools;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;


public class KeyTools {

    public static boolean isValidFchAddr(String addr) {
        // TODO Auto-generated method stub
        try {
            byte[] addrBytes = Base58.decode(addr);

            byte[] suffix = new byte[4];
            byte[] addrNaked = new byte[21];

            System.arraycopy(addrBytes, 0, addrNaked, 0, 21);
            System.arraycopy(addrBytes, 21, suffix, 0, 4);

            byte[] hash = SHA.Sha256x2(addrNaked);

            byte[] hash4 = new byte[4];
            System.arraycopy(hash, 0, hash4, 0, 4);

            return addrNaked[0] == (byte) 0x23 && Arrays.equals(suffix, hash4);
        }catch (Exception ignore){
            return  false;
        }
    }

    public static Map<String, String> pubKeyToAddresses(String pubKey) {

        String pubKey33;

        if (pubKey.length() == 130) {
            try {
                pubKey33 = compressPk65To33(pubKey);
            } catch (Exception e) {
                return null;
            }
        } else {
            pubKey33 = pubKey;
        }

        String fchAddr = pubKeyToFchAddr(pubKey33);
        String btcAddr = pubKeyToBtcAddr(pubKey33);
        String ethAddr = pubKeyToEthAddr(pubKey);
        String ltcAddr = pubKeyToLtcAddr(pubKey33);
        String dogeAddr = pubKeyToDogeAddr(pubKey33);
        String trxAddr = pubKeyToTrxAddr(pubKey33);

        Map<String, String> map = new HashMap<>();
        map.put(Constants.FCH_ADDR, fchAddr);
        map.put(Constants.BTC_ADDR, btcAddr);
        map.put(Constants.ETH_ADDR, ethAddr);
        map.put(Constants.BCH_ADDR, btcAddr);
        map.put(Constants.LTC_ADDR, ltcAddr);
        map.put(Constants.DOGE_ADDR, dogeAddr);
        map.put(Constants.TRX_ADDR, trxAddr);

        return map;
    }

    public static Map<String, String> hash160ToAddresses(byte[] hash160) {
        String fchAddr = hash160ToFchAddr(hash160);
        String btcAddr = hash160ToBtcAddr(hash160);
        String ltcAddr = hash160ToLtcAddr(hash160);
        String dogeAddr = hash160ToDogeAddr(hash160);

        Map<String, String> map = new HashMap<>();
        map.put(Constants.FCH_ADDR, fchAddr);
        map.put(Constants.BTC_ADDR, btcAddr);
        map.put(Constants.BCH_ADDR, btcAddr);
        map.put(Constants.LTC_ADDR, ltcAddr);
        map.put(Constants.ETH_ADDR,null);
        map.put(Constants.TRX_ADDR,null);
        map.put(Constants.DOGE_ADDR, dogeAddr);

        return map;
    }

    public static String parsePkFromUnlockScript(String hexScript) {
        byte[] bScript = HexFormat.of().parseHex(hexScript);//HexFormat.of().parseHex(hexScript);
        int sigLen = Byte.toUnsignedInt(bScript[0]);//Length of signature;
        //Skip signature/跳过签名。
        //Read pubKey./读公钥
        byte pubKeyLenB = bScript[sigLen + 1]; //公钥长度
        int pubKeyLen = Byte.toUnsignedInt(pubKeyLenB);
        byte[] pubKeyBytes = new byte[pubKeyLen];
        System.arraycopy(bScript, sigLen + 2, pubKeyBytes, 0, pubKeyLen);
        return HexFormat.of().formatHex(pubKeyBytes);//HexFormat.of().formatHex(pubKeyBytes);
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

            byte[] yByteArray = y.toByteArray();

            // Ensuring y is always exactly 32 bytes
            byte[] yBytes = new byte[32];
            if (yByteArray.length == 33) {
                System.arraycopy(yByteArray, 1, yBytes, 0, 32);
            } else {
                System.arraycopy(yByteArray, 0, yBytes, 32 - yByteArray.length, yByteArray.length);
            }

            return "04" + PK33.substring(2) + HexFormat.of().formatHex(yBytes);
        } else {
            return null;
        }
    }


    public static byte[] recoverPK33ToPK65(byte[] PK33) {
        String str = HexFormat.of().formatHex(PK33);
        String pubKey65 = recoverPK33ToPK65(str);
        if(pubKey65!=null)
            return HexFormat.of().parseHex(pubKey65);
        else return null;
    }
    

    public static String compressPk65To33(String pk64_65) throws Exception {
        String publicKey;
        if (pk64_65.length() == 130 && pk64_65.startsWith("04")) {
            publicKey = pk64_65.substring(2);
        } else if (pk64_65.length() == 128) {
            publicKey = pk64_65;
        } else {
            throw new Exception("public key is invalid");
        }
        String keyX = publicKey.substring(0, publicKey.length() / 2);
        String keyY = publicKey.substring(publicKey.length() / 2);
        String y_d = keyY.substring(keyY.length() - 1);
        String header;
        if ((Integer.parseInt(y_d, 16) & 1) == 0) {
            header = "02";
        } else {
            header = "03";
        }
        return header + keyX;
    }



    public static String compressPK65ToPK33(byte[] bytesPK65) {
        byte[] pk33 = new byte[33];
        byte[] y = new byte[32];
        System.arraycopy(bytesPK65, 1, pk33, 1, 32);
        System.arraycopy(bytesPK65, 33, y, 0, 32);
        BigInteger Y = new BigInteger(y);
        BigInteger TWO = new BigInteger("2");
        BigInteger ZERO = new BigInteger("0");
        if (Y.mod(TWO).equals(ZERO)) {
            pk33[0] = 0x02;
        } else {
            pk33[0] = 0x03;
        }
        return BytesTools.bytesToHexStringLE(BytesTools.invertArray(pk33));
    }

    public static String hash160ToFchAddr(String hash160Hex) {

        byte[] b = HexFormat.of().parseHex(hash160Hex);

        byte[] d = {0x23};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToFchAddr(byte[] hash160Bytes) {

        byte[] prefixForFch = {0x23};
        byte[] hash160WithPrefix = new byte[21];
        System.arraycopy(prefixForFch, 0, hash160WithPrefix, 0, 1);
        System.arraycopy(hash160Bytes, 0, hash160WithPrefix, 1, 20);


        byte[] hashWithPrefix = SHA.Sha256x2(hash160WithPrefix);
        byte[] checkHash = new byte[4];
        System.arraycopy(hashWithPrefix, 0, checkHash, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(hash160WithPrefix, checkHash);

        return Base58.encode(addrRaw);
    }

    public static byte[] addrToHash160(String addr) {

        byte[] addrBytes = Base58.decode(addr);
        byte[] hash160Bytes = new byte[20];
        System.arraycopy(addrBytes,1,hash160Bytes,0,20);
        return hash160Bytes;
    }



    public static String hash160ToBtcAddr(String hash160Hex) {

        byte[] b = HexFormat.of().parseHex(hash160Hex);

        byte[] d = {0x00};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToBtcAddr(byte[] hash160Bytes) {
        byte[] d = {0x00};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToDogeAddr(String hash160Hex) {

        byte[] b = HexFormat.of().parseHex(hash160Hex);

        byte[] d = {0x1e};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToDogeAddr(byte[] hash160Bytes) {
        byte[] d = {0x1e};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToLteAddr(String hash160Hex) {

        byte[] b = HexFormat.of().parseHex(hash160Hex);

        byte[] d = {0x30};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(b, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String hash160ToLtcAddr(byte[] hash160Bytes) {

        byte[] d = {0x30};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

//    public static String hash160ToTrxAddr(byte[] hash160Bytes) {
//
//        byte[] d = {0x41};
//        byte[] e = new byte[21];
//        System.arraycopy(d, 0, e, 0, 1);
//        System.arraycopy(hash160Bytes, 0, e, 1, 20);
//
//        byte[] c = SHA.Sha256x2(e);
//        byte[] f = new byte[4];
//        System.arraycopy(c, 0, f, 0, 4);
//        byte[] addrRaw = BytesTools.bytesMerger(e, f);
//
//        return Base58.encode(addrRaw);
//    }

    public static String hash160ToMultiAddr(byte[] hash160Bytes) {
        byte[] d = {0x05};
        byte[] e = new byte[21];
        System.arraycopy(d, 0, e, 0, 1);
        System.arraycopy(hash160Bytes, 0, e, 1, 20);

        byte[] c = SHA.Sha256x2(e);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(e, f);

        return Base58.encode(addrRaw);
    }

    public static String pubKeyToFchAddr(String a) {
        byte[] b = SHA.Sha256(HexFormat.of().parseHex(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToFchAddr(h);
    }

    public static String pubKeyToFchAddr(byte[] a) {
        byte[] b = SHA.Sha256(a);
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToFchAddr(h);
    }

    public static String pubKeyToMultiSigAddr(String a) {
        byte[] b = SHA.Sha256(HexFormat.of().parseHex(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToMultiAddr(h);
    }

    public static String pubKeyToMultiSigAddr(byte[] a) {
        byte[] b = SHA.Sha256(a);
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToMultiAddr(h);
    }

    public static String pubKeyToBtcAddr(String a) {
        byte[] b = SHA.Sha256(HexFormat.of().parseHex(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToBtcAddr(h);
    }

    public static String pubKeyToBtcAddr(byte[] a) {
        byte[] b = SHA.Sha256(a);
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToBtcAddr(h);
    }

    public static String pubKeyToTrxAddr(String a) {
        if(a==null)return null;
        String pubKey65;
        if (a.length() == 130) {
            pubKey65 = a;
        } else {
            pubKey65 = recoverPK33ToPK65(a);
        }
        if(pubKey65==null)return null;
        String pubKey64 = pubKey65.substring(2);

        byte[] pubKey64Bytes = HexFormat.of().parseHex(pubKey64);
        byte[] pukHash64Hash = SHA.sha3(pubKey64Bytes);

        byte[] pukHashWithPrefix = new byte[21];
        pukHashWithPrefix[0] = 0x41;
        System.arraycopy(pukHash64Hash, 12, pukHashWithPrefix, 1, 20);

        byte[] c = SHA.Sha256x2(pukHashWithPrefix);
        byte[] f = new byte[4];
        System.arraycopy(c, 0, f, 0, 4);
        byte[] addrRaw = BytesTools.bytesMerger(pukHashWithPrefix, f);

        return Base58.encode(addrRaw);
    }

    public static String pubKeyToDogeAddr(String a) {
        byte[] b = SHA.Sha256(HexFormat.of().parseHex(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToDogeAddr(h);
    }

    public static String pubKeyToDogeAddr(byte[] a) {
        byte[] b = SHA.Sha256(a);
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToDogeAddr(h);
    }

    public static String pubKeyToLtcAddr(String a) {
        byte[] b = SHA.Sha256(HexFormat.of().parseHex(a));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToLtcAddr(h);
    }

    public static String pubKeyToLtcAddr(byte[] a) {
        byte[] b = SHA.Sha256(a);
        byte[] h = SHA.Ripemd160(b);
        String address = KeyTools.hash160ToLtcAddr(h);
        return address;
    }

    public static String pubKeyToEthAddr(String a) {

        String pubKey65;
        if (a.length() == 130) {
            pubKey65 = a;
        } else {
            pubKey65 = recoverPK33ToPK65(a);
        }

        String pubKey64 = pubKey65.substring(2);

        byte[] pubKey64Bytes = HexFormat.of().parseHex(pubKey64);
        byte[] pukHash64Hash = SHA.sha3(pubKey64Bytes);
        String fullHash = HexFormat.of().formatHex(pukHash64Hash);

        return "0x" + fullHash.substring(24);
    }

    public static String pubKeyToEthAddr(byte[] b) {
        String a = HexFormat.of().formatHex(b);

        String pubKey65 = recoverPK33ToPK65(a);

        String pubKey64 = pubKey65.substring(2);

        byte[] pubKey64Bytes = HexFormat.of().parseHex(pubKey64);
        byte[] pukHash64Hash = SHA.sha3(pubKey64Bytes);
        String fullHash = HexFormat.of().formatHex(pukHash64Hash);

        return "0x" + fullHash.substring(24);
    }

    public static String pubKeyToAtomAddr(String a) {
        byte[] sha256 = SHA.Sha256(HexFormat.of().parseHex(a));
        byte[] ripemd160 = SHA.Ripemd160(sha256);
        return Bech32.encode("cosmos", ripemd160);
    }

    public static String scriptToMultiAddr(String script) {
        byte[] b = SHA.Sha256(HexFormat.of().parseHex(script));
        byte[] h = SHA.Ripemd160(b);
        return KeyTools.hash160ToMultiAddr(h);
    }
    public static byte[] priKeyToPubKey(byte[] priKey32Bytes) {
        ECKey eckey = ECKey.fromPrivate(priKey32Bytes);
        return eckey.getPubKey();
    }


    public static String priKeyToPubKey(String priKey) {
        // TODO Auto-generated method stub
        //私钥如果长度为38字节，则为压缩格式。构成为：前缀80+32位私钥+压缩标志01+4位校验位。
        byte[] priKey32Bytes;
        byte[] priKeyBytes;
        byte[] suffix;
        byte[] priKeyForHash;
        byte[] hash;
        byte[] hash4;

        int len = priKey.length();

        switch (len) {
            case 64 -> priKey32Bytes = HexFormat.of().parseHex(priKey);
            case 52 -> {
                if (!(priKey.charAt(0) == 'L' || priKey.charAt(0) == 'K')) {
                    System.out.println("It's not a private key.");
                    return null;
                }
                priKeyBytes = Base58.decode(priKey);
                suffix = new byte[4];
                priKeyForHash = new byte[34];
                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 34);
                System.arraycopy(priKeyBytes, 34, suffix, 0, 4);
                hash = SHA.Sha256x2(priKeyForHash);
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
            }
            case 51 -> {
                if (priKey.charAt(0) != '5') {
                    System.out.println("It's not a private key.");
                    return null;
                }
                priKeyBytes = Base58.decode(priKey);
                suffix = new byte[4];
                priKeyForHash = new byte[33];
                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 33);
                System.arraycopy(priKeyBytes, 33, suffix, 0, 4);
                hash = SHA.Sha256x2(priKeyForHash);
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
            }
            default -> {
                System.out.println("It's not a private key.");
                return null;
            }
        }

        ECKey eckey = ECKey.fromPrivate(priKey32Bytes);

        String pubKey = HexFormat.of().formatHex(eckey.getPubKey());

        return pubKey;
    }

    public static String getPriKey32(String priKey) {
        byte[] priKey32Bytes;
        byte[] priKeyBytes;
        byte[] suffix;
        byte[] priKeyForHash;
        byte[] hash;
        byte[] hash4;
        int len = priKey.length();

        switch (len) {
            case 64:
                priKey32Bytes = HexFormat.of().parseHex(priKey);
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

                hash = SHA.Sha256x2(priKeyForHash);

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

                hash = SHA.Sha256x2(priKeyForHash);

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

        return HexFormat.of().formatHex(priKey32Bytes);
    }

    public static byte[] getPriKey32(byte[] priKey) {
        byte[] priKey32Bytes;
        byte[] priKeyBytes;
        byte[] suffix;
        byte[] priKeyForHash;
        byte[] hash;
        byte[] hash4;
        int len = priKey.length;

        switch (len) {
            case 32 -> priKey32Bytes = priKey;
            case 38 -> {
                priKeyBytes = priKey;
                suffix = new byte[4];
                priKeyForHash = new byte[34];
                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 34);
                System.arraycopy(priKeyBytes, 34, suffix, 0, 4);
                hash = SHA.Sha256x2(priKeyForHash);
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
            }
            case 37 -> {
                priKeyBytes = priKey;
                suffix = new byte[4];
                priKeyForHash = new byte[33];
                System.arraycopy(priKeyBytes, 0, priKeyForHash, 0, 33);
                System.arraycopy(priKeyBytes, 33, suffix, 0, 4);
                hash = SHA.Sha256x2(priKeyForHash);
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
            }
            default -> {
                System.out.println("It's not a private key.");
                return null;
            }
        }

        return priKey32Bytes;
    }

    public static boolean checkSum(String str) {
        byte[] strBytes;
        byte[] suffix;
        byte[] hash;
        byte[] hash4 = new byte[4];

        strBytes = HexFormat.of().parseHex(str);
        int len = str.length();

        suffix = new byte[4];
        byte[] strNaked = new byte[len - 4];

        System.arraycopy(strBytes, 0, strNaked, 0, len - 4);
        System.arraycopy(strBytes, len - 4, suffix, 0, 4);

        hash = SHA.Sha256x2(strNaked);
        System.arraycopy(hash, 0, hash4, 0, 4);

        return Arrays.equals(suffix, hash4);
    }

    public static boolean isValidPubKey(String puk) {
        // TODO Auto-generated method stub
        String prefix = "";
        if (puk.length() > 2) prefix = puk.substring(0, 2);
        if (puk.length() == 66) {
            return prefix.equals("02") || prefix.equals("03");
        } else if (puk.length() == 130) {
            return prefix.equals("04");
        }
        return false;
    }


    public static String priKey32To38(String priKey32){
        /*
        26字节长度为WIF compressed私钥格式。过程：
        在32位私钥加入版本号前缀0x80
        再加入压缩标志后缀0x01
        sha256x2(80+priKey32+01)取前4字节checksum
        对80+priKey32+01+checksum取base58编码
         */

        String priKey26;
        if(priKey32.length()!=64){
            System.out.println("Private keys must be 32 bytes");
            return null;
        }
        // Keys that have compressed public components have an extra 1 byte on the end in dumped form.
        byte[] bytes32 = BytesTools.hexToByteArray(priKey32);
        byte[] bytes38 = priKey32To38Compressed(bytes32);

        priKey26 = Base58.encode(bytes38);
        return priKey26;
    }

    private static byte[] priKey32To38Compressed(byte[] bytes32) {
        byte[] bytes34 = new byte[34];
        bytes34[0]= (byte) 0x80;
        System.arraycopy(bytes32, 0, bytes34, 1, 32);
        bytes34[33] = 1;

        byte[] hash = Hash.Sha256x2(bytes34);
        byte[] hash4 = new byte[4];
        System.arraycopy(hash, 0, hash4, 0, 4);

        byte[] bytes38 = new byte[38];

        System.arraycopy(bytes34, 0, bytes38, 0, 34);
        System.arraycopy(hash4, 0, bytes38, 34, 4);
        return bytes38;
    }

    public static String priKey32To37(String priKey32) {
        /*
        26字节长度为WIF compressed私钥格式。过程：
        在32位私钥加入版本号前缀0x80
        sha256x2(80+priKey32+01)取前4字节checksum
        对80+priKey32+01+checksum取base58编码
         */

        String priKey37;
        if(priKey32.length()!=64){
            System.out.println("Private keys must be 32 bytes");
            return null;
        }
        // Keys that have compressed public components have an extra 1 byte on the end in dumped form.
        byte[] bytes33 = new byte[33];
        byte[] bytes32 = BytesTools.hexToByteArray(priKey32);
        bytes33[0]= (byte) 0x80;
        System.arraycopy(bytes32, 0, bytes33, 1, 32);

        byte[] hash = Hash.Sha256x2(bytes33);
        byte[] hash4 = new byte[4];
        System.arraycopy(hash, 0, hash4, 0, 4);

        byte[] bytes37 = new byte[37];

        System.arraycopy(bytes33, 0, bytes37, 0, 33);
        System.arraycopy(hash4, 0, bytes37, 33, 4);

        priKey37 = Base58.encode(bytes37);
        return priKey37;
    }

}




