package writeEs;

import constants.IndicesNames;
import fchClass.*;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest.Builder;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fileTools.OpReFileTools;
import parser.Preparer;
import parser.ReadyBlock;
import redis.clients.jedis.Jedis;
import esTools.EsTools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class BlockWriter {

	private static final Logger log = LoggerFactory.getLogger(BlockWriter.class);
	private static final Jedis jedis = new Jedis();

	public void writeIntoEs(ElasticsearchClient esClient, ReadyBlock readyBlock1,OpReFileTools opReFile) throws Exception {
		ReadyBlock readyBlock = readyBlock1;	

		Block block = readyBlock.getBlock();
		BlockHas blockHas = readyBlock.getBlockHas();
		ArrayList<Tx> txList = readyBlock.getTxList();
		ArrayList<TxHas> txHasList = readyBlock.getTxHasList();
		ArrayList<Cash> inList = readyBlock.getInList();
		ArrayList<Cash> outList = readyBlock.getOutWriteList();
		ArrayList<OpReturn> opReturnList = readyBlock.getOpReturnList();
		BlockMark blockMark = readyBlock.getBlockMark();
		ArrayList<Address> addrList = readyBlock.getAddrList();
		
		opReFile.writeOpReturnListIntoFile(opReturnList);


		Builder br = new Builder();
		putBlock(esClient, block, br);
		putBlockHas(blockHas, br);
		putTx(esClient, txList, br);
		putTxHas(esClient, txHasList, br);
		putUtxo(esClient, outList, br);
		putStxo(esClient, inList, br);
		putOpReturn(esClient, opReturnList, br);
		putAddress(esClient, addrList, br);
		putBlockMark(esClient, blockMark, br);
		BulkResponse response = EsTools.bulkWithBuilder(esClient, br);

		try {
			jedis.set("bestHeight", String.valueOf(block.getHeight()));
			jedis.set("bestBlockId", block.getBlockId());
		}catch(Exception e){
			log.warn("Redis isn't ready. Reading redis is ignored.");
		}

		System.out.println("Main chain linked. "
				+"Orphan: "+Preparer.orphanList.size()
				+" Fork: "+Preparer.forkList.size()
				+" id: "+blockMark.getBlockId()
				+" file: "+Preparer.CurrentFile
				+" pointer: "+Preparer.Pointer
				+" Height:"+blockMark.getHeight());

		
		response.items().iterator();

		if (response.errors()) {
			log.error("bulkWriteToEs error");
			for(BulkResponseItem item:response.items()) {
				if(item.error()!=null) {
					System.out.println("index: "+item.index()+", Type: "+item.error().type()+"\nReason: "+item.error().reason());
				}
			}	
			throw new Exception("bulkWriteToEs error");
		};

		Preparer.mainList.add(blockMark);
		if (Preparer.mainList.size() > EsTools.READ_MAX) {
			Preparer.mainList.remove(0);
		}
		Preparer.BestHash = blockMark.getBlockId();
		Preparer.BestHeight = blockMark.getHeight();
	}

	private void putBlockMark(ElasticsearchClient esClient, BlockMark blockMark, Builder br) throws Exception {

		// BulkRequest.Builder br = new BulkRequest.Builder();
		br.operations(op -> op.index(i -> i.index(IndicesNames.BLOCK_MARK).id(blockMark.getBlockId()).document(blockMark)));
        // EsTools.bulkWithBuilder(esClient.esClient, br);
	}

	private void putAddress(ElasticsearchClient esClient, ArrayList<Address> addrList, Builder br) throws Exception {

		if (addrList.size() > EsTools.WRITE_MAX / 5) {
			Iterator<Address> iter = addrList.iterator();
			ArrayList<String> idList = new ArrayList<String>();
			while (iter.hasNext())
				idList.add(iter.next().getFid());
			EsTools.bulkWriteList(esClient, IndicesNames.ADDRESS, addrList, idList, Address.class);
			TimeUnit.SECONDS.sleep(3);
		} else {
			Iterator<Address> iterAd = addrList.iterator();
			while (iterAd.hasNext()) {
				Address am = iterAd.next();
				br.operations(op -> op.index(i -> i.index(IndicesNames.ADDRESS).id(am.getFid()).document(am)));
			}
		}
	}

	private void putOpReturn(ElasticsearchClient esClient, ArrayList<OpReturn> opReturnList, Builder br)
			throws Exception {

		if (opReturnList != null) {
			if (opReturnList.size() > 100) {
				Iterator<OpReturn> iter = opReturnList.iterator();
				ArrayList<String> idList = new ArrayList<String>();
				while (iter.hasNext())
					idList.add(iter.next().getTxId());
				EsTools.bulkWriteList(esClient, IndicesNames.OPRETURN, opReturnList, idList, OpReturn.class);
				TimeUnit.SECONDS.sleep(3);
			} else {
				Iterator<OpReturn> iterOR = opReturnList.iterator();
				while (iterOR.hasNext()) {
					OpReturn or = iterOR.next();
					br.operations(op -> op.index(i -> i.index(IndicesNames.OPRETURN).id(or.getTxId()).document(or)));
				}
			}
		}
	}

	private void putStxo(ElasticsearchClient esClient, ArrayList<Cash> inList, Builder br) throws Exception {
		if (inList != null) {
			if (inList.size() > EsTools.WRITE_MAX / 5) {
				Iterator<Cash> iter = inList.iterator();
				ArrayList<String> idList = new ArrayList<String>();
				while (iter.hasNext())
					idList.add(iter.next().getCashId());
				EsTools.bulkWriteList(esClient, IndicesNames.CASH, inList, idList, Cash.class);
				TimeUnit.SECONDS.sleep(3);
			} else {
				Iterator<Cash> iterTxo = inList.iterator();
				while (iterTxo.hasNext()) {
					Cash om = iterTxo.next();
					br.operations(op -> op.index(i -> i.index(IndicesNames.CASH).id(om.getCashId()).document(om)));
				}
			}
		}
	}

	private void putUtxo(ElasticsearchClient esClient, ArrayList<Cash> outList, Builder br) throws Exception {
		if (outList.size() > EsTools.WRITE_MAX / 5) {
			Iterator<Cash> iter = outList.iterator();
			ArrayList<String> idList = new ArrayList<String>();
			while (iter.hasNext())
				idList.add(iter.next().getCashId());
			EsTools.bulkWriteList(esClient, IndicesNames.CASH, outList, idList, Cash.class);
			TimeUnit.SECONDS.sleep(3);
		} else {
			Iterator<Cash> iterTxo = outList.iterator();
			while (iterTxo.hasNext()) {
				Cash om = iterTxo.next();
				br.operations(op -> op.index(i -> i.index(IndicesNames.CASH).id(om.getCashId()).document(om)));
			}
		}
	}

	private void putTxHas(ElasticsearchClient esClient, ArrayList<TxHas> txHasList, Builder br) throws Exception {
		if (txHasList != null) {
			if (txHasList.size() > EsTools.WRITE_MAX / 5) {
				Iterator<TxHas> iter = txHasList.iterator();
				ArrayList<String> idList = new ArrayList<String>();
				while (iter.hasNext())
					idList.add(iter.next().getTxId());
				EsTools.bulkWriteList(esClient, IndicesNames.TX_HAS, txHasList, idList, TxHas.class);
				TimeUnit.SECONDS.sleep(3);
			} else {
				Iterator<TxHas> iterOInTx = txHasList.iterator();
				while (iterOInTx.hasNext()) {
					TxHas ot = iterOInTx.next();
					br.operations(op -> op.index(i -> i.index(IndicesNames.TX_HAS).id(ot.getTxId()).document(ot)));
				}
			}
		}
	}

	private void putTx(ElasticsearchClient esClient, ArrayList<Tx> txList, Builder br) throws Exception {
		if (txList.size() > EsTools.WRITE_MAX / 5) {
			Iterator<Tx> iter = txList.iterator();
			ArrayList<String> idList = new ArrayList<String>();
			while (iter.hasNext())
				idList.add(iter.next().getTxId());
			EsTools.bulkWriteList(esClient, IndicesNames.TX, txList, idList, Tx.class);
			TimeUnit.SECONDS.sleep(3);
		} else {
			Iterator<Tx> iterTx = txList.iterator();
			while (iterTx.hasNext()) {
				Tx tm = iterTx.next();
				br.operations(op -> op.index(i -> i.index(IndicesNames.TX).id(tm.getTxId()).document(tm)));
			}
		}
	}

	private void putBlockHas(BlockHas blockHas, Builder br) {
		br.operations(op -> op.index(i -> i.index(IndicesNames.BLOCK_HAS).id(blockHas.getBlockId()).document(blockHas)));
	}

	private void putBlock(ElasticsearchClient esClient, Block block, Builder br) throws Exception {
		br.operations(op -> op.index(i -> i.index(IndicesNames.BLOCK).id(block.getBlockId()).document(block)));
	}
}
