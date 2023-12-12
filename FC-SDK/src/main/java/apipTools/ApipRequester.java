package apipTools;

import apipClass.Fcdsl;
import apipClass.TxInfo;
import apipClient.ApipClient;
import apipClient.ApipDataGetter;
import apipClient.BlockchainAPIs;

import java.util.List;

public class ApipRequester {

    public static List<TxInfo> getFidTxs(String urlHead, String fid, String via, byte[]sessionKey){
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.addNewQuery().addNewTerms().addNewFields("inMarks.fid","outMarks.fid").addNewValues(fid);
//        fcdsl.addNewAfter(last); //需要翻页时取上一次请求的ResponseBody中的last值。
        ApipClient apipClient = BlockchainAPIs.txSearchPost(urlHead,fcdsl,null,sessionKey);
        if(apipClient.isBadResponse("get TXs"))return null;
//        String[] last = apipClient.getResponseBody().getLast(); //本次取回的最后一条排序值，用于确定下一次请求位置。
        List<TxInfo> txInfoList = ApipDataGetter.getTxInfoList(apipClient);
        return txInfoList;
    }
}
