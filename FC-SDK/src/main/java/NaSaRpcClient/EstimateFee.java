package NaSaRpcClient;

import com.google.gson.Gson;
import javaTools.JsonTools;
import org.junit.Test;

public class EstimateFee {
    private Object[] params;
    private double result;
    private ResultSmart resultSmart;

    public static String ESTIMATE_FEE = "estimatefee";
    public static String ESTIMATE_SMART_FEE = "estimatesmartfee";

    @Test
    public void test(){
        estimatefee( "http://127.0.0.1:8332", "username", "password");
        JsonTools.gsonPrint(result);

        estimatefee(6, "http://127.0.0.1:22555", "username", "password");
        JsonTools.gsonPrint(result);


        estimatesmartfee(3, "http://127.0.0.1:22555", "username", "password");
        JsonTools.gsonPrint(resultSmart);
    }
    public double estimatefee(String url, String username, String password){

        RpcRequest jsonRPC2Request = new RpcRequest(ESTIMATE_FEE, null);

        JsonTools.gsonPrint(jsonRPC2Request);

        result = (double) RpcRequest.requestRpc(url, username,password,ESTIMATE_FEE,jsonRPC2Request);
        return result;
    }
    public double estimatefee(int nBlocks, String url, String username, String password){
        params = new Object[]{nBlocks};
        RpcRequest jsonRPC2Request = new RpcRequest(ESTIMATE_FEE, params);

        JsonTools.gsonPrint(jsonRPC2Request);

        result = (double) RpcRequest.requestRpc(url, username,password,ESTIMATE_FEE,jsonRPC2Request);
        return result;
    }
    public ResultSmart estimatesmartfee(int nBlocks, String url, String username, String password){
        params = new Object[]{nBlocks};
        RpcRequest jsonRPC2Request = new RpcRequest(ESTIMATE_SMART_FEE, params);

        JsonTools.gsonPrint(jsonRPC2Request);

        Object result1 = RpcRequest.requestRpc(url, username, password, ESTIMATE_SMART_FEE, jsonRPC2Request);
        Gson gson = new Gson();
        resultSmart = gson.fromJson(gson.toJson(result1),ResultSmart.class);
        return resultSmart;
    }
    public class ResultSmart{
        private double feerate;
        private long blocks;

        public double getFeerate() {
            return feerate;
        }

        public void setFeerate(double feerate) {
            this.feerate = feerate;
        }

        public long getBlocks() {
            return blocks;
        }

        public void setBlocks(long blocks) {
            this.blocks = blocks;
        }
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}
