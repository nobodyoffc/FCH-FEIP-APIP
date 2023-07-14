package test;

import APIP0V1_OpenAPI.*;
import fchClass.Address;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static constants.IndicesNames.ADDRESS;

@WebServlet("/apip3/v1/ids")
public class Ids extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.reset();
        response.setContentType("application/json;charset=utf-8");

        Replier replier = new Replier();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

       DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        List<Address> meetList = null;

        try {
            meetList = esRequest.doRequest(ADDRESS, null,Address.class);

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
