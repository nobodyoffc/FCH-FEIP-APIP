package RPC;

import com.googlecode.jsonrpc4j.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public JsonRpcHttpClient getClient() throws IOException {
        System.out.println("Creating freecash RPC client...");
        JsonRpcHttpClient client = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            if(rpcPassword==null) {
                System.out.println("--------\nInput the password of user " + rpcUser + " to freecash RPC, enter to exit: ");
                String rpcPassword = br.readLine();
                if ("".equals(rpcPassword)) return null;
                this.rpcPassword = rpcPassword;
            }
            try {
                String cred = Base64.encodeBytes((rpcUser + ":" + rpcPassword).getBytes());
                Map<String, String> headers = new HashMap<String, String>(1);
                headers.put("Authorization", "Basic " + cred);
                client = new JsonRpcHttpClient(new URL("http://" + rpcIp + ":" + rpcPort), headers);
                ParseTools.gsonPrint(client.invoke("getblockchaininfo",new Object[]{},Object.class));
                log.debug("Freecash RPC client is ready.");
                break;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Failed to Create freecash RPC.");
                break;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        br.close();
        return client;
    }

    public static JsonRpcHttpClient getFcRpcClient(String rpcIp, long rpcPort,String rpcUser) throws Throwable {

        JsonRpcHttpClient client;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Input the password of user "+rpcUser+" for freecash RPC, enter to exit: ");
        String rpcPassword = br.readLine();
        if ("".equals(rpcPassword)) return null;
        try {

            String cred = Base64.encodeBytes((rpcUser + ":" + rpcPassword).getBytes());
            //TODO
            System.out.println(cred);
            Map<String, String> headers = new HashMap<String, String>(1);
            headers.put("Authorization", "Basic " + cred);
            client = new JsonRpcHttpClient(new URL("http://" + rpcIp + ":" + rpcPort), headers);
            ParseTools.gsonPrint(client.invoke("getblockchaininfo", new Object[]{}, Object.class));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        br.close();
        return client;
    }
}
