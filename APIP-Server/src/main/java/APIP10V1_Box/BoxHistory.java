package APIP10V1_Box;

import APIP1V1_OpenAPI.*;
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


@WebServlet(APIP10V1Path + BoxHistoryAPI)
public class BoxHistory extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if (dataCheckResult == null) return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("height",false,"index",false,null,null);

        //Add condition

        //Request
        String index = IndicesFEIP.BoxHistIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(), requestBody, response, replier);
        List<personal.BoxHistory> meetList;
        try {
            meetList = esRequest.doRequest(index, sort, personal.BoxHistory.class);
            if (meetList == null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader, String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", BoxHistoryAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }
}