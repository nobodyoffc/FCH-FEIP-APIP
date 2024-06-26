package APIP17V1_Avatar;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.Fcdsl;
import avatar.AvatarMaker;
import constants.ApiNames;
import constants.ReplyInfo;
import initial.Initiator;
import constants.Strings;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static constants.Strings.CONFIG;


@WebServlet(ApiNames.APIP17V1Path + ApiNames.AvatarsAPI)
public class Avatars extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();
        
        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        String[] addrs = checkBody(requestBody);
        if(addrs==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        String avatarBasePath;
        String avatarPngPath;
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            avatarBasePath = jedis.hget(CONFIG, Strings.AVATAR_ELEMENTS_PATH);
            avatarPngPath = jedis.hget(CONFIG, Strings.AVATAR_PNG_PATH);
        }
        if(!avatarPngPath.endsWith("/"))avatarPngPath  = avatarPngPath+"/";
        if(!avatarBasePath.endsWith("/"))avatarBasePath = avatarBasePath+"/";

        AvatarMaker.getAvatars(addrs,avatarBasePath,avatarPngPath);

        Base64.Encoder encoder = Base64.getEncoder();
        Map<String,String> addrPngBase64Map = new HashMap<>();
        for(String addr1 : addrs){
            File file = new File(avatarPngPath+addr1+".png");
            FileInputStream fis = new FileInputStream(file);
            String pngStr = encoder.encodeToString(fis.readAllBytes());
            addrPngBase64Map.put(addr1,pngStr);
            file.delete();
            fis.close();
        }
        //response
        replier.setData(addrPngBase64Map);
        replier.setGot(addrPngBase64Map.size());
        replier.setTotal((long) addrPngBase64Map.size());

        response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code0Success));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(ReplyInfo.SignInHeader,sign);

        writer.write(reply);
        return;
    }

    private String[] checkBody(RequestBody requestBody) {

        if(requestBody.getFcdsl()==null) return null;
        Fcdsl fcdsl = requestBody.getFcdsl();
        if(fcdsl.getIds()==null)return null;
        String addrs[] = fcdsl.getIds();

        if(addrs.length>20)return null;
        for(String addr:addrs){
            if(addr.length()!=34)return null;
            String first = addr.substring(0, 1);
            if(!(first.equals("F")||first.equals("3")))return null;
        }

        return addrs;
    }
}