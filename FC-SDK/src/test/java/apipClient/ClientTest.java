package apipClient;

import apipClass.Fcdsl;
import apipClass.TxInfo;
import javaTools.JsonTools;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HexFormat;
import java.util.List;

public class ClientTest {
    public static void main(String[] args) throws IOException {

    }



    private static void totalsGet() {
        String urlHead="https://cid.cash/APIP";
        ApipClient body = new OpenAPIs().totalsGet(urlHead);
        JsonTools.gsonPrint(body.getResponseBody());
    }

    private static void totalsPost() throws IOException {
        SessionSample sessionSample = getSessionSample();
        byte[] sessionKey = HexFormat.of().parseHex(sessionSample.sessionKey);
        String urlHead = "https://cid.cash/APIP";
        ApipClient body = new OpenAPIs().totalsPost(urlHead, null, sessionKey);
        JsonTools.gsonPrint(body.getResponseBody());

    }


    @NotNull
    private static SessionSample getSessionSample() {
        String headerSignKey = "Sign";
        String headerSessionNameKey = "SessionName";
        String headerSessionNameValue = "d0ae4755bbb4";
        String sessionKey = "d0ae4755bbb42cf9074e969caa5305ca90182ac1b6ebe639105b3faecbbd281a";
;
//        String headerSessionNameValue= "d2b1b8fd910a";
//        String sessionKey ="d2b1b8fd910afff3130cc526722fe92ed555adce8dc8e55b803a9afd8c740b08";
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
