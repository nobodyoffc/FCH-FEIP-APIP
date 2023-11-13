package APIP3V1_CidInfo;

import APIP0V1_OpenAPI.DataCheckResult;
import APIP0V1_OpenAPI.DataRequestHandler;
import APIP0V1_OpenAPI.Replier;
import APIP0V1_OpenAPI.RequestChecker;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Nobody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(ApiNames.APIP3V1Path + ApiNames.NobodyByIdsAPI)
public class NobodyByIds extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null){
            return;
        }

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        DataRequestHandler dataRequestHandler = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);

        List<Nobody> meetList;

        try {
            meetList = dataRequestHandler.doRequest(IndicesNames.NOBODY, null, Nobody.class);
            if(meetList==null||meetList.size()==0){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        if(meetList.size() ==0){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound(addr));
            return;
        }

        replier.setData(meetList);
        replier.setGot(meetList.size());
        replier.setTotal(meetList.size());

        dataRequestHandler.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        return requestBody.getFcdsl().getIds() != null;
    }
}
