package FreeGetAPIs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import constants.ApiNames;
import fcTools.ParseTools;
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

        if (Initiator.isFreeGetForbidden(writer)) return;
        ElasticsearchClient esClient = Initiator.esClient;

        IndicesResponse result = esClient.cat().indices();
        List<IndicesRecord> indicesRecordList = result.valueBody();

        Map<String, String> docsCountInIndex = new HashMap<>();
        for(IndicesRecord record : indicesRecordList){
            docsCountInIndex.put(record.index(),record.docsCount());
        }

        writer.write(ParseTools.gsonString(docsCountInIndex));
    }
}