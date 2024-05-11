package CryptoKeep;

import APIP0V1_OpenAPI.Replier;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import constants.ApiNames;
import constants.ReplyInfo;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static initial.Initiator.*;

@WebServlet(ApiNames.CryptoKeepPath + ApiNames.MadeCryptoKeepAPI)
public class MadeCryptoKeep extends HttpServlet {


    public static final String CRYPTO_KEEP_MADE_KEY = "crypto_keep_made_key";
    public static final String KEY = "key";
    public static final String CRYPTO_KEEP = "crypto_keep";
    public static final String CRYPTO_KEEP_HISTORY = "crypto_keep_history";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String sn = request.getParameter("sn");
        String chipId = request.getParameter("chipId");
        String version = request.getParameter("version");
        String key = request.getHeader(KEY);
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(sn==null || chipId==null || version==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("The SN, chipId and version are required.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        try(Jedis jedis = jedisPool.getResource()){
            if(!key.equals(jedis.get(CRYPTO_KEEP_MADE_KEY))){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Wrong key.");
                writer.write(replier.reply1020OtherError());
                return;
            }
        }

        CryptoKeep cryptoKeep = new CryptoKeep(sn,chipId,version,System.currentTimeMillis());

        IndexResponse result = esClient.index(i -> i.index(CRYPTO_KEEP).id(sn).document(cryptoKeep));

        if(result.result()!=Result.Created){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Failed to write made info into ES. SN:"+sn+" It may be exited.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        CryptoKeepHist cryptoKeepHist = new CryptoKeepHist(sn,"made",System.currentTimeMillis());
        cryptoKeepHist.setChipId(chipId);
        cryptoKeepHist.setVersion(version);
        result = esClient.index(i -> i.index(CRYPTO_KEEP_HISTORY).id(sn+"_"+System.currentTimeMillis()).document(cryptoKeepHist));
        if(result.result()!=Result.Created){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Failed to write op history into ES.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        replier.setData("Done.");
        replier.setTotal(1L);
        replier.setGot(1);

        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
