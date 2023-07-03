package fcTools;

import FchClass.Cash;
import com.google.gson.Gson;

import java.util.List;

public class CryptoSigner {
    public static String makeRawTxForCs(DataForOffLineTx sendRequestForCs, List<Cash> meetList) {
        Gson gson = new Gson();
        StringBuilder RawTx = new StringBuilder("[");
        int i =0;
        for(Cash cash:meetList){
            if(i>0)RawTx.append(",");
            RawTxForCs rawTxForCs = new RawTxForCs();
            rawTxForCs.setAddress(cash.getFid());
            rawTxForCs.setAmount((double) cash.getValue() / FcConstant.FchToSatoshi);
            rawTxForCs.setTxid(cash.getBirthTxId());
            rawTxForCs.setIndex(cash.getBirthIndex());
            rawTxForCs.setSeq(i);
            rawTxForCs.setDealType(1);
            RawTx.append(gson.toJson(rawTxForCs));
            i++;
        }
        int j = 0;
        if(sendRequestForCs.getSendToList()!=null) {
            for (SendTo sendTo : sendRequestForCs.getSendToList()) {
                RawTxForCs rawTxForCs = new RawTxForCs();
                rawTxForCs.setAddress(sendTo.getFid());
                rawTxForCs.setAmount(sendTo.getAmount());
                rawTxForCs.setSeq(j);
                rawTxForCs.setDealType(2);
                RawTx.append(",");
                RawTx.append(gson.toJson(rawTxForCs));
                j++;
            }
        }

        if(sendRequestForCs.getMsg()!=null) {
            RawOpReturnForCs rawOpReturnForCs = new RawOpReturnForCs();
            rawOpReturnForCs.setMsg(sendRequestForCs.getMsg());
            rawOpReturnForCs.setMsgType(2);
            rawOpReturnForCs.setSeq(j);
            rawOpReturnForCs.setDealType(3);
            RawTx.append(",");
            RawTx.append(gson.toJson(rawOpReturnForCs));
        }
        RawTx.append("]");
        return RawTx.toString();
    }

    public static DataForOffLineTx parseDataForOffLineTxFromOther(Object other) {
        Gson gson = new Gson();
        ParseTools.gsonPrint(other);
        return gson.fromJson(gson.toJson(other), DataForOffLineTx.class);
    }
}
