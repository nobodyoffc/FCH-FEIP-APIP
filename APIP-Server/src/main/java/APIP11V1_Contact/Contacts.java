package APIP11V1_Contact;

import APIP0V1_OpenAPI.*;
import apipClass.*;
import constants.*;
import feipClass.Contact;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


@WebServlet(ApiNames.APIP11V1Path + ApiNames.ContactsAPI)
public class Contacts extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        if (RequestChecker.isPublicSessionKey(response, replier, writer, addr)) return;

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
//        if(!isThisApiRequest(requestBody)){
//            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
//            writer.write(replier.reply1012BadQuery(addr));
//            return;
//        }

        //Set default sort.
        ArrayList<Sort> sort = Sort.makeSortList("birthHeight",false,"contactId",true,null,null);

        //Add condition
        Fcdsl fcdsl = Fcdsl.addExceptTermsToFcdsl(requestBody, Strings.ACTIVE, Values.FALSE);
        requestBody.setFcdsl(fcdsl);

        //Request
        String index = IndicesNames.CONTACT;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Contact> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Contact.class);
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
        replier.setNonce(requestBody.getNonce());
        replier.setData(meetList);
        replier.setGot(meetList.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    //    private boolean isThisApiRequest(DataRequestBody requestBody) {
//        if(requestBody.getFcdsl()==null)
//            return false;
//        if(requestBody.getFcdsl().getQuery()==null)
//            return false;
//        if(requestBody.getFcdsl().getQuery().getTerms()==null)
//            return false;
//        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("owner"))
//            return false;
//        return true;
//    }
}