package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import esTools.EsTools;
import fchClass.Address;
import apipClass.CidInfo;
import feipClass.Cid;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.CidInfoByIdsAPI)
public class CidInfoByIds extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Cid> meetCidList;
        List<Address> meetAddrList;

        try {
            meetAddrList = esRequest.doRequest(IndicesNames.ADDRESS, null,Address.class);
            if(meetAddrList==null) return;


            EsTools.MgetResult<Cid> multiResult = EsTools.getMultiByIdList(Initiator.esClient, IndicesNames.CID, Arrays.asList(requestBody.getFcdsl().getIds()), Cid.class);
            meetCidList = multiResult.getResultList();
            //            meetCidList = esRequest.doRequest(IndicesNames.CID,null, Cid.class);
        } catch (Exception e) {
//            e.printStackTrace();
//            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
//            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        List<CidInfo> cidInfoList = CidInfo.mergeCidInfoList(meetCidList,meetAddrList);

        if(cidInfoList==null || cidInfoList.size() == 0){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }
        for(CidInfo cidInfo : cidInfoList){
            cidInfo.reCalcWeight();
        }

        Map<String,CidInfo> meetMap = new HashMap<>();
        for(CidInfo cidInfo :cidInfoList){
            meetMap.put(cidInfo.getFid(),cidInfo);
        }
        replier.setGot(meetMap.size());
        replier.setTotal((long) meetMap.size());
        replier.setData(meetMap);
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        return requestBody.getFcdsl().getIds() != null;
    }
}
