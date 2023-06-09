package APIP1V1_OpenAPI;

import initial.RedisKeys;
import initial.StartWeb;
import redisTools.ReadRedis;

import static initial.StartWeb.*;

public class Replier {

    private int code;
    private String message;
    private long balance;
    private int total;
    private long bestHeight;
    private Object data;
    private String[] last;

    public String reply(int code, String message, Object data, String userAddr, int total) {
        Replier replier = new Replier();
        replier.setCode(code);
        replier.setMessage(message);
        if(data!=null) {
            replier.setData(data);
        }

        long balance = ReadRedis.readHashLong(jedis0,RedisKeys.Balance,userAddr);

        balance =  balance - price;

        if(balance<=0) {
            jedis0.hdel(RedisKeys.Balance, userAddr);
            jedis0.hdel(RedisKeys.AddrSessionName,userAddr);
        }else{
            jedis0.hset(RedisKeys.Balance,userAddr,String.valueOf(balance));
            replier.setBalance(balance);
        }

        bestHeight = redisTools.ReadRedis.readLong(jedis0,RedisKeys.BestHeight);
        replier.setBestHeight(bestHeight);
        replier.setGot(total);

        return StartWeb.gson.toJson(replier);
    }

    public String reply(int code, String message, Object data, String userAddr) {
        return reply(code, message, data, userAddr,0);
    }

    public String reply0Success(String addr){
        return reply(Constant.Code0Success, Constant.Msg0Success,data,addr,total);
    }

    public String reply1000MissSign(Object data,String addr){
        return reply(Constant.Code1000MissSign, Constant.Msg1000MissSign,data,addr);
    }

    public String reply1001MissPubKey(Object data,String addr){
        return reply(Constant.Code1001MissPubKey, Constant.Msg1001MissPubKey,data,addr);
    }

    public String reply1002MissSessionName (Object data,String addr){
        return reply(Constant.Code1002MissSessionName , Constant.Msg1002MissSessionName ,data,addr);
    }

    public String reply1003MissBody(Object data,String addr){
        return reply(Constant.Code1003MissBody, Constant.Msg1003MissBody,data,addr);
    }

    public String reply1004InsufficientBalance (Object data,String addr){
        return reply(Constant.Code1004InsufficientBalance , Constant.Msg1004InsufficientBalance ,data,addr);
    }

    public String reply1005UrlUnequal(Object data,String addr){
        return reply(Constant.Code1005UrlUnequal, Constant.Msg1005UrlUnequal,data,addr);
    }

    public String reply1006RequestTimeExpired(Object data,String addr){
        return reply(Constant.Code1006RequestTimeExpired, Constant.Msg1006RequestTimeExpired,data,addr);
    }

    public String reply1007UsedNonce(Object data,String addr){
        return reply(Constant.Code1007UsedNonce, Constant.Msg1007UsedNonce,data,addr);
    }

    public String reply1008BadSign(Object data,String addr){
        return reply(Constant.Code1008BadSign, Constant.Msg1008BadSign,data,addr);
    }

    public String reply1009SessionTimeExpired(Object data,String addr){
        return reply(Constant.Code1009SessionTimeExpired, Constant.Msg1009SessionTimeExpired,data,addr);
    }

    public String reply1010TooMuchData(Object data,String addr){
        return reply(Constant.Code1010TooMuchData, Constant.Msg1010TooMuchData,data,addr);
    }

    public String reply1011DataNotFound(Object data,String addr){
        return reply(Constant.Code1011DataNotFound, Constant.Msg1011DataNotFound,data,addr);
    }

    public String reply1012BadQuery(Object data,String addr){
        return reply(Constant.Code1012BadQuery, Constant.Msg1012BadQuery,data,addr);
    }

    public String reply1020OtherError (Object data,String addr){
        return reply(Constant.Code1020OtherError , Constant.Msg1020OtherError ,data,addr);
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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
}
