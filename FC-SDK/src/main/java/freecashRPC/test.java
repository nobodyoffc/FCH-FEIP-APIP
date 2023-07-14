package freecashRPC;

import org.junit.Test;

public class test {
    public static void main(String[] args) throws Throwable {
        //RPC.NewFcRpcClient.getFcRpcClient("127.0.0.1",8332,"liu");
//        NewFcRpcClient newFcRpcClient = new NewFcRpcClient("127.0.0.1",8332,"liu");
//        JsonRpcHttpClient fcClient = newFcRpcClient.getClient();

        NewFcRpcClient newFcRpcClient = new NewFcRpcClient("127.0.0.1",8332,"user","password");
        newFcRpcClient.getClientSilent();
    }
}
