package APIP0V1_OpenAPI;

import apipClass.*;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import co.elastic.clients.json.JsonData;
import constants.Constants;
import constants.ReplyInfo;
import javaTools.BytesTools;
import cryptoTools.SHA;
import esTools.Sort;
import initial.Initiator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DataRequestHandler {
    private final Replier replier;
    private final HttpServletResponse response;
    private final ElasticsearchClient esClient;
    private final String addr;
    private final DataRequestBody dataRequestBody;
    private final PrintWriter writer;

    public DataRequestHandler(String addr, DataRequestBody dataRequestBody, HttpServletResponse response, Replier replier) throws IOException {
        this.dataRequestBody = dataRequestBody;
        this.addr = addr;
        this.esClient = Initiator.esClient;
        this.response = response;
        this.replier = replier;
        this.writer = response.getWriter();
    }

    public <T> List<T> doRequest(String index, ArrayList<Sort> sort, Class<T> tClass) {
        if(index==null||tClass==null)return null;

        SearchRequest.Builder builder = new SearchRequest.Builder();
        SearchRequest searchRequest;
        builder.index(index);

        Fcdsl fcdsl;

        if(dataRequestBody.getFcdsl()==null){
            MatchAllQuery matchAllQuery = getMatchAllQuery();
            builder.query(q->q.matchAll(matchAllQuery));
        }else{
            fcdsl = dataRequestBody.getFcdsl();

            if(fcdsl.getIds()!=null)
                return doIdsRequest(index,tClass);

            if(fcdsl.getQuery() == null && fcdsl.getExcept()==null && fcdsl.getFilter()==null){
                MatchAllQuery matchAllQuery = getMatchAllQuery();
                builder.query(q->q.matchAll(matchAllQuery));
            }else {
                List<Query> queryList = null;
                if(fcdsl.getQuery()!=null) {
                    apipClass.Query query = fcdsl.getQuery();
                    queryList = getQueryList(query);
                }
                
                List<Query> filterList = null;
                if(fcdsl.getFilter()!=null) {
                    Filter fcFilter = fcdsl.getFilter();
                    filterList = getQueryList(fcFilter);
                }
                
                List<Query> exceptList = null;
                if(fcdsl.getExcept()!=null) {
                    Except fcExcept = fcdsl.getExcept();
                    exceptList = getQueryList(fcExcept);
                }

                BoolQuery.Builder bBuilder = QueryBuilders.bool();
                if(queryList!=null && queryList.size()>0)
                    bBuilder.must(queryList);
                if(filterList!=null && filterList.size()>0)
                    bBuilder.filter(filterList);
                if(exceptList!=null && exceptList.size()>0)
                    bBuilder.mustNot(exceptList);

                builder.query(q -> q.bool(bBuilder.build()));
            }

            int size=0;
            try {
                if(fcdsl.getSize()!= null) {
                    size = Integer.parseInt(fcdsl.getSize());
                }
            }catch(Exception e){
                e.printStackTrace();
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
                writer.write(replier.reply1012BadQuery(addr));
                return null;
            }
            if(size==0 || size> Constants.MaxRequestSize) size= Constants.DefaultSize;
            builder.size(size);

            if(fcdsl.getSort()!=null) {
                sort = fcdsl.getSort();
            }
            if(sort!=null) {
                if (sort.size() > 0) {
                    builder.sort(Sort.getSortList(sort));
                }
            }
            if(fcdsl.getAfter()!=null){
                List<String>  after = fcdsl.getAfter();
                builder.searchAfter(after);
            }
        }

        TrackHits.Builder tb = new TrackHits.Builder();
        tb.enabled(true);
        builder.trackTotalHits(tb.build());
        searchRequest = builder.build();

        SearchResponse<T> result;
        try {
            result = esClient.search(searchRequest, tClass);
        }catch(Exception e){
            e.printStackTrace();
            this.response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return null;
        }

        if(result==null){
            this.response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return null;
        }

        assert result.hits().total() != null;
        replier.setTotal(result.hits().total().value());

        List<Hit<T>> hitList = result.hits().hits();
        if(hitList.size()==0){
            this.response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return null;
        }

        List<T> tList = new ArrayList<>();
        for(Hit<T> hit : hitList){
            tList.add(hit.source());
        }

        List<String> sortList = hitList.get(hitList.size()-1).sort();

        String[] last = new String[sortList.size()];
        last = sortList.toArray(last);
        if(last.length>0)
            replier.setLast(last);
        return tList;

    }

    private List<Query> getQueryList(apipClass.Query query) {
        BoolQuery termsQuery;
        BoolQuery partQuery;
        BoolQuery matchQuery;
        BoolQuery rangeQuery;
        BoolQuery existsQuery;
        BoolQuery unexistsQuery;
        BoolQuery equalsQuery;

        List<Query> queryList = new ArrayList<>();
        if(query.getTerms()!=null){
            termsQuery = getTermsQuery(query.getTerms());
            Query q = new Query.Builder().bool(termsQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(query.getPart()!=null){
            partQuery = getPartQuery(query.getPart());
            Query q = new Query.Builder().bool(partQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(query.getMatch()!=null){
            matchQuery = getMatchQuery(query.getMatch());
            Query q = new Query.Builder().bool(matchQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(query.getExists()!=null){
            existsQuery = getExistsQuery(query.getExists());
            Query q = new Query.Builder().bool(existsQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(query.getUnexists()!=null){
            unexistsQuery = getUnexistQuery(query.getUnexists());
            Query q = new Query.Builder().bool(unexistsQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(query.getEquals()!=null){
            equalsQuery = getEqualQuery(query.getEquals());
            Query q = new Query.Builder().bool(equalsQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(query.getRange()!=null){
            rangeQuery = getRangeQuery(query.getRange());
            Query q = new Query.Builder().bool(rangeQuery).build();
            if(q!=null)queryList.add(q);
        }

        if(queryList.size()==0){
            return null;
        }

        return queryList;
    }

    private <T> List<T> doIdsRequest(String index, Class<T> clazz) {

        ArrayList<String> idList = new ArrayList<>(Arrays.asList(dataRequestBody.getFcdsl().getIds()));
        if(idList.size()> Constants.MaxRequestSize) {
            this.response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1010TooMuchData));
            replier.setData(new HashMap<String, Integer>().put("maxSize", Constants.MaxRequestSize));
            writer.write(replier.reply1010TooMuchData(addr));
            return null;
        }

        MgetResponse<T> result;
        try {
            result = esClient.mget(m -> m.index(index).ids(idList), clazz);
        }catch(Exception e){
            this.response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return null;
        }
        List<MultiGetResponseItem<T>> items = result.docs();

        ListIterator<MultiGetResponseItem<T>> iter = items.listIterator();
        List<T> meetList = new ArrayList<>();
        while(iter.hasNext()) {
            MultiGetResponseItem<T> item = iter.next();
            if(item.result().found()) {
                meetList.add(item.result().source());
            }
        }

        if(meetList.size()==0) {
            this.response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return null;
        }else return meetList;
    }

    private BoolQuery getMatchQuery(Match match) {
        BoolQuery.Builder bBuilder = new BoolQuery.Builder();

        if(match.getValue()==null)return null;
        if(match.getFields()==null || match.getFields().length==0)return null;

        List<Query> queryList = new ArrayList<>();

        for(String field: match.getFields()){
            if(field.isBlank())continue;
            MatchQuery.Builder mBuilder = new MatchQuery.Builder();
            mBuilder.field(field);
            mBuilder.query(match.getValue());

            queryList.add(new Query.Builder().match(mBuilder.build()).build());
        }
        bBuilder.should(queryList);
        return bBuilder.build();
    }

    private BoolQuery getExistsQuery(String[] exists) {
        BoolQuery.Builder ebBuilder = new BoolQuery.Builder();
        List<Query> eQueryList = new ArrayList<>();
        for(String e: exists) {
            if(e.isBlank())continue;
            ExistsQuery.Builder eBuilder = new ExistsQuery.Builder();
            eBuilder.queryName("exists");
            eBuilder.field(e);
            eQueryList.add(new Query.Builder().exists(eBuilder.build()).build());
        }
        return ebBuilder.must(eQueryList).build();
    }

    private BoolQuery getUnexistQuery(String[] unexist) {
        BoolQuery.Builder ueBuilder = new BoolQuery.Builder();
        List<Query> queryList = new ArrayList<>();
        for(String e: unexist) {
            if(e.isBlank())continue;
            ExistsQuery.Builder eBuilder = new ExistsQuery.Builder();
            eBuilder.queryName("exist");
            eBuilder.field(e);
            queryList.add(new Query.Builder().exists(eBuilder.build()).build());
        }
        return ueBuilder.mustNot(queryList).build();
    }

    private BoolQuery getEqualQuery(Equals equals) {
        if(equals.getValues()==null|| equals.getFields()==null)return null;

        BoolQuery.Builder boolBuilder;

        List<FieldValue> valueList = new ArrayList<>();
        for(String str: equals.getValues()){
            if(str.isBlank())continue;
            if(str.contains(".")){
                try {
                    valueList.add(FieldValue.of(Double.parseDouble(str)));
                }catch(Exception e){
                    this.response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
                    writer.write(replier.reply1012BadQuery(addr));
                    return null;
                }
            }else{
                try {
                    valueList.add(FieldValue.of(Long.parseLong(str)));
                }catch(Exception e){
                                this.response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
                    return null;
                }
            }
        }

        boolBuilder = makeBoolShouldTermsQuery(equals.getFields(), valueList);
        boolBuilder.queryName("equal");

        return  boolBuilder.build();
    }

    private BoolQuery getRangeQuery(Range range) {
        if(range.getFields()==null)return null;

        String[] fields = range.getFields();

        if(fields.length==0)return null;

        BoolQuery.Builder bBuilder = new BoolQuery.Builder();
        bBuilder.queryName("range");

        List<Query> queryList = new ArrayList<>();

        for(String field : fields){
            if(field.isBlank())continue;
            RangeQuery.Builder rangeBuider = new RangeQuery.Builder();
            rangeBuider.field(field);

            int count = 0;
            if(range.getGt()!=null){
                rangeBuider.gt(JsonData.of(range.getGt()));
                count++;
            }
            if(range.getGte()!=null){
                rangeBuider.gte(JsonData.of(range.getGte()));
                count++;
            }
            if(range.getLt()!=null){
                rangeBuider.lt(JsonData.of(range.getLt()));
                count++;
            }
            if(range.getLte()!=null){
                rangeBuider.lte(JsonData.of(range.getLte()));
                count++;
            }
            if(count==0)return null;

            queryList.add(new Query.Builder().range(rangeBuider.build()).build());
        }

        bBuilder.must(queryList);
        return bBuilder.build();
    }

    private BoolQuery getPartQuery(Part part) {
        BoolQuery.Builder partBoolBuilder = new BoolQuery.Builder();
        boolean isCaseInSensitive;
        try{
           isCaseInSensitive = Boolean.parseBoolean(part.getIsCaseInsensitive());
        }catch(Exception e){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return null;
        }

        List<Query> queryList = new ArrayList<>();
        for(String field:part.getFields()){
            if(field.isBlank())continue;
            WildcardQuery wQuery = WildcardQuery.of(w -> w
                    .field(field)
                    .caseInsensitive(isCaseInSensitive)
                    .value("*"+part.getValue()+"*"));
            queryList.add(new Query.Builder().wildcard(wQuery).build());
        }
        partBoolBuilder.should(queryList);
        partBoolBuilder.queryName("part");
        return partBoolBuilder.build();
    }

    private BoolQuery getTermsQuery(Terms terms) {
        BoolQuery.Builder termsBoolBuider;

        List<FieldValue> valueList = new ArrayList<>();
        for(String value : terms.getValues()){
            if(value.isBlank())continue;
            valueList.add(FieldValue.of(value));
        }

        termsBoolBuider = makeBoolShouldTermsQuery(terms.getFields(),valueList);
        termsBoolBuider.queryName("terms");
        return termsBoolBuider.build();
    }

    private BoolQuery.Builder makeBoolShouldTermsQuery(String[] fields, List<FieldValue> valueList) {
        BoolQuery.Builder termsBoolBuider = new BoolQuery.Builder();

        List<Query> queryList = new ArrayList<>();
        for(String field:fields){
            TermsQuery tQuery = TermsQuery.of(t -> t
                    .field(field)
                    .terms(t1 -> t1
                            .value(valueList)
                    ));

            queryList.add(new Query.Builder().terms(tQuery).build());
        }
        termsBoolBuider.should(queryList);
        return termsBoolBuider;
    }

    private MatchAllQuery getMatchAllQuery() {
        MatchAllQuery.Builder queryBuilder = new MatchAllQuery.Builder();
        queryBuilder.queryName("all");
        return queryBuilder.build();
    }

//    public void writeSuccess(String sessionKey, int nPrice) {
//
//        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
//        String reply = replier.reply0Success(addr,nPrice);
//        if(reply==null)return;
//        String sign = symSign(reply,sessionKey);
//        if(sign==null)return;
//        response.setHeader(ReplyInfo.SignInHeader,sign);
//        writer.write(reply);
//    }

    public void writeSuccess(String sessionKey) {
        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = symSign(reply,sessionKey);
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);
        writer.write(reply);
    }

    public static String symSign(String replyJson,String sessionKey) {
        if(replyJson==null || sessionKey==null)return null;
        byte[] replyJsonBytes = replyJson.getBytes();
        byte[] keyBytes = BytesTools.hexToByteArray(sessionKey);
        byte[] bytes = BytesTools.bytesMerger(replyJsonBytes,keyBytes);
        byte[] signBytes = SHA.Sha256x2(bytes);
        return BytesTools.bytesToHexStringBE(signBytes);
    }
}
