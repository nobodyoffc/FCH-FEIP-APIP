package NaSaRpcClient;//package NaSaRpc;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class test {
//    public static void main(String[] args) {
//
//    }
//    public void request(){
//        Map<String,String> headerMap = new HashMap<>();
//        headerMap.put("Accept", "application/json");
//        headerMap.put("Content-type", "application/json");
//
//        try {
//            // Create an HTTP client
//            HttpClient httpClient = HttpClients.createDefault();
//
//            // Create an HTTP POST request
//            HttpPost httpPost = new HttpPost(rpcUrl);
//
//            // Construct the JSON-RPC request to list UTXOs for the address
//            String jsonRpcRequest = "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"listunspent\",\"params\":["+minConf+", 999999999,[\"" + address + "\"]]}";
//            StringEntity entity = new StringEntity(jsonRpcRequest);
//            httpPost.setEntity(entity);
//
//            // Set headers
//            httpPost.setHeader("Accept", "application/json");
//            httpPost.setHeader("Content-type", "application/json");
//
//            // Execute the request
//            HttpResponse response = httpClient.execute(httpPost);
//
//            // Parse the response
//            HttpEntity responseEntity = response.getEntity();
//            String responseBody = EntityUtils.toString(responseEntity);
//
//            System.out.println("UTXOs for address " + address + ": " + responseBody);
//            return responseBody;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
