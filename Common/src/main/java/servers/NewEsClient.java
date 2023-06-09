package servers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class NewEsClient {

    ElasticsearchClient esClient;
    RestClient restClient;
    RestClientTransport transport;

    public ElasticsearchClient checkEsClient(ElasticsearchClient esClient, ConfigBase configBase) throws IOException, KeyManagementException, NoSuchAlgorithmException {

        if (esClient == null) {
            if (configBase.getEsUsername() == null) {
                return getClientHttp(configBase.getEsIp(), configBase.getEsPort());
            } else {
                System.out.println("Input the password of " + configBase.getEsUsername() + ". Press 'h' to start a HTTP client:");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String password = br.readLine();
                if ("".equals(password) || "h".equals(password))
                    return getClientHttp(configBase.getEsIp(), configBase.getEsPort());
                try {
                    return getClientHttps(configBase.getEsIp(), configBase.getEsPort(), configBase.getEsUsername(), password);
                } catch (Exception e) {
                    System.out.println("Create SSL ES client failed.");
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return esClient;
    }

    public ElasticsearchClient getClientHttp(String ip, int port) throws ElasticsearchException, IOException {

        System.out.println("Creating a client on " + ip + ":" + port + ".....");

        try {
            // Create a client without authentication check
            restClient = RestClient.builder(
                    new HttpHost(ip, port))
                    .setRequestConfigCallback(requestConfigBuilder -> {
                        return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
                                .setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
                    })
                    .build();
            // Create the transport with a Jackson mapper
            transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            // And create the API client
            esClient = new ElasticsearchClient(transport);

            System.out.println("Client has been created. Cluster name:" + esClient.info().clusterName());

            return esClient;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The elasticsearch server may need a authorization. Try to create a client with HTTPS.");
            return null;
        }
    }


    public ElasticsearchClient getClientHttps(String host, int port, String username, String password) throws ElasticsearchException, IOException, NoSuchAlgorithmException, KeyManagementException {
        System.out.println("Creating a client with authentication on: " + host + ":" + port);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        restClient = RestClient.builder(new HttpHost(host, port, "https"))
                .setHttpClientConfigCallback(h -> h.setDefaultCredentialsProvider(credentialsProvider))
                .setRequestConfigCallback(requestConfigBuilder -> {
                    return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
                            .setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
                })
                .build();

        // Create the transport with a Jackson mapper
        transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        esClient = new ElasticsearchClient(transport);

        System.out.println("Client has been created. Cluster name:" + esClient.info().clusterName());

        return esClient;
    }

    public void shutdownClient() throws IOException {
        if (this.esClient != null) this.esClient.shutdown();
        if (this.transport != null) this.transport.close();
        if (this.restClient != null) this.restClient.close();
    }

}
