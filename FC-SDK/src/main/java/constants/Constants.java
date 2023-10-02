package constants;

public class Constants {


    public static final Long OneDayInterval = 1440L;
    public static final Long BalanceBackupInterval = OneDayInterval;
    public static final Long RewardInterval = OneDayInterval*10;
    public static  final String UserDir = "user.dir";
    public static final int MaxRequestSize = 100;
    public static final int DefaultSize = 20;
    public static final String zeroBlockId = "00000000cbe04361b1d6de82b893a7d8419e76e99dd2073ac0db2ba0e652eea8";
    public static final String MAGIC = "f9beb4d9";
    public static final String ALG_SIGN_TX_BY_CRYPTO_SIGN = "SignTxByCryptoSign@No1_NrC7";

    public static final long FchToSatoshi = 100000000;
    public static final double MinPayValue = 0.00001;

    public static final  String OPRETURN_FILE_DIR = "opreturn";
    public static final  String OPRETURN_FILE_NAME = "opreturn0.byte";
    public static final String FCH_ADDR = "fchAddr";
    public static final String BTC_ADDR = "btcAddr";
    public static final String ETH_ADDR = "ethAddr";
    public static final String LTC_ADDR = "ltcAddr";
    public static final String BCH_ADDR = "bchAddr";
    public static final String DOGE_ADDR = "dogeAddr";
    public static final String TRX_ADDR = "trxAddr";
    public static final String REWARD_HTML_FILE = "index.html";
    public static final String REWARD_HISTORY_FILE = "rewardHistory.json";
    public static final int FCH_LENGTH = 17;
    public static final int DATE_TIME_LENGTH = 19;
    public static final int FID_LENGTH = 34;
    public static final int HEX256_LENGTH = 64;
    public static final int PUBLIC_KEY_BYTES_LENGTH = 33;
    public static final int PRIVATE_KEY_BYTES_LENGTH = 32;
    public static final int SYM_KEY_BYTES_LENGTH = 32;
    public static final String ECC_AES_256_K1_P7 = "EccAes256K1P7@No1_NrC7";
    public static final int IV_BYTES_LENGTH = 16;
    public static final int K_BYTES = 1024;
    public static final int M_BYTES = 1024*1024;
    public static final int G_BYTES = 1024*1024*1024;
    public static final int MAX_FILE_SIZE_M = 200;
    public static final String DOT_FV = ".fv";
    public static final int MaxOpFileSize = 200*1024*1024;//251658240;
    public static int RedisDb4Webhook = 4;
    public static final String FCH_TICKET="FCH";
}
