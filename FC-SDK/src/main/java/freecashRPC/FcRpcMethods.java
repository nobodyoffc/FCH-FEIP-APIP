package freecashRPC;

import com.google.gson.Gson;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import javaTools.JsonTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FcRpcMethods {

    public static String getChainInfo(JsonRpcHttpClient fcClient) throws Throwable {
        return fcClient.invoke("getblockchaininfo",new Object[] {},String.class);
    }
    public static String getRawTx(JsonRpcHttpClient fcClient, String  txid) throws Throwable {
        if(txid==null)return null;
        Object[] params = new Object[] { txid };
        return fcClient.invoke("getrawtransaction",params,String.class);
    }

    public static String[] getTxIds(JsonRpcHttpClient fcClient)  {
        ArrayList<String> idList;
        try {
            idList = (ArrayList<String>) fcClient.invoke("getrawmempool", new Object[]{}, Object.class);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
        return idList.toArray(new String[0]);
    }

    public static String decodeTx(JsonRpcHttpClient fcClient, Object rawTx){
        if(rawTx==null||"".equals(rawTx))return null;
        Object result;
        try {
            result = fcClient.invoke("decoderawtransaction", new Object[]{rawTx}, Object.class);
        } catch (Throwable e) {
            return e.getMessage();
        }
        return JsonTools.getNiceString(result);
    }

    public static String sendTx(JsonRpcHttpClient fcClient, String rawTx) {
        if(rawTx==null||"".equals(rawTx))return null;
        String result = null;
        try {
            result = fcClient.invoke("sendrawtransaction", new String[]{rawTx}, String.class);
            return JsonTools.getNiceString(result);
        } catch (Throwable e) {
            return e.getMessage();
        }
    }

    public static String createRawTx(JsonRpcHttpClient fcClient, List<Map<String, Object>> inputs,Map<String, Object> outputs) throws Throwable {

        Object[] params = new Object[]{inputs, outputs};
        String rawTransactionHex = (String) fcClient.invoke("createrawtransaction", params, Object.class);
        return rawTransactionHex;
    }

    public static SignResult signRawTxWithWallet(JsonRpcHttpClient fcClient, String rawUnsignedTx) throws Throwable {
        Object[] params = new Object[]{rawUnsignedTx};
        Gson gson = new Gson();
        Object result = fcClient.invoke("signrawtransactionwithwallet", params, Object.class);
        SignResult signResult = gson.fromJson(JsonTools.getNiceString(result),SignResult.class);
        return signResult;
    }
}
