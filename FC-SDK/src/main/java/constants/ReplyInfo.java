package constants;

public class ReplyInfo {
    //Header name
    public static final String CodeInHeader = "Code";
    public static final String SignInHeader = "Sign";
    public static final String FidInHeader = "FID";
    public static final String SessionNameInHeader = "SessionName";
    //Code and messages
    public static final int Code0Success = 0;
    public static final String Msg0Success = "Success.";
    public static final int Code1000SignMissed = 1000;
    public static final String Msg1000SignMissed = "Sign missed in request header.";
    public static final int Code1001PubKeyMissed = 1001;
    public static final String Msg1001PubKeyMissed = "PubKey missed in request header.";
    public static final int Code1002SessionNameMissed = 1002;
    public static final String Msg1002SessionNameMissed = "SessionName missed in request header.";
    public static final int Code1003BodyMissed = 1003;
    public static final String Msg1003BodyMissed = "Request body missed.";
    public static final int Code1004InsufficientBalance = 1004;
    public static final String Msg1004InsufficientBalance = "Insufficient balance. Buy the service please.";
    public static final int Code1005UrlUnequal = 1005;
    public static final String Msg1005UrlUnequal = "The request URL is not the same as the one you signed.";
    public static final int Code1006RequestTimeExpired = 1006;
    public static final String Msg1006RequestTimeExpired = "Request expired.";
    public static final int Code1007UsedNonce = 1007;
    public static final String Msg1007UsedNonce = "Nonce had been used.";
    public static final int Code1008BadSign = 1008;
    public static final String Msg1008BadSign = "Failed to verify signature.";
    public static final int Code1009SessionTimeExpired = 1009;
    public static final String Msg1009SessionTimeExpired = "NO such sessionName or it was expired. Please sign in again.";
    public static final int Code1010TooMuchData = 1010;
    public static final String Msg1010TooMuchData = "Too much data to be requested.";
    public static final int Code1011DataNotFound = 1011;
    public static final String Msg1011DataNotFound = "No data meeting the conditions.";
    public static final int Code1012BadQuery = 1012;
    public static final String Msg1012BadQuery = "Bad query. Check your request body referring related APIP document.";
    public static final int Code1013BadRequest = 1013;
    public static final String Msg1013BadRequest = "Bad request. Please check request body.";
    public static final int Code1014ApiSuspended = 1014;//The API is suspended
    public static final String Msg1014ApiSuspended  = "The API is suspended";
    public static final int Code1015FidMissed = 1015;
    public static final String Msg1015FidMissed  = "FID missed in request header.";
    public static final int Code1016IllegalUrl = 1016;
    public static final String Msg1016IllegalUrl  = "Illegal URL.";
    public static final int Code1020OtherError = 1020;
    public static final String Msg1020OtherError = "Other error.";

    public static final int Code2001NoFreeGet = 2001;
    public static final String Msg2001NoFreeGet = "FreeGet API is not active now.";
    public static final int Code2002CidNoFound = 2002;
    public static final String Msg2002CidNoFound = "Cid no found.";

    public static final int Code2003IllegalFid = 2003;
    public static final String Msg2003IllegalFid = "Illegal FID.";

    public static final int Code2004RawTxNoHex = 2004;
    public static final String Msg2004RawTxNoHex = "Raw TX must be in HEX.";

    public static final int Code2005SendTxFailed = 2005;
    public static final String Msg2005SendTxFailed = "Send TX failed.";

    public static final int Code2006AppNoFound = 2006;
    public static final String Msg2006AppNoFound = "App no found.";

    public static final int Code2007CashNoFound = 2007;
    public static final String Msg2007CashNoFound = "Cash no found.";

    public static final int Code2008ServiceNoFound = 2008;
    public static final String Msg2008ServiceNoFound= "Cash no found.";
    public static final int Code2009NoFreeSessionKey= 2009;
    public static final String Msg2009NoFreeSessionKey= "Can not get free sessionKey.";
    public static final int Code2010ErrorFromFchRpc= 2010;
    public static final String Msg2010ErrorFromFchRpc= "Error from freecash RPC.";

}
