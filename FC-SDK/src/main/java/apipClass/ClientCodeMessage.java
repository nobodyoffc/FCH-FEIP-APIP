package apipClass;

public class ClientCodeMessage {

    public static int Code1ResponseIsNull =1;
    public static String Msg1ResponseIsNull = "Http response is null.";
    public static int Code2GetRequestFailed = 2;
    public static String Msg2GetRequestFailed="The request of GET is failed.";
    public static int Code3CloseHttpClietFailed = 3;
    public static String Msg3CloseHttpClietFailed = "Failed to close the http client.";
    public static int Code4RequestUrlIsAbsent= 4;

    public static String Msg4RequestUrlIsAbsent = "The URL of requesting is absent.";
    public static int Code5RequestBodyIsAbsent=5;
    public static String Msg5RequestBodyIsAbsent="The request body is absent.";
    public static int Code6ResponseStatusWrong = 6;
    public static String Msg6ResponseStatusWrong = "The status of response is";
    public static int Code7DoPostRequestWrong  = 7;
    public static String Msg7DoPostRequestWrong = "Do post request wrong.";
    public static int Code8BadResponseSign = 8;
    public static String Msg8BadResponseSign = "The sign in header is not correct to the response body.";
    public static int Code9BadQuery=9;
    public static String Msg9BadQuery = "Bad FCDSL query for this API.";
}
