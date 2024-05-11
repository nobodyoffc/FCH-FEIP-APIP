package SwapHall;

import APIP0V1_OpenAPI.Replier;
import co.elastic.clients.elasticsearch.core.GetResponse;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import constants.Strings;
import initial.Initiator;
import javaTools.BytesTools;
import redis.clients.jedis.Jedis;
import swapClass.SwapAffair;
import swapClass.SwapPendingData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static initial.Initiator.esClient;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapPendingAPI)
public class SwapPending extends HttpServlet {

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
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("SID is required.");
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
            GetResponse<SwapPendingData> response1 = esClient.get(b -> b
                    .index(IndicesNames.SWAP_PENDING)
                    .id(finalSid)
            , SwapPendingData.class);

            if (response1.found()) {
                SwapPendingData swapPending = response1.source();
                if(swapPending==null){
                    Replier.replyNoData(response,writer,replier);
                    return;
                }
                List<SwapAffair> swapPendingList = swapPending.getPendingList();
                if(swapPendingList==null||swapPendingList.isEmpty()){
                    Replier.replyNoData(response,writer,replier);
                    return;
                }
                replier.setData(swapPendingList);
                replier.setTotal((long) swapPendingList.size());
                replier.setGot(swapPendingList.size());
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
                writer.write(replier.reply0Success());
            } else {
                Replier.replyNoData(response,writer,replier);
            }
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            replier.setData(e.fillInStackTrace());
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError());
        }
    }
}
