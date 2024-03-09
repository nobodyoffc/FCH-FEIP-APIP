package NaSaRpcClient.data;

import java.util.List;

public class TransactionRPC {
    private double amount;
    private double fee;
    private int confirmations;
    private String blockhash;
    private int blockindex;
    private long blocktime;
    private String txid;
    private List<String> walletconflicts;
    private long time;
    private long timereceived;
    private String bip125_replaceable;
    private List<TransactionDetail> details;
    private String hex;

    // Getters and setters for the fields

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getBlockhash() {
        return blockhash;
    }

    public void setBlockhash(String blockhash) {
        this.blockhash = blockhash;
    }

    public int getBlockindex() {
        return blockindex;
    }

    public void setBlockindex(int blockindex) {
        this.blockindex = blockindex;
    }

    public long getBlocktime() {
        return blocktime;
    }

    public void setBlocktime(long blocktime) {
        this.blocktime = blocktime;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public List<String> getWalletconflicts() {
        return walletconflicts;
    }

    public void setWalletconflicts(List<String> walletconflicts) {
        this.walletconflicts = walletconflicts;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTimereceived() {
        return timereceived;
    }

    public void setTimereceived(long timereceived) {
        this.timereceived = timereceived;
    }

    public String getBip125_replaceable() {
        return bip125_replaceable;
    }

    public void setBip125_replaceable(String bip125_replaceable) {
        this.bip125_replaceable = bip125_replaceable;
    }

    public List<TransactionDetail> getDetails() {
        return details;
    }

    public void setDetails(List<TransactionDetail> details) {
        this.details = details;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}

class TransactionDetail {
    private boolean involvesWatchonly;
    private String account;
    private String address;
    private String category;
    private double amount;
    private String label;
    private int vout;
    private double fee;
    private boolean abandoned;

    // Getters and setters for the fields

    public boolean isInvolvesWatchonly() {
        return involvesWatchonly;
    }

    public void setInvolvesWatchonly(boolean involvesWatchonly) {
        this.involvesWatchonly = involvesWatchonly;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getVout() {
        return vout;
    }

    public void setVout(int vout) {
        this.vout = vout;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public void setAbandoned(boolean abandoned) {
        this.abandoned = abandoned;
    }
}

