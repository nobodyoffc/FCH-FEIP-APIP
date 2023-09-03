package apipRequest;

import apipClass.ApipDataRequestParams;
import apipClass.DataRequestBody;
import apipClass.DataResponseBody;
import apipClass.Fcdsl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;
import constants.Strings;
import cryptoTools.SHA;
import feipClass.Service;
import javaTools.BytesTools;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;

public class PostRequester {

    public static void main(String[] args) {
        String requestBody = "{"
                + "\"url\": \"https://cid.cash/APIP/apip3/v1/cidInfoByIds\","
                + "\"time\": 1688888076895,"
                + "\"nonce\": 121,"
                + "\"via\":\"F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW\","
                + "\"fcdsl\":{"
                + "\"ids\":[\"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\", \"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv\"]"
                + "}}";



        String requestUrl = "https://cid.cash/APIP/apip3/v1/cidInfoByIds";

        String headerSignKey = "Sign";
        String sessionKey = "7beec9dc02752c4d1009b4be8f65f7173f8e54c8f8baba35565e65f099ce4281";

        byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);
        byte[] sessionKeyBytes = HexFormat.of().parseHex(sessionKey);
        byte[] hashContentBytes = BytesTools.bytesMerger(requestBodyBytes, sessionKeyBytes);
        byte[] signBytes = SHA.Sha256x2(hashContentBytes);
        String headerSignValue = HexFormat.of().formatHex(signBytes);

        String headerSessionNameKey = "SessionName";
        String headerSessionNameValue = "7beec9dc0275";

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(headerSignKey,headerSignValue);
        headerMap.put(headerSessionNameKey,headerSessionNameValue);

        System.out.println(requestPost(requestUrl, headerMap,requestBody));
    }

    public static String requestPost(String requestUrl, HashMap<String, String> headerMap,  String requestBody) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(requestUrl);
            for(String key: headerMap.keySet()){
                httpPost.setHeader(key,headerMap.get(key));
            }
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(requestBody);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Service> searchService(ApipDataRequestParams apipDataRequestParams, byte[]sessionKey,String owner, String type,boolean onlyActive,boolean ignoreClosed) {
        DataRequestBody dataRequestBody = new DataRequestBody();
        DataResponseBody dataResponseBody;
        List<Service> serviceList = null;
        try {
            Gson gson = new Gson();
            String url = apipDataRequestParams.getUrlHead()+ ApiNames.APIP6V1Path+ApiNames.ServiceSearchAPI;

            dataRequestBody.makeRequestBody(url,apipDataRequestParams.getVia());
            Fcdsl fcdsl = FcdslMaker.makeFcdslForService(owner, type,"20",null,onlyActive,ignoreClosed);

            dataRequestBody.setFcdsl(fcdsl);
            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put(Strings.SESSION_NAME, apipDataRequestParams.getSessionName());

            String requestBodyJson = gson.toJson(dataRequestBody);

            byte[] sign = SHA.Sha256x2(BytesTools.bytesMerger(requestBodyJson.getBytes(StandardCharsets.UTF_8),sessionKey));
            headerMap.put(Strings.SIGN, HexFormat.of().formatHex(sign));
            String responseJson = requestPost(url, headerMap, requestBodyJson);

            dataResponseBody = gson.fromJson(responseJson, DataResponseBody.class);
            if(dataResponseBody.getCode()!=0){
                System.out.println(responseJson);
            }else {
                Object data = dataResponseBody.getData();
                Type t = new TypeToken<List<Service>>() {
                }.getType();
                serviceList = gson.fromJson(gson.toJson(data), t);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Requesting APIP wrong.");
            return null;
        }
        return serviceList;
    }
}
