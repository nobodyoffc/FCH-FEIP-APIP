package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.Replier;
import apipClass.FreecashInfo;
import constants.ApiNames;
import constants.ReplyInfo;
import constants.Strings;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.BlockTimeHistoryAPI)
public class BlockTimeHistory extends HttpServlet {
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

        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        String countStr = request.getParameter("count");
        long startTime = 0;
        long endTime = 0;
        int count = 0;

        if(startTimeStr!=null)startTime=Long.parseLong(startTimeStr);
        if(endTimeStr!=null)endTime=Long.parseLong(endTimeStr);
        if(countStr!=null)count=Integer.parseInt(countStr);

        if (Replier.checkAndResponseOtherError(count > FreecashInfo.MAX_REQUEST_COUNT, response, replier, "The count can not be bigger than " + FreecashInfo.MAX_REQUEST_COUNT, writer))
            return;

        Map<Long, Long> hist = FreecashInfo.blockTimeHistory(startTime, endTime, count, Initiator.esClient);

        if (Replier.checkAndResponseOtherError(hist == null, response, replier, "Failed to get block time history.", writer)) return;

        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        replier.setGot(hist.size());
        replier.setTotal(replier.getBestHeight()-1);
        replier.setData(hist);
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
