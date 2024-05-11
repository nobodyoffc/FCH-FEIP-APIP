package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.Replier;
import apipClass.FreecashInfo;
import constants.*;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.ChainInfoAPI)
public class ChainInfo extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write("The API is forbidden.");
            return;
        }

        String height = request.getParameter("height");

        FreecashInfo freecashInfo = new FreecashInfo();
        if(height==null) {
            freecashInfo.infoBest("http://"+ Initiator.configAPIP.getRpcIp() + ":" + Initiator.configAPIP.getRpcPort()
                    , Initiator.configAPIP.getRpcUser()
                    , Initiator.configAPIP.getRpcPassword());
            replier.setBestHeight(freecashInfo.getHeight());
        }else {
            freecashInfo.infoByHeight(Long.parseLong(height),Initiator.esClient);
            try(Jedis jedis = Initiator.jedisPool.getResource()) {
                replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
            }
        }

        replier.setGot(1);
        replier.setTotal(1L);
        replier.setData(freecashInfo);
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
