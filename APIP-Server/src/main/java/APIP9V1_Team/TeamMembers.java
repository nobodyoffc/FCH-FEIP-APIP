package APIP9V1_Team;

import APIP0V1_OpenAPI.*;
import initial.Initiator;
import FeipClass.Team;
import startFEIP.IndicesFEIP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.Constant.*;


@WebServlet(APIP9V1Path +TeamMembersAPI)
public class TeamMembers extends HttpServlet {

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

        //Add condition

        //Request
        String index = IndicesFEIP.TeamIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Team> meetList;
        try {
            meetList = esRequest.doRequest(index,null, Team.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(CodeInHeader,String.valueOf(Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        
        //Make data
        Map<String,String[]> dataMap = new HashMap<>();
        dataMap.put(meetList.get(0).getTid(),meetList.get(0).getMembers());

        //Response
        replier.setData(dataMap);
        replier.setGot(dataMap.size());
        replier.setTotal(dataMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", TeamMembersAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

        return;
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        if(requestBody.getFcdsl().getIds().length!=1)
            return false;
//        if(requestBody.getFcdsl().getQuery()==null)
//            return false;
//        if(requestBody.getFcdsl().getQuery().getTerms()==null)
//            return false;
//        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("activeMembers"))
//            return false;
//        if(!requestBody.getFcdsl().getFilter().getTerms().getValues()[0].equals("rate"))
//            return false;
        return true;
    }
}