package main.testAPIs;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.DataRequest;
import APIP0V1_OpenAPI.RequestChecker;
import data.Tx;
import writeEs.Indices;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/apip3/v1/match")
public class Match extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json;charset=utf-8");

        RequestChecker requestChecker = new RequestChecker(request,response);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        DataRequest esRequest = new DataRequest(dataCheckResult.getAddr(),dataCheckResult.getDataRequestBody(),response);

        List<Tx> meetList = null;

        try {
            meetList = esRequest.doRequest(Indices.TxIndex, Tx.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(meetList==null)return;
        esRequest.writeSuccess(meetList,meetList.size(),dataCheckResult.getSessionKey());
        return;
    }
}
