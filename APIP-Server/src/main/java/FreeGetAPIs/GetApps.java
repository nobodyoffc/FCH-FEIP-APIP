package FreeGetAPIs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.ApiNames;
import constants.IndicesNames;
import data.ReplierForFree;
import feipClass.App;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.FreeGet + ApiNames.GetAppsAPI)
public class GetApps extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        ReplierForFree replier = new ReplierForFree();

        if (Initiator.isFreeGetForbidden(writer)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            replier.setOther();
            replier.setData("Error: FreeGet API is not active now.");
            writer.write(replier.toJson());
            return;
        }
        ElasticsearchClient esClient = Initiator.esClient;

        SearchResponse<App> result = esClient.search(s -> s.index(IndicesNames.APP)
                .query(q -> q.term(t -> t.field("active").value(true)))
                .size(20)
                .sort(so -> so.field(f -> f.field("tRate").order(SortOrder.Desc).field("tCdd").order(SortOrder.Desc)))
                , App.class);
        List<Hit<App>> hitList = result.hits().hits();
        if(hitList==null || hitList.size()==0){
            replier.setOther();
            replier.setData("App no found.");
            writer.write(replier.toJson());
            return;
        }
        List<App> foundList = new ArrayList<>();
        for(Hit<App> hit : hitList){
            foundList.add(hit.source());
        }

        assert result.hits().total() != null;
        replier.setTotal(result.hits().total().value());
        replier.setGot(foundList.size());
        replier.setSuccess();
        replier.setData(foundList);
        writer.write(replier.toJson());
    }
}