package mainTest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.checkerframework.checker.units.qual.C;
import servers.NewEsClient;
import startFCH.ConfigFCH;
import writeEs.CdMaker;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class testWeightMaker {
    public static void main(String[] args) {
        CdMaker cdMaker = new CdMaker();
        ConfigFCH configAPIP = new ConfigFCH();
        ElasticsearchClient esClient = null;
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigFCH.class);
            if (configAPIP.getEsIp() == null || configAPIP.getEsPort() == 0)
                System.out.println("Es IP is null. Config first.");
            NewEsClient newEsClient = new NewEsClient();
            esClient = newEsClient.checkEsClient(esClient, configAPIP);
            cdMaker.makeAddrCd(esClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
