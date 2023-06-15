package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import startFCH.IndicesFCH;
import writeEs.P2SH;

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

import static api.Constant.*;

@WebServlet(APIP2V1Path + P2shByIdsAPI)
public class P2shByIds extends HttpServlet {

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
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Request
        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<P2SH> meetList;
        try {
            meetList = esRequest.doRequest(IndicesFCH.P2SHIndex, null, P2SH.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Map<String,P2SH> meetMap = new HashMap<>();
        for(P2SH p2sh :meetList){
            meetMap.put(p2sh.getFid(),p2sh);
        }

        //response
        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        int nPrice = Integer.parseInt(initial.Initiator.jedis0Common.hget("nPrice", P2shByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(),nPrice);
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }
}
