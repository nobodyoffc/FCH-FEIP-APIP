package APIP0V1_OpenAPI;

import co.elastic.clients.elasticsearch.core.GetResponse;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import constants.Strings;
import feipClass.Service;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static constants.Strings.*;

@WebServlet(ApiNames.APIP0V1Path + ApiNames.GetServiceAPI)
public class GetService extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        Replier replier = new Replier();

        String sid = request.getParameter("sid");
        if(sid!=null){
            GetResponse<? extends String> result = Initiator.esClient.get(g -> g.index(IndicesNames.SERVICE).id(sid), SERVICE.getClass());
            if(!result.found()){
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2007CashNoFound));
                writer.write(replier.reply2007CashNoFound());
                return;
            }
            replier.setData(result.source());
            replier.setTotal(1L);
            replier.setGot(1);
            try (Jedis jedis = Initiator.jedisPool.getResource()) {
                replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
            } catch (Exception e){
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Jedis wrong when get service. Error:"+e.getMessage());
                writer.write(replier.reply1020OtherError());
                return;
            }
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
            writer.write(replier.reply0Success());
            return;
        }

        try (Jedis jedis = Initiator.jedisPool.getResource()) {

            GetResponse<Service> result = Initiator.esClient.get(g -> g.index(SERVICE).id(Initiator.service.getSid()), Service.class);
            if(!result.found()){
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2008ServiceNoFound));
                writer.write(replier.reply2008ServiceNoFound());
                return;
            }
            replier.setData(result.source());
            replier.setTotal(1L);
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
