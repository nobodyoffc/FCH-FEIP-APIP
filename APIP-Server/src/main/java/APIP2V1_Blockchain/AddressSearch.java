package APIP2V1_Blockchain;

import APIP0V1_OpenAPI.*;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Address;
import APIP1V1_FCDSL.Sort;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(ApiNames.APIP2V1Path + ApiNames.AddressSearchAPI)
public class AddressSearch extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Replier replier = new Replier();
        PrintWriter writer = response.getWriter();

        RequestChecker requestChecker = new RequestChecker(request,response, replier);

        DataCheckResult dataCheckResult = requestChecker.checkDataRequest();

        if(dataCheckResult==null)return;

        String addr = dataCheckResult.getAddr();

        DataRequestBody requestBody = dataCheckResult.getDataRequestBody();

        //Set default sort.
        ArrayList<Sort> sort =Sort.makeSortList("lastHeight",false,"fid",true,null,null);

        //Request
        String index = IndicesNames.ADDRESS;

        DataRequestHandler esRequest = new DataRequestHandler(dataCheckResult.getAddr(),requestBody,response,replier);
        List<Address> meetList;
        try {
            meetList = esRequest.doRequest(index,sort, Address.class);
            if(meetList==null){
                return;
            }
            //make addrList
            for(Address addr1 : meetList ){
                addr1.setTrxAddr(null);
                addr1.setDogeAddr(null);
                addr1.setLtcAddr(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1012BadQuery));
            writer.write(replier.reply1012BadQuery(addr));
            return;
        }

        //response

        replier.setData(meetList);
        replier.setGot(meetList.size());
        int nPrice = Integer.parseInt(Initiator.jedis0Common.hget("nPrice", ApiNames.AddressSearchAPI));
        esRequest.writeSuccess(dataCheckResult.getSessionKey(), nPrice);
    }
}

