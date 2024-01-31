package APIP0V1_OpenAPI;

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

@WebServlet(ApiNames.APIP0V1Path + ApiNames.GetServiceAPI)
public class GetService extends HttpServlet {
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

            replier.setData(service);
            replier.setTotal(1);
            replier.setGot(1);
            jedis.select(0);
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));

            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
            writer.write(replier.reply0Success());

        } catch (Exception e){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Jedis wrong when get service. Error:"+e.getMessage());
            writer.write(replier.reply1020OtherError());
        }
    }
}
