package FreeGetAPIs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.ApiNames;
import constants.IndicesNames;
import feipClass.Service;
import fcTools.ParseTools;
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

@WebServlet(ApiNames.FreeGet + ApiNames.GetServicesAPI)
public class GetServices extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        if (Initiator.isFreeGetForbidden(writer)) return;
        ElasticsearchClient esClient = Initiator.esClient;


        SearchResponse<Service> cashResult = esClient.search(s -> s.index(IndicesNames.SERVICE)
                .query(q -> q.term(t -> t.field("active").value(true)))
                .size(20)
                .sort(so -> so.field(f -> f.field("tRate").order(SortOrder.Desc).field("tCdd").order(SortOrder.Desc)))
                , Service.class);
        List<Hit<Service>> hitList = cashResult.hits().hits();
        if(hitList==null || hitList.size()==0){
            writer.write("Service no found.");
            return;
        }
        List<Service> foundList = new ArrayList<>();
        for(Hit<Service> hit : hitList){
            foundList.add(hit.source());
        }
        writer.write(ParseTools.gsonString(foundList));
    }
}