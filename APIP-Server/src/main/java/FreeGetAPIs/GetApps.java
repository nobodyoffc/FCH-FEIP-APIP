package FreeGetAPIs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import FeipClass.App;
import fcTools.ParseTools;
import initial.Initiator;
import startFEIP.IndicesFEIP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static api.Constant.*;

@WebServlet(FreeGet + GetAppsAPI)
public class GetApps extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();

        if (!Initiator.isFreeGetAllowed(writer)) return;

        ElasticsearchClient esClient = Initiator.esClient;


        SearchResponse<App> cashResult = esClient.search(s -> s.index(IndicesFEIP.AppIndex)
                .query(q -> q.term(t -> t.field("active").value(true)))
                .size(20)
                .sort(so -> so.field(f -> f.field("tRate").order(SortOrder.Desc).field("tCdd").order(SortOrder.Desc)))
                , App.class);
        List<Hit<App>> hitList = cashResult.hits().hits();
        if(hitList==null || hitList.size()==0){
            writer.write("App no found.");
            return;
        }
        List<App> foundList = new ArrayList<>();
        for(Hit<App> hit : hitList){
            foundList.add(hit.source());
        }
        writer.write(ParseTools.gsonString(foundList));
    }
}