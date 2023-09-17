package initial;

import constants.Constants;
import redis.clients.jedis.Jedis;

import static constants.Strings.*;

public class ServerParamsInRedis {
    private long windowTime;
    private long price;
    private long balance;
    private int nPrice;
    private boolean isPricePerRequest;
    private long bestHeight;

    public ServerParamsInRedis(String fid, String apiName) {
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            try {
                this.windowTime = Long.parseLong(jedis.hget(CONFIG, WINDOW_TIME));
            }catch (Exception ignore){
                this.windowTime=0;
            }

            try {
                this.price = (long)(Double.parseDouble(jedis.hget(CONFIG,PRICE))*Constants.FchToSatoshi);
            }catch (Exception e){
                e.printStackTrace();
                this.price=0;
            }

            try {
                this.nPrice = Integer.parseInt(jedis.hget(Initiator.serviceName+"_"+N_PRICE,apiName));
            }catch (Exception e){
                e.printStackTrace();
                this.nPrice=0;
            }

            try {
                this.isPricePerRequest = Boolean.parseBoolean(jedis.hget(CONFIG,IS_PRICE_PER_REQUEST));
            }catch (Exception ignore){
                this.isPricePerRequest= false;
            }

            try {
                this.bestHeight = Long.parseLong(jedis.get(BEST_HEIGHT));
            }catch (Exception ignore){
                this.bestHeight=0;
            }

            if(fid==null){
                this.balance = 0;
            }else{
                try {
                    this.balance = Long.parseLong(jedis.hget(Initiator.serviceName+"_"+ FID_BALANCE,fid));
                }catch (Exception ignore){
                    this.balance=0;
                }
            }
        }
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public long getWindowTime() {
        return windowTime;
    }

    public void setWindowTime(long windowTime) {
        this.windowTime = windowTime;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getnPrice() {
        return nPrice;
    }

    public void setnPrice(int nPrice) {
        this.nPrice = nPrice;
    }

    public boolean isPricePerRequest() {
        return isPricePerRequest;
    }

    public void setPricePerRequest(boolean pricePerRequest) {
        isPricePerRequest = pricePerRequest;
    }
}
