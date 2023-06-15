package APIP8V1_Group;

import APIP0V1_OpenAPI.*;
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
import java.util.ArrayList;
import java.util.List;

import APIP1V1_FCDSL.Sort;
import static api.Constant.*;


@WebServlet(APIP8V1Path +MyGroupsAPI)
public class MyGroups extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Replier replier = new Replier();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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
        ArrayList<Sort> sort =Sort.makeSortList("lastHeight",false,"gid",true,null,null);

        //Add condition

        //Request
        String index = IndicesFEIP.GroupIndex;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Group> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Group.class);
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
        List<Data> dataList = new ArrayList<>();
        for(Group group: meetList){
            Data data = new Data();
            data.name = group.getName();
            data.gid = group.getGid();
            data.tCdd = group.gettCdd();
            data.memberNum = group.getMemberNum();

            dataList.add(data);
        }

        //response
        replier.setData(dataList);
        replier.setGot(dataList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", MyGroupsAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);

        return;
    }

    class Data {
        String name;
        long memberNum;
        String gid;
        long tCdd;
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getQuery()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms().getFields()==null)
            return false;
        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("members"))
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms().getValues()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms().getValues().length!=1)
            return false;
        return true;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("This API accepts only POST request. 抱歉！");
    }
}