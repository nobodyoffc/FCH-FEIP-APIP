package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.Sort;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.ApiNames;
import constants.FieldNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Address;
import feipClass.Cid;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.IndicesNames.ADDRESS;
import static constants.IndicesNames.CID;
import static constants.Strings.FID;
import static initial.Initiator.esClient;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.FidCidSeekAPI)
public class FidCidSeek extends HttpServlet {

    @Override
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

        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);


        Map<String, String[]> addrCidsMap = new HashMap<>();
        String value;
        try {
            value = requestBody.getFcdsl().getQuery().getPart().getValue();//requestCheckResult.getRequestBody().getFcdsl().getQuery().getPart().getValue();
        }catch (Exception ignore){
            value = request.getParameter("part");
        }

        String finalValue = value;
        SearchResponse<Address> result = esClient.search(s -> s.index(ADDRESS).query(q -> q.wildcard(w -> w.field(FID)
                .caseInsensitive(true)
                .value("*" + finalValue + "*"))), Address.class);

        if (result.hits().hits().size() > 0) {
            for (Hit<Address> hit : result.hits().hits()) {
                Address address = hit.source();
                addrCidsMap.put(address.getFid(),new String[0]);
            }
        }

        SearchResponse<Cid> result1 = esClient.search(s -> s.index(CID).query(q -> q.wildcard(w -> w.field(FieldNames.USED_CIDS)
                .caseInsensitive(true)
                .value("*" + finalValue + "*"))), Cid.class);
        if (result1.hits().hits().size() > 0) {
            for (Hit<Cid> hit : result1.hits().hits()) {
                Cid cid = hit.source();
                addrCidsMap.put(cid.getFid(), cid.getUsedCids());
            }
        }
        if(addrCidsMap.size() ==0){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }
//        Map<String, String[]> cidAddrMap = new HashMap<>();
//        List<Cid> meetList = null;
//
//        //Set default sort.
//        ArrayList<Sort> sort = Sort.makeSortList("lastHeight",false,"fid",true,null,null);
//
//        try {
//            meetList = esRequest.doRequest(IndicesNames.CID, sort, Cid.class);
//            if(meetList==null){
//                return;
//            }
//            //Make data
//            for(Cid cid:meetList){
//                cidAddrMap.put(cid.getFid(),cid.getUsedCids());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
//            writer.write(replier.reply1012BadQuery(addr));
//            return;
//        }
//
//        if(meetList.size() ==0){
//            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
//            writer.write(replier.reply1011DataNotFound(addr));
//            return;
//        }


        replier.setGot(addrCidsMap.size());
        replier.setData(addrCidsMap);
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl().getQuery().getPart()==null)
            return false;
        return true;
    }
}
