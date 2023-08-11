package FreeGetAPIs;

import com.google.gson.Gson;
import constants.ApiNames;
import constants.Strings;
import fcTools.ParseTools;
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

@WebServlet(ApiNames.FreeGet + ApiNames.GetFreeServiceAPI)
public class GetFreeService  extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        ApipService service;
        try (Jedis jedis = Initiator.jedisPool.getResource()) {
            if (Initiator.isFreeGetForbidden(writer)) return;
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
            try {
                String key = jedis.hget(Initiator.serviceName+"_"+Strings.FID_SESSION_NAME, PUBLIC);
                jedis.select(1);
                publicSessionKey = jedis.hget(key, SESSION_KEY);
                data.put(SESSION_KEY, publicSessionKey);
                writer.write(ParseTools.gsonString(data));
            }catch (Exception e){
                data.put("Error", "Can't get free sessionKey.");
                writer.write(ParseTools.gsonString(data));
            }
        }
    }
}
