package apipRequest;

import apipClass.*;
import fipaClass.LinkInfo;
import feipClass.Service;

public class ApipRequestBuilder {
    //1. urlBuilder;
    //2. headerMapBuilder;
    //3. responseFcBuilder;
    //4. transfer;
    //5. FcdslBuilder
    private Service apipService;
    private ApipParams apipParams;
    private ApipParamsForClient apipParamsForUser;
    private RequestBody signInRequestBody;
    private RequestBody dataRequestBody;
    private Fcdsl fcdsl;
    private ApipUrl apipUrls;
    private LinkInfo requestFc;
    private LinkInfo responseFc;
    private ResponseBody responseBody;
}
