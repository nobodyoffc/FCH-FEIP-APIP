package APIP12V1_Secret;

import APIP0V1_OpenAPI.*;
import apipClass.Fcdsl;
import apipClass.Filter;
import apipClass.Terms;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import feipClass.Secret;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import esTools.Sort;


@WebServlet(ApiNames.APIP12V1Path + ApiNames.SecretsDeletedAPI)
public class SecretsDeleted extends HttpServlet {

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

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Check API
        if(!isThisApiRequest(requestBody)){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("lastHeight",false,"secretId",true,null,null);

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
        String index = IndicesNames.SECRET;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Secret> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Secret.class);
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
        if(!requestBody.getFcdsl().getQuery().getTerms().getFields()[0].equals("owner"))
            return false;
        return true;
    }
}