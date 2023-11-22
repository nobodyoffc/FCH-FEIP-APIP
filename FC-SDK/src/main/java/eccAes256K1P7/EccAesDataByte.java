package eccAes256K1P7;

import javaTools.BytesTools;
import javaTools.JsonTools;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

import static javaTools.BytesTools.hexCharArrayToByteArray;
import static javaTools.BytesTools.utf8CharArrayToByteArray;

public class EccAesDataByte {
    private EccAesType type;
    private String alg;
    private byte[] msg;
    private byte[] symKey;
    private byte[] password;
    private byte[] pubKeyA;
    private byte[] pubKeyB;
    private byte[] priKeyA;
    private byte[] priKeyB;
    private byte[] iv;
    private byte[] sum;
    private byte[] cipher;
    private String error;

    public EccAesDataByte() {
    }

    public static EccAesDataByte fromEccAesData(EccAesData eccAesData) {
        EccAesDataByte eccAesDataByte = new EccAesDataByte();

        if(eccAesData.getType()!=null)
            eccAesDataByte.setType(eccAesData.getType());
        if(eccAesData.getAlg()!=null)
            eccAesDataByte.setAlg(eccAesData.getAlg());
        if(eccAesData.getCipher()!=null)
            eccAesDataByte.setCipher(Base64.getDecoder().decode(eccAesData.getCipher()));
        if(eccAesData.getIv()!=null)
            eccAesDataByte.setIv(HexFormat.of().parseHex(eccAesData.getIv()));
        if(eccAesData.getMsg()!=null)
            eccAesDataByte.setMsg(eccAesData.getMsg().getBytes(StandardCharsets.UTF_8));
        if(eccAesData.getPassword()!=null)
            eccAesDataByte.setPassword(utf8CharArrayToByteArray(eccAesData.getPassword()));
        if(eccAesData.getPubKeyA()!=null)
            eccAesDataByte.setPubKeyA(HexFormat.of().parseHex(eccAesData.getPubKeyA()));
        if(eccAesData.getPubKeyB()!=null)
            eccAesDataByte.setPubKeyB(HexFormat.of().parseHex(eccAesData.getPubKeyB()));
        if(eccAesData.getPriKeyA()!=null)
            eccAesDataByte.setPriKeyA(hexCharArrayToByteArray(eccAesData.getPriKeyA()));
        if(eccAesData.getPriKeyB()!=null)
            eccAesDataByte.setPriKeyB(hexCharArrayToByteArray(eccAesData.getPriKeyB()));
        if(eccAesData.getSymKey()!=null)
            eccAesDataByte.setSymKey(hexCharArrayToByteArray(eccAesData.getSymKey()));
        if(eccAesData.getSum()!=null)
            eccAesDataByte.setSum(HexFormat.of().parseHex(eccAesData.getSum()));
        if(eccAesData.getError()!=null)
            eccAesDataByte.setError(eccAesData.getError());

        return eccAesDataByte;
    }

    public String getJson(){
        EccAesData eccAesData = EccAesData.fromEccAesDataByte(this);
        return JsonTools.getNiceString(eccAesData);
    }

//    public String makeIvAndCipherJson() {
//        String ivStr = HexFormat.of().formatHex(this.iv);
//        String cipherStr = Base64.getEncoder().encodeToString(this.cipher);
//        return "{\"iv\":\""+ivStr+"\",\"cipher\":\""+cipherStr+"\"}";
//    }
//
//    public String makePubKeyIvAndCipherJson() {
//        String pubKeyAStr = HexFormat.of().formatHex(this.pubKeyA);
//        String ivStr = HexFormat.of().formatHex(this.iv);
//        String cipherStr = Base64.getEncoder().encodeToString(this.cipher);
//        return "{\"pubKeyA\":\""+pubKeyAStr +"\",\"iv\":\""+ivStr+"\",\"cipher\":\""+cipherStr+"\"}";
//
//    }

    public void clearAllSensitiveData(){
        clearPassword();
        clearSymKey();
        clearPriKeyA();
        clearPriKeyB();
    }

    public void clearAllSensitiveDataButSymKey(){
        clearPassword();
        clearPriKeyA();
        clearPriKeyB();
    }

    public void clearSymKey(){
        BytesTools.clearByteArray(this.symKey);
        this.symKey=null;
    }

    public void clearPassword(){
        BytesTools.clearByteArray(this.password);
        this.password=null;
    }

    public void clearPriKeyA(){
        BytesTools.clearByteArray(this.priKeyA);
        this.priKeyA=null;
    }

    public void clearPriKeyB(){
        BytesTools.clearByteArray(this.priKeyB);
        this.priKeyB=null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public EccAesDataByte(EccAesType type) {
        this.type = type;
    }

    public EccAesType getType() {
        return type;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getSymKey() {
        return symKey;
    }

    public void setSymKey(byte[] symKey) {
        this.symKey = symKey;
    }

    public void setType(EccAesType type) {
        this.type = type;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public byte[] getSum() {
        return sum;
    }

    public void setSum(byte[] sum) {
        this.sum = sum;
    }

    public byte[] getCipher() {
        return cipher;
    }

    public void setCipher(byte[] cipher) {
        this.cipher = cipher;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public byte[] getPubKeyA() {
        return pubKeyA;
    }

    public void setPubKeyA(byte[] pubKeyA) {
        this.pubKeyA = pubKeyA;
    }

    public byte[] getPubKeyB() {
        return pubKeyB;
    }

    public void setPubKeyB(byte[] pubKeyB) {
        this.pubKeyB = pubKeyB;
    }

    public byte[] getPriKeyA() {
        return priKeyA;
    }

    public void setPriKeyA(byte[] priKeyA) {
        this.priKeyA = priKeyA;
    }

    public byte[] getPriKeyB() {
        return priKeyB;
    }

    public void setPriKeyB(byte[] priKeyB) {
        this.priKeyB = priKeyB;
    }

}
