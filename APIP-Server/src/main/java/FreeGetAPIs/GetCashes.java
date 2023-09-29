package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import apipClass.Sort;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.*;
import fchClass.Cash;
import initial.Initiator;
import redis.clients.jedis.Jedis;
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

@WebServlet(ApiNames.FreeGetPath + ApiNames.GetCashesAPI)
public class GetCashes extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }
        String idRequested = request.getParameter("fid");
        if (idRequested==null){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2003IllegalFid));
            writer.write(replier.reply2003IllegalFid());
            return;
        }

        if(request.getParameter("amount")!=null){
            long amount=(long)(Double.parseDouble(request.getParameter("amount"))* Constants.FchToSatoshi);
            CashListReturn cashListReturn = getCashListForPay(amount,idRequested,Initiator.esClient);
            if(cashListReturn.getCode()!=0){
                replier.setData(cashListReturn.getMsg());
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code1020OtherError));
                writer.write(replier.reply1020OtherError());
                return;
            }

            replier.setData(cashListReturn.getCashList());
            int size = cashListReturn.getCashList().size();
            replier.setTotal(cashListReturn.getTotal());
            replier.setGot(size);
            try(Jedis jedis = Initiator.jedisPool.getResource()) {
                replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
            }
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
            writer.write(replier.reply0Success());
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
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2007CashNoFound));
                writer.write(replier.reply2007CashNoFound());
                return;
            }


            List<Cash> cashList = new ArrayList<>();
            for(Hit<Cash> hit : hitList){
                cashList.add(hit.source());
            }

            assert cashResult.hits().total() != null;
            replier.setTotal(cashResult.hits().total().value());
            replier.setGot(cashList.size());
            replier.setData(cashList);
            try(Jedis jedis = Initiator.jedisPool.getResource()) {
                replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
            }
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
            writer.write(replier.reply0Success());
            return;
        }else {
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2003IllegalFid));
            writer.write(replier.reply2003IllegalFid());
        }
    }
}