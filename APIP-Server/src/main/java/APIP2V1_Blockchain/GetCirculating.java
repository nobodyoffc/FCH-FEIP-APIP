package APIP2V1_Blockchain;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregate;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import constants.ApiNames;
import constants.FieldNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fcTools.ParseTools;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.GetCirculatingAPI)
public class GetCirculating extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write("The API is forbidden.");
            return;
        }

        try {// Create a SearchRequest targeting the 'cash' index
            SearchRequest.Builder sb = new SearchRequest.Builder();
            sb.index(IndicesNames.ADDRESS);
            sb.size(0);
            sb.trackTotalHits(tr->tr.enabled(true));

            sb.aggregations("sum", a -> a.sum(s -> s.field(FieldNames.BALANCE)));
            SearchResponse<Void> result = Initiator.esClient.search(sb.build(), void.class);

            Aggregate aggregate = result.aggregations().get("sum");

            SumAggregate sumAggregate = aggregate.sum();
            double sum = ParseTools.roundDouble8(sumAggregate.value()/100000000);
            writer.write(String.format("%.8f", sum));
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("Error");
        }
    }
}
