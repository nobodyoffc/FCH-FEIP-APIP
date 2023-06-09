package RPC;

import org.junit.Test;

public class test {
    public static void main(String[] args) throws Throwable {
        //RPC.NewFcRpcClient.getFcRpcClient("127.0.0.1",8332,"liu");
//        NewFcRpcClient newFcRpcClient = new NewFcRpcClient("127.0.0.1",8332,"liu");
//        JsonRpcHttpClient fcClient = newFcRpcClient.getClient();

        NewFcRpcClient.getFcRpcClient("127.0.0.1",8332,"liu");
    }


    @Test
    public void getClient() throws Throwable {
        NewFcRpcClient.getFcRpcClient("127.0.0.1",8332,"liu");

    }
}
