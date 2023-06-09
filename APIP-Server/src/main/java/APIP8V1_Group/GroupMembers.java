package APIP8V1_Group;

import APIP1V1_OpenAPI.*;
import initial.Initiator;
import FeipClass.Group;
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


@WebServlet(APIP8V1Path +GroupMembersAPI)
public class GroupMembers extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

//        //Set default sort.

        //Add condition

        //Request
        String index = IndicesFEIP.GroupIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Group> meetList;
        try {
            meetList = esRequest.doRequest(index,null, Group.class);
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
        for(Group group:meetList){
            dataMap.put(group.getGid(),group.getMembers());
        }

        //Response
        replier.setData(dataMap);
        replier.setGot(dataMap.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", GroupMembersAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

        return;
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getIds()==null)
            return false;
//        if(requestBody.getFcdsl().getIds().length!=1)
//            return false;
        return true;
    }
}