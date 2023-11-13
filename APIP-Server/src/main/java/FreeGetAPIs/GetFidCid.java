package FreeGetAPIs;

import APIP0V1_OpenAPI.Replier;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.ApiNames;
import constants.IndicesNames;
import constants.ReplyInfo;
import constants.Strings;
import apipClass.CidInfo;
import fchClass.Address;
import feipClass.Cid;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static apipClass.CidInfo.mergeCidInfo;

@WebServlet(ApiNames.FreeGetPath + ApiNames.GetFidCidAPI)
public class GetFidCid extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String idRequested = request.getParameter("id");
        PrintWriter writer = response.getWriter();
        Replier replier = new Replier();

        if(Initiator.forbidFreeGet){
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2001NoFreeGet));
            writer.write(replier.reply2001NoFreeGet());
            return;
        }

        ElasticsearchClient esClient = Initiator.esClient;

        if(idRequested.contains("_")){
            SearchResponse<Cid> result = esClient.search(s -> s.index(IndicesNames.CID)
                            .query(q -> q
                                    .term(t -> t.field("usedCids").value(idRequested)))
                    , Cid.class);

            List<Hit<Cid>> hitList = result.hits().hits();
            if(hitList==null || hitList.size()==0){
                response.setHeader(ReplyInfo.CodeInHeader, String.valueOf(ReplyInfo.Code2002CidNoFound));
                writer.write(replier.reply2002CidNoFound());
                return;
            }
            Cid cid = hitList.get(0).source();
            GetResponse<Address> fidResult = esClient.get(g -> g.index(IndicesNames.ADDRESS).id(cid.getFid()), Address.class);
            Address fid = fidResult.source();

            CidInfo cidInfo = mergeCidInfo(cid,fid);
            replier.setTotal(1);
            replier.setGot(1);
            try(Jedis jedis = Initiator.jedisPool.getResource()) {
                replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
            }
            replier.setData(cidInfo);
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
            writer.write(replier.reply0Success());
            return;
        }else if(idRequested.charAt(0) == 'F' || idRequested.charAt(0) == '3'){
            GetResponse<Address> fidResult = esClient.get(g -> g.index(IndicesNames.ADDRESS).id(idRequested), Address.class);
            Address addr = fidResult.source();
            if(addr!=null && addr.getFid()!=null){
                GetResponse<Cid> cidResult = esClient.get(g -> g.index(IndicesNames.CID).id(addr.getFid()), Cid.class);
                Cid cid = cidResult.source();
                if(cid ==null){
                    replier.setData(addr);
                    replier.setTotal(1);
                    replier.setGot(1);
                    try(Jedis jedis = Initiator.jedisPool.getResource()) {
                        replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
                    }
                    response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
                    writer.write(replier.reply0Success());
                }else {
                    CidInfo cidInfo = CidInfo.mergeCidInfo(cid, addr);
                    replier.setTotal(1);
                    replier.setGot(1);
                    try(Jedis jedis = Initiator.jedisPool.getResource()) {
                        replier.setBestHeight(Long.parseLong(jedis.get(Strings.BEST_HEIGHT)));
                    }
                    replier.setData(cidInfo);
                    response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code0Success));
                    writer.write(replier.reply0Success());
                }
            }else {
                response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2002CidNoFound));
                writer.write(replier.reply2002CidNoFound());
            }
        }else {
            response.setHeader(ReplyInfo.CodeInHeader,String.valueOf(ReplyInfo.Code2003IllegalFid));
            writer.write(replier.reply2003IllegalFid());
        }
    }
}