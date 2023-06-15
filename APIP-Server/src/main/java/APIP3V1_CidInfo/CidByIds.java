package APIP3V1_CidInfo;

import APIP1V1_OpenAPI.*;
import FeipClass.Cid;
import fc_dsl.Sort;
import initial.Initiator;
import startFEIP.IndicesFEIP;

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

import static api.Constant.*;

@WebServlet(APIP3V1Path + CidByIdsAPI)
public class CidByIds extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        if(!isThisApiRequest(requestBody)){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        Map<String, String> fidCidMap = new HashMap<>();

        List<Cid> meetList;

        try {
            meetList = esRequest.doRequest(IndicesFEIP.CidIndex, null, Cid.class);
            if(meetList==null){
                return;
            }
            //Make data
            for(Cid cid:meetList){
                String cidStr = cid.getCid();
                if(cidStr==null)cidStr="";
                fidCidMap.put(cid.getFid(),cidStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        if(meetList.size() ==0){
            response.setHeader(CodeInHeader,String.valueOf(Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }

        replier.setData(fidCidMap);
        replier.setGot(fidCidMap.size());
        replier.setTotal(fidCidMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", CidByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(),nPrice);
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        return requestBody.getFcdsl().getIds() != null;
    }
}
