package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(ApiNames.FreeGetPath + ApiNames.GetTotalsAPI)
public class GetTotals extends HttpServlet {

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

        ElasticsearchClient esClient = Initiator.esClient;

        IndicesResponse result = esClient.cat().indices();
        List<IndicesRecord> indicesRecordList = result.valueBody();

        Map<String, String> docsCountInIndex = new HashMap<>();
        for(IndicesRecord record : indicesRecordList){
            if(record.index()==null||record.index().contains("_"))continue;
            docsCountInIndex.put(record.index(),record.docsCount());
        }
        replier.setTotal((long) docsCountInIndex.size());
        replier.setGot(docsCountInIndex.size());
        replier.setData(docsCountInIndex);
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}