package SwapHall;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.google.gson.Gson;
import constants.*;
import feipClass.Service;
import initial.Initiator;
import javaTools.JsonTools;
import redis.clients.jedis.Jedis;
import swapClass.SwapParams;
import swapClass.SwapRegisterInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static constants.FieldNames.REGISTERED_SWAP;
import static constants.FieldNames.SID;
import static constants.Strings.DOT_JSON;
import static initial.Initiator.esClient;
import static initial.Initiator.serviceName;

@WebServlet(ApiNames.SwapHallPath + ApiNames.SwapRegisterAPI)
public class SwapRegister extends HttpServlet {

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

        Object swapRegisterRequestData = dataCheckResult.getDataRequestBody().getFcdsl().getOther();

        Gson gson = new Gson();
        Map<String,String> dataMap = JsonTools.getStringStringMap(gson.toJson(swapRegisterRequestData));

        if(dataMap == null || dataMap.get(SID)==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            replier.setData("The key of sid is no found in fcdsl.other.");
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String sid = dataMap.get(SID);
        String swapRegisterInfoJson;
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            swapRegisterInfoJson = jedis.hget(APIP_SWAP_SID_ADDR_KEY, sid);
        }
        if(swapRegisterInfoJson!=null) {
            replier.setData(sid + " had been registered" + " by " + addr);
            writer.write(replier.reply0Success(addr));
            return;
        }

        String index = IndicesNames.SERVICE;

        GetResponse<Service> result = esClient.get(g -> g.index(index).id(sid), Service.class);

        Service swapService = result.source();
        if(swapService==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
            replier.setData("Failed to get the swap service: "+sid);
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }

        if(swapService.isClosed()){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("The swap service "+sid+" is set to closed.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        if(!swapService.isActive()){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("The swap service "+sid+" is no longer active.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        ArrayList<String> swapRelatedAddrList = new ArrayList<>();
        swapRelatedAddrList.add(swapService.getOwner());
        if(swapService.getWaiters()!=null)
            swapRelatedAddrList.addAll(Arrays.asList(swapService.getWaiters()));
        try {
            String swapParamsJson = gson.toJson(swapService.getParams());
            SwapParams swapParams = gson.fromJson(swapParamsJson, SwapParams.class);
            if(swapParams.getgAddr()!=null)
                swapRelatedAddrList.add(swapParams.getgAddr());
            if(swapParams.getmAddr()!=null)
                swapRelatedAddrList.add(swapParams.getmAddr());
        }catch (Exception ignore){}
        boolean goodFid = false;
        for(String fid: swapRelatedAddrList){
            if(fid.equals(addr)){
                goodFid=true;
                break;
            }
        }
        if (!goodFid){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Requester "+addr+" is not the owner, waiter, or dealer of the swap service."+Arrays.toString(swapRelatedAddrList.toArray()));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        SwapRegisterInfo swapRegisterInfo = new SwapRegisterInfo();
        swapRegisterInfo.setSid(sid);
        swapRegisterInfo.setRegisterer(addr);
        swapRegisterInfo.setRegisterTime(System.currentTimeMillis());

        saveSwapIntoRedis(gson, sid, swapRegisterInfo);
        saveSwapIntoFile(swapRegisterInfo);

        replier.setData(sid+" registered"+" by "+addr);
        writer.write(replier.reply0Success(addr));
    }

    private static void saveSwapIntoFile(SwapRegisterInfo swapRegisterInfo) throws IOException {
        File file = new File(APIP_SWAP_SID_ADDR_KEY + DOT_JSON);
        if(!file.exists())file.createNewFile();
        List<SwapRegisterInfo> swapRegisterInfoList = JsonTools.readJsonObjectListFromFile(APIP_SWAP_SID_ADDR_KEY + DOT_JSON, SwapRegisterInfo.class);
        if(swapRegisterInfoList==null)swapRegisterInfoList=new ArrayList<>();
        swapRegisterInfoList.add(swapRegisterInfo);
        JsonTools.writeObjectListToJsonFile(swapRegisterInfoList,APIP_SWAP_SID_ADDR_KEY + DOT_JSON,false);
    }

    private static void saveSwapIntoRedis(Gson gson, String sid, SwapRegisterInfo swapRegisterInfo) {
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.hset(APIP_SWAP_SID_ADDR_KEY, sid, gson.toJson(swapRegisterInfo));
        }
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        return requestBody.getFcdsl().getOther() != null;
    }


}

