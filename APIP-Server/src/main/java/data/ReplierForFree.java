package data;

import com.google.gson.Gson;
import constants.ReplyInfo;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import static constants.Strings.BEST_HEIGHT;

public class ReplierForFree {
    private int code;
    private String message;
    private long bestHeight;
    private int got;
    private long total;
    private Object data;

    public int getGot() {
        return got;
    }

    public void setGot(int got) {
        this.got = got;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
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


    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setSuccess(String[] last) {
        setSuccess();
        if(last!=null) {
        }
    }
    public void setSuccess(){
        this.code = 0;
        this.message = ReplyInfo.Msg0Success;
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            try {
                this.bestHeight = Long.parseLong(jedis.get(BEST_HEIGHT));
            }catch (Exception ignore){
                this.bestHeight=0;
            }
        }
    }

    public void setOther() {
        this.code = 1020;
        this.message = ReplyInfo.Msg1020OtherError;
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            try {
                this.bestHeight = Long.parseLong(jedis.get(BEST_HEIGHT));
            }catch (Exception ignore){
                this.bestHeight=0;
            }
        }
    }
    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
