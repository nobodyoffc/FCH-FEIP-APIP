package CidCashTools;

import APIP0V1_OpenAPI.*;
import apipClass.DataRequestBody;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Address;
import co.elastic.clients.elasticsearch.core.GetResponse;
import keyTools.KeyTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static constants.Constants.*;
import static initial.Initiator.esClient;

@WebServlet(ToolsPath + ApiNames.AddressesAPI)
public class Addresses extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, RuntimeException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

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

        String input = (String)requestBody.getFcdsl().getOther();

        Map<String, String> addrMap = new HashMap<>();
        String pubKey = null;
        if(input.startsWith("F")){
            GetResponse<Address> result = esClient.get(g -> g.index(IndicesNames.ADDRESS).id(input), Address.class);

            if(!result.found()){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
                replier.setData("This FID has never seen on blockchain of Freecash.");
                writer.write(replier.reply1020OtherError(addr));
                return;
            }
            pubKey = result.source().getPubKey();
        }else if (input.startsWith("02")||input.startsWith("03")){
            pubKey = input;
        }else{
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("FID or Public Key are needed.");
            writer.write(replier.reply1020OtherError(addr));
            return;
        }
        addrMap.put("FCH",KeyTools.pubKeyToFchAddr(pubKey));
        addrMap.put("BTC",KeyTools.pubKeyToBtcAddr(pubKey));
        addrMap.put("ETH",KeyTools.pubKeyToEthAddr(pubKey));
        addrMap.put("TRX",KeyTools.pubKeyToTrxAddr(pubKey));
        addrMap.put("LTC",KeyTools.pubKeyToLtcAddr(pubKey));
        addrMap.put("DOGE",KeyTools.pubKeyToDogeAddr(pubKey));


        replier.setData(addrMap);
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

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getOther()==null)
            return false;
        return true;
    }
}