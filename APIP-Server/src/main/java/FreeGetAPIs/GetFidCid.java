package FreeGetAPIs;

import constants.ApiNames;
import constants.IndicesNames;
import fchClass.Address;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import data.CidInfo;
import fcTools.ParseTools;
import feipClass.Cid;
import initial.Initiator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static data.CidInfo.mergeCidInfo;

@WebServlet(ApiNames.FreeGet + ApiNames.GetFidCidAPI)
public class GetFidCid extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String idRequested = request.getParameter("id");
        PrintWriter writer = response.getWriter();
        if (Initiator.isFreeGetForbidden(writer)) return;
        ElasticsearchClient esClient = Initiator.esClient;

        if(idRequested.contains("_")){
            SearchResponse<Cid> result = esClient.search(s -> s.index(IndicesNames.CID)
                            .query(q -> q
                                    .term(t -> t.field("usedCids").value(idRequested)))
                    , Cid.class);

            List<Hit<Cid>> hitList = result.hits().hits();
            if(hitList==null || hitList.size()==0){

                System.out.println("search: "+idRequested+"hit: "+ParseTools.gsonString(hitList));

                writer.write("Cid no found.");
                return;
            }
            Cid cid = hitList.get(0).source();
            GetResponse<Address> fidResult = esClient.get(g -> g.index(IndicesNames.ADDRESS).id(cid.getFid()), Address.class);
            Address fid = fidResult.source();

            CidInfo cidInfo = mergeCidInfo(cid,fid);

            writer.write(ParseTools.gsonString(cidInfo));

        }else if(idRequested.charAt(0) == 'F' || idRequested.charAt(0) == '3'){
            GetResponse<Address> fidResult = esClient.get(g -> g.index(IndicesNames.ADDRESS).id(idRequested), Address.class);
            Address addr = fidResult.source();
            if(addr!=null && addr.getFid()!=null){
                GetResponse<Cid> cidResult = esClient.get(g -> g.index(IndicesNames.CID).id(addr.getFid()), Cid.class);
                Cid cid = cidResult.source();
                if(cid ==null){
                    writer.write(ParseTools.gsonString(addr));
                }else {
                    CidInfo cidInfo = CidInfo.mergeCidInfo(cid, addr);
                    writer.write(ParseTools.gsonString(cidInfo));
                }
            }else {
                writer.write("Fid no found.");
            }
        }else {
            writer.write("Illegal Fid.");
        }
    }
}