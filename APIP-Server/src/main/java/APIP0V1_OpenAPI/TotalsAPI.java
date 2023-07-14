package APIP0V1_OpenAPI;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import constants.ApiNames;
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

@WebServlet(ApiNames.APIP0V1Path + ApiNames.TotalsAPI)
public class TotalsAPI extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();
        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        ElasticsearchClient esClient = Initiator.esClient;

        IndicesResponse result = esClient.cat().indices();
        List<IndicesRecord> indicesRecordList = result.valueBody();

        Map<String, String> allSumMap = new HashMap<>();
        for(IndicesRecord record : indicesRecordList){
            allSumMap.put(record.index(),record.docsCount());
        }

        //response
        replier.setData(allSumMap);
        replier.setGot(allSumMap.size());
        replier.setTotal(allSumMap.size());

        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", ApiNames.TotalsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }
}