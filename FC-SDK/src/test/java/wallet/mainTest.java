package wallet;

import fc.Hex;
import fcTools.FchMainNetwork;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.Wallet;
import txTools.RawTxParser;

import java.util.HexFormat;
import java.util.concurrent.ExecutionException;

public class mainTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException, BlockStoreException {
        FchMainNetwork params = FchMainNetwork.MAINNETWORK;
        Wallet wallet = Wallet.createDeterministic(params, Script.ScriptType.P2PKH);
        // Initialize PeerGroup
        PeerGroup peerGroup = new PeerGroup(params, new BlockChain(params, wallet, new MemoryBlockStore(params)));
        peerGroup.addWallet(wallet);
        peerGroup.start();

        String rawTxStr = "020000000103e4098e009a50175828c74163b991f99d32ee94dc4fb61cc81f8540fc4b2a3b010000006441aa1fe2f8bc404dcbaf129bce6a24b1e20d30c377c022ee3dd027c707656083605933174db5cbe1b2a150de12d6dc77de16a8b1abbb63709187d2e05184f007b9412102259760c11dd28136bf4360b0d8dbd9b2cb4c430e05810ef9a735f2ac21d97151ffffffff0380969800000000001976a91461c42abb6e3435e63bd88862f3746a3f8b86354288ac0000000000000000046a02686907939800000000001976a914ddaa205bf30cc11a2464c6ba56f4ce14299e78ad88ac00000000";
        byte[] rawTx = HexFormat.of().parseHex(rawTxStr);
        Transaction transaction = new Transaction(org.bitcoinj.fch.FchMainNetwork.MAINNETWORK,rawTx);

// Broadcast the transaction
        TransactionBroadcast broadcast = peerGroup.broadcastTransaction(transaction);
        broadcast.broadcast().get(); // Wait for the broadcast to complete

// Shut down the PeerGroup
        peerGroup.stop();

    }
}
