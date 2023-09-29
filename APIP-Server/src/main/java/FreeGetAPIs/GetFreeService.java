package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import constants.Strings;
import initial.Initiator;
import redis.clients.jedis.Jedis;
import service.ApipService;
import service.Params;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static constants.FieldNames.OWNER;
import static constants.Strings.*;

@WebServlet(ApiNames.FreeGetPath + ApiNames.GetFreeServiceAPI)
public class GetFreeService  extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        ApipService service;

        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }

        try (Jedis jedis = Initiator.jedisPool.getResource()) {

            service = new Gson().fromJson(jedis.get(Initiator.serviceName +"_"+ Strings.SERVICE), ApipService.class);
            Map<String, String> data = new HashMap<>();

            data.put(SID, service.getSid());
            data.put(SERVICE_NAME, service.getStdName());
            data.put(OWNER, service.getOwner());
            data.put(WAITER, service.getWaiters()[0]);
            Params params = service.getParams();

            if(params.getUrlHead()!=null)data.put(URL_HEAD, params.getUrlHead());
            if(params.getCurrency()!=null)data.put(CURRENCY, params.getCurrency());
            if(params.getAccount()!=null)data.put(ACCOUNT, params.getAccount());
            if(params.getMinPayment()!=null)data.put(MIN_PAYMENT, params.getMinPayment());
            if(params.getPricePerKBytes()!=null)data.put(PRICE_PER_K_BYTES, params.getPricePerKBytes());
            if(params.getPricePerRequest()!=null)data.put(PRICE_PER_REQUEST, params.getPricePerRequest());
            if(params.getConsumeViaShare()!=null)data.put(CONSUME_VIA_SHARE,params.getConsumeViaShare());
            if(params.getOrderViaShare()!=null)data.put(ORDER_VIA_SHARE,params.getOrderViaShare());

            String publicSessionKey;
            String key = jedis.hget(Initiator.serviceName+"_"+Strings.FID_SESSION_NAME, PUBLIC);
            jedis.select(1);
            publicSessionKey = jedis.hget(key, SESSION_KEY);
            data.put(SESSION_KEY, publicSessionKey);

            replier.setData(data);
            replier.setTotal(1);
            replier.setGot(1);
            jedis.select(0);
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));

            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
            writer.write(replier.reply0Success());

        } catch (Exception e){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2009NoFreeSessionKey));
            writer.write(replier.reply2009NoFreeSessionKey());
        }
    }
}
