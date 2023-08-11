package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Address;
import data.CidInfo;
import feipClass.Cid;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import esTools.Sort;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.CidInfoSearchAPI)
public class CidInfoSearch extends HttpServlet {

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

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("lastHeight",false,"fid",true,null,null);

        //Request

        String index = IndicesNames.CID;
        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(), requestBody, response, replier);
        List<Cid> meetCidList;
        List<CidInfo> cidInfoList;
        try {
            meetCidList = esRequest.doRequest(index,sort, Cid.class);
            if(meetCidList==null){
                return;
            }
            //make addrList
            List<String> idList = new ArrayList<>();
            for(Cid cid : meetCidList) idList.add(cid.getFid());

            MgetResponse<Address> result = Initiator.esClient.mget(m -> m.index(IndicesNames.ADDRESS).ids(idList), Address.class);
            ArrayList<Address> addrList = new ArrayList<>();
            for(MultiGetResponseItem<Address> item :result.docs()){
                addrList.add(item.result().source());
            }
            cidInfoList = CidInfo.mergeCidInfoList(meetCidList,addrList);
            if(cidInfoList.size() == 0){
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
                writer.write(replier.reply1011DataNotFound(addr));
                return;
            }
            for(CidInfo cidInfo : cidInfoList){
                cidInfo.setTrxAddr(null);
                cidInfo.setDogeAddr(null);
                cidInfo.setLtcAddr(null);
                cidInfo.reCalcWeight();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response
        replier.setData(cidInfoList);
        replier.setGot(cidInfoList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }
}