package FreeGetAPIs;

import apipClass.Sort;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.ApiNames;
import constants.Constants;
import constants.IndicesNames;
import data.ReplierForFree;
import fcTools.ParseTools;
import fchClass.Cash;
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
        ReplierForFree replier = new ReplierForFree();

        if (Initiator.isFreeGetForbidden(writer)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            replier.setOther();
            replier.setData("Error: FreeGet API is not active now.");
            writer.write(replier.toJson());
            return;
        }
        String idRequested = request.getParameter("fid");
        if (idRequested==null){
            replier.setOther();
            replier.setData("Fid is null.");
            writer.write(replier.toJson());
            return;
        }

        if(request.getParameter("amount")!=null){
            long amount=(long)(Double.parseDouble(request.getParameter("amount"))* Constants.FchToSatoshi);
            CashListReturn cashListReturn = getCashListForPay(amount,idRequested,Initiator.esClient);
            if(cashListReturn.getCode()!=0){
                replier.setOther();
                replier.setData(cashListReturn.getMsg());
                writer.write(replier.toJson());
                return;
            }

            replier.setSuccess();
            replier.setData(cashListReturn.getCashList());
            writer.write(replier.toJson());
            return;
        }

        ElasticsearchClient esClient = Initiator.esClient;

        if(idRequested.charAt(0) == 'F' || idRequested.charAt(0) == '3'){

            ArrayList<Sort> sortList = Sort.makeSortList("valid", false, "cd", true, "cashId", true);


            SearchResponse<Cash> cashResult = esClient.search(s -> s.index(IndicesNames.CASH)
                    .query(q ->q.bool(b->b
                                            .must(m->m.term(t -> t.field("owner").value(idRequested)))
                                            .must(m1->m1.term(t1->t1.field("valid").value(true)))
                            )
                    )
                    .trackTotalHits(tr->tr.enabled(true))
                    .aggregations("sum",a->a.sum(s1->s1.field("value")))
                    .sort(Sort.getSortList(sortList))
                    .size(20), Cash.class);

            List<Hit<Cash>> hitList = cashResult.hits().hits();

            if(hitList==null || hitList.size()==0){
                replier.setOther();
                replier.setData("Cash no found.");
                writer.write(replier.toJson());
                return;
            }


            List<Cash> cashList = new ArrayList<>();
            for(Hit<Cash> hit : hitList){
                cashList.add(hit.source());
            }

            assert cashResult.hits().total() != null;
            replier.setTotal(cashResult.hits().total().value());
            replier.setGot(cashList.size());
            replier.setSuccess();
            replier.setData(cashList);
            writer.write(replier.toJson());
        }else {
            replier.setOther();
            replier.setData("Illegal FID.");
            writer.write(replier.toJson());
        }
    }
}