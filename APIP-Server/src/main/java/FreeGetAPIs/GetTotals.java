package FreeGetAPIs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import constants.ApiNames;
import data.ReplierForFree;
import initial.Initiator;

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

@WebServlet(ApiNames.FreeGet + ApiNames.GetTotalsAPI)
public class GetTotals extends HttpServlet {

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

        IndicesResponse result = esClient.cat().indices();
        List<IndicesRecord> indicesRecordList = result.valueBody();

        Map<String, String> docsCountInIndex = new HashMap<>();
        for(IndicesRecord record : indicesRecordList){
            if(record.index()==null||record.index().contains("_"))continue;
            docsCountInIndex.put(record.index(),record.docsCount());
        }
        replier.setSuccess();
        replier.setTotal(docsCountInIndex.size());
        replier.setGot(docsCountInIndex.size());
        replier.setData(docsCountInIndex);
        writer.write(replier.toJson());
    }
}