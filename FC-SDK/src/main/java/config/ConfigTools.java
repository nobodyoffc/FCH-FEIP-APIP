package config;

import eccAes256K1P7.EccAes256K1P7;
import eccAes256K1P7.EccAesDataByte;
import eccAes256K1P7.EccAesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigTools {

    private static final Logger log = LoggerFactory.getLogger(ConfigTools.class);
    public static EccAesDataByte encryptInitSymKey(byte[] passwordBytes, byte[] initSymKey) {
        EccAes256K1P7 ecc = new EccAes256K1P7();
        EccAesDataByte eccAesDataByte = new EccAesDataByte();
        eccAesDataByte.setType(EccAesType.Password);
        eccAesDataByte.setMsg(initSymKey);
        eccAesDataByte.setPassword(passwordBytes);
        ecc.encrypt(eccAesDataByte);
        if(eccAesDataByte.getError()!=null){
            System.out.println("Encrypt sessionKey to redis wrong: "+eccAesDataByte.getError());
            return null;
        }
        return eccAesDataByte;
    }

    //    public void setInitSymKeyCipher(byte[] passwordBytes) {
//        byte[] randomSymKey = BytesTools.getRandomBytes(32);
//        EccAesDataByte eccAesDataByte = encryptInitSymKey(passwordBytes, randomSymKey);
//        if (eccAesDataByte == null) return;
//        StartMake.initSymKey=randomSymKey;
//        this.initSymKeyCipher = EccAesData.fromEccAesDataByte(eccAesDataByte).toJson();
//    }
}
