package APIP13V1_Mail;

import APIP0V1_OpenAPI.*;
import APIP1V1_FCDSL.Fcdsl;
import APIP1V1_FCDSL.Filter;
import APIP1V1_FCDSL.Terms;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import initial.Initiator;
import feipClass.Mail;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import esTools.Sort;


@WebServlet(ApiNames.APIP13V1Path + ApiNames.MailsDeletedAPI)
public class MailsDeleted extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        if (RequestChecker.checkPublicSessionKey(response, replier, writer, addr)) return;

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("lastHeight",false,"mailId",true,null,null);

        //Add condition
        Fcdsl fcdsl;
        if(requestBody.getFcdsl()!=null) {
            fcdsl = requestBody.getFcdsl();
        }else fcdsl= new Fcdsl();

        Filter filter;
        if(fcdsl.getFilter()!=null) {
            filter = fcdsl.getFilter();
        }else filter=new Filter();

        Terms terms;
        if(filter.getTerms()!=null) {
            terms = filter.getTerms();
        }else terms=new Terms();

        terms.setFields(new String[]{"active"});
        terms.setValues(new String[]{"false"});
        filter.setTerms(terms);
        fcdsl.setFilter(filter);
        requestBody.setFcdsl(fcdsl);

        //Request
        String index = IndicesNames.MAIL;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Mail> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Mail.class);
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

        //response
        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(DataRequestBody requestBody) {
        if(requestBody.getFcdsl()==null)
            return false;
        if(requestBody.getFcdsl().getQuery()==null)
            return false;
        if(requestBody.getFcdsl().getQuery().getTerms()==null)
            return false;
        return true;
    }
}