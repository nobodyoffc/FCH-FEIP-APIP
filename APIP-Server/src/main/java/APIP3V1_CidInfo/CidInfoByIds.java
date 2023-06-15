package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.*;
import FchClass.Address;
import data.CidInfo;
import FeipClass.Cid;
import initial.Initiator;
import startFCH.IndicesFCH;
import startFEIP.IndicesFEIP;

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

import static api.Constant.*;

@WebServlet(APIP3V1Path + CidInfoByIdsAPI)
public class CidInfoByIds extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        if(!isThisApiRequest(requestBody)){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Cid> meetCidList;
        List<Address> meetAddrList;

        try {
            meetCidList = esRequest.doRequest(IndicesFEIP.CidIndex,null, Cid.class);
            if(meetCidList==null ){
                response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
                writer.write(replier.reply1012BadQuery(addr));
                return;
            }

            meetAddrList = esRequest.doRequest(IndicesFCH.AddressIndex, null,Address.class);
            if(meetAddrList==null){
                response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
                writer.write(replier.reply1012BadQuery(addr));
                return;
            }
        //make addrList

        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        List<CidInfo> cidInfoList = CidInfo.mergeCidInfoList(meetCidList,meetAddrList);

        if(cidInfoList.size() == 0){
            response.setHeader(CodeInHeader,String.valueOf(Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }
        for(CidInfo cidInfo : cidInfoList){
            cidInfo.setTrxAddr(null);
            cidInfo.setDogeAddr(null);
            cidInfo.setLtcAddr(null);
            cidInfo.reCalcWeight();
        }

        Map<String,CidInfo> meetMap = new HashMap<>();
        for(CidInfo cidInfo :cidInfoList){
            meetMap.put(cidInfo.getFid(),cidInfo);
        }
        replier.setGot(meetMap.size());
        replier.setTotal(meetMap.size());
        replier.setData(meetMap);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", CidInfoByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(),nPrice);

    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }


}
