package fipaClass;

public enum Algorithm {
    ECC256k1_AES256CBC("ECC256k1_AES256CBC"),
    EccAes256BitPay_No1_NrC7("EccAes256BitPay@No1_NrC7"),
    EccAes256K1P7_No1_NrC7("EccAes256K1P7@No1_NrC7"),
    EcdsaBtcMsg_No1_NrC7("EcdsaBtcMsg@No1_NrC7"),
    SignTxByCryptoSign_No1_NrC7("SignTxByCryptoSign@No1_NrC7");

//    private final String name;

    Algorithm(String name) {}

    public String getName() {
        return this.name();
    }
}
