package SwapHall;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.ReplyInfo;
import esTools.EsTools;
import initial.Initiator;
import javaTools.JsonTools;
import redis.clients.jedis.Jedis;
import swapData.*;
import swapData.SwapPendingData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static constants.FieldNames.REGISTERED_SWAP;
import static constants.FieldNames.SID;
import static constants.IndicesNames.*;
import static constants.Strings.*;
import static initial.Initiator.esClient;
import static initial.Initiator.serviceName;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapUpdateAPI)
public class SwapUpdate extends HttpServlet {

    public static final String APIP_SWAP_SID_ADDR_KEY = serviceName + "_" + REGISTERED_SWAP;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Object uploadedMapObj = dataCheckResult.getDataRequestBody().getFcdsl().getOther();

        Gson gson = new Gson();
        Map<String,Object> dataMap = JsonTools.getStringObjectMap(gson.toJson(uploadedMapObj));

        JsonTools.gsonPrint(dataMap);

        if(dataMap.get(SID) == null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            replier.setData("The key of sid is no found in fcdsl.other.");
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        SwapStateData swapState = gson.fromJson(gson.toJson(dataMap.get(STATE)), SwapStateData.class);

        if(swapState==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("No swapState was found in the other filed of the FCDSL of the request body.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        String sid = swapState.getSid();
        String swapRegisterInfoJson;
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            swapRegisterInfoJson = jedis.hget(APIP_SWAP_SID_ADDR_KEY, sid);
        }
        if(swapRegisterInfoJson==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("The swap is not registered in "+ Initiator.service.getSid());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        SwapRegisterInfo swapRegisterInfo = gson.fromJson(swapRegisterInfoJson,SwapRegisterInfo.class);
        if(swapRegisterInfo==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Failed to get the swap register info of "+ swapState.getSid());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        if(!swapRegisterInfo.getRegisterer().equals(addr)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("The uploader has to be the swap registerer "+ swapRegisterInfo.getRegisterer());
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        List<String> resultList = new ArrayList<>();
        String stateResult = writeStateToEs(esClient,swapState);
        resultList.add(stateResult);

        if(dataMap.get(LP)!=null){
            SwapLpData lpMaps = gson.fromJson(gson.toJson(dataMap.get(LP)), SwapLpData.class);
            String result = writeLpToEs(esClient,lpMaps);
            resultList.add(result);
        }

        if(dataMap.get(PENDING)!=null){
            List<SwapAffair> pendingList = SwapDataGetter.getSwapAffairList(dataMap.get(PENDING));
            String result = writePendingToEs(esClient,pendingList,swapState.getSid());
            resultList.add(result);
        }

        if(dataMap.get(FINISHED)!=null){
            List<SwapAffair> finishedList = SwapDataGetter.getSwapAffairList(dataMap.get(FINISHED));
            String result = writeFinishedToEs(esClient,finishedList,swapState.getSid());
            resultList.add(result);
        }

        if(dataMap.get(PRICE)!=null){
            List<SwapPriceData> swapPriceList = SwapDataGetter.getSwapPriceList(dataMap.get(PRICE));
            String result = writePriceToEs(esClient,swapPriceList);
            resultList.add(result);
        }

        replier.setData(resultList);
        writer.write(replier.reply0Success(addr));
    }

    private String writePriceToEs(ElasticsearchClient esClient, List<SwapPriceData> swapPriceList) {
        if(swapPriceList==null||swapPriceList.isEmpty())return "No data.";
        ArrayList<String>idList =new ArrayList<>();
        for(SwapPriceData swapPriceData : swapPriceList){
            idList.add(swapPriceData.getId());
        }
        try {
            EsTools.bulkWriteList(esClient,SWAP_PRICE,(ArrayList<SwapPriceData>) swapPriceList,idList,SwapPriceData.class);
        } catch (Exception e) {
            return "Failed to write into ES: "+e.getMessage();
        }
        return "Saved swap price to ES.";
    }

    private String writePendingToEs(ElasticsearchClient esClient, List<SwapAffair> pendingList, String sid) {

        SwapPendingData swapPending = new SwapPendingData();
        swapPending.setSid(sid);
        swapPending.setPendingList(pendingList);
        try {
            IndexResponse result = esClient.index(i -> i.index(SWAP_PENDING).id(sid).document(swapPending));
        } catch (IOException e) {
            return "Failed to write swap pending into ES.";
        }
        return "Swap pending was updated to ES.";
    }

    private String writeFinishedToEs(ElasticsearchClient esClient, List<SwapAffair> finishedList, String sid) {
        if(finishedList==null||finishedList.isEmpty())return "No data.";
        ArrayList<String>idList =new ArrayList<>();
        for(SwapAffair swapAffair : finishedList){
            swapAffair.setSid(sid);
            idList.add(swapAffair.getId());
        }
        try {
            EsTools.bulkWriteList(esClient,SWAP_FINISHED,(ArrayList<SwapAffair>) finishedList,idList,SwapAffair.class);
        } catch (Exception e) {
            return "Failed to write into ES: "+e.getMessage();
        }
        return "Saved swap finished to ES.";
    }

    private String writeLpToEs(ElasticsearchClient esClient, SwapLpData lpMaps) {
        if(lpMaps==null||lpMaps.getSid()==null)return "No data.";
        try {
            esClient.index(i -> i.index(SWAP_LP).id(lpMaps.getSid()).document(lpMaps));
        } catch (IOException e) {
            return "Failed to write swap lpMaps into ES.";
        }
        return "Saved swap lpMaps to ES.";
    }

    private String writeStateToEs(ElasticsearchClient esClient, SwapStateData swapState) {
        if(swapState==null||swapState.getSid()==null)return "No data.";
        try {
            esClient.index(i -> i.index(SWAP_STATE).id(swapState.getSid()).document(swapState));
        } catch (IOException e) {
            return "Failed to write swap state into ES.";
        }
        return "Saved swap state to ES.";
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        return requestBody.getFcdsl().getOther() != null;
    }
}

