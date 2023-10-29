import apipRequest.Reply;
import com.google.gson.Gson;
import cryptoTools.SHA;
import javaTools.BytesTools;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;

import static apipRequest.PostRequester.postRequest;
import static apipRequest.PostRequester.postRequestCheckSign;

public class HttpTest {
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

        HttpResponse response = postRequest(requestUrl, headerMap, requestBody);
        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        System.out.println("no check response body:\n"+ responseBody);
        Header[] headers = response.getHeaders("Code");
        Header header1 = headers[0];
        System.out.println("header[0]:");
        System.out.println("name:"+header1.getName());
        System.out.println("value:"+header1.getValue());

        System.out.println("code header len:"+headers.length);

        Header[] allHeaders = response.getAllHeaders();
        for(Header header:allHeaders){
            System.out.print(header.getName());
            System.out.println(":"+header.getValue());
        }

        System.out.println();
        HttpResponse responseCheck = postRequestCheckSign(requestUrl, headerMap, requestBody, sessionKeyBytes);
        responseBody = new String(responseCheck.getEntity().getContent().readAllBytes());
        System.out.println("checked:"+responseBody);

        Gson gson = new Gson();
        Reply replier = gson.fromJson(responseBody, Reply.class);
        System.out.println(replier.getMessage());

    }




}
