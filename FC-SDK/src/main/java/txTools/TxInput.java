package txTools;

import fchClass.Cash;

import java.util.ArrayList;
import java.util.List;

public class TxInput {

    private byte[] priKey32;

    private String txId;

    private long amount;

    private int index;

    public static List<TxInput> cashListToTxInputList(List<Cash> cashList, byte[]priKey32){
        List<TxInput> txInputList = new ArrayList<>();
        for (Cash cash:cashList){
            TxInput txInput = cashToTxInput(cash,priKey32);
            if(txInput!=null){
                txInputList.add(txInput);
            }
        }
        if(txInputList.isEmpty())return null;
        return txInputList;
    }
    public static TxInput cashToTxInput(Cash cash, byte[]priKey32){
        if(cash==null){
            System.out.println("Cash is null.");
            return null;
        }
        if(!cash.isValid()){
            System.out.println("Cash has been spent.");
            return null;
        }
        TxInput txInput = new TxInput();

        txInput.setPriKey32(priKey32);
        txInput.setAmount(cash.getValue());
        txInput.setTxId(cash.getBirthTxId());
        txInput.setIndex(cash.getBirthIndex());

        return txInput;
    }

    public byte[] getPriKey32() {
        return priKey32;
    }

    public void setPriKey32(byte[] priKey32) {
        this.priKey32 = priKey32;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
