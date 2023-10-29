package apipRequest;

import apipClass.DataRequestBody;
import apipClass.Fcdsl;
import apipClass.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.ApiNames;
import constants.Strings;
import constants.UpStrings;
import cryptoTools.SHA;
import feipClass.Service;
import javaTools.BytesTools;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;

import static apipTools.ApipTools.isGoodSign;

public class PostRequester {

    public static void main(String[] args) throws IOException {
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

        System.out.println("no check:"+ new String(postRequest(requestUrl, headerMap,requestBody).getEntity().getContent().readAllBytes()));

        System.out.println("checked:"+new String(postRequestCheckSign(requestUrl, headerMap,requestBody,sessionKeyBytes).getEntity().getContent().readAllBytes()));
    }

//    private static String getResponseBodyString(HttpResponse response) {
//        String result;
//        try {
//            result = new String(response.getEntity().getContent().readAllBytes());
//            return result;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    private static String getResponseBodyString(HttpResponse response) {
//        try (InputStream in = response.getEntity().getContent()) {
//            ByteArrayOutputStream result = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = in.read(buffer)) != -1) {
//                result.write(buffer, 0, length);
//            }
//            return result.toString(StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    public static String requestPost(String requestUrl, HashMap<String, String> headerMap, String requestBody) {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpPost httpPost = new HttpPost(requestUrl);
        if(headerMap!=null) {
            for (String key : headerMap.keySet()) {
                httpPost.setHeader(key, headerMap.get(key));
            }
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

    public static HttpResponse postRequest(String requestUrl, HashMap<String, String> headerMap, String requestBody) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(requestUrl);
            if(headerMap!=null) {
                for (String key : headerMap.keySet()) {
                    httpPost.setHeader(key, headerMap.get(key));
                }
            }
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(requestBody);
            httpPost.setEntity(entity);

            return httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResponseFc parseApipResponse(HttpResponse response){
        Gson gson = new Gson();
        Reply replier;
        String sign;
        byte[] bodyBytes;
        ResponseFc responseFc = new ResponseFc();
        try{
            sign = response.getHeaders(UpStrings.SIGN)[0].getValue();

            bodyBytes = response.getEntity().getContent().readAllBytes();
            String responseBody = new String(bodyBytes);
            System.out.println("checked:"+responseBody);

            replier = gson.fromJson(responseBody, Reply.class);
            System.out.println(replier.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        responseFc.setSign(sign);
        responseFc.setReplyBytes(bodyBytes);
        responseFc.setReply(replier);
        return responseFc;
    }



    public static HttpResponse postRequestCheckSign(String requestUrl, HashMap<String, String> headerMap, String requestBody, byte[]sessionKey) {
        HttpResponse response = postRequest(requestUrl, headerMap, requestBody);
        if(response==null)return null;

        Header[] headers = response.getHeaders(UpStrings.SIGN);

        if(headers == null||headers.length==0){
            return response;
        }

        String sign;

        sign = response.getHeaders(UpStrings.SIGN)[0].getValue();

        byte[] responseBody;

        try {
            responseBody = response.getEntity().getContent().readAllBytes();//EntityUtils.toByteArray(responseEntity);

            if(sign!=null) {
                if (!isGoodSign(responseBody, sign, sessionKey)) {
                    System.out.println("Bad sign of the data from APIP.");
                    return null;
                }
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Service> searchService(ApipParamsForUser apipDataRequestParams, byte[]sessionKey, String owner, String type, boolean onlyActive, boolean ignoreClosed) {
        System.out.println("Search your maker services...");

        DataRequestBody dataRequestBody = new DataRequestBody();
        ResponseBody dataResponseBody;
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

            byte[] allBytes=BytesTools.bytesMerger(requestBodyJson.getBytes(StandardCharsets.UTF_8),sessionKey);

            byte[] sign = SHA.Sha256x2(allBytes);
            headerMap.put(UpStrings.SIGN, HexFormat.of().formatHex(sign));
            String responseJson = requestPost(url, headerMap, requestBodyJson);

            dataResponseBody = gson.fromJson(responseJson, ResponseBody.class);
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
