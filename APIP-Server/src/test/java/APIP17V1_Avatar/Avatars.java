package APIP17V1_Avatar;

import APIP1V1_OpenAPI.*;
import fc_dsl.Fcdsl;
import initial.Initiator;
import startAPIP.RedisKeys;

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

import static api.Constant.*;


@WebServlet(APIP17V1Path +AvatarsAPI)
public class Avatars extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();
        
        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();
        replier.setNonce(requestBody.getNonce());
        //Check API
        String[] addrs = checkBody(requestBody);
        if(addrs==null){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        String avatarBasePath = Initiator.jedis0Common.get(RedisKeys.AvatarBasePath);
        String avatarFilePath = Initiator.jedis0Common.get(RedisKeys.AvatarPngPath);

        if(!avatarFilePath.endsWith("/"))avatarFilePath  = avatarFilePath+"/";
        if(!avatarBasePath.endsWith("/"))avatarBasePath = avatarBasePath+"/";

        AvatarMaker.getAvatars(addrs,avatarBasePath,avatarFilePath);

        Base64.Encoder encoder = Base64.getEncoder();
        Map<String,String> addrPngBase64Map = new HashMap<>();
        for(String addr1 : addrs){
            File file = new File(avatarFilePath+addr1+".png");
            FileInputStream fis = new FileInputStream(file);
            String pngStr = encoder.encodeToString(fis.readAllBytes());
            addrPngBase64Map.put(addr1,pngStr);
            file.delete();
            fis.close();
        }
        //response
        replier.setData(addrPngBase64Map);
        replier.setGot(addrPngBase64Map.size());
        replier.setTotal(addrPngBase64Map.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", AvatarsAPI));
        response.setHeader(CodeInHeader, String.valueOf(Code0Success));
        String reply = replier.reply0Success(addr,nPrice);
        if(reply==null)return;
        String sign = DataRequestHandler.symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader(SignInHeader,sign);

        writer.write(reply);
        return;
    }

    private String[] checkBody(DataRequestBody requestBody) {

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