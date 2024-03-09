package SwapHall;

import APIP0V1_OpenAPI.Replier;
import apipClass.Sort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import constants.Strings;
import esTools.EsTools;
import feipClass.Service;
import initial.Initiator;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import swapData.SwapInfoData;
import swapData.SwapParams;
import swapData.SwapStateData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static constants.FieldNames.LAST;
import static constants.FieldNames.SID;
import static constants.IndicesNames.SERVICE;
import static constants.IndicesNames.SWAP_STATE;
import static constants.Strings.ASC;
import static fcTools.ParseTools.roundDouble8;
import static initial.Initiator.esClient;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapInfoAPI)
public class SwapInfo extends HttpServlet {

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

        String sidStr = request.getParameter(SID);

        if(sidStr!=null){
            String[] sids = sidStr.split(",");
            EsTools.MgetResult<SwapStateData> mgetResult = null;
            try {
                mgetResult = EsTools.getMultiByIdList(esClient, SWAP_STATE, Arrays.asList(sids), SwapStateData.class);
            } catch (Exception e) {
                Replier.replyOtherError(response,writer,replier,"Failed to mGet from ES.");
                return;
            }
            if(mgetResult.getResultList().isEmpty()){
                Replier.replyNoData(response,writer,replier);
                return;
            }
            List<SwapStateData> swapStateList = mgetResult.getResultList();
            List<SwapInfoData> swapInfoList = makeSwapInfoList(swapStateList, response, writer, replier);
            if (swapInfoList == null || swapInfoList.isEmpty()) {
                Replier.replyNoData(response,writer,replier);
                return;
            }

            replySuccess(response, writer, replier, swapInfoList, swapInfoList.size(), swapInfoList.size(), null);
            return;
        }

        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
        searchBuilder.index(SWAP_STATE);

        Sort sort = new Sort();
        sort.setField(SID);
        sort.setOrder(ASC);

        ArrayList<Sort> sortList = new ArrayList<>();
        sortList.add(sort);
        List<SortOptions> sortOptionsList = Sort.getSortList(sortList);

        searchBuilder.size(20);
        searchBuilder.sort(sortOptionsList);
        String lastStr = request.getParameter(LAST);
        if(lastStr!=null){
            String[] last = lastStr.split(",");
            searchBuilder.searchAfter(Arrays.asList(last));
        }

        SearchResponse<SwapStateData> result = esClient.search(searchBuilder.build(), SwapStateData.class);
        long total = 0;
        if(result.hits().total()!=null) total = result.hits().total().value();
        if(result.hits().total()==null || total==0){
            Replier.replyNoData(response,writer,replier);
            return;
        }
        String[] last = result.hits().hits().get(result.hits().hits().size() - 1).sort().toArray(new String[0]);

        List<SwapStateData> swapStateList = new ArrayList<>();
        for(Hit<SwapStateData> hit : result.hits().hits()){
            if(hit.source()==null)continue;
            swapStateList.add(hit.source());
        }

        List<SwapInfoData> swapInfoList = makeSwapInfoList(swapStateList, response, writer, replier);
        if (swapInfoList == null || swapInfoList.isEmpty()) {
            Replier.replyNoData(response,writer,replier);
            return;
        }

        replySuccess(response, writer, replier, swapInfoList, total, swapInfoList.size(), last);
    }

    @Nullable
    private List<SwapInfoData> makeSwapInfoList(List<SwapStateData> swapStateList, HttpServletResponse response, PrintWriter writer, Replier replier) {
        Gson gson = new Gson();
        List<String> sidList = new ArrayList<>();
        Map<String, SwapStateData> swapStateMap = new HashMap<>();

        for(SwapStateData swapState : swapStateList){
            sidList.add(swapState.getSid());
            swapStateMap.put(swapState.getSid(),swapState);
        }

        EsTools.MgetResult<Service> mgetResult1 = null;
        try {
            mgetResult1 = EsTools.getMultiByIdList(esClient, SERVICE, sidList, Service.class);
        } catch (Exception e) {
            Replier.replyOtherError(response, writer, replier,"Failed to mGet from ES.");
            return null;
        }

        if(mgetResult1.getResultList().isEmpty()){
            Replier.replyNoData(response, writer, replier);
            return null;
        }

        List<SwapInfoData> swapInfoList = new ArrayList<>();

        List<Service> swapServiceList = mgetResult1.getResultList();

        for(Service service:swapServiceList){
            if(service.isActive()){
                SwapParams swapParams = gson.fromJson(gson.toJson(service.getParams()),SwapParams.class);
                if(swapParams!=null){
                    service.setParams(swapParams);
                    SwapInfoData swapInfo = makeSwapInfo(swapStateMap.get(service.getSid()),service,swapParams);
                    swapInfoList.add(swapInfo);
                }
            }
        }
        if(swapInfoList.isEmpty()){
            Replier.replyNoData(response, writer, replier);
            return null;
        }
        return swapInfoList;
    }

    private static void replySuccess(HttpServletResponse response, PrintWriter writer, Replier replier, Object data, long total, int count, String[] last) {
        replier.setData(data);
        replier.setLast(last);
        replier.setTotal(total);
        replier.setGot(count);
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }

    private SwapInfoData makeSwapInfo(SwapStateData swapState, Service service, SwapParams swapParams) {
        SwapInfoData swapInfoData = new SwapInfoData();

        swapInfoData.setSid(swapState.getSid());

        swapInfoData.setName(service.getStdName());
        swapInfoData.setOwner(service.getOwner());
        swapInfoData.settRate(String.valueOf(service.gettRate()));
        swapInfoData.settCdd(String.valueOf(service.gettCdd()));

        swapInfoData.setgTick(swapParams.getgTick());
        swapInfoData.setmTick(swapParams.getmTick());
        swapInfoData.setgAddr(swapParams.getgAddr());
        swapInfoData.setmAddr(swapParams.getmAddr());
        swapInfoData.setgConfirm(swapParams.getgConfirm());
        swapInfoData.setmConfirm(swapParams.getmConfirm());
        swapInfoData.setSwapFee(swapParams.getSwapFee());
        swapInfoData.setServiceFee(swapParams.getServiceFee());
        swapInfoData.setgWithdrawFee(swapParams.getgWithdrawFee());
        swapInfoData.setmWithdrawFee(swapParams.getmWithdrawFee());

        swapInfoData.setgSum(swapState.getgSum());
        swapInfoData.setmSum(swapState.getmSum());
        swapInfoData.setgPendingSum(swapState.getgPendingSum());
        swapInfoData.setmPendingSum(swapState.getmPendingSum());

        double dM = 1-Double.parseDouble(swapInfoData.getSwapFee())-Double.parseDouble(swapInfoData.getServiceFee());
        double dG = ammCalculator(swapState.getmSum() + swapState.getmPendingSum(), swapState.getgSum() + swapState.getgPendingSum(), dM);
        double price = roundDouble8(1/ dG);

        swapInfoData.setPrice(price);
        swapInfoData.setLastTime(swapState.getLastTime());

        return swapInfoData;
    }

    public static double ammCalculator(double x, double y, double dX){
        return (y*dX)/(x+dX);
    }
}
