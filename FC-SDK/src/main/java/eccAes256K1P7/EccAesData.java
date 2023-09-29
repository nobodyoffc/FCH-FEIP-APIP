package eccAes256K1P7;

import com.google.gson.Gson;
import constants.Constants;
import javaTools.BytesTools;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

import static javaTools.BytesTools.byteArrayToHexCharArray;
import static javaTools.BytesTools.byteArrayToUtf8CharArray;

public class EccAesData {
    private EccAesType type;
    private String alg;
    private String msg;
    private String cipher;
    private char[] symKey;
    private char[] password;
    private String pubKeyA;
    private String pubKeyB;
    private char[] priKeyA;
    private char[] priKeyB ;
    private String iv;
    private String sum;
    private String error;

    public EccAesData() {
    }

    /**For all types and operations. From Json string without sensitive data.
     */
    public EccAesData(String eccAesDataJson){
        fromJson(eccAesDataJson);
    }

    /**For all type. Encrypt or Decrypt.
     */
    public EccAesData(String eccAesDataJson,char[] key){
        fromJson(eccAesDataJson);
        switch (this.type){
            case AsyOneWay -> priKeyB=key;
            case AsyTwoWay -> checkKeyPairAndSetPriKey(this,key);
            case SymKey -> symKey=key;
            case Password -> password=key;
        }
    }

    /**For AsyOneWay encrypt. The classic encrypting mode.
     */
    public EccAesData(EccAesType asyOneWay,String msg,String pubKeyB) {
        if(asyOneWay==EccAesType.AsyOneWay) {
            this.type= asyOneWay;
            this.alg = Constants.ECC_AES_256_K1_P7;
            this.msg = msg;
            this.pubKeyB = pubKeyB;
        }else{
            this.error="Constructing wrong. "+EccAesType.AsyOneWay + " is required for this constructor. ";
        }
    }

    /**For AsyTwoWay encrypt
     */
    public EccAesData(EccAesType asyTwoWay,String msg,String pubKeyB,char[] priKeyA) {
        if(asyTwoWay==EccAesType.AsyTwoWay) {
            this.type= asyTwoWay;
            this.alg = Constants.ECC_AES_256_K1_P7;
            this.msg = msg;
            this.pubKeyB = pubKeyB;
            this.priKeyA = priKeyA;
        }else{
            this.error="Constructing wrong. "+EccAesType.AsyTwoWay + " is needed for this constructor. ";
        }
    }

    /**For SymKey or Password encrypt
     */
    public EccAesData(EccAesType symKeyOrPasswordType,String msg,char[] symKeyOrPassword) {
        this.type = symKeyOrPasswordType;
        switch (symKeyOrPasswordType){
            case SymKey -> symKey=symKeyOrPassword;
            case Password -> password=symKeyOrPassword;
            default -> this.error= "Constructing wrong. "+EccAesType.SymKey + " or "+ EccAesType.Password + " is required for this constructor. ";
        }
        this.alg = Constants.ECC_AES_256_K1_P7;
        this.msg = msg;
    }

    /**For AsyOneWay or AsyTwoWay decrypt
     */
    public EccAesData(EccAesType asyOneWayOrAsyTwoWayType,String pubKeyA,String pubKeyB,String iv,String cipher,@Nullable String sum,char[] priKey) {
        if(asyOneWayOrAsyTwoWayType==EccAesType.AsyOneWay||asyOneWayOrAsyTwoWayType==EccAesType.AsyTwoWay) {
            byte[] pubKeyBytesA = HexFormat.of().parseHex(pubKeyA);
            byte[] pubKeyBytesB = HexFormat.of().parseHex(pubKeyB);
            byte[] priKeyBytes = BytesTools.hexCharArrayToByteArray(priKey);
            this.alg = Constants.ECC_AES_256_K1_P7;
            this.type = asyOneWayOrAsyTwoWayType;
            this.iv = iv;
            this.cipher = cipher;
            this.sum = sum;
            this.pubKeyA = pubKeyA;
            this.pubKeyB = pubKeyB;
            if(EccAes256K1P7.isTheKeyPair(pubKeyBytesA,priKeyBytes)) {
                this.priKeyA = priKey;
            }else if(EccAes256K1P7.isTheKeyPair(pubKeyBytesB,priKeyBytes)){
                this.priKeyB = priKey;
            }else this.error = "The priKey doesn't match pubKeyA or pubKeyB.";
        }else this.error= "Constructing wrong. "+EccAesType.AsyOneWay+" or"+EccAesType.AsyTwoWay+" is required for this constructor. ";
        System.out.println("TEST::"+new Gson().toJson(this));
    }

    public EccAesData(EccAesType symKeyOrPasswordType, String iv, String cipher, @Nullable String sum, char[] symKeyOrPassword) {
        this.alg = Constants.ECC_AES_256_K1_P7;
        this.type = symKeyOrPasswordType;
        this.iv = iv;
        this.cipher = cipher;
        this.sum = sum;
        if(symKeyOrPasswordType==EccAesType.SymKey) {
            this.symKey = symKeyOrPassword;
        }else if(symKeyOrPasswordType==EccAesType.Password){
            this.password = symKeyOrPassword;
        }else {
            this.error= "Constructing wrong. "+EccAesType.SymKey+" or"+EccAesType.Password+" is required for this constructor. ";
        }
    }

    private void checkKeyPairAndSetPriKey(EccAesData eccAesData, char[] key) {
        byte[] keyBytes = BytesTools.hexCharArrayToByteArray(key);
        if(eccAesData.getPubKeyA()!=null){
            byte[] pubKey = HexFormat.of().parseHex(eccAesData.getPubKeyA());
            if(EccAes256K1P7.isTheKeyPair(pubKey, keyBytes)){
                eccAesData.setPriKeyA(key);
                return;
            }else eccAesData.setPriKeyB(key);
        }
        if(eccAesData.getPubKeyB()!=null){
            byte[] pubKey = HexFormat.of().parseHex(eccAesData.getPubKeyB());
            if(EccAes256K1P7.isTheKeyPair(pubKey, keyBytes)){
                eccAesData.setPriKeyB(key);
                return;
            }else eccAesData.setPriKeyA(key);
        }
        eccAesData.setError("No pubKeyA or pubKeyB.");
    }

    public void fromJson(String json) {
        EccAesData eccAesData = new Gson().fromJson(json,EccAesData.class);
        this.type = eccAesData.getType();
        this.alg = eccAesData.getAlg();
        this.msg = eccAesData.getMsg();
        this.cipher = this.getCipher();
        this.pubKeyA = eccAesData.getPubKeyA();
        this.pubKeyB = eccAesData.getPubKeyB();
        this.sum = eccAesData.getSum();
        this.error = eccAesData.getError();
    }

    public String toJson(){
        clearAllSensitiveData();
        return new Gson().toJson(this);
    }

    public static EccAesData fromEccAesDataByte(EccAesDataByte eccAesDataByte) {
        EccAesData eccAesData = new EccAesData();

        if(eccAesDataByte.getType()!=null)
            eccAesData.setType(eccAesDataByte.getType());
        if(eccAesDataByte.getAlg()!=null)
            eccAesData.setAlg(eccAesDataByte.getAlg());
        if(eccAesDataByte.getCipher()!=null)
            eccAesData.setCipher(Base64.getEncoder().encodeToString(eccAesDataByte.getCipher()));
        if(eccAesDataByte.getIv()!=null)
            eccAesData.setIv(HexFormat.of().formatHex(eccAesDataByte.getIv()));
        if(eccAesDataByte.getMsg()!=null)
            eccAesData.setMsg(new String(eccAesDataByte.getMsg(), StandardCharsets.UTF_8));
        if(eccAesDataByte.getPassword()!=null)
            eccAesData.setPassword(byteArrayToUtf8CharArray(eccAesDataByte.getPassword()));
        if(eccAesDataByte.getPubKeyA()!=null)
            eccAesData.setPubKeyA(HexFormat.of().formatHex(eccAesDataByte.getPubKeyA()));
        if(eccAesDataByte.getPubKeyB()!=null)
            eccAesData.setPubKeyB(HexFormat.of().formatHex(eccAesDataByte.getPubKeyB()));
        if(eccAesDataByte.getPriKeyA()!=null)
            eccAesData.setPriKeyA(byteArrayToHexCharArray(eccAesDataByte.getPriKeyA()));
        if(eccAesDataByte.getPriKeyB()!=null)
            eccAesData.setPriKeyB(byteArrayToHexCharArray(eccAesDataByte.getPriKeyB()));
        if(eccAesDataByte.getSymKey()!=null)
            eccAesData.setSymKey(byteArrayToHexCharArray(eccAesDataByte.getSymKey()));
        if(eccAesDataByte.getSum()!=null)
            eccAesData.setSum(HexFormat.of().formatHex(eccAesDataByte.getSum()));
        if(eccAesDataByte.getError()!=null)
            eccAesData.setError(eccAesDataByte.getError());

        return eccAesData;
    }

    public void clearCharArray(char[] array) {
        if(array!=null){
            Arrays.fill(array, '\0');
            array=null;
        }
    }

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

    public EccAesType getType() {
        return type;
    }

    public void setType(EccAesType type) {
        this.type = type;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public char[] getSymKey() {
        return symKey;
    }

    public void setSymKey(char[] symKey) {
        this.symKey = symKey;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getPubKeyA() {
        return pubKeyA;
    }

    public void setPubKeyA(String pubKeyA) {
        this.pubKeyA = pubKeyA;
    }

    public String getPubKeyB() {
        return pubKeyB;
    }

    public void setPubKeyB(String pubKeyB) {
        this.pubKeyB = pubKeyB;
    }

    public char[] getPriKeyA() {
        return priKeyA;
    }

    public void setPriKeyA(char[] priKeyA) {
        this.priKeyA = priKeyA;
    }

    public char[] getPriKeyB() {
        return priKeyB;
    }

    public void setPriKeyB(char[] priKeyB) {
        this.priKeyB = priKeyB;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public void clearPassword() {
        clearCharArray(password);
        this.password=null;
    }

    public void clearSymKey() {
        clearCharArray(symKey);
        this.symKey=null;
    }

    public void clearPriKeyA() {
        clearCharArray(priKeyA);
        this.priKeyA=null;
    }
    public void clearPriKeyB() {
        clearCharArray(priKeyB);
        this.priKeyB=null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
