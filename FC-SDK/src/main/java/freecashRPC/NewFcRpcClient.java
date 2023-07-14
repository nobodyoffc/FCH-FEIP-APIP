package freecashRPC;

import com.googlecode.jsonrpc4j.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NewFcRpcClient {
    private JsonRpcHttpClient client;
    private String rpcUser;
    private String rpcAllowip;
    private String rpcIp;
    private int rpcPort ;//8332

    private String rpcPassword;

    private static final Logger log = LoggerFactory.getLogger(NewFcRpcClient.class);
    public NewFcRpcClient(String rpcIp,int rpcPort,String rpcUser,String rpcPassword) {
        this.rpcUser = rpcUser;
        this.rpcIp = rpcIp;
        this.rpcPort = rpcPort;
        this.rpcPassword = rpcPassword;
    }
    public JsonRpcHttpClient getClientSilent()  {
        JsonRpcHttpClient client = null;

        String cred = Base64.encodeBytes((rpcUser + ":" + rpcPassword).getBytes());
        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("Authorization", "Basic " + cred);
        try {
            client = new JsonRpcHttpClient(new URL("http://" + rpcIp + ":" + rpcPort), headers);

        } catch (MalformedURLException e) {
            log.error("MalformedURLException when creating Freecash RPC client.",e);
        }
        try {
            ParseTools.gsonPrint(client.invoke("getblockchaininfo",new Object[]{},Object.class));
        } catch (Throwable e) {
            log.error("Create Freecash RPC client error.",e);
            return null;
        }
        log.debug("Freecash RPC client is ready.");

        return client;
    }
    public JsonRpcHttpClient getClient(BufferedReader br) throws IOException {
        System.out.println("Creating freecash RPC client...");
        JsonRpcHttpClient client;

        while(true) {
            if(rpcPassword==null) {
                System.out.println("--------\nInput the password of user " + rpcUser + " to freecash RPC, enter to exit: ");
                String rpcPassword = br.readLine();
                if ("".equals(rpcPassword)) return null;
                this.rpcPassword = rpcPassword;
                client=getClientSilent();
                if(client!=null)break;
            }
        }
        return client;
    }

}
