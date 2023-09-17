package APIP20V1_Webhook;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.DataRequestHandler;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.DataRequestBody;
import apipClass.WebhookInfo;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.Constants;
import constants.IndicesNames;
import constants.ReplyInfo;
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

            webhookInfo.setOwner(addr);
            webhookInfo.setMethod(ApiNames.NewCashByFidsAPI);
            hookId = HexFormat.of().formatHex(SHA.Sha256x2(SHA.stringMerge2Utf8(webhookInfo.getOwner(), StandardCharsets.UTF_8,webhookInfo.getMethod(),StandardCharsets.UTF_8)));
            webhookInfo.setHookId(hookId);

            if("subscribe".equals(webhookInfo.getOp())) {
                saveSubscribe(webhookInfo);
                replier.setData("Done. The hookId is: "+hookId);
            }else if("unsubscribe".equals(webhookInfo.getOp())){
                deleteWebhook(webhookInfo);
                replier.setData("HookId "+hookId + " deleted.");
            }else {
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("The op in request body is wrong.");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
        } catch (Exception e) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            writer.write(replier.reply1020OtherError(addr));
            return;
        }

        replier.setGot(1);
        replier.setTotal(1);
        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);
        writer.write(replier.reply0Success(addr));
//        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
//        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private void deleteWebhook(WebhookInfo webhookInfo) {
        deleteWebhookFromRedis(webhookInfo.getOwner());
        deleteWebhookFromEs(webhookInfo.getHookId());
    }

    private void deleteWebhookFromRedis(String owner) {
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.select(Constants.RedisDb4Webhook);
            jedis.hdel(ApiNames.NewCashByFidsAPI,owner);
            jedis.del(owner);
        }
    }

    private void saveSubscribe(WebhookInfo webhookInfo) {
        addSubscribeToRedis(webhookInfo);
        saveSubscribeToEs(webhookInfo);
    }

    private void saveSubscribeToEs(WebhookInfo webhookInfo) {
        try {
            Initiator.esClient.index(i -> i.index(Initiator.serviceName.toLowerCase() + "_" + IndicesNames.WEBHOOK).id(webhookInfo.getHookId()).document(webhookInfo));
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

    private void addSubscribeToRedis(WebhookInfo data) {
        Gson gson = new Gson();
        try(Jedis jedis = Initiator.jedisPool.getResource()){
            jedis.select(Constants.RedisDb4Webhook);
            String dataJson = gson.toJson(data);
            jedis.hset(data.getMethod(),data.getOwner(),dataJson);
        }
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        return requestBody.getFcdsl().getOther() != null;
    }
}
