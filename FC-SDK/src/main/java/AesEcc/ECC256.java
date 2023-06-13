package AesEcc;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import fcTools.KeyTools;

public class ECC256 {
	
    ECIES ecies;

    //构造函数初始化，会分配密钥
    public ECC256() {
        EXPList.set_EXP_List();
        ecies = new ECIES();
        ecies.generateKeyPair();
    }

    //获取ECC公钥
    public String getEciesPubKey() {
        byte[] pk = ecies.pubKey2Bytes();
        return AES256.byteToHexString(pk);
    }

    //更换ECC的公钥
    public void setEciesPubKey(String pk) {
    	if(pk.length()==66) {
            String pubKey65 = recoverPubKey33To65(pk);
            pk = pubKey65.substring(2);
    	}else if(pk.length()!=128) {
    		System.out.println("Bad public key.");
    		return;
    	}
    	
        byte[] pk_final = new byte[64];
        byte[] pk_buf = AES256.hexStringToBytes(pk.toUpperCase());
        System.arraycopy(pk_buf, 0, pk_final, 64 - pk_buf.length, pk_buf.length);

        ecies.getPair(pk_final);
    }

    //获取ECC私钥
    public String getEciesPriKey() {
        byte[] sk = ecies.priKey2Bytes();
        return AES256.byteToHexString(sk);
    }

    //更换ECC的私钥
    public void setEciesPriKey(String priKey) {
    	
        if(priKey.length()==52){
            priKey = KeyTools.getPriKey32(priKey);
        }else if(priKey.length()==51){
        	priKey =KeyTools.getPriKey32(priKey);
        }else if(priKey.length()!=64){
            System.out.println("Only 64 chars hex, 52 or 51 chars base58 string can be accepted.");
        };
    	
        ecies.generateKeyPair(priKey);
    }

    //加密
    public String encrypt(String msg) throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException {
        byte[] ct = ecies.encrypt(msg);
        String cipherBase64 = Base64.getEncoder().encodeToString(ct);
        
        return cipherBase64;
    }

    //解密
    public String decrypt(String cipherBase64) throws Exception {
        byte[] ct = Base64.getDecoder().decode(cipherBase64);
        byte[] pt = ecies.decrypt(ct);
        String textUtf8 = new String(pt,"utf-8");

        return textUtf8;
    }
    
    //33字节（66hex）压缩公钥还原为65字节（130hex）公钥
    private String recoverPubKey33To65(String PK33) {

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

            return "04" + PK33.substring(2) + AES256.byteToHexString((y.toByteArray()));
        } else return null;
    }
}
