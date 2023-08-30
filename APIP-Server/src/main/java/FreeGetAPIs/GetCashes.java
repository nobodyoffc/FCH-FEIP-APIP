package FreeGetAPIs;

import constants.ApiNames;
import constants.Constants;
import constants.IndicesNames;
import constants.ReplyInfo;
import fchClass.Cash;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import fcTools.ParseTools;
import initial.Initiator;
import walletTools.CashListReturn;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static walletTools.WalletTools.getCashListForPay;

@WebServlet(ApiNames.FreeGet + ApiNames.GetCashesAPI)
public class GetCashes extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        if (Initiator.isFreeGetForbidden(writer)) return;
        String idRequested = request.getParameter("fid");
        if (idRequested==null){
            writer.write("Fid is null.");
            return;
        }

        if(request.getParameter("amount")!=null){
            long amount=(long)(Double.parseDouble(request.getParameter("amount"))* Constants.FchToSatoshi);
            CashListReturn cashListReturn = getCashListForPay(amount,idRequested,Initiator.esClient);
            if(cashListReturn.getCode()!=0){
                writer.write(cashListReturn.getMsg());
                return;
            }
            writer.write(ParseTools.gsonString(cashListReturn.getCashList()));
            return;
        }

        ElasticsearchClient esClient = Initiator.esClient;

        if(idRequested.charAt(0) == 'F' || idRequested.charAt(0) == '3'){
            SearchResponse<Cash> cashResult = esClient.search(s -> s.index(IndicesNames.CASH)
                    .query(q -> q.term(t -> t.field("fid").value(idRequested)))
                    .size(20)
                    .sort(so -> so.field(f -> f.field("valid").order(SortOrder.Desc).field("birthTime").order(SortOrder.Desc)))
                    , Cash.class);
            List<Hit<Cash>> hitList = cashResult.hits().hits();
            if(hitList==null || hitList.size()==0){
                writer.write("Cash no found.");
                return;
            }
            List<Cash> cashList = new ArrayList<>();
            for(Hit<Cash> hit : hitList){
                cashList.add(hit.source());
            }
            writer.write(ParseTools.gsonString(cashList));
        }else {
            writer.write("Illegal Fid.");
        }
    }
}