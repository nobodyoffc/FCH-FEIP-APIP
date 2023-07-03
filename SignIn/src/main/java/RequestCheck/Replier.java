package RequestCheck;


import Tools.ReadRedis;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

public class Replier {

    private static Jedis jedis0Common = new Jedis();
    private static Jedis jedis1 = new Jedis();
    static {
        jedis1.select(1);
    }
    private static Jedis jedis2 = new Jedis();
    static {
        jedis2.select(2);
    }
    Gson gson = new Gson();
    private int code;
    private String message;
    private long nonce;
    private long bestHeight;
    private Object data;

    public String reply(String userAddr) {
        return reply();
    }

    public String reply() {
        bestHeight = ReadRedis.readLong(jedis0Common, "bestHeight");
        return gson.toJson(this);
    }

    public String reply0Success(String addr){
        code = Constant.Code0Success;
        message = Constant.Msg0Success;
        return reply(addr);
    }

    public String reply1000MissSign(){
        code = Constant.Code1000MissSign;
        message = Constant.Msg1000MissSign;
        return reply();
    }

    public String reply1001MissPubKey(){
        code = Constant.Code1001MissPubKey;
        message = Constant.Msg1001MissPubKey;
        return reply();
    }

    public String reply1002MissSessionName (){
        code = Constant.Code1002MissSessionName;
        message = Constant.Msg1002MissSessionName;
        return reply();
    }

    public String reply1003MissBody(String addr){
        code = Constant.Code1003MissBody;
        message = Constant.Msg1003MissBody;
        return reply(addr);
    }

    public String reply1004InsufficientBalance (String addr){
        code = Constant.Code1004InsufficientBalance;
        message = Constant.Msg1004InsufficientBalance;
        return reply(addr);
    }

    public String reply1005UrlUnequal(String addr){
        code = Constant.Code1005UrlUnequal;
        message = Constant.Msg1005UrlUnequal;
        return reply(addr);
    }

    public String reply1006RequestTimeExpired(String addr){
        code = Constant.Code1006RequestTimeExpired;
        message = Constant.Msg1006RequestTimeExpired;
        return reply(addr);
    }

    public String reply1007UsedNonce(String addr){
        code = Constant.Code1007UsedNonce;
        message = Constant.Msg1007UsedNonce;
        return reply(addr);
    }

    public String reply1008BadSign(String addr){
        code = Constant.Code1008BadSign;
        message = Constant.Msg1008BadSign;
        return reply(addr);
    }

    public String reply1009SessionTimeExpired(){
        code = Constant.Code1009SessionTimeExpired;
        message = Constant.Msg1009SessionTimeExpired;
        return reply();
    }

    public String reply1010TooMuchData(String addr){
        code = Constant.Code1010TooMuchData;
        message = Constant.Msg1010TooMuchData;
        return reply(addr);
    }

    public String reply1011DataNotFound(String addr){
      code = Constant.Code1011DataNotFound;
        message = Constant.Msg1011DataNotFound;
        return reply(addr);
    }

    public String reply1012BadQuery(String addr){
        code = Constant.Code1012BadQuery;
        message = Constant.Msg1012BadQuery;
        return reply(addr);
    }

    public String reply1013BadRequst(String addr) {
        code = Constant.Code1013BadRequest;
        message = Constant.Msg1013BadRequest;
        return reply(addr);
    }
    public String reply1014ApiSuspended(String addr) {
        code = Constant.Code1014ApiSuspended;
        message = Constant.Msg1014ApiSuspended;
        return reply(addr);
    }
    public String reply1020OtherError (String addr){
        code = Constant.Code1020OtherError;
        message = Constant.Msg1020OtherError;
        return reply(addr);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }


    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
