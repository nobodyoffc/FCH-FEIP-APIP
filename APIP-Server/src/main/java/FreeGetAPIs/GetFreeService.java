package FreeGetAPIs;

import com.google.gson.Gson;
import constants.ApiNames;
import constants.Strings;
import fcTools.ParseTools;
import initial.Initiator;
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
        if (Initiator.isFreeGetForbidden(writer)) return;

        ApipService service = new Gson().fromJson(Initiator.jedis0Common.get(Initiator.serviceName+ Strings.SERVICE),ApipService.class);

        Map<String,String> data = new HashMap<>();
        data.put(SID,service.getSid());
        data.put(SERVICE_NAME,service.getStdName());
        data.put(OWNER,service.getOwner());
        Params params = service.getParams();
        data.put(URL_HEAD,params.getUrlHead());
        data.put(CURRENCY,params.getCurrency());
        data.put(ACCOUNT,params.getAccount());
        data.put(WAITER,service.getWaiters()[0]);
        data.put(MIN_PAYMENT,params.getMinPayment());
        data.put(PRICE_PER_K_BYTES,params.getPricePerKBytes());
        data.put(PRICE_PER_REQUEST,params.getPricePerRequest());
        String publicSessionKey = Initiator.jedis1Session.hget(Initiator.jedis0Common.hget(FID_SESSION_NAME,PUBLIC),SESSION_KEY);
        data.put(SESSION_KEY,publicSessionKey);

        writer.write(ParseTools.gsonString(data));
    }
}
