package APIP9V1_Team;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.TeamOtherPersonsData;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import feipClass.Team;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet(ApiNames.APIP9V1Path + ApiNames.TeamOtherPersonsAPI)
public class TeamOtherPersons extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.

        //Add condition

        //Request
        String index = IndicesNames.TEAM;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Team> meetList;
        try {
            meetList = esRequest.doRequest(index,null, Team.class);
            if(meetList==null){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }
        
        //Make data
        TeamOtherPersonsData teamOtherPersons = new TeamOtherPersonsData();
        Team team = meetList.get(0);
        teamOtherPersons.setTid(team.getTid());
        teamOtherPersons.setInvitees(team.getInvitees());
        teamOtherPersons.setTransferee(team.getTransferee());
        teamOtherPersons.setNotAgreeMembers(team.getNotAgreeMembers());

        //Response
        replier.setData(teamOtherPersons);
        replier.setGot(1);
        replier.setTotal(1L);
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
        if(requestBody.getFcdsl().getIds().length!=1)
            return false;
        return true;
    }
}