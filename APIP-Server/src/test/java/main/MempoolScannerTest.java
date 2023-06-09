package main;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.googlecode.jsonrpc4j.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import FchClass.Cash;
import javaTools.BytesTools;
import fcTools.ParseTools;
import fcTools.ParseTools.VarintResult;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import servers.EsTools;
import servers.NewEsClient;
import startAPIP.ConfigAPIP;
import startFCH.IndicesFCH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static fcTools.ParseTools.parseVarint;

public class MempoolScannerTest extends Thread {
    /**
     * 1. getrawmempool 获得交易id数组
     * 2. getmempooltransaction 获得原始交易
     * 3. 解析原始交易，得到 1)input的id列表,2)output新cash列表
     * 4. 从Es获得input的cash信息
     * 5. redis开一个新库：1）id为address；2）key：income数量，income金额，spend数量，spend金额，net净变化。
     * 6. 将所有in和out按addr累加到redis
     * 7. API查询addrs,响应income数量，income金额，spend数量，spend金额，net净变化。
     */
    private Jedis jedis3Unconfirmed;
    private JsonRpcHttpClient client;
    private static NewEsClient newEsClient = new NewEsClient();
    private ElasticsearchClient esClient = null;

    private String rpcUser; //验证用户名
    private String rpcPassword; //验证密码
    private String rpcIp; //验证ip
    private int rpcPort ;// "8332"验证端口

    private String esIp; //验证端口
    private int esPort; //验证端口

    private String InCashListKey = "inCashList";
    private String OutCashListKey = "outCashList";

    private String SpendCount = "spendCount";
    private String SpendValue = "spendValue";
    private String IncomeCount = "incomeCount";
    private String IncomeValue = "incomeValue";

    public MempoolScannerTest() {
        this.jedis3Unconfirmed = new Jedis();
        {
            jedis3Unconfirmed.select(3);
        }

        ConfigAPIP configAPIP = new ConfigAPIP();
        try {
            configAPIP = configAPIP.getClassInstanceFromFile(ConfigAPIP.class);

            configAPIP.writeConfigToFile();
            this.rpcUser = configAPIP.getRpcUser();
            this.rpcIp = configAPIP.getRpcIp();
            this.rpcPort = configAPIP.getRpcPort();
            this.esIp = configAPIP.getRpcIp();
            this.esPort = configAPIP.getEsPort();

            this.esClient = newEsClient.checkEsClient(esClient,configAPIP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        System.out.println("Scanning mempool...");
        while(true) {
            String[] txIds = new String[0];
            try {
                txIds = getTxIds();
                for (String txid : txIds) {
                    byte[] txBytes = BytesTools.hexToByteArray(getRawTx(txid));
                    Map<String, List<Cash>> cashListMap = paseTx(txBytes, txid);
                    addCashListToRedis(cashListMap);
                }
                TimeUnit.SECONDS.sleep(30);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void addCashListToRedis(Map<String, List<Cash>> cashListMap) {
        List<Cash> inList = cashListMap.get(InCashListKey);
        List<Cash> outList = cashListMap.get(OutCashListKey);

        for(Cash cash:inList){
            String addr = cash.getFid();
            //income数量，income金额，spend数量，spend金额，net净变化
            int spendCount = 0;
            long spendValue = 0;
            if(jedis3Unconfirmed.hget(addr,SpendCount)!=null) {
                spendCount = Integer.parseInt(jedis3Unconfirmed.hget(addr, SpendCount));
                spendValue = Long.parseLong(jedis3Unconfirmed.hget(addr, SpendValue));
            }
            spendValue += cash.getValue();
            spendCount++;
            jedis3Unconfirmed.hset(addr,SpendValue,String.valueOf(spendValue));
            jedis3Unconfirmed.hset(addr,SpendCount,String.valueOf(spendCount));
        }

        for(Cash cash:outList){
            String addr = cash.getFid();
            //income数量，income金额，spend数量，spend金额，net净变化
            int incomeCount = 0;
            long incomeValue = 0;
            if(jedis3Unconfirmed.hget(addr,IncomeCount)!=null) {
                incomeCount = Integer.parseInt(jedis3Unconfirmed.hget(addr, IncomeCount));
                incomeValue = Long.parseLong(jedis3Unconfirmed.hget(addr, IncomeValue));
            }
            incomeValue += cash.getValue();
            incomeCount++;
            jedis3Unconfirmed.hset(addr,IncomeValue,String.valueOf(incomeValue));
            jedis3Unconfirmed.hset(addr,IncomeCount,String.valueOf(incomeCount));
        }
    }

    private Map<String, List<Cash>>  paseTx(byte[] txBytes, String txid) throws Exception {

        // Read tx version/读取交易的版本
        byte[] b4Version = new byte[4];
        ByteArrayInputStream blockInputStream = new ByteArrayInputStream(txBytes);
        blockInputStream.read(b4Version);

        // Read inputs /读输入
        // ParseTxOutResult parseTxOutResult
        List<String> inIdList = parseInput(blockInputStream);

        List<Cash> inCashList = getInCashList(inIdList);

        // Read outputs /读输出
        // Parse Outputs./解析输出。
        List<Cash> outCashList = parseOut(blockInputStream,txid);

        Map<String, List<Cash>> cashListMap = new HashMap<>();

        cashListMap.put(InCashListKey,inCashList);
        cashListMap.put(OutCashListKey,outCashList);

        return cashListMap;

    }

    private List<Cash> parseOut(ByteArrayInputStream blockInputStream, String txid) throws IOException {

        ArrayList<Cash> outList = new ArrayList<Cash>();// For returning outputs without

        // Parse output count.
        // 解析输出数量。
        VarintResult varintParseResult = new VarintResult();
        varintParseResult = ParseTools.parseVarint(blockInputStream);
        long outputCount = varintParseResult.number;


        for (int j = 0; j < outputCount; j++) {
            // Start one output.
            // 开始解析一个输出。
            Cash out = new Cash();
            out.setBirthIndex(j);

            // Read the value of this output in satoshi.
            // 读取该输出的金额，以聪为单位。
            byte[] b8Value = new byte[8];
            blockInputStream.read(b8Value);
            out.setValue(BytesTools.bytes8ToLong(b8Value,true));

            // Parse the length of script.
            // 解析脚本长度。
            varintParseResult = ParseTools.parseVarint(blockInputStream);
            long scriptSize = varintParseResult.number;

            byte[] bScript = new byte[(int) scriptSize];
            blockInputStream.read(bScript);

            // Add block and tx information to output./给输出添加区块和交易信息。
            // Add information where it from/添加来源信息
            out.setValid(true);
            out.setCashId(ParseTools.calcTxoId(txid, j));

            // Add this output to List.
            // 添加输出到列表。
            outList.add(out);
        }

        return outList;
    }

    private List<Cash> getInCashList(List<String> inIdList) throws Exception {
        EsTools.MgetResult<Cash> result = EsTools.getMultiByIdList(esClient, IndicesFCH.CashIndex, inIdList, Cash.class);
        return result.getResultList();
    }

    private List<String> parseInput(ByteArrayInputStream blockInputStream) throws IOException {

        // Get input count./获得输入数量
        VarintResult varintParseResult = new VarintResult();
        varintParseResult = parseVarint(blockInputStream);
        long inputCount = varintParseResult.number;

        // Read inputs /读输入
        List<String> inIdList = new ArrayList<>();
        for (int j = 0; j < inputCount; j++) {
            // Read preTXHash and preOutIndex./读取前交易哈希和输出索引。
            byte[] b36PreTxIdAndIndex = new byte[32 + 4];
            blockInputStream.read(b36PreTxIdAndIndex);

            inIdList.add(ParseTools.calcTxoIdFromBytes(b36PreTxIdAndIndex));
        }
        return inIdList;
    }


    private String getRawTx(String txid) throws Throwable {
        Object[] params = new Object[] { txid };
        return client.invoke("getrawtransaction",(Object) params,String.class);
    }

    private String[] getTxIds() throws Throwable {

        return client.invoke("getrawmempool",new Object[]{},String[].class);
    }

    // 比特币身份认证

    @Test
    public void getClient() throws Throwable {

        JsonRpcHttpClient client = null;
        Scanner sc = new Scanner(System.in);
       // while(true) {
            System.out.println("Input the password of user 'liu' to freecash RPC, enter to exit: ");
            rpcUser="liu";
            rpcPassword = "liu";//sc.nextLine();
            if("".equals(rpcPassword)) return;
            try {
                String cred = Base64.encodeBytes((rpcUser + ":" + rpcPassword).getBytes());
                //TODO
                System.out.println(cred);
                Map<String, String> headers = new HashMap<String, String>(1);
                headers.put("Authorization", "Basic " + cred);
                client = new JsonRpcHttpClient(new URL("http://" + "127.0.0.1" + ":" + 8332), headers);
                this.client = client;
                ParseTools.gsonPrint(client.invoke("getblockchaininfo",new Object[]{},Object.class));
                String[] ids = getTxIds();
                ParseTools.gsonPrint(ids);
                if(ids.length>0)
                    ParseTools.gsonPrint(getRawTx(ids[0]));
                String rawTx = "020000000106155b7150ba10949bdceea8a0f26cdd4641713ca8994165882e043202dcd10a000000006441baa4a2d1adb060599f6463fa004162aa5e821ae77e4624ee01c7448a400aa1480d03c9fc4e2e86134aff7d75edae83f1675aa07b218799e6fd3df55177581d764121030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312affffffff02404b4c00000000001976a91461c42abb6e3435e63bd88862f3746a3f8b86354288ac654a4c00000000001976a91461c42abb6e3435e63bd88862f3746a3f8b86354288ac00000000";

                //System.out.println(client.invoke("decoderawtransaction",new Object[]{rawTx,true},Object.class));
                Object result = client.invoke("decoderawtransaction", new Object[]{rawTx}, Object.class);
                System.out.println(ParseTools.gsonString(result));

                try {
                    result = client.invoke("sendrawtransaction", new Object[]{rawTx}, Object.class);

                }catch (Exception e){
                    String msg = e.getMessage();
                    System.out.println(msg);
                }
                ParseTools.gsonPrint(result);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to Create freecash RPC.");

            }
            String[] params = new String[0];
            System.out.println(client.invoke("getnewaddress", new Object[]{}, Object.class));
      //  }
        return ;
    }
}
