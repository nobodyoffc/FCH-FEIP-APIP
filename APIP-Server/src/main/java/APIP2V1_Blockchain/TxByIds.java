package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import FchClass.Tx;
import FchClass.TxHas;
import data.TxInfo;
import initial.Initiator;
import startFCH.IndicesFCH;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static api.Constant.*;

@WebServlet(APIP2V1Path + TxByIdsAPI)
public class TxByIds extends HttpServlet {

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

        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.

        //Request

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<TxHas> txHasList;
        try {
            txHasList = esRequest.doRequest(IndicesFCH.TxHasIndex,null, TxHas.class);
            if(txHasList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //make data
        List<String> idList = new ArrayList<>();
        for(TxHas txhas : txHasList){
            idList.add(txhas.getTxId());
        }

        List<Tx> txList = null;
        try {
            txList = servers.EsTools.getMultiByIdList(Initiator.esClient, IndicesFCH.TxIndex, idList, Tx.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<TxInfo> meetList = TxInfo.mergeTxAndTxHas(txList, txHasList);


        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        replier.setTotal(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", TxByIdsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

        return;
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        return true;
    }
}
