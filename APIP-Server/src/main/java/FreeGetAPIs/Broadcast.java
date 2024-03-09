package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import constants.ApiNames;
import constants.ReplyInfo;
import constants.Strings;
import freecashRPC.FcRpcMethods;
import initial.Initiator;
import javaTools.BytesTools;
import redis.clients.jedis.Jedis;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static initial.Initiator.fcClient;

@WebServlet(ApiNames.FreeGetPath + ApiNames.BroadcastAPI)
public class Broadcast extends HttpServlet {


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String rawTxHex = request.getParameter("rawTx");
        rawTxHex=rawTxHex.toLowerCase();
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }

        if(!BytesTools.isHexString(rawTxHex)){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code2004RawTxNoHex));
            writer.write(replier.reply2004RawTxNoHex());
            return;
        }

        String result;
        try {
            result = FcRpcMethods.sendTx(fcClient,rawTxHex);
        } catch (Throwable e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code2005SendTxFailed));
            writer.write(replier.reply2005SendTxFailed());
            return;
        }
        if(result.contains("{")){
            replier.setData(result);
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code2010ErrorFromFchRpc));
            writer.write(replier.reply2010ErrorFromFchRpc());
            return;
        }

        if(result.startsWith("\""))result=result.substring(1);
        if(result.endsWith("\""))result=result.substring(0,result.length()-1);

        if(!BytesTools.isHexString(result)){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData(result);
            writer.write(replier.reply1020OtherError());
            return;
        }

        replier.setData(result);
        replier.setTotal(1);
        replier.setGot(1);
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
