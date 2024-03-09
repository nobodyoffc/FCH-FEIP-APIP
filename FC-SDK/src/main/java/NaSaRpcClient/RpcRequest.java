package NaSaRpcClient;

import com.google.gson.Gson;
import javaTools.BytesTools;
import netTools.ApacheHttp;
import netTools.Constants;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RpcRequest {

    private String jsonrpc = "2.0";


    private String id;


    private String method;


    private Object[] params;

    // Constructor
    public RpcRequest(
            String method,
            Object[] params) {
        this.method = method;
        this.params = params;
        byte[] rd = BytesTools.getRandomBytes(4);
        this.id = String.valueOf(BytesTools.byte4ArrayToUnsignedInt(rd));
    }

    public static Object requestRpc(String url,String username,String password,String method, RpcRequest jsonRPC2Request) {

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(Constants.HeaderKey_Accept,Constants.HeaderValue_Application_Json);
        headerMap.put(Constants.HeaderKey_Content_type,Constants.HeaderValue_Application_Json);
        if(username!=null){
            String cred = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            headerMap.put(Constants.HeaderKey_Authorization,"Basic "+cred);
        }
        ApacheHttp.Request request = new ApacheHttp.Request(url, Constants.POST,jsonRPC2Request.toJson(),headerMap);
        ApacheHttp.Response response = ApacheHttp.request(request);

        boolean badResponse = false;
        if(response.isBadResponse())badResponse=true;

        String bodyStr = response.getBody();
        RpcResponse rpcResponse = null;
        if(bodyStr!=null){
            rpcResponse = new Gson().fromJson(bodyStr, RpcResponse.class);
            if(rpcResponse.isBadResult(method))badResponse=true;
        }

        return badResponse ? null : rpcResponse.getResult();
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    // Getters and setters (optional)
    // You can generate getters and setters for the private fields using your IDE.
    // For brevity, they are not included here.

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
