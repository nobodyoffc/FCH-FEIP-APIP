package APIP19V1_Nid;

import APIP0V1_OpenAPI.*;
import APIP1V1_FCDSL.Sort;
import FeipClass.Nid;
import initial.Initiator;
import startFEIP.IndicesFEIP;

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


@WebServlet(APIP19V1Path +NidSearchAPI)
public class NidSearch extends HttpServlet {

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

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("birthTime",false,"nameId",true,null,null);

        //Add condition

        //Request
        String index = IndicesFEIP.NidIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Nid> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Nid.class);
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
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", NidSearchAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

        return;
    }
}