package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import apipClass.DataRequestBody;
import apipClass.Sort;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import esTools.EsTools;
import fchClass.Tx;
import fchClass.TxHas;
import data.TxInfo;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.TxSearchAPI)
public class TxSearch extends HttpServlet {

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

        //Check API

        //Set default sort.
        ArrayList<Sort> sort = Sort.makeSortList("height",false,"txId",true,null,false);

        //Add condition

        //Request
        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<TxHas> txHasList;
        try {
            txHasList = esRequest.doRequest(IndicesNames.TX_HAS,sort, TxHas.class);
            if(txHasList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        List<String> idList = new ArrayList<>();
        for(TxHas txhas :txHasList){
            idList.add(txhas.getTxId());
        }

        List<Tx> txList = null;
        try {
            txList = EsTools.getMultiByIdList(Initiator.esClient, IndicesNames.TX, idList, Tx.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<TxInfo> meetList = TxInfo.mergeTxAndTxHas(txList, txHasList);

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());

    }

}