package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import apipClass.DataRequestBody;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;
import constants.ReplyInfo;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(ApiNames.APIP18V1Path + ApiNames.UnconfirmedAPI)
public class Unconfirmed extends HttpServlet {

    private String SpendCount = "spendCount";
    private String SpendValue = "spendValue";
    private String IncomeCount = "incomeCount";
    private String IncomeValue = "incomeValue";
    private String TxValueMap = "txValueMap";

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
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        List<UnconfirmedInfo> meetList = new ArrayList<>();

        Jedis jedis = new Jedis();
        jedis.select(3);
        for(String id: dataCheckResult.getDataRequestBody().getFcdsl().getIds()) {
            Map<String, String> resultMap = null;
            try {
                resultMap = jedis.hgetAll(id);
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
            if(resultMap.get(TxValueMap)!=null){
                Type mapType = new TypeToken<Map<String, Long>>(){}.getType();

                info.txValueMap = new Gson().fromJson(resultMap.get(TxValueMap),mapType);
            }
            info.net = info.incomeValue -info.spendValue;
            meetList.add(info);
        }
        jedis.close();
        if(meetList.size()==0){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }
        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        replier.setTotal(meetList.size());

        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);

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
        private Map<String,Long> txValueMap;
    }
}
