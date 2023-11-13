package APIP9V1_Team;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import apipClass.Sort;
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
import java.util.ArrayList;
import java.util.List;


@WebServlet(ApiNames.APIP9V1Path + ApiNames.TeamSearchAPI)
public class TeamSearch extends HttpServlet {

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

        //Set default sort.
        ArrayList<Sort> sort = Sort.makeSortList("active",false,"tRate",false,"tid",true);

        //Add condition

        //Request
        String index = IndicesNames.TEAM;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Team> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Team.class);
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
        for(Team team :meetList){
            team.setMemberNum(team.getMembers().length);
            team.setMembers(null);
            team.setExMembers(null);
            team.setNotAgreeMembers(null);
            team.setTransferee(null);
            team.setInvitees(null);
        }

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());

    }
}