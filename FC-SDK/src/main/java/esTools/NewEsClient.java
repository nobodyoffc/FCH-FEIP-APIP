package esTools;

import eccAes256K1P7.Aes256CbcP7;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import config.ConfigBase;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.*;

import static constants.Strings.ES_PASSWORD_CIPHER;

public class NewEsClient {

    ElasticsearchClient esClient;
    RestClient restClient;
    RestClientTransport transport;
    private static final Logger log = LoggerFactory.getLogger(NewEsClient.class);

    public ElasticsearchClient getSimpleEsClient() throws IOException {

        return getClientHttp("127.0.0.1",9200);
    }

    public ElasticsearchClient getSimpleEsClientSSL(BufferedReader br) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        System.out.println("Input username: ");
        String user = br.readLine();
        System.out.println("Input password:");
        String password = br.readLine();

        return getClientHttps("127.0.0.1",9200,user,password);
    }

    public ElasticsearchClient getElasticSearchClient(BufferedReader br,  ConfigBase config, Jedis jedis) throws Exception {

        boolean isSSL = true;

        //Check ES username
        String username=config.getEsUsername();
        if(username==null){
            System.out.println("Create ES client without SSL? 'y' to confirm:");
            String input = br.readLine();
            if("y".equals(input)) {
                isSSL = false;
            }else{
                System.out.println("Input ES username: ");
                input = br.readLine();
                if(!"".equals(input))config.setEsUsername(input);
            }
        }else{
            System.out.println("ES username is: "+ username+". " +
                    "\nEnter to get client with it. " +
                    "\n'q' to quit. " +
                    "\n'r' to reset the user. " +
                    "\n'd' to delete it and get client without SSL:");
            String input = br.readLine();
            if("q".equals(input))return null;
            if("r".equals(input)){
                input = br.readLine();
                if(!"".equals(input))config.setEsUsername(input);
            }else if("d".equals(input)){
                config.setEsUsername((String) null);
                isSSL=false;
            }
        }

        //Check ES password
        String password = null;
        if(isSSL) {
            if (jedis != null) {
                password = getEsPassword(config, jedis);
            }
            if (password == null) {
                System.out.println("Input the password of " + config.getEsUsername() + " 'n' to create without SSL:");
                password = br.readLine();
                if ("n".equals(password)) {
                    config.setEsUsername((String) null);
                    isSSL=false;
                }
            }
        }

        //Create client
        try {
            if (isSSL) {
                esClient = getClientHttps(config.getEsIp(), config.getEsPort(), config.getEsUsername(), password);
            }else esClient = getClientHttp(config.getEsIp(), config.getEsPort());

            //Save encrypted password if there is a jedis
            if (esClient != null) {
                if (isSSL && password!= null && jedis!=null) {
                    setEncryptedEsPassword(password, config, jedis);
                    config.writeConfigToFile();
                }
            } else {
                log.debug("Create SSL ES client failed. Check ES and Config.json.");
                return null;
            }
        } catch (Exception e) {
            log.debug("Create SSL ES client failed. ");
            e.printStackTrace();
            return null;
        }

        return esClient;
    }

    public String getEsPassword(ConfigBase config, Jedis jedis) throws Exception {
        String passwordCipher;
        try{
            passwordCipher = jedis.get(ES_PASSWORD_CIPHER);
            if(passwordCipher==null)return null;
        }catch (Exception e){
            return null;
        }
        String password;
        try {
            password = Aes256CbcP7.decrypt(passwordCipher, config.getRandomSymKeyHex());
        }catch (Exception e){
            log.debug("Decrypt ES password wrong.");
            return null;
        }
        return password;
    }
    public void setEncryptedEsPassword(String password, ConfigBase config,Jedis jedis) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        if(config.getRandomSymKeyHex()==null){
            config.setSymKey();
            config.writeConfigToFile();
        }

        String esPasswordCipher = Aes256CbcP7.encrypt(password,config.getRandomSymKeyHex().toCharArray());

        jedis.set(ES_PASSWORD_CIPHER,esPasswordCipher);
        System.out.println("Your ES password is encrypted and saved locally.");
        log.debug("ES password is encrypted and saved locally.");
    }

    public ElasticsearchClient getEsClientSilent(ConfigBase config,Jedis jedis) throws Exception {

        boolean isSSL = true;

        //Check ES username
        String username=config.getEsUsername();
        if(username==null){
            isSSL=false;
        }

        //Check ES password
        String password = null;
        if(isSSL) {
            if (jedis != null) {
                password = getEsPassword(config, jedis);
            }
            if (password == null) {
                isSSL=false;
            }
        }

        //Create client
        try {
            if (isSSL) {
                esClient = getClientHttps(config.getEsIp(), config.getEsPort(), config.getEsUsername(), password);
            }else esClient = getClientHttp(config.getEsIp(), config.getEsPort());

            if (esClient == null) {
                log.debug("Create SSL ES client failed. Check ES and Config.json.");
                return null;
            }
        } catch (Exception e) {
            log.debug("Create SSL ES client failed. ");
            e.printStackTrace();
            return null;
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
