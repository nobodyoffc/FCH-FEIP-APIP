package CryptoKeep;

import APIP0V1_OpenAPI.Replier;
import constants.ApiNames;
import constants.ReplyInfo;
import cryptoTools.Hash;
import esTools.EsTools;
import javaTools.JsonTools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static CryptoKeep.MadeCryptoKeep.CRYPTO_KEEP;
import static initial.Initiator.esClient;

@WebServlet(ApiNames.CryptoKeepPath + ApiNames.CheckCryptoKeepAPI)
public class CheckCryptoKeep extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String sn = request.getParameter("sn");
        String chipIdHash = request.getParameter("chipIdHash");

        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(sn==null || chipIdHash==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("The SN and chipIdHash are required.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        CryptoKeep cryptoKeep = EsTools.getById(esClient, CRYPTO_KEEP, sn, CryptoKeep.class);

        if(cryptoKeep==null){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1011DataNotFound));
            writer.write(replier.reply1011DataNotFound());
            return;
        }

        String chipIdHashForCheck = Hash.Sha256(cryptoKeep.getChipId());
        if(!chipIdHash.equals(chipIdHashForCheck)){
            response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code1020OtherError));
            replier.setData("Wrong chipIdHash.");
            writer.write(replier.reply1020OtherError());
            return;
        }

        cryptoKeep.setChipId(null);

        replier.setData(JsonTools.getNiceString(cryptoKeep));
        replier.setTotal(1L);
        replier.setGot(1);
        response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
        writer.write(replier.reply0Success());
    }
}
