package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.FreeGetPath + ApiNames.GetServicesAPI)
public class GetServices extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String sid = request.getParameter("id");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }
        ElasticsearchClient esClient = Initiator.esClient;
        SearchResponse<Service> result;
        List<Hit<Service>> hitList;
        if(sid!=null){
            result = esClient.search(s -> s.index(IndicesNames.SERVICE)
                            .query(q -> q.term(t -> t.field("sid").value(sid)))
                    , Service.class);
            hitList = result.hits().hits();
        }else {
            result = esClient.search(s -> s.index(IndicesNames.SERVICE)
                            .query(q -> q.term(t -> t.field("active").value(true)))
                            .size(20)
                            .sort(so -> so.field(f -> f.field("tRate").order(SortOrder.Desc).field("tCdd").order(SortOrder.Desc)))
                    , Service.class);
            hitList = result.hits().hits();
        }

        if (hitList == null || hitList.size() == 0) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code2008ServiceNoFound));
            writer.write(replier.reply2008ServiceNoFound());
            return;
        }

        List<Service> foundList = new ArrayList<>();
        for(Hit<Service> hit : hitList){
            foundList.add(hit.source());
        }

        assert result.hits().total() != null;
        replier.setTotal(result.hits().total().value());
        replier.setGot(foundList.size());
        replier.setData(foundList);
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}