package SwapHall;

import APIP0V1_OpenAPI.Replier;
import co.elastic.clients.elasticsearch.core.GetResponse;
import constants.ApiNames;
import constants.ReplyInfo;
import constants.Strings;
import initial.Initiator;
import javaTools.BytesTools;
import redis.clients.jedis.Jedis;
import swapClass.SwapStateData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static constants.IndicesNames.SWAP_STATE;
import static initial.Initiator.esClient;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapStateAPI)
public class SwapStatus extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }

        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }

        String sid = request.getParameter(Strings.SID);
        if(sid==null){
            replier.setData("SID is required.");
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError());
            return;
        }
        sid=sid.toLowerCase();

        if(!BytesTools.isHexString(sid)||sid.length()!=64){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("It's not a SID.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        String finalSid = sid;

        try {
            GetResponse<SwapStateData> response1 = esClient.get(b -> b
                    .index(SWAP_STATE)
                    .id(finalSid)
            , SwapStateData.class);

            if (response1.found()) {
                SwapStateData swapState = response1.source();
                replier.setData(swapState);
                replier.setTotal(1L);
                replier.setGot(1);
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
                writer.write(replier.reply0Success());
            } else {
                // Handle the case where the document doesn't exist
                replier.setTotal(0L);
                replier.setGot(0);
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
                writer.write(replier.reply1011DataNotFound());
            }
        } catch (Exception e) {
            // Handle exceptions
            replier.setData(e.getMessage());
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError());
        }
    }
}
