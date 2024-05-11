package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import feipClass.Cid;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.CidByIdsAPI)
public class CidByIds extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null){
            return;
        }

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler dataRequestHandler = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        Map<String, String> fidCidMap = new HashMap<>();

        List<Cid> meetList;

        try {
            meetList = dataRequestHandler.doRequest(IndicesNames.CID, null, Cid.class);
            if(meetList==null)return;

            //Make data
            for(Cid cid:meetList){
                String cidStr = cid.getCid();
                if(cidStr==null)cidStr="";
                fidCidMap.put(cid.getFid(),cidStr);
            }
        } catch (Exception e) {
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        if(meetList.size() ==0){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }

        replier.setData(fidCidMap);
        replier.setGot(fidCidMap.size());
        replier.setTotal((long) fidCidMap.size());

        dataRequestHandler.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        return requestBody.getFcdsl().getIds() != null;
    }
}
