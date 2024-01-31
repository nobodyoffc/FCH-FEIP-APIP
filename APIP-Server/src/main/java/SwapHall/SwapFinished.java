package SwapHall;

import APIP0V1_OpenAPI.Replier;
import apipClass.Sort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.ApiNames;
import constants.FieldNames;
import constants.ReplyInfo;
import constants.Strings;
import esTools.EsTools;
import initial.Initiator;
import redis.clients.jedis.Jedis;
import swapData.SwapAffair;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static constants.FieldNames.*;
import static constants.IndicesNames.SWAP_FINISHED;
import static constants.Strings.ASC;
import static constants.Strings.DESC;
import static initial.Initiator.esClient;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapFinishedAPI)
public class SwapFinished extends HttpServlet {

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

        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }

        String sid = request.getParameter(SID);
        if(sid==null){
            Replier.replyOtherError(response,writer,replier,"SID is required.");
            return;
        }
        String lastStr = request.getParameter(FieldNames.LAST);

        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();

        List<SortOptions> sortOptionsList = Sort.makeTwoFieldsSort(FieldNames.GET_TIME,DESC,FieldNames.ID,ASC);

        searchBuilder.index(SWAP_FINISHED);
        searchBuilder.sort(sortOptionsList);
        searchBuilder.size(20);
        if(lastStr!=null) {
            String[] last = lastStr.split(",");
            searchBuilder.searchAfter(Arrays.asList(last));
        }

        Query query = EsTools.getTermsQuery(SID,sid.toLowerCase());

        searchBuilder.query(query);
        SearchRequest searchRequest = searchBuilder.build();
        SearchResponse<SwapAffair> result = esClient.search(searchRequest, SwapAffair.class);

        if(result==null||result.hits().total()==null){
            Replier.replyOtherError(response,writer,replier,"Searching ES wrong.");
            return;
        }

        String[] last = result.hits().hits().get(result.hits().hits().size() - 1).sort().toArray(new String[0]);
        long total = result.hits().total().value();
        List<Hit<SwapAffair>> hitList = result.hits().hits();
        List<SwapAffair> swapAffairList = new ArrayList<>();
        for(Hit<SwapAffair> hit : hitList){
            swapAffairList.add(hit.source());
        }

        if(swapAffairList.isEmpty()){
            Replier.replyNoData(response, writer, replier);
            return;
        }

        replier.setData(swapAffairList);
        replier.setTotal(total);
        replier.setLast(last);
        replier.setGot(swapAffairList.size());
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
