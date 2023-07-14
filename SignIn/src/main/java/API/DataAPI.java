package API;

import RequestCheck.DataCheckResult;
import RequestCheck.DataRequestBody;
import RequestCheck.Replier;
import RequestCheck.RequestChecker;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static Tools.SymSign.symSign;

@WebServlet("/personal")
public class DataAPI extends HttpServlet {

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


        //response
        replier.setData("返回给用户的数据");
        response.setHeader("Code", String.valueOf(0));
        String reply = replier.reply0Success(addr);
        if(reply==null)return;
        String sign = symSign(reply,dataCheckResult.getSessionKey());
        if(sign==null)return;
        response.setHeader("Sign",sign);
        writer.write(reply);
    }
}