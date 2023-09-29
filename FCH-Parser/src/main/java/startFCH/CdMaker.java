package startFCH;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import esTools.NewEsClient;
import fchClass.Block;
import menu.Inputer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class CdMaker {
    static NewEsClient newEsClient = new NewEsClient();
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Is SSL EsClient? 'y' to confirm. Other to create http EsClient:");
        String input = Inputer.inputString(br);
        ElasticsearchClient esClient;
        if("y".equals(input))esClient=getSimpleEsClientSSL(br);
        else esClient = getSimpleEsClient();
        System.out.println(esClient.info());

        writeEs.CdMaker cdMaker = new writeEs.CdMaker();
        Block bestBlock = new Block();
        bestBlock.setTime(1695263776);
        cdMaker.makeUtxoCd(esClient,bestBlock);
        cdMaker.makeAddrCd(esClient);
        System.out.println("CDs of cashes and addresses were made.");
        br.close();
        newEsClient.shutdownClient();
    }

    private static ElasticsearchClient getSimpleEsClient() throws IOException {

        return newEsClient.getClientHttp("127.0.0.1",9200);
    }
    private static ElasticsearchClient getSimpleEsClientSSL(BufferedReader br) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        System.out.println("Input username: ");
        String user = br.readLine();
        System.out.println("Input password:");
        String password = br.readLine();

        return newEsClient.getClientHttps("127.0.0.1",9200,user,password);
    }
}
