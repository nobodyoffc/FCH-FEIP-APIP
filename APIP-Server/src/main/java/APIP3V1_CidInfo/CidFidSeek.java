package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import esTools.Sort;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.FidCidSeekAPI)
public class CidFidSeek extends HttpServlet {

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

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        Map<String, String[]> cidAddrMap = new HashMap<>();

        List<Cid> meetList = null;

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("lastHeight",false,"fid",true,null,null);

        try {
            meetList = esRequest.doRequest(IndicesNames.CID, sort, Cid.class);
            if(meetList==null){
                return;
            }
            //Make data
            for(Cid cid:meetList){
                cidAddrMap.put(cid.getFid(),cid.getUsedCids());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        if(meetList.size() ==0){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }
        replier.setGot(cidAddrMap.size());
        replier.setData(cidAddrMap);
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl().getQuery().getPart()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getPart().getFields()==null)
            return false;
        return true;
    }
}
