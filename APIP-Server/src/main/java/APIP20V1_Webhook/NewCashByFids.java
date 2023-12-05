package APIP20V1_Webhook;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import apipClass.WebhookRequestBody;
import com.google.gson.Gson;
import constants.*;
import cryptoTools.SHA;
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
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@WebServlet(ApiNames.APIP20V1Path + ApiNames.NewCashByFidsAPI)
public class NewCashByFids extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        Replier replier = new Replier();
        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        if (RequestChecker.isPublicSessionKey(response, replier, writer, addr)) return;

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        Object webhookRequestData = dataCheckResult.getDataRequestBody().getFcdsl().getOther();

        Gson gson = new Gson();
        WebhookRequestBody webhookRequestBody;

        String hookUserId ;
        try {
            webhookRequestBody = gson.fromJson(gson.toJson(webhookRequestData), WebhookRequestBody.class);

            webhookRequestBody.setUserName(addr);
            webhookRequestBody.setMethod(ApiNames.NewCashByFidsAPI);
            hookUserId = HexFormat.of().formatHex(SHA.Sha256x2(SHA.stringMergeToBytes(webhookRequestBody.getUserName(), StandardCharsets.UTF_8,webhookRequestBody.getMethod(),StandardCharsets.UTF_8)));
            webhookRequestBody.setHookUserId(hookUserId);
            Map<String,String> dataMap = new HashMap<>();
            switch (webhookRequestBody.getOp()) {
                case Strings.SUBSCRIBE -> {
                    saveSubscribe(webhookRequestBody);
                    dataMap.put(Strings.OP, Strings.SUBSCRIBE);
                    dataMap.put(Strings.HOOK_USER_ID, hookUserId);
                    replier.setData(dataMap);
                }
                case Strings.UNSUBSCRIBE -> {
                    deleteWebhook(webhookRequestBody);
                    dataMap.put(Strings.OP, Strings.UNSUBSCRIBE);
                    dataMap.put(Strings.HOOK_USER_ID, hookUserId);
                    replier.setData(dataMap);
                }
                case Strings.CHECK -> {
                    String subscription = getWebhookFromRedis(webhookRequestBody.getUserName());
                    dataMap.put(Strings.OP, Strings.CHECK);
                    if (subscription == null) {
                        dataMap.put(Strings.FOUND, Values.FALSE);
                    } else {
                        dataMap.put(Strings.FOUND, Values.TRUE);
                        dataMap.put(Strings.SUBSCRIBE, subscription);
                    }
                    replier.setData(dataMap);
                }
                default -> {
                    response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                    dataMap.put(Strings.ERROR, "The op in request body is wrong.");
                    replier.setData(dataMap);
                    writer.write(replier.reply1020OtherError(addr));
                    return;
                }
            }
        } catch (Exception e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        if (Replier.makeSingleReplier(response, replier, dataCheckResult, addr)) return;
        writer.write(replier.reply0Success(addr));

    }

    private void deleteWebhook(WebhookRequestBody webhookInfo) {
        deleteWebhookFromRedis(webhookInfo.getUserName());
        deleteWebhookFromEs(webhookInfo.getHookUserId());
    }

    private void deleteWebhookFromRedis(String owner) {
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.select(Constants.RedisDb4Webhook);
            jedis.hdel(ApiNames.NewCashByFidsAPI,owner);
            jedis.del(owner);
        }
    }

    private String getWebhookFromRedis(String owner) {
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.select(Constants.RedisDb4Webhook);
            return jedis.hget(ApiNames.NewCashByFidsAPI, owner);
        }
    }

    private void saveSubscribe(WebhookRequestBody webhookInfo) {
        addSubscribeToRedis(webhookInfo);
        saveSubscribeToEs(webhookInfo);
    }

    private void saveSubscribeToEs(WebhookRequestBody webhookInfo) {
        try {
            Initiator.esClient.index(i -> i.index(Initiator.serviceName.toLowerCase() + "_" + IndicesNames.WEBHOOK).id(webhookInfo.getHookUserId()).document(webhookInfo));
        } catch (IOException e) {
            System.out.println("ES client wrong.");
        }
    }

    private void deleteWebhookFromEs(String hookId) {
        try {
            Initiator.esClient.delete(d -> d.index(IndicesNames.WEBHOOK).id(hookId));
        } catch (IOException e) {
            System.out.println("ES client wrong.");
        }
    }

    private void addSubscribeToRedis(WebhookRequestBody data) {
        Gson gson = new Gson();
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.select(Constants.RedisDb4Webhook);
            String dataJson = gson.toJson(data);
            jedis.hset(data.getMethod(),data.getUserName(),dataJson);
        }
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        return requestBody.getFcdsl().getOther() != null;
    }
}
