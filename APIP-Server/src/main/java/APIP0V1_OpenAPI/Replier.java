package APIP0V1_OpenAPI;


import constants.ReplyInfo;
import initial.Initiator;
import order.Order;
import redisTools.ReadRedis;
import service.ApipService;
import service.Params;
import constants.Strings;

import java.util.HashMap;
import java.util.Map;

import static initial.Initiator.*;

public class Replier {

    private int code;
    private String message;
    private long nonce;
    private long balance;
    private int got;
    private long total;
    private long bestHeight;
    private Object data;
    private String via;



    private String[] last;

    public String reply(String userAddr,int nPrice) {
        return replyBase(userAddr,nPrice);
    }

    public String reply(String userAddr) {
        return replyBase(userAddr,1);
    }

    public String reply() {
        return replyBase();
    }

    public String replyBase() {
        bestHeight = redisTools.ReadRedis.readLong(jedis0Common, Strings.BEST_HEIGHT);

        return Initiator.gson.toJson(this);
    }

    public String replyBase(String userAddr, int nPrice) {

        String replyJson = Initiator.gson.toJson(this);
        long price = readPrice();

        balance = ReadRedis.readHashLong(jedis0Common, Strings.USER, userAddr);
        double cost = 0;
        if(isPricePerKBytes){
            cost = price * nPrice * (Math.ceil((double) replyJson.getBytes().length / 1024));
            balance =  balance - (long)cost;
        }else if(isPricePerRequest) {
            balance = balance - (nPrice * price);
        }

        if(balance<=0) {
            jedis0Common.hdel(Strings.USER, userAddr);
            jedis0Common.hdel(Strings.FID_SESSION_NAME, userAddr);
            code = ReplyInfo.Code1004InsufficientBalance;
            message = ReplyInfo.Msg1004InsufficientBalance;
            got = 0;
            Map<String,Object> d = new HashMap<>();

            String serviceStr = jedis0Common.get(Initiator.serviceName+Strings.SERVICE);
            if(serviceStr==null){
                data= "Can't read service from Redis. Set it with ApipManager.jar.";
                return Initiator.gson.toJson(this);
            }
            ApipService service1 = gson.fromJson(serviceStr, ApipService.class);
            Params params = service1.getParams();

            d.put("currency",params.getCurrency());
            d.put("sendFrom",userAddr);
            d.put("sendTo",params.getAccount());
            d.put("minimumPay",params.getMinPayment());
            d.put("writeInOpReturn",gson.toJson(Order.getJsonBuyOrder(service.getSid())));
            d.put("note","When writing OpReturn, remove the escape character!");

            data = d;
            last = null;
            balance = 0;
            return Initiator.gson.toJson(this);
        }else{
            jedis0Common.hset(Strings.USER, userAddr, String.valueOf(balance));
            if(this.via!=null){
                long viaT = ReadRedis.readHashLong(jedis0Common, Strings.CONSUME_VIA, this.via);
                jedis0Common.hset(Strings.CONSUME_VIA, this.via, String.valueOf(viaT+(long)cost));
            }
        }

        bestHeight = redisTools.ReadRedis.readLong(jedis0Common, Strings.BEST_HEIGHT);

        return Initiator.gson.toJson(this);
    }

    public String reply0Success(String addr,int nPrice){
        code = ReplyInfo.Code0Success;
        message = ReplyInfo.Msg0Success;
        return reply(addr,nPrice);
    }

    public String reply0Success(String addr){
        code = ReplyInfo.Code0Success;
        message = ReplyInfo.Msg0Success;
        return reply(addr);
    }

    public String reply1000MissSign(){
        code = ReplyInfo.Code1000SignMissed;
        message = ReplyInfo.Msg1000SignMissed;
        return reply();
    }

    public String reply1001MissPubKey(){
        code = ReplyInfo.Code1001PubKeyMissed;
        message = ReplyInfo.Msg1001PubKeyMissed;
        return reply();
    }

    public String reply1002MissSessionName (){
        code = ReplyInfo.Code1002SessionNameMissed;
        message = ReplyInfo.Msg1002SessionNameMissed;
        return reply();
    }

    public String reply1003MissBody(String addr){
        code = ReplyInfo.Code1003BodyMissed;
        message = ReplyInfo.Msg1003BodyMissed;
        return reply(addr);
    }

    public String reply1004InsufficientBalance (String addr){
        code = ReplyInfo.Code1004InsufficientBalance;
        message = ReplyInfo.Msg1004InsufficientBalance;
        return reply(addr);
    }

    public String reply1005UrlUnequal(String addr){
        code = ReplyInfo.Code1005UrlUnequal;
        message = ReplyInfo.Msg1005UrlUnequal;
        return reply(addr);
    }

    public String reply1006RequestTimeExpired(String addr){
        code = ReplyInfo.Code1006RequestTimeExpired;
        message = ReplyInfo.Msg1006RequestTimeExpired;
        return reply(addr);
    }

    public String reply1007UsedNonce(String addr){
        code = ReplyInfo.Code1007UsedNonce;
        message = ReplyInfo.Msg1007UsedNonce;
        return reply(addr);
    }

    public String reply1008BadSign(String addr){
        code = ReplyInfo.Code1008BadSign;
        message = ReplyInfo.Msg1008BadSign;
        return reply(addr);
    }

    public String reply1009SessionTimeExpired(){
        code = ReplyInfo.Code1009SessionTimeExpired;
        message = ReplyInfo.Msg1009SessionTimeExpired;
        return reply();
    }

    public String reply1010TooMuchData(String addr){
        code = ReplyInfo.Code1010TooMuchData;
        message = ReplyInfo.Msg1010TooMuchData;
        return reply(addr);
    }

    public String reply1011DataNotFound(String addr){
      code = ReplyInfo.Code1011DataNotFound;
        message = ReplyInfo.Msg1011DataNotFound;
        return reply(addr);
    }

    public String reply1012BadQuery(String addr){
        code = ReplyInfo.Code1012BadQuery;
        message = ReplyInfo.Msg1012BadQuery;
        return reply(addr);
    }

    public String reply1013BadRequst(String addr) {
        code = ReplyInfo.Code1013BadRequest;
        message = ReplyInfo.Msg1013BadRequest;
        return reply(addr);
    }
    public String reply1014ApiSuspended(String addr) {
        code = ReplyInfo.Code1014ApiSuspended;
        message = ReplyInfo.Msg1014ApiSuspended;
        return reply(addr);
    }

    public String reply1015FidMissed() {
        code = ReplyInfo.Code1015FidMissed;
        message = ReplyInfo.Msg1015FidMissed;
        return reply();
    }
    public String reply1020OtherError (String addr){
        code = ReplyInfo.Code1020OtherError;
        message = ReplyInfo.Msg1020OtherError;
        return reply(addr);
    }

    public String reply1020OtherError (){
        code = ReplyInfo.Code1020OtherError;
        message = ReplyInfo.Msg1020OtherError;
        return reply();
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

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getGot() {
        return got;
    }

    public void setGot(int got) {
        this.got = got;
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public String[] getLast() {
        return last;
    }

    public void setLast(String[] last) {
        this.last = last;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }
}
