package APIP11V1_Contact;

import APIP0V1_OpenAPI.*;
import apipClass.RequestBody;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import feipClass.Contact;

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


@WebServlet(ApiNames.APIP11V1Path + ApiNames.ContactByIdsAPI)
public class ContactByIds extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request, response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if (dataCheckResult == null) return;

        String addr = dataCheckResult.getAddr();

        if (RequestChecker.isPublicSessionKey(response, replier, writer, addr)) return;

        RequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
        if (!isThisApiRequest(requestBody)) {
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.

        //Request
        String index = IndicesNames.CONTACT;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(), requestBody, response, replier);
        List<Contact> meetList;
        try {
            meetList = esRequest.doRequest(index, null, Contact.class);
            if (meetList == null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        Map<String,Contact> meetMap = new HashMap<>();
        for(Contact contact :meetList){
            meetMap.put(contact.getContactId(),contact);
        }
        //response
        replier.setData(meetMap);
        replier.setGot(meetMap.size());
        replier.setTotal((long) meetMap.size());
        esRequest.writeSuccess(dataCheckResult.getSessionKey());
    }

    private boolean isThisApiRequest(RequestBody requestBody) {
        if (requestBody.getFcdsl() == null)
            return false;
        if (requestBody.getFcdsl().getIds() == null)
            return false;
        return true;
    }
}