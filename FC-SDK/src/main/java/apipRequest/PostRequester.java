package apipRequest;

import apipClass.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.*;
import cryptoTools.SHA;
import fipaClass.LinkInfo;
import feipClass.Service;
import fipaClass.Signature;
import javaTools.BytesTools;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static apipTools.ApipTools.isGoodSign;

public class PostRequester {
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

    public static LinkInfo parseApipResponse(HttpResponse response){
        if(response==null)return null;
        Gson gson = new Gson();
        ResponseBody replier;
        String sign;
        byte[] bodyBytes;
        LinkInfo linkInfo = new LinkInfo();
        try{
            sign = response.getHeaders(UpStrings.SIGN)[0].getValue();

            bodyBytes = response.getEntity().getContent().readAllBytes();
            String responseBody = new String(bodyBytes);

            replier = gson.fromJson(responseBody, ResponseBody.class);
            System.out.println(replier.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Signature signature = new Signature(sign);
        linkInfo.setSignature(signature);
        linkInfo.setBodyBytes(bodyBytes);
        linkInfo.setBody(replier);
        return linkInfo;
    }

    public static HttpResponse postRequestCheckSign(String requestUrl, HashMap<String, String> headerMap, String requestBody, byte[]sessionKey) {
        HttpResponse response = postRequest(requestUrl, headerMap, requestBody);
        if(response==null)return null;

        Header[] headers = response.getHeaders(UpStrings.SIGN);

        if(headers == null||headers.length==0){
            System.out.println("No sign in header.");
            return response;
        }

        String sign = headers[0].getValue();

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

    public static List<Service> searchOwnerServices(ApipParamsForClient apipDataRequestParams, byte[]sessionKey, String owner, String type, boolean onlyActive, boolean ignoreClosed) {
        String ask = "Search your maker services...";
        String urlTail = ApiNames.APIP6V1Path+ApiNames.ServiceSearchAPI;
        Fcdsl fcdsl = makeFcdslForService(owner, type,"20",null,onlyActive,ignoreClosed);

        return getPostResponseList(apipDataRequestParams, sessionKey, ask, urlTail, fcdsl, Service.class);
    }

    @Nullable
    public static <T> List<T> getPostResponseList(ApipParamsForClient apipDataRequestParams, byte[] sessionKey, String ask, String urlTail, Fcdsl fcdsl, Class<T> tClass) {
        try {
            Type t = new TypeToken<List<T>>() {}.getType();
            System.out.println(ask);
            LinkInfo response = requestApip(apipDataRequestParams, sessionKey, urlTail, fcdsl);
            ResponseBody responseBody= (ResponseBody)response.getBody();

            Gson gson1 = new Gson();

            if(responseBody.getCode()!=0){
                System.out.println(responseBody.getMessage());
                return null;
            }else {
                Object data = responseBody.getData();
                return gson1.fromJson(gson1.toJson(data), t);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Requesting APIP wrong.");
            return null;
        }
    }

    public static Map<String, Service> getServiceByIds(ApipParamsForClient apipDataRequestParams, String[] sids, byte[]sessionKey) {
        System.out.println("Get services "+sids);

        Map<String,Service> serviceMap = null;
        try {
            String urlTail = ApiNames.APIP6V1Path+ApiNames.ServiceByIdsAPI;

            Fcdsl fcdsl = makeFcdslForService(sids);

            LinkInfo response = requestApip(apipDataRequestParams, sessionKey, urlTail, fcdsl);
            ResponseBody responseBody= (ResponseBody) response.getBody();

            Gson gson1 = new Gson();

            if(responseBody.getCode()!=0){
                System.out.println(responseBody.getMessage());
            }else {
                Object data = responseBody.getData();
                Type t = new TypeToken<Map<String,Service>>() {
                }.getType();
                
                serviceMap = gson1.fromJson(gson1.toJson(data), t);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Requesting APIP wrong.");
            return null;
        }
        return serviceMap;
    }

    private static LinkInfo requestApip(ApipParamsForClient apipDataRequestParams, byte[] sessionKey, String urlTail, Fcdsl fcdsl) {
        String url = apipDataRequestParams.getUrlHead()+ urlTail;
        RequestBody dataRequestBody = new RequestBody();

        dataRequestBody.makeRequestBody(url, apipDataRequestParams.getVia());

        dataRequestBody.setFcdsl(fcdsl);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(Strings.SESSION_NAME, apipDataRequestParams.getSessionName());
        Gson gson = new Gson();
        String requestBodyJson = gson.toJson(dataRequestBody);

        byte[] allBytes=BytesTools.bytesMerger(requestBodyJson.getBytes(StandardCharsets.UTF_8), sessionKey);

        byte[] sign = SHA.Sha256x2(allBytes);
        headerMap.put(UpStrings.SIGN, HexFormat.of().formatHex(sign));

        HttpResponse response = postRequestCheckSign(url, headerMap, requestBodyJson,sessionKey);

        return parseApipResponse(response);
    }


    public static Fcdsl makeFcdslForService(String[] sids) {
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIds(sids);
        return fcdsl;
    }

    public static Fcdsl makeFcdslForService(String owner, String type, String size, List<String> after, boolean onlyActive, boolean ignoreClosed) {
        Fcdsl fcdsl = new Fcdsl();

        Query query = new Query();
        Terms terms = new Terms();
        String[] termsFields = new String[]{FieldNames.OWNER};
        String[] termsValues = new String[]{owner};
        terms.setFields(termsFields);
        terms.setValues(termsValues);
        query.setTerms(terms);
        fcdsl.setQuery(query);

        if(type!=null) {
            Filter filter = new Filter();
            Terms terms1 = new Terms();
            String[] terms1Fields = new String[]{Strings.TYPES};
            String[] terms1Values = new String[]{type};
            terms1.setFields(terms1Fields);
            terms1.setValues(terms1Values);
            filter.setTerms(terms1);
            fcdsl.setFilter(filter);
        }
        if(onlyActive){
            Except except = new Except();
            Terms terms2 = new Terms();
            String[] terms2Fields = new String[]{Strings.ACTIVE};
            String[] terms2Values = new String[]{Values.FALSE};
            terms2.setFields(terms2Fields);
            terms2.setValues(terms2Values);
            except.setTerms(terms2);
            fcdsl.setExcept(except);
        }else if(ignoreClosed){
            Except except = new Except();
            Terms terms2 = new Terms();
            String[] terms2Fields = new String[]{Strings.CLOSED};
            String[] terms2Values = new String[]{Values.TRUE};
            terms2.setFields(terms2Fields);
            terms2.setValues(terms2Values);
            except.setTerms(terms2);
            fcdsl.setExcept(except);
        }

        if(size!=null)fcdsl.setSize(size);

        if(after!=null)fcdsl.setAfter(after);

        ArrayList<Sort> sortList = Sort.makeSortList("active", false, "lastTime", false, "sid", true);
        fcdsl.setSort(sortList);

        return fcdsl;
    }
}
