package FreeGetAPIs;

import constants.ApiNames;
import data.ReplierForFree;
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
        ReplierForFree replier = new ReplierForFree();

        if (Initiator.isFreeGetForbidden(writer)) return;

        if(!BytesTools.isHexString(rawTxHex)){
                replier.setOther();
                replier.setData("Error: Raw TX must be in HEX.");
                writer.write(replier.toJson());
                return;
            }

        String result;
        try {
            result = FcRpcMethods.sendTx(fcClient,rawTxHex);
        } catch (Throwable e) {
            replier.setOther();
            replier.setData("Error: Send TX failed.");
            writer.write(replier.toJson());
            return;
        }
        if(result.contains("{")){
            replier.setOther();
            replier.setData(result);
            writer.write(replier.toJson());
            return;
        }


        if(result.startsWith("\"")){
            result=result.substring(1);
            if(result.endsWith("\""))result=result.substring(0,result.length()-2);
            replier.setData(result);
            replier.setSuccess();
        }

        writer.write(replier.toJson());
    }
}
