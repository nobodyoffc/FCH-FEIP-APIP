package freecashRPC;

import com.google.gson.Gson;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import fcTools.ParseTools;

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
        return ParseTools.gsonString(result);
    }

    public static String sendTx(JsonRpcHttpClient fcClient, Object rawTx) {
        if(rawTx==null||"".equals(rawTx))return null;
        Object result = null;
        try {
            result = fcClient.invoke("sendrawtransaction", new Object[]{rawTx}, Object.class);
            return ParseTools.gsonString(result);
        } catch (Throwable e) {
            return e.getMessage();
        }
    }

//createrawtransaction "[{\"txid\":\"9f5d54e8e134b145753de35c2417b9d21df5e06a072a0f780a99af9d5298a515\",\"vout\":2}]" "[{\"data\":\"00010203\"}]"
    public static String createRawTx(JsonRpcHttpClient fcClient, List<Map<String, Object>> inputs,Map<String, Object> outputs) throws Throwable {

        Object[] params = new Object[]{inputs, outputs};
        String rawTransactionHex = (String) fcClient.invoke("createrawtransaction", params, Object.class);
        return rawTransactionHex;
    }

    public static SignResult signRawTxWithWallet(JsonRpcHttpClient fcClient, String rawUnsignedTx) throws Throwable {
        Object[] params = new Object[]{rawUnsignedTx};
        Gson gson = new Gson();
        Object result = fcClient.invoke("signrawtransactionwithwallet", params, Object.class);
        SignResult signResult = gson.fromJson(ParseTools.gsonString(result),SignResult.class);
        return signResult;
    }
}
