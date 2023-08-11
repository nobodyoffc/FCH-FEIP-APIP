package mainTest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import esTools.NewEsClient;
import writeEs.CdMaker;

import java.io.IOException;

public class MainTest {
    public static void main(String[] args) throws Exception {
        ElasticsearchClient esClient = getSimpleEsClient();
        System.out.println(esClient.info());

        CdMaker cdMaker = new CdMaker();
        cdMaker.makeAddrCd(esClient);
    }

    private static ElasticsearchClient getSimpleEsClient() throws IOException {
        NewEsClient newEsClient = new NewEsClient();
        return newEsClient.getClientHttp("127.0.0.1",9200);
    }
}
