package servers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;

import java.io.IOException;

public class test {
    public static void main(String[] args) throws IOException {
        ElasticsearchClient esClient = null;
        String ip = "127.0.0.1";
        int port = 9200;
        NewEsClient newEsClient = new NewEsClient();
        esClient = newEsClient.getClientHttp(ip, port);
        System.out.println(esClient.info().toString());
        newEsClient.shutdownClient();

    }
}
