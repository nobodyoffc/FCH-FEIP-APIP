package SwapHall;

import APIP0V1_OpenAPI.Replier;
import apipClass.Sort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import constants.ApiNames;
import constants.FieldNames;
import constants.ReplyInfo;
import constants.Strings;
import esTools.EsTools;
import initial.Initiator;
import redis.clients.jedis.Jedis;
import swapData.SwapPriceData;

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
import static constants.IndicesNames.SWAP_PRICE;
import static constants.Strings.ASC;
import static constants.Strings.DESC;
import static initial.Initiator.esClient;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapPriceAPI)
public class SwapPrices extends HttpServlet {

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
        String gTick = request.getParameter(G_TICK);
        String mTick = request.getParameter(M_TICK);
        String lastStr = request.getParameter(FieldNames.LAST);
        String startTime = request.getParameter(START_TIME);
        String endTime = request.getParameter(END_TIME);

        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();

        List<SortOptions> sortOptionsList = Sort.makeTwoFieldsSort(FieldNames.TIME,DESC,FieldNames.SID,ASC);

        searchBuilder.index(SWAP_PRICE);
        searchBuilder.sort(sortOptionsList);
        searchBuilder.size(20);
        if(lastStr!=null) {
            String[] last = lastStr.split(",");
            searchBuilder.searchAfter(Arrays.asList(last));
        }

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        List<Query> queryList=new ArrayList<>();
        if(sid!=null) {
            Query query = EsTools.getTermsQuery(SID,sid.toLowerCase());
            queryList.add(query);
        }else {
            if (gTick != null) {
                Query query = EsTools.getTermsQuery(G_TICK, gTick.toLowerCase());
                queryList.add(query);
            }
            if (mTick != null) {
                Query query = EsTools.getTermsQuery(M_TICK, mTick.toLowerCase());
                queryList.add(query);
            }
        }

        if(startTime!=null||endTime!=null){
            RangeQuery.Builder rqb = new RangeQuery.Builder();
            if(startTime!=null)
                rqb.gte(JsonData.of(startTime));
            if(endTime!=null)
                rqb.lt(JsonData.of(endTime));
            Query query = new Query.Builder().range(rqb.build()).build();
            queryList.add(query);
        }

        BoolQuery boolQuery = boolBuilder.must(queryList).build();

        Query query = new Query(boolQuery);
        searchBuilder.query(query);
        SearchRequest searchRequest = searchBuilder.build();
        SearchResponse<SwapPriceData> result = esClient.search(searchRequest, SwapPriceData.class);

        long total=0;
        if(result!=null && result.hits().total()!=null)
            total=result.hits().total().value();
        if(total==0){
            Replier.replyNoData(response,writer,replier);
            return;
        }

        String[] last = result.hits().hits().get(result.hits().hits().size() - 1).sort().toArray(new String[0]);

        List<Hit<SwapPriceData>> hitList = result.hits().hits();
        List<SwapPriceData> swapPriceList = new ArrayList<>();
        for(Hit<SwapPriceData> hit : hitList){
            swapPriceList.add(hit.source());
        }

        if(swapPriceList.isEmpty()){
            Replier.replyNoData(response, writer, replier);
            return;
        }

        replier.setData(swapPriceList);
        replier.setTotal(total);
        replier.setLast(last);
        replier.setGot(swapPriceList.size());
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }

}
