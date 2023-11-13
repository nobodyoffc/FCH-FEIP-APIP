import apipClass.ResponseBody;
import com.google.gson.Gson;
import cryptoTools.SHA;
import fcTools.ParseTools;
import fipaClass.LinkInfo;
import javaTools.BytesTools;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;

import static apipRequest.PostRequester.postRequest;
import static apipRequest.PostRequester.postRequestCheckSign;

public class HttpTest {
    public static void main(String[] args) throws IOException {

        testTotals();
    }

    private static void testTotals() {
        SessionSample sessionSample = getSessionSample();
        byte[] sessionKey = HexFormat.of().parseHex(sessionSample.sessionKey);


    }

    private static void testObjectTransfer() {
        LinkInfo response = new LinkInfo();
        response.setFrom("A");
        ResponseBody responseBody = new ResponseBody();
        responseBody.setMessage("body");
        response.setBody(responseBody);

        ResponseBody responseBody1= (ResponseBody)response.getBody();

        ParseTools.gsonPrint(responseBody1);
        System.out.println(responseBody1.getMessage());
    }

    private static void cidInfoByIdsText() throws IOException {
        RequestBodySampleCidInfoByIds sampleCidInfoByIds = getRequestBodySampleCidInfoByIds();
        SessionSample sessionSample = getSessionSample();

        RequestSample requestSample = getRequestSample(sampleCidInfoByIds, sessionSample);

        HttpResponse response = postRequest(sampleCidInfoByIds.requestUrl(), requestSample.headerMap(), sampleCidInfoByIds.requestBody());

        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        System.out.println("no check response body:\n"+ responseBody);
        Header[] headers = response.getHeaders("Code");
        Header header1 = headers[0];
        System.out.println("code header len:"+headers.length);
        System.out.println("header[0]:");
        System.out.println("name:"+header1.getName());
        System.out.println("value:"+header1.getValue());

        System.out.println();
        Header[] allHeaders = response.getAllHeaders();
        for(Header header:allHeaders){
            System.out.print(header.getName());
            System.out.println(":"+header.getValue());
        }

        System.out.println();
        HttpResponse responseCheck = postRequestCheckSign(sampleCidInfoByIds.requestUrl(), requestSample.headerMap(), sampleCidInfoByIds.requestBody(), requestSample.sessionKeyBytes());
        responseBody = new String(responseCheck.getEntity().getContent().readAllBytes());
        System.out.println("checked:"+responseBody);

        Gson gson = new Gson();
        ResponseBody replier = gson.fromJson(responseBody, ResponseBody.class);
        System.out.println(replier.getMessage());
    }

    @NotNull
    private static RequestSample getRequestSample(RequestBodySampleCidInfoByIds sampleCidInfoByIds, SessionSample sessionSample) {
        byte[] requestBodyBytes = sampleCidInfoByIds.requestBody().getBytes(StandardCharsets.UTF_8);
        byte[] sessionKeyBytes = HexFormat.of().parseHex(sessionSample.sessionKey());
        byte[] hashContentBytes = BytesTools.bytesMerger(requestBodyBytes, sessionKeyBytes);
        byte[] signBytes = SHA.Sha256x2(hashContentBytes);
        String headerSignValue = HexFormat.of().formatHex(signBytes);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(sessionSample.headerSignKey(),headerSignValue);
        headerMap.put(sessionSample.headerSessionNameKey(), sessionSample.headerSessionNameValue());
        RequestSample requestSample = new RequestSample(sessionKeyBytes, headerMap);
        return requestSample;
    }

    private record RequestSample(byte[] sessionKeyBytes, HashMap<String, String> headerMap) {
    }

    @NotNull
    private static SessionSample getSessionSample() {
        String headerSignKey = "Sign";
        String sessionKey = "7beec9dc02752c4d1009b4be8f65f7173f8e54c8f8baba35565e65f099ce4281";
        String headerSessionNameKey = "SessionName";
        String headerSessionNameValue = "7beec9dc0275";
        SessionSample sessionSample = new SessionSample(headerSignKey, sessionKey, headerSessionNameKey, headerSessionNameValue);
        return sessionSample;
    }

    private record SessionSample(String headerSignKey, String sessionKey, String headerSessionNameKey, String headerSessionNameValue) {
    }

    @NotNull
    private static RequestBodySampleCidInfoByIds getRequestBodySampleCidInfoByIds() {
        String requestBody = "{"
                + "\"url\": \"https://cid.cash/APIP/apip3/v1/cidInfoByIds\","
                + "\"time\": 1688888076895,"
                + "\"nonce\": 121,"
                + "\"via\":\"F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW\","
                + "\"fcdsl\":{"
                + "\"ids\":[\"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\", \"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv\"]"
                + "}}";

        String requestUrl = "https://cid.cash/APIP/apip3/v1/cidInfoByIds";
        RequestBodySampleCidInfoByIds sampleCidInfoByIds = new RequestBodySampleCidInfoByIds(requestBody, requestUrl);
        return sampleCidInfoByIds;
    }

    private record RequestBodySampleCidInfoByIds(String requestBody, String requestUrl) {
    }
}
