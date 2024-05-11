package APIP18V1_Wallet;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.UnconfirmedInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.*;
import initial.Initiator;
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

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
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
                info.setFid(id);
                info.setIncomeCount(0);
                info.setIncomeValue(0);
                info.setSpendCount(0);
                info.setSpendValue(0);
                info.setNet(0);
                meetList.add(info);
                continue;
            }
            UnconfirmedInfo info = new UnconfirmedInfo();
            info.setFid(id);

            if (resultMap.get(IncomeCount) != null) info.setIncomeCount(Integer.parseInt(resultMap.get(IncomeCount)));
            if (resultMap.get(IncomeValue) != null) info.setIncomeValue(Long.parseLong(resultMap.get(IncomeValue)));
            if (resultMap.get(SpendCount) != null) info.setSpendCount(Integer.parseInt(resultMap.get(SpendCount)));
            if (resultMap.get(SpendValue) != null) info.setSpendValue(Long.parseLong(resultMap.get(SpendValue)));
            if(resultMap.get(TxValueMap)!=null){
                Type mapType = new TypeToken<Map<String, Long>>(){}.getType();

                info.setTxValueMap(new Gson().fromJson(resultMap.get(TxValueMap),mapType));
            }
            info.setNet(info.getIncomeValue() -info.getSpendValue());
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
        replier.setTotal((long) meetList.size());

        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);

        writer.write(reply);

    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }
        String fid = request.getParameter("fid");
        if (fid==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2003IllegalFid));
            writer.write(replier.reply2003IllegalFid());
            return;
        }


        List<UnconfirmedInfo> meetList = new ArrayList<>();
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            jedis.select(3);
            Map<String, String> resultMap = null;
            try {
                resultMap = jedis.hgetAll(fid);
            } catch (Exception e) {
                UnconfirmedInfo info = new UnconfirmedInfo();
                info.setFid(fid);
                info.setIncomeCount(0);
                info.setIncomeValue(0);
                info.setSpendCount(0);
                info.setSpendValue(0);
                info.setNet(0);
                meetList.add(info);
            }
            UnconfirmedInfo info = new UnconfirmedInfo();
            info.setFid(fid);
            if (resultMap.get(IncomeCount) != null) info.setIncomeCount(Integer.parseInt(resultMap.get(IncomeCount)));
            if (resultMap.get(IncomeValue) != null) info.setIncomeValue(Long.parseLong(resultMap.get(IncomeValue)));
            if (resultMap.get(SpendCount) != null) info.setSpendCount(Integer.parseInt(resultMap.get(SpendCount)));
            if (resultMap.get(SpendValue) != null) info.setSpendValue(Long.parseLong(resultMap.get(SpendValue)));
            if (resultMap.get(TxValueMap) != null) {
                Type mapType = new TypeToken<Map<String, Long>>() {
                }.getType();

                info.setTxValueMap(new Gson().fromJson(resultMap.get(TxValueMap), mapType));
            }
            info.setNet(info.getIncomeValue() - info.getSpendValue());
            meetList.add(info);

            if (meetList.size() == 0) {
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1011DataNotFound));
                writer.write(replier.reply1011DataNotFound());
                return;
            }
            //response
            replier.setData(meetList);
            replier.setGot(meetList.size());
            replier.setTotal((long) meetList.size());
            jedis.select(0);
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success();
        if(reply==null)return;

        writer.write(reply);
    }
}
