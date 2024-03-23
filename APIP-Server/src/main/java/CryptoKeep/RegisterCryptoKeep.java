package CryptoKeep;

import APIP0V1_OpenAPI.Replier;
import apipClass.WebhookPushBody;
import apipClient.ApipDataGetter;
import apipTools.ApipTools;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.google.gson.Gson;
import constants.ApiNames;
import constants.OpNames;
import constants.ReplyInfo;
import constants.Strings;
import cryptoTools.Hash;
import esTools.EsTools;
import fchClass.OpReturn;
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
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static CryptoKeep.MadeCryptoKeep.CRYPTO_KEEP;
import static CryptoKeep.MadeCryptoKeep.CRYPTO_KEEP_HISTORY;
import static initial.Initiator.esClient;
import static initial.Initiator.jedisPool;

//TODO untested
@WebServlet(ApiNames.CryptoKeepPath + ApiNames.RegisterCryptoKeepAPI)
public class RegisterCryptoKeep extends HttpServlet {

    private static final String CRYPTO_KEEP_WEBHOOK_KEY = "crypto_keep_webhook_key";
    public static final String CRYPTO_KEEP_TYPE = "CryptoKeep";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();
        byte[] bodyBytes = request.getInputStream().readAllBytes();
        if(bodyBytes==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Failed to get request body from webhook.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        Gson gson = new Gson();
        String bodyJson = new String(bodyBytes);

        WebhookPushBody postBody = gson.fromJson(bodyJson,WebhookPushBody.class);
        if(postBody==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Failed to get WebhookPushBody from bodyJson:"+bodyJson);
            writer.write(replier.reply1020OtherError());
            return;
        }

        String dataStr = postBody.getData();
        String sign = postBody.getSign();
        String key;

        try(Jedis jedis = jedisPool.getResource()){
            key = jedis.get(CRYPTO_KEEP_WEBHOOK_KEY);
            byte[] dataBytes = dataStr.getBytes(StandardCharsets.UTF_8);
            String checkSign = ApipTools.getSessionKeySign(HexFormat.of().parseHex(key), dataBytes);
            if(!sign.equals(checkSign)){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1008BadSign));
                replier.setData("Failed to write made info into ES.");
                writer.write(replier.reply1008BadSign());
                return;
            }
        }

        String opReturnListStr = postBody.getData();
        List<OpReturn> opReturnList = ApipDataGetter.getOpReturnList(opReturnListStr);
        if(opReturnList==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Failed to parse OpReturn list.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        int total = 0;
        int done = 0;

        for(OpReturn opReturn :opReturnList){
            total++;
            String dataJson = opReturn.getOpReturn();
            CryptoKeepData cryptoKeepData;
            try{
                cryptoKeepData = gson.fromJson(dataJson,CryptoKeepData.class);
            }catch (Exception ignore){
                continue;
            }
            if(!cryptoKeepData.getType().equals(CRYPTO_KEEP_TYPE))continue;
            CryptoKeepData.Data registerData = cryptoKeepData.getData();
            if(registerData==null||registerData.getOp()==null||registerData.getSn()==null||registerData.getFidChipIdHash()==null)continue;
            if(registerData.getOp().equals(OpNames.REGISTER))continue;
            String sn = registerData.getSn();
            if(sn==null)continue;
            CryptoKeep cryptoKeep = EsTools.getById(esClient, CRYPTO_KEEP, sn, CryptoKeep.class);
            if(cryptoKeep==null)continue;
            String checkFidChipIdHash = Hash.Sha256(opReturn.getSigner() + cryptoKeep.getChipId());
            if(!registerData.getFidChipIdHash().equals(checkFidChipIdHash))continue;

            cryptoKeep.setOwner(opReturn.getSigner());
            cryptoKeep.setRegisterTime(System.currentTimeMillis());

            IndexResponse result = esClient.index(i -> i.index(CRYPTO_KEEP).id(sn).document(cryptoKeep));

            if(result.result()!=Result.Updated){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Failed to update register info into ES for"+opReturn.getSigner());
                writer.write(replier.reply1020OtherError());
                return;
            }

            CryptoKeepHist cryptoKeepHist = new CryptoKeepHist(sn,OpNames.REGISTER,System.currentTimeMillis());
            cryptoKeepHist.setOwner(opReturn.getSigner());
            cryptoKeepHist.setRegisterTxId(opReturn.getTxId());

            result = esClient.index(i -> i.index(CRYPTO_KEEP_HISTORY).id(sn+"_"+System.currentTimeMillis()).document(cryptoKeepHist));
            if(result.result()!=Result.Created){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("Failed to write op history into ES.");
                writer.write(replier.reply1020OtherError());
                return;
            }
            done++;
        }

        replier.setData("Done.");
        replier.setTotal(total);
        replier.setGot(done);
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
        }
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
