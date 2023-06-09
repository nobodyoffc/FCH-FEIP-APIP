package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Address;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.AddressByIdsAPI)
public class AddressByIds extends HttpServlet {

    @Override
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

        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Address> meetAddrList;
        try{
            meetAddrList = esRequest.doRequest(IndicesNames.ADDRESS, null,Address.class);
            if(meetAddrList==null){
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
                writer.write(replier.reply1012BadQuery(addr));
                return;
            }
            //make addrList
            for(Address addr1 : meetAddrList){
                addr1.setTrxAddr(null);
                addr1.setDogeAddr(null);
                addr1.setLtcAddr(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Map<String,Address> meetMap = new HashMap<>();
        for(Address address :meetAddrList){
            meetMap.put(address.getFid(),address);
        }

        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        replier.setTotal(meetMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", ApiNames.AddressByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }


    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }
}
