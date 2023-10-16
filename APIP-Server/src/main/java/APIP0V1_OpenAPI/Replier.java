package APIP0V1_OpenAPI;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import constants.Constants;
import constants.ReplyInfo;
import initial.Initiator;
import initial.ServerParamsInRedis;
import order.Order;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import constants.Strings;
import walletTools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private transient ServerParamsInRedis paramsInRedis;
    private transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String reply(String userAddr) {
        if(userAddr==null)
            return replyBase();
        return replyBase(userAddr);
    }

    public String reply() {
        return replyBase();
    }

    public String replyBase() {
        if(paramsInRedis!=null) bestHeight = paramsInRedis.getBestHeight();
        return gson.toJson(this);
    }

    public String replyBase(String userAddr) {

        String replyJson = gson.toJson(this);

        if(paramsInRedis==null){
            balance = 0;
            return gson.toJson(this);
        }
        long price = paramsInRedis.getPrice();
        balance = paramsInRedis.getBalance();
        bestHeight = paramsInRedis.getBestHeight();
        Integer nPrice = paramsInRedis.getnPrice();

        double cost = 0;
        if (paramsInRedis.isPricePerRequest()) {
            balance = balance - (nPrice * price);
        } else if (isPricePerRequest) {
            if(nPrice ==null) nPrice =0;
            cost = price * nPrice * (Math.ceil((double) replyJson.getBytes().length / 1024));
            balance = balance - (long) cost;
        }

        try(Jedis jedis0Common = Initiator.jedisPool.getResource()) {
            if (balance <= 0) {
                jedis0Common.hdel(Initiator.serviceName+"_"+Strings.FID_BALANCE, userAddr);
                jedis0Common.hdel(Initiator.serviceName+"_"+Strings.FID_SESSION_NAME, userAddr);
                code = ReplyInfo.Code1004InsufficientBalance;
                message = ReplyInfo.Msg1004InsufficientBalance;
                got = 0;
                Map<String, Object> d = new HashMap<>();

                d.put("currency", params.getCurrency());
                d.put("sendFrom", userAddr);
                d.put("sendTo", params.getAccount());
                d.put("minPayment", params.getMinPayment());
                String buyJson = gson.toJson(Order.getJsonBuyOrder(service.getSid()));
                d.put("writeInOpReturn", buyJson);
                d.put("note", "When writing OpReturn, remove the escape character!");

                long value = (long) (Double.parseDouble(params.getMinPayment()) * Constants.FchToSatoshi);

                CashListReturn cashListReturn = WalletTools.getCashListForPay(value, userAddr, esClient);

                if(cashListReturn.getCode()==0) {
                    DataForOffLineTx dataForOffLineTx = new DataForOffLineTx();
                    dataForOffLineTx.setMsg(buyJson);

                    List<SendTo> sendToList = new ArrayList<>();
                    SendTo sendTo1 = new SendTo();
                    sendTo1.setFid(params.getAccount());
                    sendTo1.setAmount(Double.parseDouble(params.getMinPayment()));
                    sendToList.add(sendTo1);
                    dataForOffLineTx.setSendToList(sendToList);

                    String offLingTxJson = CryptoSigner.makeRawTxForCs(dataForOffLineTx, cashListReturn.getCashList());
                    d.put(Strings.UNSIGNED_TX_FOR_CS, offLingTxJson);
                }else{
                    d.put(Strings.UNSIGNED_TX_FOR_CS,cashListReturn.getMsg());
                }

                data = d;
                last = null;
                balance = 0;
                return gson.toJson(this);
            } else {
                jedis0Common.hset(Initiator.serviceName+"_"+Strings.FID_BALANCE, userAddr, String.valueOf(balance));
                if (this.via != null) {
                    long viaT = ReadRedis.readHashLong(jedis0Common, Initiator.serviceName+"_"+Strings.CONSUME_VIA, this.via);
                    jedis0Common.hset(Initiator.serviceName+"_"+Strings.CONSUME_VIA, this.via, String.valueOf(viaT + (long) cost));
                }
            }
        }

        return gson.toJson(this);
    }

    public String reply0Success(String addr){
        code = ReplyInfo.Code0Success;
        message = ReplyInfo.Msg0Success;
        return reply(addr);
    }

    public String reply0Success(){
        code = ReplyInfo.Code0Success;
        message = ReplyInfo.Msg0Success;
        return reply();
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
    public String reply1016IllegalUrl() {
        code = ReplyInfo.Code1016IllegalUrl;
        message = ReplyInfo.Msg1016IllegalUrl;
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

    public String reply2001NoFreeGet (){
        code = ReplyInfo.Code2001NoFreeGet;
        message = ReplyInfo.Msg2001NoFreeGet;
        return reply();
    }
    public String reply2002CidNoFound (){
        code = ReplyInfo.Code2002CidNoFound;
        message = ReplyInfo.Msg2002CidNoFound;
        return reply();
    }
    public String reply2003IllegalFid (){
        code = ReplyInfo.Code2003IllegalFid;
        message = ReplyInfo.Msg2003IllegalFid;
        return reply();
    }

    public String reply2004RawTxNoHex (){
        code = ReplyInfo.Code2004RawTxNoHex;
        message = ReplyInfo.Msg2004RawTxNoHex;
        return reply();
    }

    public String reply2005SendTxFailed (){
        code = ReplyInfo.Code2005SendTxFailed;
        message = ReplyInfo.Msg2005SendTxFailed;
        return reply();
    }
    public String reply2006AppNoFound  (){
        code = ReplyInfo.Code2006AppNoFound ;
        message = ReplyInfo.Msg2006AppNoFound ;
        return reply();
    }
    public String reply2007CashNoFound  (){
        code = ReplyInfo.Code2007CashNoFound;
        message = ReplyInfo.Msg2007CashNoFound;
        return reply();
    }
    public String reply2008ServiceNoFound(){
        code = ReplyInfo.Code2008ServiceNoFound;
        message = ReplyInfo.Msg2008ServiceNoFound;
        return reply();
    }

    public String reply2009NoFreeSessionKey(){
        code = ReplyInfo.Code2009NoFreeSessionKey;
        message = ReplyInfo.Msg2009NoFreeSessionKey;
        return reply();
    }

    public String reply2010ErrorFromFchRpc(){
        code = ReplyInfo.Code2010ErrorFromFchRpc;
        message = ReplyInfo.Msg2010ErrorFromFchRpc;
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

    public ServerParamsInRedis getParamsInRedis() {
        return paramsInRedis;
    }

    public void setParamsInRedis(ServerParamsInRedis paramsInRedis) {
        this.paramsInRedis = paramsInRedis;
    }

    public void clean() {
//        code=0;
//        message=null;
        nonce=0;
        balance=0;
        got=0;
        total=0;
        bestHeight=0;
        data=null;
        via=null;
        last=null;
    }
}
