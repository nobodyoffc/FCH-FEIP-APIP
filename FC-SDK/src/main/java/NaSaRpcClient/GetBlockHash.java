package NaSaRpcClient;

import org.junit.Test;

public class GetBlockHash {

    public static String method = "getblockhash";

    @Test
    public void test(){

        long height= 10000;

        String urlFch = "http://127.0.0.1:8332";
        String urlDoge = "http://127.0.0.1:22555";
        String result = getBlockHash(height,urlFch,"username","password");
        System.out.println(result);

        result = getBlockHash(height,urlDoge,"username","password");
        System.out.println(result);
    }

    public String getBlockHash(long height, String url, String username, String password){
        RpcRequest jsonRPC2Request = new RpcRequest(method,new Object[]{height});
        return (String)RpcRequest.requestRpc(url, username,password,method,jsonRPC2Request);
    }
}
