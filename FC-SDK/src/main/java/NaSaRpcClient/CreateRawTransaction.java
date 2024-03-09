package NaSaRpcClient;

import NaSaRpcClient.data.TransactionBrief;
import com.google.gson.Gson;
import javaTools.JsonTools;
import org.junit.Test;

import java.util.*;

public class CreateRawTransaction {
    private CreateRawTransactionParamsFch paramsFch;
    private CreateRawTransactionParamsDoge paramsDoge;
    private String result;


    public static String method = "createrawtransaction";

    @Test
    public void test(){
        String urlDoge = "http://127.0.0.1:22555";
        String urlFch = "http://127.0.0.1:8332";
        result = createRawTransactionFch("FJYN3D7x4yiLF692WUAe7Vfo2nQpYDNrC7", 0.1, "hi", urlFch,"username","password");
        JsonTools.gsonPrint(result);
        result = createRawTransactionDoge("DHrM7fdWX5Px9vSbPNW3gsJrtG872HU3JB", 0.1, "hi", urlDoge,"username","password");
        JsonTools.gsonPrint(result);
    }

    public String createRawTransactionFch(String toAddr, double amount, String opreturn, String url, String username, String password){
        CreateRawTransactionParamsFch createRawTransactionParams = new CreateRawTransactionParamsFch(toAddr,amount,opreturn);
        RpcRequest jsonRPC2Request = new RpcRequest(method, createRawTransactionParams.toParams());

        JsonTools.gsonPrint(jsonRPC2Request);

        Object result = RpcRequest.requestRpc(url,username,password,"listSinceBlock", jsonRPC2Request);
        return (String)result;
    }

    public String createRawTransactionDoge(String toAddr, double amount, String opreturn, String url, String username, String password){
        CreateRawTransactionParamsDoge createRawTransactionParams = new CreateRawTransactionParamsDoge(toAddr,amount,opreturn);
        RpcRequest jsonRPC2Request = new RpcRequest(method, createRawTransactionParams.toParams());

        JsonTools.gsonPrint(jsonRPC2Request);

        Object result = RpcRequest.requestRpc(url,username,password,"listSinceBlock", jsonRPC2Request);
        return (String)result;
    }

    public static class CreateRawTransactionParamsFch {
        private List<Input> inputs;
        private List<Map<String, Object>>  outputs; // addr:amount or data:hex
        private String lockTime;

        public CreateRawTransactionParamsFch(){};
        public CreateRawTransactionParamsFch(String addr, double amount, String opreturn) {
            this.inputs = new ArrayList<>();
            this.outputs = new ArrayList<>();
            Map<String,Object> output1 = new HashMap<>();
            output1.put(addr,amount);
            outputs.add(output1);
            if(opreturn!=null && !opreturn.isBlank()){
                Map<String,Object> output2 = new HashMap<>();
                output2.put("data", HexFormat.of().formatHex(opreturn.getBytes()));
                outputs.add(output2);
            }
        }

        public CreateRawTransactionParamsFch(List<Input> inputs, List<Map<String, Object>> outputs, String lockTime) {
            this.inputs = inputs;
            this.outputs = outputs;
            this.lockTime = lockTime;
        }

        public List<Input> getInputs() {
            return inputs;
        }

        public void setInputs(List<Input> inputs) {
            this.inputs = inputs;
        }

        public List<Map<String, Object>> getOutputs() {
            return outputs;
        }

        public void setOutputs(List<Map<String, Object>> outputs) {
            this.outputs = outputs;
        }

        public String getLockTime() {
            return lockTime;
        }

        public void setLockTime(String lockTime) {
            this.lockTime = lockTime;
        }

        public class Input {
            private String txid;
            private int vout;
            private Integer sequence; // Optional, represented as Integer to allow null value

            public Input(String txid, int vout) {
                this.txid = txid;
                this.vout = vout;
            }

            public Input(String txid, int vout, Integer sequence) {
                this.txid = txid;
                this.vout = vout;
                this.sequence = sequence;
            }

            public String getTxid() {
                return txid;
            }

            public void setTxid(String txid) {
                this.txid = txid;
            }

            public int getVout() {
                return vout;
            }

            public void setVout(int vout) {
                this.vout = vout;
            }

            public Integer getSequence() {
                return sequence;
            }

            public void setSequence(Integer sequence) {
                this.sequence = sequence;
            }
        }

        public Object[] toParams(){
            List<Object> objects = new ArrayList<>();
            objects.add(inputs);
            objects.add(outputs);
            if(lockTime!=null) {
                objects.add(Long.parseLong(lockTime));
           }

            Object[] params = objects.toArray();
            //TODO
            JsonTools.gsonPrint(params);
            return params;
       }
        public String toJson(){
            return new Gson().toJson(toParams());
        }
    }

    public static class CreateRawTransactionParamsDoge {
        private List<Input> inputs;
        private Map<String, Object>  outputs; // addr:amount or data:hex
        private String lockTime;

        public CreateRawTransactionParamsDoge(){};
        public CreateRawTransactionParamsDoge(String addr, double amount, String opreturn) {
            this.inputs = new ArrayList<>();
            this.outputs = new HashMap<>();
            outputs.put(addr,amount);
            if(opreturn!=null && !opreturn.isBlank()){
                outputs.put("data", HexFormat.of().formatHex(opreturn.getBytes()));
            }
        }

        public CreateRawTransactionParamsDoge(List<Input> inputs, Map<String, Object> outputs, String lockTime) {
            this.inputs = inputs;
            this.outputs = outputs;
            this.lockTime = lockTime;
        }

        public List<Input> getInputs() {
            return inputs;
        }

        public void setInputs(List<Input> inputs) {
            this.inputs = inputs;
        }

        public Map<String, Object> getOutputs() {
            return outputs;
        }

        public void setOutputs(Map<String, Object> outputs) {
            this.outputs = outputs;
        }

        public String getLockTime() {
            return lockTime;
        }

        public void setLockTime(String lockTime) {
            this.lockTime = lockTime;
        }

        public class Input {
            private String txid;
            private int vout;
            private Integer sequence; // Optional, represented as Integer to allow null value

            public Input(String txid, int vout) {
                this.txid = txid;
                this.vout = vout;
            }

            public Input(String txid, int vout, Integer sequence) {
                this.txid = txid;
                this.vout = vout;
                this.sequence = sequence;
            }

            public String getTxid() {
                return txid;
            }

            public void setTxid(String txid) {
                this.txid = txid;
            }

            public int getVout() {
                return vout;
            }

            public void setVout(int vout) {
                this.vout = vout;
            }

            public Integer getSequence() {
                return sequence;
            }

            public void setSequence(Integer sequence) {
                this.sequence = sequence;
            }
        }

        public Object[] toParams(){
            List<Object> objects = new ArrayList<>();
            objects.add(inputs);
            objects.add(outputs);
            if(lockTime!=null) {
                objects.add(Long.parseLong(lockTime));
            }

            Object[] params = objects.toArray();
            return params;
        }
        public String toJson(){
            return new Gson().toJson(toParams());
        }
    }

    public CreateRawTransactionParamsFch getParamsFch() {
        return paramsFch;
    }

    public void setParamsFch(CreateRawTransactionParamsFch paramsFch) {
        this.paramsFch = paramsFch;
    }

    public static class ListSinceBlockResult {
        private TransactionBrief[] transactions;
        private String lastblock;

        public TransactionBrief[] getTransactions() {
            return transactions;
        }

        public void setTransactions(TransactionBrief[] transactions) {
            this.transactions = transactions;
        }

        public String getLastblock() {
            return lastblock;
        }

        public void setLastblock(String lastblock) {
            this.lastblock = lastblock;
        }
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
