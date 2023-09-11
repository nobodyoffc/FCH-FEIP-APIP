package APIP20V1_Webhook;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.DataRequestHandler;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.DataRequestBody;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.google.gson.Gson;
import constants.*;
import cryptoTools.SHA;
import apipClass.WebhookInfo;
import fcTools.ParseTools;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@WebServlet(ApiNames.APIP20V1Path + ApiNames.NewCashByFidsAPI)
public class NewCashByFids extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        /*
        监控：newCash，newblock，spentCash，changedCash，newOpReturn，nConfirm
        解析fcdsl.other中的fids。
        将request fid，endpoint与fids添加到redis，并加密备份到本地文件。
        确定一个扫描器，在feip或apipManager里，监控redis列表中的变化。两个大类：block文件更新或mempool
        运行一个handle
        运行一个sender
         */

        Replier replier = new Replier();
        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        if (RequestChecker.isPublicSessionKey(response, replier, writer, addr)) return;

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        Object fidsObj = dataCheckResult.getDataRequestBody().getFcdsl().getOther();

        Gson gson = new Gson();
        WebhookInfo webhookInfo;

        String hookId ;
        try {
            webhookInfo = gson.fromJson(gson.toJson(fidsObj), WebhookInfo.class);

            hookId = HexFormat.of().formatHex(SHA.Sha256x2(SHA.stringMerge2Utf8(webhookInfo.getOwner(), StandardCharsets.UTF_8,webhookInfo.getMethod(),StandardCharsets.UTF_8)));
            if(webhookInfo.getOwner()==null)webhookInfo.setOwner(addr);
            if("subscribe".equals(webhookInfo.getOp())) {
                saveSubscribe(webhookInfo);
            }else if("unsubscribe".equals(webhookInfo.getOp())){
                deleteWebhook(webhookInfo.getHookId());
            }else {
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("The op in request body is wrong.");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        replier.setData("Done. The hookId is: "+hookId);
        replier.setGot(1);
        replier.setTotal(1);

        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);

        writer.write(reply);
    }

    private void deleteWebhook(String hookId) {
        deleteWebhookFromRedis(hookId);
        deleteWebhookFromEs(hookId);
    }

    private void deleteWebhookFromRedis(String hookId) {
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.select(Constants.RedisDb4Webhook);
            jedis.del(Strings.HOOK_INFO_+hookId);
            jedis.del(hookId);
        }
    }

    private void saveSubscribe(WebhookInfo data) {
        addSubscribeToRedis(data);
        saveSubscribeToEs(data);
    }

    private void saveSubscribeToEs(WebhookInfo data) {
        try {
            IndexResponse result = Initiator.esClient.index(i -> i.index(IndicesNames.WEBHOOK).id(data.getHookId()).document(data));
            //TODO
            ParseTools.gsonPrint(result);
        } catch (IOException e) {
            System.out.println("ES client wrong.");
        }
    }

    private void deleteWebhookFromEs(String hookId) {
        try {
            DeleteResponse result = Initiator.esClient.delete(d->d.index(IndicesNames.WEBHOOK).id(hookId));
            //TODO
            ParseTools.gsonPrint(result);
        } catch (IOException e) {
            System.out.println("ES client wrong.");
        }
    }

    private void addSubscribeToRedis(WebhookInfo data) {
        Gson gson = new Gson();
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            String dataJson = gson.toJson(data);
            jedis.hset(data.getMethod(),data.getOwner(),dataJson);
        }
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getOther()==null)
            return false;
        return true;
    }
}
