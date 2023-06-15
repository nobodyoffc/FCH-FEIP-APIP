package APIP0V1_OpenAPI;


import api.Constant;
import initial.Initiator;
import order.Order;
import redisTools.ReadRedis;
import service.Params;
import startAPIP.RedisKeys;

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
        bestHeight = redisTools.ReadRedis.readLong(jedis0Common, RedisKeys.BestHeight);

        return Initiator.gson.toJson(this);
    }

    public String replyBase(String userAddr, int nPrice) {

        String replyJson = Initiator.gson.toJson(this);

        balance = ReadRedis.readHashLong(jedis0Common, RedisKeys.Balance, userAddr);

        if(isPricePerKBytes){
            double cost = price * nPrice * (Math.ceil((double) replyJson.getBytes().length / 1024));
            balance =  balance - (long)cost;
        }else if(isPricePerRequest) {
            balance = balance - (nPrice * price);
        }

        if(balance<=0) {
            jedis0Common.hdel(RedisKeys.Balance, userAddr);
            jedis0Common.hdel(RedisKeys.AddrSessionName, userAddr);
            code = Constant.Code1004InsufficientBalance;
            message = Constant.Msg1004InsufficientBalance;
            got = 0;
            Map<String,Object> d = new HashMap<>();

            Params params = service.getParams();
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
            jedis0Common.hset(RedisKeys.Balance, userAddr, String.valueOf(balance));
        }

        bestHeight = redisTools.ReadRedis.readLong(jedis0Common, RedisKeys.BestHeight);

        return Initiator.gson.toJson(this);
    }

    public String reply0Success(String addr,int nPrice){
        code = Constant.Code0Success;
        message = Constant.Msg0Success;
        return reply(addr,nPrice);
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
}
