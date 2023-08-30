package FreeGetAPIs;

import constants.ApiNames;
import freecashRPC.FcRpcMethods;
import initial.Initiator;
import javaTools.BytesTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static initial.Initiator.fcClient;

@WebServlet(ApiNames.FreeGet + ApiNames.BroadcastAPI)
public class Broadcast extends HttpServlet {


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String rawTxHex = request.getParameter("rawTx");
        rawTxHex=rawTxHex.toLowerCase();
        PrintWriter writer = response.getWriter();

        if (Initiator.isFreeGetForbidden(writer)) return;

        if(!BytesTools.isHexString(rawTxHex)){
                writer.write("Raw TX must be in HEX.");
                return;
            }

        String result;
        try {
            result = FcRpcMethods.sendTx(fcClient,rawTxHex);
        } catch (Throwable e) {
            writer.write("Send TX failed.");
            return;
        }
        if(result.contains("{")){
            writer.write(result);
            return;
        }

        if(result.startsWith("\""))result=result.substring(1);
        if(result.endsWith("\""))result=result.substring(0,result.length()-2);
        writer.write(result);
    }
}
