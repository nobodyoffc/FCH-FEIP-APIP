package requester;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ApachePostRequester {

    public static void main(String[] args) {
        String requestUrl = "https://cid.cash/APIP/apip3/v1/cidInfoByIds";
        String headerKey = "Sign";
        String headerValue = "carmx";
        String requestBody = "{"
                + "\"url\": \"https://cid.cash/APIP/apip3/v1/cidInfoByIds\","
                + "\"time\": 1688888076895,"
                + "\"nonce\": 121,"
                + "\"via\":\"F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW\","
                + "\"fcdsl\":{"
                + "\"ids\":[\"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\", \"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv\"]"
                + "}}";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.setHeader(headerKey, headerValue);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(requestBody);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                System.out.println(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
