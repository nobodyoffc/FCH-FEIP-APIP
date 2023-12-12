package javaTools;

import apipClass.ClientCodeMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;

public class HttpRequester {
    private final Logger log = LoggerFactory.getLogger(HttpRequester.class);
    private int code;
    private String message;
    private String responseBodyStr;
    public void request(String getOrPost, String endpoint, Map<String,String> requestHeaderMap, @Nullable String requestBody){
        CloseableHttpResponse httpResponse;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet httpGet = null;
            HttpPost httpPost = null;
            if(getOrPost.equalsIgnoreCase("get")) {
                httpGet = new HttpGet(endpoint);
            }else if(getOrPost.equalsIgnoreCase("post")){
                httpPost = new HttpPost(endpoint);
            }else return;

            if(requestHeaderMap!=null) {
                for (String key : requestHeaderMap.keySet()) {
                    if(getOrPost.equalsIgnoreCase("get"))httpGet.setHeader(key, requestHeaderMap.get(key));
                    else httpPost.setHeader(key, requestHeaderMap.get(key));
                }
            }

            if(getOrPost.equalsIgnoreCase("post")){
                StringEntity entity = new StringEntity(requestBody);
                httpPost.setEntity(entity);
            }

            if(getOrPost.equalsIgnoreCase("post")){
                httpResponse = httpClient.execute(httpPost);
            }else httpResponse = httpClient.execute(httpGet);

            if (httpResponse == null) {
                log.debug("httpResponse == null.");
                code= ClientCodeMessage.Code1ResponseIsNull;
                message = ClientCodeMessage.Msg1ResponseIsNull;
                return;
            }

            if(httpResponse.getStatusLine().getStatusCode() != 200){
                log.debug("Post response status: {} {}",httpResponse.getStatusLine().getStatusCode(),httpResponse.getStatusLine().getReasonPhrase());
                code= ClientCodeMessage.Code6ResponseStatusWrong;
                message = ClientCodeMessage.Msg6ResponseStatusWrong+": "+httpResponse.getStatusLine().getStatusCode()+" " +httpResponse.getStatusLine().getReasonPhrase();
                return;
            }

            byte[] responseBodyBytes = httpResponse.getEntity().getContent().readAllBytes();

            responseBodyStr = new String(responseBodyBytes);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Do post request wrong.");
            code= ClientCodeMessage.Code7DoPostRequestWrong;
            message = ClientCodeMessage.Msg7DoPostRequestWrong;
        }
    }

    public Logger getLog() {
        return log;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseBodyStr() {
        return responseBodyStr;
    }

    public void setResponseBodyStr(String responseBodyStr) {
        this.responseBodyStr = responseBodyStr;
    }
}
