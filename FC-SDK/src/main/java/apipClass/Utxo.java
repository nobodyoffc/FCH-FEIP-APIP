package apipClass;

import fchClass.Cash;

public class Utxo {
    private String addr;
    private String txId;
    private int index;
    private double amount;
    private long cd;

    public static Utxo cashToUtxo(Cash cash) {
        Utxo utxo = new Utxo();
        utxo.setAddr(cash.getOwner());
        utxo.setTxId(cash.getBirthTxId());
        utxo.setIndex(cash.getBirthIndex());
        utxo.setAmount((double) cash.getValue()/100000000);
        utxo.setCd(cash.getCd());
        return utxo;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getCd() {
        return cd;
    }

    public void setCd(long cd) {
        this.cd = cd;
    }
}
