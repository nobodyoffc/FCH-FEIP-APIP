package fipaClass;

import com.google.gson.Gson;
import constants.Constants;
import constants.Strings;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

public class Signature {
    private String fid;
    private String msg;
    private String sign;
    private String alg;

    private String address;
    private String message;
    private String signature;
    private String algorithm;

    private String symKeyName;

    private Type type;

    public enum Type{
        SymSign,AsySign
    }

    public Signature(String symSign) {
        this.type = Type.SymSign;
        this.sign = symSign;
    }

    public Signature(String symSign,String symKeyName) {
        this.type = Type.SymSign;
        this.sign = symSign;
        this.symKeyName = symKeyName;
    }

    public Signature(String fid, String msg, String EcdsaBtcMsgSign) {
        this.type = Type.AsySign;

        if(fid!=null) {
            this.fid = fid;
            this.address = fid;
        }

        if(msg!=null) {
            this.msg = msg;
            this.message = msg;
        }

        if(EcdsaBtcMsgSign!=null) {
            this.sign = EcdsaBtcMsgSign;
            this.signature = EcdsaBtcMsgSign;
        }

        this.alg = Constants.EcdsaBtcMsg_No1_NrC7;
        this.algorithm = Constants.EcdsaBtcMsg_No1_NrC7;
    }

    public Signature(String fid, String msg, String sign, String alg) {
        this.type = Type.AsySign;

        if(fid!=null) {
            this.fid = fid;
            this.address = fid;
        }

        if(msg!=null) {
            this.msg = msg;
            this.message = msg;
        }

        if(sign!=null) {
            this.sign = sign;
            this.signature = sign;
        }

        if(alg!=null){
            this.alg = alg;
            this.algorithm = alg;
        }
    }

    public boolean isPrepared(){
        return (fid!=null && msg!=null && sign!=null && alg!=null);
    }
    public void setBitPayEncryptAlg(){
        algorithm = Constants.EccAes256BitPay_No1_NrC7;
        alg = Constants.EccAes256BitPay_No1_NrC7;
    }

    public boolean verify(){
        if(!isPrepared())return false;

        try {
            String pubKey = ECKey.signedMessageToKey(message, sign).getPublicKeyAsHex();
            String signFid = KeyTools.pubKeyToFchAddr(pubKey);
            return fid.equals(signFid);
        } catch (SignatureException e) {
            return false;
        }
    }

    public String toJson(){
        Gson gson = new Gson();
        if(type==Type.AsySign) return gson.toJson(new ShortSign(fid,msg,sign,alg));
        if(type==Type.SymSign){
            return getSymSign(gson);
        }
        return gson.toJson(this);
    }

    public String toJsonSym(){
        Gson gson = new Gson();
        return getSymSign(gson);
    }

    private String getSymSign(Gson gson) {
        if(type==Type.AsySign)return null;
        if(symKeyName==null)return sign;
        else {
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put(Strings.SYM_SIGN,sign);
            dataMap.put(Strings.SYM_KEY_NAME,symKeyName);
            return gson.toJson(dataMap);
        }
    }

    public String toJsonAsyShort(){
        Gson gson = new Gson();
        return gson.toJson(new ShortSign(fid,msg,sign,alg));
    }

    public String toJsonAsyLong(){
        Gson gson = new Gson();
        LongSign longSign = new LongSign(fid,msg,sign,alg);

        return gson.toJson(longSign);
    }


    static class ShortSign {
        String fid;
        String msg;
        String sign;
        String alg;

         ShortSign(String fid, String msg, String sign, String alg) {
            this.fid = fid;
            this.msg = msg;
            this.sign = sign;
            this.alg = alg;
        }
    }

    static class LongSign {
        String address;
        String message;
        String signature;
        String algorithm;

        LongSign(String fid, String msg, String sign, String alg) {
            this.address = fid;
            this.message = msg;
            this.signature = sign;
            this.algorithm = alg;
        }
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSymKeyName() {
        return symKeyName;
    }

    public void setSymKeyName(String symKeyName) {
        this.symKeyName = symKeyName;
    }
}
