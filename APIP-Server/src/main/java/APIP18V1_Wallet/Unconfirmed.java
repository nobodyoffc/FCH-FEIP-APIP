package APIP18V1_Wallet;

import APIP1V1_OpenAPI.*;
import fcTools.ParseTools;
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
import java.util.Map;

import static api.Constant.*;

@WebServlet(APIP18V1Path +UnconfirmedAPI)
public class Unconfirmed extends HttpServlet {

    private String SpendCount = "spendCount";
    private String SpendValue = "spendValue";
    private String IncomeCount = "incomeCount";
    private String IncomeValue = "incomeValue";

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

        List<UnconfirmedInfo> meetList = new ArrayList<>();

        for(String id: dataCheckResult.getDataRequestBody().getFcdsl().getIds()) {
            Map<String, String> resultMap = null;
            try {
                resultMap = Initiator.jedis3Mempool.hgetAll(id);

                if(resultMap == null||resultMap.size()==0){
                    response.setHeader(CodeInHeader, String.valueOf(Code1011DataNotFound));
                    writer.write(replier.reply1011DataNotFound(addr));
                    return;
                }
            }catch(Exception e){
                UnconfirmedInfo info = new UnconfirmedInfo();
                info.fid = id;
                info.incomeCount = 0;
                info.incomeValue = 0;
                info.spendCount = 0;
                info.spendValue = 0;
                info.net = 0;
                meetList.add(info);
                continue;
            }
            UnconfirmedInfo info = new UnconfirmedInfo();
            info.fid = id;
            if(resultMap.get(IncomeCount)!=null)info.incomeCount = Integer.parseInt(resultMap.get(IncomeCount));
            if(resultMap.get(IncomeValue)!=null)info.incomeValue = Long.parseLong(resultMap.get(IncomeValue));
            if(resultMap.get(SpendCount)!=null)info.spendCount = Integer.parseInt(resultMap.get(SpendCount));
            if(resultMap.get(SpendValue)!=null)info.spendValue = Long.parseLong(resultMap.get(SpendValue));
            info.net = info.incomeValue -info.spendValue;
            meetList.add(info);
        }
        if(meetList.size()==0){
            response.setHeader(CodeInHeader, String.valueOf(Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }
        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        replier.setTotal(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", UnconfirmedAPI));
        response.setHeader(CodeInHeader, String.valueOf(Code0Success));
        String reply = replier.reply0Success(addr,nPrice);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(SignInHeader,sign);

        writer.write(reply);

    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }

    private class UnconfirmedInfo {
        private String fid;
        private long net;
        private int spendCount;
        private long spendValue;
        private int incomeCount;
        private long incomeValue;
    }
}
