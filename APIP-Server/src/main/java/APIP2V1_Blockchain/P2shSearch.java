package APIP2V1_Blockchain;

import APIP1V1_OpenAPI.*;
import startFCH.IndicesFCH;
import writeEs.P2SH;
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

import fc_dsl.Sort;
import static api.Constant.*;


@WebServlet(APIP2V1Path + P2shSearchAPI)
public class P2shSearch extends HttpServlet {

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
        replier.setNonce(requestBody.getNonce());
        //Check API

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("birthHeight",false,"fid",true,null,null);

        //Add condition

        //Request
        String index = IndicesFCH.P2SHIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<P2SH> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, P2SH.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response
        replier.setNonce(requestBody.getNonce());
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", P2shSearchAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

    }
}