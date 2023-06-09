package APIP9V1_Team;

import APIP1V1_OpenAPI.*;
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


@WebServlet(APIP9V1Path + TeamOtherPersonsAPI)
public class TeamOtherPersons extends HttpServlet {

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
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("tid",meetList.get(0).getTid());
        dataMap.put("transferee",meetList.get(0).getTransferee());
        dataMap.put("invitees",meetList.get(0).getInvitees());
        dataMap.put("notAgreeMembers",meetList.get(0).getNotAgreeMembers());

        //Response
        replier.setData(dataMap);
        replier.setGot(1);
        replier.setTotal(1);
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", TeamOtherPersonsAPI));
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
        return true;
    }
}