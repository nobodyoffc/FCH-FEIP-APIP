package parser;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import FchClass.Block;
import FchClass.BlockMark;
import javaTools.BytesTools;
import cryptoTools.SHA;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import startFCH.IndicesFCH;
import writeEs.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChainParser {

	public static final int FILE_END = -1;
	public static final int WRONG = -2;
	public static final int HEADER_FORK = -3;
	public static final int REPEAT = -4;
	public static final int WAIT_MORE = 0;
	public static final String MAGIC = "f9beb4d9";
	public static final  String OpRefileName = "opreturn0.byte";

	private static final Logger log = LoggerFactory.getLogger(ChainParser.class);

	private OpReFileTools opReFile = new OpReFileTools();

	public static Block getBestBlock(ElasticsearchClient esClient) throws ElasticsearchException, IOException {
		SearchResponse<Block> result = esClient.search(s->s
						.index(IndicesFCH.BlockIndex)
						.size(1)
						.sort(so->so.field(f->f.field("height").order(SortOrder.Desc)))
				, Block.class);
		return result.hits().hits().get(0).source();
	}

	public int startParse(ElasticsearchClient esClient) throws Exception {

		System.out.println("Started parsing file:  "+Preparer.CurrentFile+" ...");
		log.info("Started parsing file: {} ...",Preparer.CurrentFile);

		File file = new File(Preparer.Path,Preparer.CurrentFile);
		FileInputStream fis = new FileInputStream(file);
		fis.skip(Preparer.Pointer);

		int blockLength;

		long cdMakeTime = System.currentTimeMillis();

		while(true) {

			CheckResult checkResult = checkBlock(fis);
			BlockMark blockMark = checkResult.getBlockMark();
			byte[] blockBytes = checkResult.getBlockBytes();

			blockLength = checkResult.getBlockLength();

			if(blockLength == FILE_END) {
				String nextFile = BlockFileTools.getNextFile(Preparer.CurrentFile);
				if(new File(Preparer.Path, nextFile).exists()) {
					System.out.println("file "+Preparer.CurrentFile+" finished.");
					log.info("Parsing file {} finished.",Preparer.CurrentFile);
					Preparer.CurrentFile = nextFile;
					Preparer.Pointer = 0;

					fis.close();
					file = new File(Preparer.Path,Preparer.CurrentFile);
					fis = new FileInputStream(file);

					System.out.println("Started parsing file:  "+Preparer.CurrentFile+" ...");
					log.info("Started parsing file: {} ...",Preparer.CurrentFile);
					continue;
				}else {
					System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
					System.out.println(" Waiting 30 seconds for new block file ...");
					TimeUnit.SECONDS.sleep(30);
					fis.close();
					fis = new FileInputStream(file);
					fis.skip(Preparer.Pointer);
				}
			}else if(blockLength == WRONG ) {
				System.out.println("Read Magic wrong. pointer: "+Preparer.Pointer);
				log.info("Read Magic wrong. pointer: {}",Preparer.Pointer);
				return WRONG;

			}else if(blockLength == HEADER_FORK) {
				Preparer.Pointer = Preparer.Pointer + 88;
				fis.close();
				fis = new FileInputStream(file);
				fis.skip(Preparer.Pointer);

			}else if(blockLength == WAIT_MORE) {
				System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
				System.out.println(" Waiting for new block...");
				ParseTools.waitForNewItemInFile(Preparer.Path+Preparer.CurrentFile);
				fis.close();
				fis = new FileInputStream(file);
				fis.skip(Preparer.Pointer);

			}else {

				linkToChain(esClient, blockMark,blockBytes);

				recheckOrphans(esClient);

				Preparer.Pointer += blockLength;
			}

			long now = System.currentTimeMillis();
			if( now - cdMakeTime > (1000*60*60*12)) {
				Block bestBlock = getBestBlock(esClient);

				CdMaker cdMaker = new CdMaker();

				cdMaker.makeUtxoCd(esClient,bestBlock);
				log.info("All cd of UTXOs updated.");
				TimeUnit.MINUTES.sleep(2);

				cdMaker.makeAddrCd(esClient);
				log.info("All cd of addresses updated.");
				TimeUnit.MINUTES.sleep(1);

				cdMakeTime = now;
			}
		}
	}

	private CheckResult checkBlock(FileInputStream fis) throws Exception {

		BlockMark blockMark = new BlockMark();
		blockMark.set_pointer(Preparer.Pointer);
		blockMark.set_fileOrder(getFileOrder());

		CheckResult checkResult = new CheckResult();

		byte [] b8 = new byte[8];
		byte [] b4 = new byte[4];

		if(fis.read(b8) == FILE_END) {
			System.out.println("File end when reading magic. pointer: "+Preparer.Pointer);
			log.error("File end when reading magic. ");
			checkResult.setBlockLength(FILE_END);
			return checkResult;
		}

		if(b8[0]==0) {
			checkResult.setBlockLength(WAIT_MORE);
			return checkResult;
		}

		b4 = Arrays.copyOfRange(b8, 0, 4);
		String magic = BytesTools.bytesToHexStringBE(b4) ;
		if(!magic.equals(MAGIC)) {
			checkResult.setBlockLength(WRONG);
			return checkResult;
		}

		b4 = Arrays.copyOfRange(b8, 4, 8);
		int blockSize = (int) BytesTools.bytes4ToLongLE(b4);
		blockMark.setSize(blockSize);

		if(blockSize==0) {
			checkResult.setBlockLength(WAIT_MORE);
			return checkResult;
		}

		//Check valid header fork
		if(blockSize == 80) {
			checkResult.setBlockLength(HEADER_FORK);
			System.out.println("Header valid fork was found. Height: "+Preparer.BestHeight+1);
			log.info("Header valid fork was found. Height: "+Preparer.BestHeight+1);
			return checkResult;
		}

		byte[] blockBytes = new byte[blockSize];
		if(fis.read(blockBytes)== FILE_END) {
			System.out.println("File end when reading block. pointer: "+Preparer.Pointer);
			log.info("File end when reading block. Pointer:"+ Preparer.Pointer);
			checkResult.setBlockLength(FILE_END);
			return checkResult;
		}

		ByteArrayInputStream blockInputStream = new ByteArrayInputStream(blockBytes);

		byte[] blockHeadBytes = new byte[80];
		blockInputStream.read(blockHeadBytes);

		String blockId = BytesTools.bytesToHexStringLE(SHA.Sha256x2(blockHeadBytes));
		blockMark.setBlockId(blockId);

		String preId =  BytesTools.bytesToHexStringLE(Arrays.copyOfRange(blockHeadBytes, 4, 4+32));
		blockMark.setPreBlockId(preId);

		byte[] blockBodyBytes = new byte[blockSize-80];
		blockInputStream.read(blockBodyBytes);

		//Check valid header fork
		b4 = Arrays.copyOfRange(blockBodyBytes, 0, 4);
		String b4Hash = BytesTools.bytesToHexStringBE(b4) ;
		if(b4Hash.equals(MAGIC)) {
			System.out.println("Found valid header fork. Pointer: "+Preparer.Pointer);
			log.info("Found valid header fork. Pointer: {}",Preparer.Pointer);
			checkResult.setBlockLength(HEADER_FORK);
			return checkResult;
		}
		checkResult.setBlockLength(blockSize+8);
		checkResult.setBlockMark(blockMark);
		checkResult.setBlockBytes(blockBytes);
		return checkResult;
	}

	private int getFileOrder() {
		return BlockFileTools.getFileOrder(Preparer.CurrentFile);
	}

	private class CheckResult{
		int blockLength;
		BlockMark blockMark;
		byte[] blockBytes;

		public int getBlockLength() {
			return blockLength;
		}

		public void setBlockLength(int blockLength) {
			this.blockLength = blockLength;
		}

		public BlockMark getBlockMark() {
			return blockMark;
		}

		public void setBlockMark(BlockMark blockMark) {
			this.blockMark = blockMark;
		}

		public byte[] getBlockBytes() {
			return blockBytes;
		}

		public void setBlockBytes(byte[] blockBytes) {
			this.blockBytes = blockBytes;
		}
	}

	private void linkToChain(ElasticsearchClient esClient, BlockMark blockMark1, byte[] blockBytes) throws Exception {
		BlockMark blockMark = blockMark1;

		if(isRepeatBlockIgnore(blockMark))
			return;
		if(isLinkToMainChainWriteItToEs(esClient, blockMark, blockBytes))
			return;
		if(isNewForkAddMarkToEs(esClient, blockMark))
			return;
		if(isLinkedToForkWriteMarkToEs(esClient, blockMark)){

			if(isForkOverMain(blockMark)) {
				HashMap<String, ArrayList<BlockMark>> chainMap = findLoseChainAndWinChain(blockMark);
				if(chainMap == null)return;
				reorganize(esClient,chainMap);
			}
			return;
		}
		writeOrphanMark(esClient, blockMark);
		System.out.println("Orphan block"
				+". Orphan: "+ Preparer.orphanList.size()
				+". Fork: "+ Preparer.forkList.size()
				+". Height: "+ Preparer.BestHeight);
		return;
	}

	private void writeBlockMark(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException {
		esClient.index(i->i
				.index(IndicesFCH.BlockMarkIndex).id(blockMark.getBlockId()).document(blockMark));
	}

	private boolean isForkOverMain(BlockMark blockMark) {

		if(blockMark.getHeight() > Preparer.BestHeight) return true;
		return false;
	}

	private HashMap<String, ArrayList<BlockMark>> findLoseChainAndWinChain(BlockMark blockMark) {
		System.out.println("findLoseChainAndWinChain");

		BlockMark forkBlock = new BlockMark();
		BlockMark mainBlock = new BlockMark();

		ArrayList<BlockMark> winList = new ArrayList<BlockMark>();
		ArrayList<BlockMark> loseList = new ArrayList<BlockMark>();

		HashMap<String, ArrayList<BlockMark>> findMap = new HashMap<String,ArrayList<BlockMark>>();

		winList.add(blockMark);
		String preId = blockMark.getPreBlockId();

		boolean foundFormerForkBlockMark = false;

		while(true) {
			foundFormerForkBlockMark = false;
			for(int i=Preparer.forkList.size()-1;i>=0;i--) {
				forkBlock = Preparer.forkList.get(i);
				if(forkBlock.getBlockId().equals(preId)) {
					winList.add(forkBlock);
					for(int j=Preparer.mainList.size()-1; j>=Preparer.mainList.size()-31; j--) {
						mainBlock = Preparer.mainList.get(j);
						if(forkBlock.getPreBlockId().equals(mainBlock.getBlockId())){
							findMap.put("lose", loseList);
							findMap.put("win", winList);

							System.out.println("Got loseList size: "+ loseList.size()
									+ " last id:"+loseList.get(0).getBlockId()
									+ " winList size:"+winList.size()
									+ " last id:"+winList.get(0).getBlockId());
							return findMap;
						}
						loseList.add(mainBlock);
					}
					foundFormerForkBlockMark = true;
					preId = forkBlock.getPreBlockId();
				}
				if(foundFormerForkBlockMark)break;
			}
			if(!foundFormerForkBlockMark) {
				return null;
			}
		}
	}

	private void reorganize(ElasticsearchClient esClient, HashMap<String, ArrayList<BlockMark>> chainMap) throws Exception {

		ArrayList<BlockMark> loseList = chainMap.get("lose");
		ArrayList<BlockMark> winList = chainMap.get("win");

		if(loseList == null || loseList.isEmpty()) throw new Exception("loseList is null when reorganizing. ");

		long heightBeforeFork = winList.get(winList.size()-1).getHeight()-1;

		System.out.println("Reorganization happen after height: "+heightBeforeFork);
		log.info("Reorganization happen after height: "+heightBeforeFork);

		treatLoseList(esClient, loseList);

		new RollBacker().rollback(esClient,heightBeforeFork);

		treatWinList(esClient,winList);

		System.out.println("Reorganized. Fork: "+Preparer.forkList.size()+" Height: "+heightBeforeFork);
	}

	private void treatLoseList( ElasticsearchClient esClient,ArrayList<BlockMark> loseList) throws ElasticsearchException, IOException {

		BulkRequest.Builder br = new BulkRequest.Builder();
		Preparer.mainList.removeAll(loseList);

		for(int i=loseList.size()-1;i>=0;i--) {
			BlockMark bm = loseList.get(i);
			bm.setStatus(Preparer.FORK);
			Preparer.forkList.add(bm);
			br.operations(op->op.index(in->in
					.index(IndicesFCH.BlockMarkIndex)
					.id(bm.getBlockId())
					.document(bm)));
		}
		esClient.bulk(br.build());
	}

	private void treatWinList(ElasticsearchClient esClient, ArrayList<BlockMark> winList) throws Exception {

		Preparer.forkList.removeAll(winList);

		for(int i=winList.size()-1;i>=0;i--) {
			BlockMark blockMark = winList.get(i);

			blockMark.setStatus(Preparer.MAIN);
			Preparer.mainList.add(blockMark);

			byte[] blockBytes = getBlockBytes(blockMark);
			ReadyBlock rawBlock = new BlockParser().parseBlock(blockBytes,blockMark);
			ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient, rawBlock);
			new BlockWriter().writeIntoEs(esClient, readyBlock,opReFile);

			System.out.println("writeWinListToEs. i:"+i
					+" blockId:"+blockMark.getBlockId()
					+" height"+blockMark.getHeight()
					+" blockSize:"+blockMark.getSize()
					+" pointer:"+blockMark.get_pointer()
					+" blockBytes length:"+blockBytes.length);
		}

		dropOldFork(winList.get(0).getHeight());
		return;
	}

	private boolean isRepeatBlockIgnore(BlockMark blockMark) {

		if(Preparer.mainList==null || Preparer.mainList.isEmpty())return false;
		Iterator<BlockMark> iter = Preparer.mainList.iterator();
		while(iter.hasNext()) {
			if(blockMark.getBlockId().equals(iter.next().getBlockId())){
				System.out.println("Repeat block..." );
				log.info("Repeat block..." );
				return true;
			}
		}
		return false;
	}

	private boolean isLinkToMainChainWriteItToEs(ElasticsearchClient esClient, BlockMark blockMark1, byte[] blockBytes) throws Exception {

		BlockMark blockMark = blockMark1;

		if(blockMark.getPreBlockId().equals(Preparer.BestHash)){
			blockMark.setStatus(Preparer.MAIN);
			long newHeight = Preparer.BestHeight+1;
			blockMark.setHeight(newHeight);
			ReadyBlock rawBlock = new BlockParser().parseBlock(blockBytes,blockMark);
			ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient, rawBlock);
			new BlockWriter().writeIntoEs(esClient, readyBlock,opReFile);
			dropOldFork(newHeight);
			return true;
		}
		return false;
	}

	private void dropOldFork(long newHeight) {

		Iterator<BlockMark> iter = Preparer.forkList.iterator();
		while(iter.hasNext()) {
			BlockMark bm = iter.next();
			if(bm.getHeight() < newHeight-30) {
				iter.remove();
			}
		}
	}

	private boolean isNewForkAddMarkToEs(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException, InterruptedException {

		for(BlockMark bm:Preparer.mainList) {
			//TODO untested the && condition blow.
			if(blockMark.getPreBlockId().equals(bm.getBlockId()) && !bm.getBlockId().equals(Preparer.BestHash)){

				blockMark.setHeight(bm.getHeight()+1);
				blockMark.setStatus(Preparer.FORK);
				Preparer.forkList.add(blockMark);
				writeBlockMark(esClient, blockMark);

				System.out.println("New fork block. Height: "+blockMark.getHeight()+"forkList size:" + Preparer.forkList.size());
				log.info("New fork block. Height: "+blockMark.getHeight());
				return true;
			}
		}
		return false;
	}

	private boolean isLinkedToForkWriteMarkToEs(ElasticsearchClient esClient,BlockMark blockMark1) throws ElasticsearchException, IOException, InterruptedException {

		BlockMark blockMark = blockMark1;
		Iterator<BlockMark> iter = Preparer.forkList.iterator();
		while(iter.hasNext()) {
			BlockMark bm = iter.next();
			if(blockMark.getPreBlockId().equals(bm.getBlockId())){
				blockMark.setHeight(bm.getHeight()+1);
				blockMark.setStatus(Preparer.FORK);
				writeBlockMark(esClient, blockMark);
				Preparer.forkList.add(blockMark);

				System.out.println("Linked to fork block. Height: "+blockMark.getHeight() + " fork size:"+Preparer.forkList.size());
				log.info("Linked to fork block. Height: "+blockMark.getHeight());

				return true;
			}
		}

		return false;
	}

	private byte[] getBlockBytes(BlockMark bm) throws IOException {

		File file = new File(Preparer.Path, BlockFileTools.getFileNameWithOrder(bm.get_fileOrder()));
		FileInputStream fis = new FileInputStream(file);
		fis.skip(bm.get_pointer()+8);
		byte[] blockBytes = new byte[(int) bm.getSize()];
		fis.read(blockBytes);
		fis.close();
		return blockBytes;
	}

	private void writeOrphanMark(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException {

		blockMark.setStatus(Preparer.ORPHAN);
		blockMark.setOrphanHeight(Preparer.BestHeight);
		writeBlockMark(esClient, blockMark);
		Preparer.orphanList.add(blockMark);
	}

	private void recheckOrphans(ElasticsearchClient esClient) throws Exception {

		boolean found = false;

		BlockMark bestBlockMark = new BlockMark();;

		while(!found) {
			Iterator<BlockMark> iter = Preparer.orphanList.iterator();
			while(iter.hasNext()){
				BlockMark blockMark = iter.next();

				//If linked to main;
				if(blockMark.getPreBlockId().equals(Preparer.BestHash)) {
					blockMark.setHeight(Preparer.BestHeight+1);
					blockMark.setStatus(Preparer.MAIN);
					byte[] blockBytes = getBlockBytes(blockMark);

					ReadyBlock rawBlock = new BlockParser().parseBlock(blockBytes,blockMark);
					ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient, rawBlock);
					new BlockWriter().writeIntoEs(esClient, readyBlock,opReFile);

					bestBlockMark = Preparer.mainList.get(Preparer.mainList.size()-1);

					if(bestBlockMark.getBlockId()!= Preparer.BestHash) {
						System.out.println("BestHash "+Preparer.BestHash+" is not the same as mainList:"+bestBlockMark.getBlockId());
						throw new Exception("BestHash "+Preparer.BestHash+" is not the same as mainList:"+bestBlockMark.getBlockId());
					}
					iter.remove();
					found = true;
					continue;
				}
				//If new fork

				for(BlockMark bm:Preparer.mainList) {
					//TODO untested the && condition blow.
					if (blockMark.getBlockId().equals(bm.getBlockId())) {
						iter.remove();
						found=true;
						break;
					}
					if(blockMark.getPreBlockId().equals(bm.getBlockId()) && !bm.getBlockId().equals(Preparer.BestHash)){

						blockMark.setHeight(bm.getHeight()+1);
						blockMark.setStatus(Preparer.FORK);
						Preparer.forkList.add(blockMark);
						iter.remove();
						writeBlockMark(esClient, blockMark);

						System.out.println("New fork block. Height: "+blockMark.getHeight()+". ForkList size:" + Preparer.forkList.size());
						log.info("New fork block. Height: "+blockMark.getHeight());
						found = true;
						break;
					}
				}
				if(found)continue;

				//If linked to a fork;
				for(BlockMark fm: Preparer.forkList) {
					if (blockMark.getBlockId().equals(fm.getBlockId())) {
						iter.remove();
						found=true;
						break;
					}
					if(blockMark.getPreBlockId().equals(fm.getBlockId())) {
						blockMark.setHeight(fm.getHeight()+1);
						blockMark.setStatus(Preparer.FORK);
						Preparer.forkList.add(blockMark);
						iter.remove();
						if(isForkOverMain(blockMark)) {
							HashMap<String, ArrayList<BlockMark>> chainMap = findLoseChainAndWinChain(blockMark);
							if(chainMap == null)return;
							reorganize(esClient,chainMap);
						}
						found = true;
						break;
					}
				}
				if(found)continue;
			}
			if(!found)break;
		}
	}
}
