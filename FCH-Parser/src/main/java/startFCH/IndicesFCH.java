package startFCH;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;

import constants.IndicesNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import esTools.EsTools;

import java.io.IOException;


public class IndicesFCH {
	static final Logger log = LoggerFactory.getLogger(IndicesFCH.class);
			
	public static void createAllIndices(ElasticsearchClient esClient) throws ElasticsearchException, IOException {

		if (esClient == null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}

		String blockMarkJsonStr = "{\"mappings\":{\"properties\":{\"_fileOrder\":{\"type\":\"short\"},\"_pointer\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"},\"blockId\":{\"type\":\"keyword\"},\"preId\":{\"type\":\"keyword\"},\"size\":{\"type\":\"long\"},\"status\":{\"type\":\"keyword\"}}}}";
		String blockJsonStr = "{\"mappings\":{\"properties\":{\"cdd\":{\"type\":\"long\"},\"diffTarget\":{\"type\":\"long\"},\"fee\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"},\"blockId\":{\"type\":\"keyword\"},\"inValueT\":{\"type\":\"long\"},\"merkleRoot\":{\"type\":\"keyword\"},\"nonce\":{\"type\":\"long\"},\"outValueT\":{\"type\":\"long\"},\"preId\":{\"type\":\"keyword\"},\"size\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"txCount\":{\"type\":\"long\"},\"version\":{\"type\":\"keyword\"}}}}";
		String blockHasJsonStr = "{\"mappings\":{\"properties\":{\"height\":{\"type\":\"long\"},\"blockId\":{\"type\":\"keyword\"},\"txMarks\":{\"properties\":{\"cdd\":{\"type\":\"long\"},\"fee\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"outValue\":{\"type\":\"long\"}}}}}}";
		String txJsonStr = "{\"mappings\":{\"properties\":{\"blockId\":{\"type\":\"keyword\"},\"blockTime\":{\"type\":\"long\"},\"cdd\":{\"type\":\"long\"},\"coinbase\":{\"type\":\"text\"},\"fee\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"inCount\":{\"type\":\"long\"},\"inValueT\":{\"type\":\"long\"},\"lockTime\":{\"type\":\"long\"},\"opReBrief\":{\"type\":\"text\"},\"outCount\":{\"type\":\"long\"},\"outValueT\":{\"type\":\"long\"},\"txIndex\":{\"type\":\"long\"},\"version\":{\"type\":\"long\"}}}}";
		String txHasJsonStr = "{\"mappings\":{\"properties\":{\"height\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"inMarks\":{\"properties\":{\"fid\":{\"type\":\"wildcard\"},\"cdd\":{\"type\":\"long\"},\"cashId\":{\"type\":\"keyword\"},\"value\":{\"type\":\"long\"}}},\"outMarks\":{\"properties\":{\"fid\":{\"type\":\"wildcard\"},\"cdd\":{\"type\":\"long\"},\"cashId\":{\"type\":\"keyword\"},\"value\":{\"type\":\"long\"}}}}}}";
		String cashJsonStr = "{\"mappings\":{\"properties\":{\"cashId\":{\"type\":\"keyword\"},\"fid\":{\"type\":\"wildcard\"},\"value\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"birthTime\":{\"type\":\"long\"},\"birthBlockId\":{\"type\":\"keyword\"},\"birthTxIndex\":{\"type\":\"long\"},\"birthTxId\":{\"type\":\"keyword\"},\"birthIndex\":{\"type\":\"long\"},\"lockScript\":{\"type\":\"text\"},\"sequence\":{\"type\":\"keyword\"},\"sigHash\":{\"type\":\"keyword\"},\"spendBlockId\":{\"type\":\"keyword\"},\"spendHeight\":{\"type\":\"long\"},\"spendTxIndex\":{\"type\":\"long\"},\"spendTxId\":{\"type\":\"keyword\"},\"spendIndex\":{\"type\":\"long\"},\"spendTime\":{\"type\":\"long\"},\"type\":{\"type\":\"keyword\"},\"unlockScript\":{\"type\":\"text\"},\"valid\":{\"type\":\"boolean\"},\"cdd\":{\"type\":\"long\"}}}}";
		String addressJsonStr = "{\"mappings\":{\"properties\":{\"balance\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"btcAddr\":{\"type\":\"wildcard\"},\"cd\":{\"type\":\"long\"},\"cdd\":{\"type\":\"long\"},\"weight\":{\"type\":\"long\"},\"dogeAddr\":{\"type\":\"wildcard\"},\"ethAddr\":{\"type\":\"wildcard\"},\"expend\":{\"type\":\"long\"},\"guide\":{\"type\":\"wildcard\"},\"fid\":{\"type\":\"wildcard\"},\"income\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"ltcAddr\":{\"type\":\"wildcard\"},\"pubKey\":{\"type\":\"wildcard\"},\"trxAddr\":{\"type\":\"wildcard\"},\"cash\":{\"type\":\"long\"}}}}";
		String opreturnJsonStr = "{\"mappings\":{\"properties\":{\"cdd\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"opReturn\":{\"type\":\"text\"},\"recipient\":{\"type\":\"wildcard\"},\"signer\":{\"type\":\"wildcard\"},\"time\":{\"type\":\"long\"},\"txIndex\":{\"type\":\"long\"}}}}";

		EsTools.createIndex(esClient, IndicesNames.BLOCK_MARK, blockMarkJsonStr);
		EsTools.createIndex(esClient, IndicesNames.BLOCK, blockJsonStr);
		EsTools.createIndex(esClient, IndicesNames.BLOCK_HAS, blockHasJsonStr);
		EsTools.createIndex(esClient, IndicesNames.TX, txJsonStr);
		EsTools.createIndex(esClient, IndicesNames.TX_HAS, txHasJsonStr);
		EsTools.createIndex(esClient, IndicesNames.CASH, cashJsonStr);
		EsTools.createIndex(esClient, IndicesNames.ADDRESS, addressJsonStr);
		EsTools.createIndex(esClient, IndicesNames.OPRETURN, opreturnJsonStr);

		String p2shJsonStr = "{\"mappings\":{\"properties\":{\"fid\":{\"type\":\"wildcard\"},\"redeemScript\":{\"type\":\"keyword\"},\"m\":{\"type\":\"short\"},\"n\":{\"type\":\"short\"},\"pubKeys\":{\"type\":\"keyword\"},\"fids\":{\"type\":\"wildcard\"},\"birthHeight\":{\"type\":\"long\"},\"birthTime\":{\"type\":\"long\"},\"birthTxId\":{\"type\":\"keyword\"}}}}";
		EsTools.createIndex(esClient, IndicesNames.P2SH, p2shJsonStr);
	}


	public static void deleteAllIndices(ElasticsearchClient esClient) throws ElasticsearchException, IOException {

		if(esClient==null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}

		EsTools.deleteIndex(esClient, IndicesNames.BLOCK_MARK);
		EsTools.deleteIndex(esClient, IndicesNames.BLOCK);
		EsTools.deleteIndex(esClient, IndicesNames.BLOCK_HAS);
		EsTools.deleteIndex(esClient, IndicesNames.TX);
		EsTools.deleteIndex(esClient, IndicesNames.TX_HAS);
		EsTools.deleteIndex(esClient, IndicesNames.CASH);
		EsTools.deleteIndex(esClient, IndicesNames.ADDRESS);
		EsTools.deleteIndex(esClient, IndicesNames.OPRETURN);
		EsTools.deleteIndex(esClient, IndicesNames.P2SH);
	}	
}
