package test;

import APIP0V1_OpenAPI.*;
import FchClass.Tx;
import APIP1V1_FCDSL.Sort;
import startFCH.IndicesFCH;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/apip3/v1/exists")
public class Exists extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Replier replier = new Replier();

        response.reset();
        response.setContentType("application/json;charset=utf-8");

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        List<Tx> meetList = null;
        //Set default sort.
        ArrayList<Sort> sort = new ArrayList<>();;
        Sort sort0 = new Sort();
        sort0.setField("height");
        sort0.setOrder("desc");
        sort.add(sort0);

        try {
            meetList = esRequest.doRequest(IndicesFCH.TxIndex,sort, Tx.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(meetList==null)return;

        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
        return;
    }
}
