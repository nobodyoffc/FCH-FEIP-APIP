package startFEIP;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servers.EsTools;

import java.io.IOException;

public class IndicesFEIP {

	static final Logger log = LoggerFactory.getLogger(IndicesFEIP.class);

	public static final String ParseMarkIndex = "parse_mark";

	public static final String CidIndex = "cid";
	public static final String CidHistIndex = "cid_history";
	public static final String RepuHistIndex = "reputation_history";

	public static final String ProtocolIndex = "protocol";
	public static final String ProtocolHistIndex = "protocol_history";

	public static final String CodeIndex = "code";
	public static final String CodeHistIndex = "code_history";

	public static final String ServiceIndex = "service";
	public static final String ServiceHistIndex = "service_history";

	public static final String AppIndex = "app";
	public static final String AppHistIndex = "app_history";

	public static final String ContactIndex = "contact";

	public static final String MailIndex = "mail";

	public static final String SecretIndex = "secret";

	public static final String BoxIndex = "box";
	public static final String BoxHistIndex = "box_history";

	public static final String StatementIndex = "statement";

	public static final String GroupIndex = "group";
	public static final String TeamIndex = "team";

	public static final String GroupHistIndex = "group_history";
	public static final String TeamHistIndex = "team_history";

	public static String OrderIndex = "order";
	public static String ProofIndex = "proof";
	public static String ProofHistIndex = "proof_history";
	public static String NidIndex = "nid";


	public static void createAllIndices(ElasticsearchClient esClient) throws ElasticsearchException, IOException {

		if (esClient == null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}


		String cidJsonStr = "{\"mappings\":{\"properties\":{\"cid\":{\"type\":\"wildcard\"},\"height\":{\"type\":\"long\"},\"homepages\":{\"type\":\"text\"},\"hot\":{\"type\":\"long\"},\"fid\":{\"type\":\"keyword\"},\"priKey\":{\"type\":\"keyword\"},\"lastHeight\":{\"type\":\"long\"},\"master\":{\"type\":\"wildcard\"},\"nameTime\":{\"type\":\"long\"},\"noticeFee\":{\"type\":\"float\"},\"reputation\":{\"type\":\"long\"},\"usedCids\":{\"type\":\"wildcard\"}}}}";
		String cidHistJsonStr = "{\"mappings\":{\"properties\":{\"homepages\":{\"type\":\"text\"},\"master\":{\"type\":\"wildcard\"},\"cipherPriKey\":{\"type\":\"keyword\"},\"alg\":{\"type\":\"wildcard\"},\"height\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"index\":{\"type\":\"short\"},\"name\":{\"type\":\"wildcard\"},\"noticeFee\":{\"type\":\"text\"},\"op\":{\"type\":\"wildcard\"},\"priKey\":{\"type\":\"keyword\"},\"signer\":{\"type\":\"wildcard\"},\"sn\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"ver\":{\"type\":\"short\"}}}}";
		String repuHistJsonStr = "{\"mappings\":{\"properties\":{\"cause\":{\"type\":\"text\"},\"height\":{\"type\":\"long\"},\"hot\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"index\":{\"type\":\"short\"},\"ratee\":{\"type\":\"wildcard\"},\"rater\":{\"type\":\"wildcard\"},\"reputation\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"}}}}";
		String parseMarkJsonStr = "{\"mappings\":{\"properties\":{\"fileName\":{\"type\":\"wildcard\"},\"lastHeight\":{\"type\":\"long\"},\"lastId\":{\"type\":\"keyword\"},\"lastIndex\":{\"type\":\"long\"},\"length\":{\"type\":\"short\"},\"pointer\":{\"type\":\"long\"}}}}";

		EsTools.createIndex(esClient, CidIndex, cidJsonStr);
		EsTools.createIndex(esClient, CidHistIndex, cidHistJsonStr);
		EsTools.createIndex(esClient, RepuHistIndex, repuHistJsonStr);
		EsTools.createIndex(esClient, ParseMarkIndex, parseMarkJsonStr);

		String protocolJsonStr = "{\"mappings\":{\"properties\":{\"pid\":{\"type\":\"keyword\"},\"type\":{\"type\":\"wildcard\"},\"sn\":{\"type\":\"wildcard\"},\"ver\":{\"type\":\"wildcard\"},\"name\":{\"type\":\"wildcard\"},\"did\":{\"type\":\"keyword\"},\"lang\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"waiters\":{\"type\":\"keyword\"},\"preDid\":{\"type\":\"keyword\"},\"fileUrls\":{\"type\":\"text\"},\"title\":{\"type\":\"wildcard\"},\"owner\":{\"type\":\"wildcard\"},\"birthTxId\":{\"type\":\"keyword\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"tCdd\":{\"type\":\"long\"},\"tRate\":{\"type\":\"float\"},\"active\":{\"type\":\"boolean\"},\"closed\":{\"type\":\"boolean\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String protocolHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"type\":{\"type\":\"wildcard\"},\"sn\":{\"type\":\"wildcard\"},\"ver\":{\"type\":\"wildcard\"},\"name\":{\"type\":\"wildcard\"},\"did\":{\"type\":\"keyword\"},\"desc\":{\"type\":\"text\"},\"lang\":{\"type\":\"wildcard\"},\"preDid\":{\"type\":\"keyword\"},\"fileUrls\":{\"type\":\"text\"},\"signer\":{\"type\":\"wildcard\"},\"pid\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"rate\":{\"type\":\"short\"},\"waiters\":{\"type\":\"keyword\"},\"cdd\":{\"type\":\"long\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String codeJsonStr = "{\"mappings\":{\"properties\":{\"codeId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"ver\":{\"type\":\"wildcard\"},\"did\":{\"type\":\"keyword\"},\"desc\":{\"type\":\"text\"},\"langs\":{\"type\":\"wildcard\"},\"urls\":{\"type\":\"text\"},\"protocols\":{\"type\":\"keyword\"},\"waiters\":{\"type\":\"keyword\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"tCdd\":{\"type\":\"long\"},\"tRate\":{\"type\":\"float\"},\"active\":{\"type\":\"boolean\"},\"closed\":{\"type\":\"boolean\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String codeHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"codeId\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"ver\":{\"type\":\"wildcard\"},\"did\":{\"type\":\"keyword\"},\"desc\":{\"type\":\"text\"},\"langs\":{\"type\":\"wildcard\"},\"urls\":{\"type\":\"text\"},\"protocols\":{\"type\":\"keyword\"},\"waiters\":{\"type\":\"keyword\"},\"rate\":{\"type\":\"short\"},\"cdd\":{\"type\":\"long\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String serviceJsonStr = "{\"mappings\":{\"properties\":{\"sid\":{\"type\":\"keyword\"},\"stdName\":{\"type\":\"wildcard\"},\"localNames\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"types\":{\"type\":\"wildcard\"},\"urls\":{\"type\":\"text\"},\"waiters\":{\"type\":\"keyword\"},\"protocols\":{\"type\":\"keyword\"},\"params\":{\"type\":\"object\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"tCdd\":{\"type\":\"long\"},\"tRate\":{\"type\":\"float\"},\"active\":{\"type\":\"boolean\"},\"closed\":{\"type\":\"boolean\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String serviceHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"stdName\":{\"type\":\"wildcard\"},\"localNames\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"types\":{\"type\":\"wildcard\"},\"urls\":{\"type\":\"text\"},\"waiters\":{\"type\":\"keyword\"},\"protocols\":{\"type\":\"keyword\"},\"params\":{\"type\":\"object\"},\"sid\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"rate\":{\"type\":\"short\"},\"cdd\":{\"type\":\"long\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String appJsonStr = "{\"mappings\":{\"properties\":{\"aid\":{\"type\":\"keyword\"},\"stdName\":{\"type\":\"wildcard\"},\"localNames\":{\"type\":\"wildcard\"},\"types\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"urls\":{\"type\":\"text\"},\"downloads\":{\"properties\":{\"os\":{\"type\":\"text\"},\"link\":{\"type\":\"text\"},\"did\":{\"type\":\"keyword\"}}},\"waiters\":{\"type\":\"keyword\"},\"protocols\":{\"type\":\"keyword\"},\"services\":{\"type\":\"keyword\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"tCdd\":{\"type\":\"long\"},\"tRate\":{\"type\":\"float\"},\"active\":{\"type\":\"boolean\"},\"closed\":{\"type\":\"boolean\"},\"closeStatement\":{\"type\":\"text\"}}}}";
		String appHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"stdName\":{\"type\":\"wildcard\"},\"localNames\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"types\":{\"type\":\"wildcard\"},\"urls\":{\"type\":\"text\"},\"downloads\":{\"properties\":{\"os\":{\"type\":\"text\"},\"link\":{\"type\":\"text\"},\"did\":{\"type\":\"keyword\"}}},\"waiters\":{\"type\":\"keyword\"},\"protocols\":{\"type\":\"keyword\"},\"services\":{\"type\":\"keyword\"},\"aid\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"rate\":{\"type\":\"short\"},\"cdd\":{\"type\":\"long\"},\"closeStatement\":{\"type\":\"text\"}}}}";

		EsTools.createIndex(esClient, ProtocolIndex, protocolJsonStr);
		EsTools.createIndex(esClient, ProtocolHistIndex, protocolHistJsonStr);
		EsTools.createIndex(esClient, CodeIndex, codeJsonStr);
		EsTools.createIndex(esClient, CodeHistIndex, codeHistJsonStr);
		EsTools.createIndex(esClient, ServiceIndex, serviceJsonStr);
		EsTools.createIndex(esClient, ServiceHistIndex, serviceHistJsonStr);
		EsTools.createIndex(esClient, AppIndex, appJsonStr);
		EsTools.createIndex(esClient, AppHistIndex, appHistJsonStr);

		String groupJsonStr = "{\"mappings\":{\"properties\":{\"gid\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"namers\":{\"type\":\"wildcard\"},\"members\":{\"type\":\"wildcard\"},\"memberNum\":{\"type\":\"long\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"cddToUpdate\":{\"type\":\"long\"},\"tCdd\":{\"type\":\"long\"}}}}";
		String groupHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"gid\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"cdd\":{\"type\":\"long\"}}}}";
		String teamJsonStr = "{\"mappings\":{\"properties\":{\"tid\":{\"type\":\"keyword\"},\"owner\":{\"type\":\"wildcard\"},\"stdName\":{\"type\":\"text\"},\"localNames\":{\"type\":\"text\"},\"consensusId\":{\"type\":\"keyword\"},\"desc\":{\"type\":\"text\"},\"members\":{\"type\":\"wildcard\"},\"exMembers\":{\"type\":\"wildcard\"},\"managers\":{\"type\":\"wildcard\"},\"transferee\":{\"type\":\"wildcard\"},\"invitees\":{\"type\":\"wildcard\"},\"leavers\":{\"type\":\"wildcard\"},\"notAgreeMembers\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"tCdd\":{\"type\":\"long\"},\"tRate\":{\"type\":\"float\"},\"active\":{\"type\":\"boolean\"}}}}";
		String teamHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"cdd\":{\"type\":\"long\"},\"tid\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"list\":{\"type\":\"wildcard\"},\"stdName\":{\"type\":\"text\"},\"localNames\":{\"type\":\"text\"},\"consensusId\":{\"type\":\"keyword\"},\"desc\":{\"type\":\"text\"},\"rate\":{\"type\":\"short\"},\"transferee\":{\"type\":\"wildcard\"}}}}";

		EsTools.createIndex(esClient, GroupIndex, groupJsonStr);
		EsTools.createIndex(esClient, GroupHistIndex, groupHistJsonStr);
		EsTools.createIndex(esClient, TeamIndex, teamJsonStr);
		EsTools.createIndex(esClient, TeamHistIndex, teamHistJsonStr);

		String contactJsonStr = "{\"mappings\":{\"properties\":{\"contactId\":{\"type\":\"keyword\"},\"alg\":{\"type\":\"wildcard\"},\"cipher\":{\"type\":\"keyword\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"active\":{\"type\":\"boolean\"}}}}";
		String mailJsonStr = "{\"mappings\":{\"properties\":{\"mailId\":{\"type\":\"keyword\"},\"sender\":{\"type\":\"wildcard\"},\"recipient\":{\"type\":\"wildcard\"},\"alg\":{\"type\":\"wildcard\"},\"cipher\":{\"type\":\"keyword\"},\"cipherSend\":{\"type\":\"keyword\"},\"cipherReci\":{\"type\":\"keyword\"},\"textId\":{\"type\":\"keyword\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"active\":{\"type\":\"boolean\"}}}}";
		String secretJsonStr = "{\"mappings\":{\"properties\":{\"secretId\":{\"type\":\"keyword\"},\"alg\":{\"type\":\"wildcard\"},\"cipher\":{\"type\":\"keyword\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"active\":{\"type\":\"boolean\"}}}}";
		String boxJsonStr = "{\"mappings\":{\"properties\":{\"bid\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"contain\":{\"type\":\"text\"},\"active\":{\"type\":\"boolean\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"}}}}";
		String boxHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"bid\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"contain\":{\"type\":\"text\"}}}}";

		EsTools.createIndex(esClient, ContactIndex, contactJsonStr);
		EsTools.createIndex(esClient, MailIndex, mailJsonStr);
		EsTools.createIndex(esClient, SecretIndex, secretJsonStr);
		EsTools.createIndex(esClient, BoxIndex, boxJsonStr);
		EsTools.createIndex(esClient, BoxHistIndex, boxHistJsonStr);

		String statementJsonStr = "{\"mappings\":{\"properties\":{\"statementId\":{\"type\":\"keyword\"},\"title\":{\"type\":\"text\"},\"content\":{\"type\":\"text\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"},\"active\":{\"type\":\"boolean\"}}}}";
		String proofJsonStr = "{\"mappings\":{\"properties\":{\"proofId\":{\"type\":\"keyword\"},\"title\":{\"type\":\"text\"},\"content\":{\"type\":\"text\"},\"cosignersInvited\":{\"type\":\"wildcard\"},\"cosignersSigned\":{\"type\":\"wildcard\"},\"isTransferable\":{\"type\":\"boolean\"},\"active\":{\"type\":\"boolean\"},\"issuer\":{\"type\":\"wildcard\"},\"owner\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"}}}}";
		String proofHistJsonStr = "{\"mappings\":{\"properties\":{\"txId\":{\"type\":\"keyword\"},\"height\":{\"type\":\"long\"},\"index\":{\"type\":\"short\"},\"time\":{\"type\":\"long\"},\"signer\":{\"type\":\"wildcard\"},\"cdd\":{\"type\":\"long\"},\"proofId\":{\"type\":\"keyword\"},\"op\":{\"type\":\"keyword\"},\"title\":{\"type\":\"text\"},\"content\":{\"type\":\"text\"},\"cosignersInvited\":{\"type\":\"wildcard\"},\"isTransferable\":{\"type\":\"boolean\"},\"allSignsRequired\":{\"type\":\"boolean\"}}}}";

		EsTools.createIndex(esClient, StatementIndex, statementJsonStr);
		EsTools.createIndex(esClient, ProofIndex, proofJsonStr);
		EsTools.createIndex(esClient, ProofHistIndex, proofHistJsonStr);

		String nidJsonStr = "{\"mappings\":{\"properties\":{\"nameId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"wildcard\"},\"desc\":{\"type\":\"text\"},\"oid\":{\"type\":\"wildcard\"},\"active\":{\"type\":\"boolean\"},\"namer\":{\"type\":\"wildcard\"},\"birthTime\":{\"type\":\"long\"},\"birthHeight\":{\"type\":\"long\"},\"lastTxId\":{\"type\":\"keyword\"},\"lastTime\":{\"type\":\"long\"},\"lastHeight\":{\"type\":\"long\"}}}}";
		EsTools.createIndex(esClient, NidIndex, nidJsonStr);

		String orderJsonStr = "{\"mappings\":{\"properties\":{\"cashId\":{\"type\":\"wildcard\"},\"fromAddr\":{\"type\":\"wildcard\"},\"toAddr\":{\"type\":\"wildcard\"},\"vias\":{\"type\":\"wildcard\"},\"amount\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"txId\":{\"type\":\"keyword\"},\"txIndex\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"}}}}";
		EsTools.createIndex(esClient, OrderIndex, orderJsonStr);

	}

	public static void deleteAllIndices(ElasticsearchClient esClient) throws IOException {

		if (esClient == null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}

		EsTools.deleteIndex(esClient, CidIndex);
		EsTools.deleteIndex(esClient, CidHistIndex);
		EsTools.deleteIndex(esClient, RepuHistIndex);
		EsTools.deleteIndex(esClient, ParseMarkIndex);

		EsTools.deleteIndex(esClient, ProtocolIndex);
		EsTools.deleteIndex(esClient, ProtocolHistIndex);
		EsTools.deleteIndex(esClient, CodeIndex);
		EsTools.deleteIndex(esClient, CodeHistIndex);
		EsTools.deleteIndex(esClient, ServiceIndex);
		EsTools.deleteIndex(esClient, ServiceHistIndex);
		EsTools.deleteIndex(esClient, AppIndex);
		EsTools.deleteIndex(esClient, AppHistIndex);

		EsTools.deleteIndex(esClient, GroupIndex);
		EsTools.deleteIndex(esClient, GroupHistIndex);
		EsTools.deleteIndex(esClient, TeamIndex);
		EsTools.deleteIndex(esClient, TeamHistIndex);

		EsTools.deleteIndex(esClient, ContactIndex);
		EsTools.deleteIndex(esClient, MailIndex);
		EsTools.deleteIndex(esClient, SecretIndex);
		EsTools.deleteIndex(esClient, BoxIndex);
		EsTools.deleteIndex(esClient, BoxHistIndex);

		EsTools.deleteIndex(esClient, StatementIndex);
		EsTools.deleteIndex(esClient, ProofIndex);
		EsTools.deleteIndex(esClient, ProofHistIndex);
		EsTools.deleteIndex(esClient, NidIndex);
		EsTools.deleteIndex(esClient, OrderIndex);

	}
}
