package APIP5V1_Code;

import APIP1V1_OpenAPI.*;
import FeipClass.Code;
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

import fc_dsl.Sort;
import static api.Constant.*;


@WebServlet(APIP5V1Path +CodeSearchAPI)
public class CodeSearch extends HttpServlet {

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
        ArrayList<Sort> sort =Sort.makeSortList("active",false,"tRate",false,"codeId",true);

        //Add condition

        //Request
        String index = IndicesFEIP.CodeIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Code> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Code.class);
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
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", CodeSearchAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }
}