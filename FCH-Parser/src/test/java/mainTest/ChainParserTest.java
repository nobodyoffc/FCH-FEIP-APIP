package mainTest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import esTools.EsTools;
import fchClass.BlockMark;
import javaTools.BytesTools;
import cryptoTools.SHA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fileTools.BlockFileTools;
import fileTools.OpReFileTools;
import parser.ReadyBlock;
import startFCH.IndicesFCH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static constants.IndicesNames.BLOCK_MARK;

public class ChainParserTest {
	private enum CheckStatus{
		LINK_MAIN, WAIT_MORE, FILE_END, WRONG, HEADER_FORK, REPEAT, NEW_FORK, LINK_FORK, ORPHAN
	}
	
	public static final String MAGIC = "f9beb4d9";
	public static final  String OpRefileName = "opreturn0.byte";

	
	Logger log = LoggerFactory.getLogger(ChainParserTest.class);
	private OpReFileTools opReFile = new OpReFileTools();
	
	//private OpReFileTools opReFile = new OpReFileTools();
	
	public int startParse(ElasticsearchClient esClient) throws Exception {
		
		System.out.println("Started parsing file:  "+PreparerTest.CurrentFile+" ...");
		log.info("Started parsing file: {} ...",PreparerTest.CurrentFile);
		
		File file = new File(PreparerTest.Path,PreparerTest.CurrentFile);
		FileInputStream fis = new FileInputStream(file);
		fis.skip(PreparerTest.Pointer);

		long cdMakeTime = System.currentTimeMillis();

		while(true) {
			CheckResult checkResult = checkBlock(fis);
			
			BlockMark blockMark = checkResult.getBlockMark();
			byte[] blockBytes = checkResult.getBlockBytes();	
			int blockLength = checkResult.getBlockLength();
			CheckStatus checkStatus = checkResult.getCheckStatus();
			
			switch (checkStatus) {
			
			case REPEAT:
				PreparerTest.Pointer += blockLength;
				break;
			case NEW_FORK:
				PreparerTest.Pointer += blockLength;	
				writeBlockMark(esClient, blockMark);
				PreparerTest.forkList.add(blockMark);
				recheckOrphans(esClient);	
				if(isAnyForkOverMain(blockMark)) {
					reorganize(esClient,blockMark);
				}
				break;
			case LINK_FORK:
				PreparerTest.Pointer += blockLength;
				writeBlockMark(esClient, blockMark);
				PreparerTest.forkList.add(blockMark);
				recheckOrphans(esClient);
				if(isAnyForkOverMain(blockMark)) {
					reorganize(esClient,blockMark);
				}
				break;
			case LINK_MAIN:
				PreparerTest.Pointer += blockLength;
				
				ReadyBlock rawBlock = parseBlock(blockBytes,blockMark);
				//ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient.esClient, rawBlock);
				//new BlockWriter().writeIntoEs(esClient.esClient, readyBlock,opReFile);
				PreparerTest.mainList.add(blockMark);
				if (PreparerTest.mainList.size() > EsTools.READ_MAX) {
					PreparerTest.mainList.remove(0);
				}
				PreparerTest.BestHash = blockMark.getBlockId();
				PreparerTest.BestHeight = blockMark.getHeight();
				
				dropOldFork(blockMark.getHeight());	
				recheckOrphans(esClient);	
				if(isAnyForkOverMain(blockMark)) {
					reorganize(esClient,blockMark);
				}
				break;
			case WAIT_MORE:
				System.out.println("Waiting 30 seconds. pointer: "+PreparerTest.Pointer);
				TimeUnit.SECONDS.sleep(30);
				fis.close();
				fis = new FileInputStream(file);
				fis.skip(PreparerTest.Pointer);
				break;
			case FILE_END:
				String nextFile = BlockFileTools.getNextFile(PreparerTest.CurrentFile);
				if(new File(PreparerTest.Path, nextFile).exists()) {
					System.out.println("file "+PreparerTest.CurrentFile+" finished.");	
					log.info("Parsing file {} finished.",PreparerTest.CurrentFile);
					PreparerTest.CurrentFile = nextFile;
					PreparerTest.Pointer = 0;
					fis.close();
					file = new File(PreparerTest.Path,PreparerTest.CurrentFile);
					fis = new FileInputStream(file);
					//TODO
					System.out.println("Start parse file "+PreparerTest.CurrentFile+".");	
					log.info("Start parsing file {}.",PreparerTest.CurrentFile);
					break;
				}else {
					System.out.println("Waiting 30 seconds for new file ...");	
						TimeUnit.SECONDS.sleep(30);
						fis.close();
						fis = new FileInputStream(file);
						fis.skip(PreparerTest.Pointer);
				}
				break;
			case WRONG:
				System.out.println("Read Magic wrong. Check blk files. file: "+PreparerTest.CurrentFile+"pointer: "+PreparerTest.Pointer);
				log.info("Read Magic wrong. file:{}pointer: {}",PreparerTest.CurrentFile,PreparerTest.Pointer);
				return 0;//TODO 检查prepare()
			case HEADER_FORK:
				PreparerTest.Pointer = PreparerTest.Pointer + 88;
				fis.close();
				fis = new FileInputStream(file);
				fis.skip(PreparerTest.Pointer);
				
				System.out.println("Header valid fork was found. Height: "+PreparerTest.BestHeight+1);
				log.info("Header valid fork was found. Height: "+PreparerTest.BestHeight+1);
				TimeUnit.SECONDS.sleep(5);
				break;
			case ORPHAN:
				//TimeUnit.SECONDS.sleep(1);
				PreparerTest.Pointer += blockLength;
				writeBlockMark(esClient, blockMark);
				PreparerTest.orphanList.add(blockMark);
				break;
			}

//			Block bestBlock = ParseTools.getBestBlock(esClient.esClient);
//			long bestBlockTime = bestBlock.getTime();
//			if(bestBlockTime*1000 - cdMakeTime > (1000*60*60*12)) {
//				CdMaker cdMaker = new CdMaker();
//
//				cdMaker.makeUtxoCd(esClient.esClient,bestBlock);
//				log.info("All cd of UTXOs updated.");
//				TimeUnit.MINUTES.sleep(2);
//
//				cdMaker.makeAddrCd(esClient.esClient);
//				log.info("All cd of addresses updated.");
//				TimeUnit.MINUTES.sleep(1);
//				
//				cdMakeTime = bestBlockTime;
//			}
		}
	}

	private CheckResult checkBlock(FileInputStream fis) throws Exception {
		
		BlockMark blockMark = new BlockMark();	
		blockMark.set_pointer(PreparerTest.Pointer);
		blockMark.set_fileOrder(getFileOrder());
		
		CheckResult checkResult = new CheckResult();
		
		byte []	b8 = new byte[8];
		byte []	b4 = new byte[4];

		//If file ends.
		if(fis.read(b8) == -1) {
			System.out.println("File end when reading magic. pointer: "+PreparerTest.Pointer);
			log.info("File end when reading magic. ");
			checkResult.setCheckStatus(CheckStatus.FILE_END);
			return checkResult;
		} 

		//If no more blocks.
		if(b8[0]==0) {
			checkResult.setCheckStatus(CheckStatus.WAIT_MORE);
			return checkResult;
		}

		//Read magic
		b4 = Arrays.copyOfRange(b8, 0, 4);
		String magic = BytesTools.bytesToHexStringBE(b4) ;
		if(!magic.equals(MAGIC)) {
			checkResult.setCheckStatus(CheckStatus.WRONG);
			return checkResult;
		}
		
		//Parse blockSize
		b4 = Arrays.copyOfRange(b8, 4, 8);
		int blockSize = (int) BytesTools.bytes4ToLongLE(b4);
		blockMark.setSize(blockSize);

		byte[] blockBytes = new byte[blockSize];
		if(fis.read(blockBytes)== -1) {
			System.out.println("File end when reading block. pointer: "+PreparerTest.Pointer);
			log.info("File end when reading block. Pointer:"+ PreparerTest.Pointer);
			checkResult.setCheckStatus(CheckStatus.FILE_END);
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
		
		//If valid headers-only fork
		b4 = Arrays.copyOfRange(blockBodyBytes, 0, 4);
		String b4Hash = BytesTools.bytesToHexStringBE(b4) ;
		if(b4Hash.equals(MAGIC)) {
			System.out.println("Found valid header fork. Pointer: "+PreparerTest.Pointer);
			log.info("Found valid header fork. Pointer: {}",PreparerTest.Pointer);
			checkResult.setCheckStatus(CheckStatus.HEADER_FORK);
			return checkResult;
		}
		
		if(isRepeated(blockMark)) {
			System.out.println("Repeat block.");
			checkResult.setBlockLength(blockSize+8);
			checkResult.setCheckStatus(CheckStatus.REPEAT);
			return checkResult;
		}

		if(isLinkToMain(blockMark)) {
			System.out.println("main block.");
			checkResult.setBlockMark(blockMark);
			checkResult.setBlockLength(blockSize+8);
			checkResult.setBlockBytes(blockBytes);
			checkResult.setCheckStatus(CheckStatus.LINK_MAIN);
			return checkResult;
		}
		if(isNewFork(blockMark)) {
			System.out.println("new fork block.");
			checkResult.setBlockMark(blockMark);
			checkResult.setBlockLength(blockSize+8);
			checkResult.setBlockBytes(blockBytes);
			checkResult.setCheckStatus(CheckStatus.NEW_FORK);
			return checkResult;
		}
			
		if(isLinkedToFork(blockMark))	{	
			System.out.println("fork block.");
			checkResult.setBlockMark(blockMark);
			checkResult.setBlockLength(blockSize+8);
			checkResult.setBlockBytes(blockBytes);
			checkResult.setCheckStatus(CheckStatus.LINK_FORK);
			return checkResult;
		}
		System.out.println("Orphan block. ID:" +blockMark.getBlockId()+"preID"+blockMark.getPreBlockId()
			+ " main" + PreparerTest.mainList.size()
			+ " orphan:"+ PreparerTest.orphanList.size()
			+ " fork" + PreparerTest.forkList.size()
		+ " height:"+ PreparerTest.BestHeight);
		if(PreparerTest.BestHeight==12443) {
			checkResult.setCheckStatus(CheckStatus.WRONG);
			return checkResult;
		}
		/////////////
		blockMark.setStatus(PreparerTest.ORPHAN);
		checkResult.setBlockMark(blockMark);	
		checkResult.setBlockLength(blockSize+8);
		checkResult.setBlockBytes(blockBytes);
		checkResult.setCheckStatus(CheckStatus.ORPHAN);
		return checkResult;
	}
	private boolean isRepeated(BlockMark blockMark) {
		// TODO Auto-generated method stub	
		for(int i=0;i<PreparerTest.mainList.size()-1;i++) {
			BlockMark bm = PreparerTest.mainList.get(i);
			if(blockMark.getBlockId().equals(bm.getBlockId())){
				//TODO
				System.out.println("Repeat block. Height: "+blockMark.getHeight());
				log.info("Repeat block. Height: "+blockMark.getHeight());
				return true;
			}
		}
		return false;
	}

	private boolean isLinkToMain(BlockMark blockMark) {
		// TODO Auto-generated method stub
		if(blockMark.getPreBlockId().equals(PreparerTest.BestHash)){
			blockMark.setStatus(PreparerTest.MAIN);
			blockMark.setHeight(PreparerTest.BestHeight+1);
			return true;
		}
		return false;
	}

	private boolean isNewFork(BlockMark blockMark) {
		// TODO Auto-generated method stub
		for(int i=PreparerTest.mainList.size()-1;i>=PreparerTest.mainList.size()-31;i--) {
			if(i < 0)return false;
			BlockMark bm = PreparerTest.mainList.get(i);
			if(blockMark.getPreBlockId().equals(bm.getBlockId())){
				blockMark.setHeight(bm.getHeight()+1);
				blockMark.setStatus(PreparerTest.FORK);			
				//TODO
				System.out.println("New fork block. Height: "+blockMark.getHeight());
				log.info("New fork block. Height: "+blockMark.getHeight());
				return true;
			}
		}
		//}
		return false;
	}

	private boolean isLinkedToFork(BlockMark blockMark) {
		// TODO Auto-generated method stub
		
		for(BlockMark bm:PreparerTest.forkList) {

			if(blockMark.getPreBlockId().equals(bm.getBlockId())){
				blockMark.setHeight(bm.getHeight()+1);
				blockMark.setStatus(PreparerTest.FORK);

				//TODO
				System.out.println("Linked tofork block. Height: "+blockMark.getHeight());
				log.info("Linked tofork block. Height: "+blockMark.getHeight());
				return true;
			}
		}
		return false;
	}

	private int getFileOrder() {
		return BlockFileTools.getFileOrder(PreparerTest.CurrentFile);
	}
	private class CheckResult{
		int blockLength;
		CheckStatus checkStatus;
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
		public CheckStatus getCheckStatus() {
			return checkStatus;
		}
		public void setCheckStatus(CheckStatus checkStatus) {
			this.checkStatus = checkStatus;
		}
	}
	
	private void linkToChain(ElasticsearchClient esClient, BlockMark blockMark1, byte[] blockBytes) throws Exception {
		BlockMark blockMark = blockMark1;
		
		if(isRepeatBlcokIgnore(blockMark)) 
			return;
		if(isLinkToMainChainWriteItToEs(esClient, blockMark, blockBytes))
			return; 	
		if(isNewForkAddMarkToEs(esClient, blockMark)) 
			return;
		if(isLinkedToForkWriteMarkToEs(esClient, blockMark)){
			if(isAnyForkOverMain(blockMark)) {
				reorganize(esClient,blockMark);
			}
			return;
		}
		writeOrphanMark(esClient, blockMark);
		return; 
	}
	private void writeBlockMark(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException {
		//esClient.esClient.index(i->i.index(Indices.BlockMarkIndex).id(blockMark.getId()).document(blockMark));
		
	}	
	private void reorganize(ElasticsearchClient esClient, BlockMark blockMark) throws Exception {

		ArrayList<BlockMark> winList = findTheBlockBeforeFork(blockMark);
		
		if(winList == null || winList.isEmpty()) return;
			BlockMark blockMarkBeforeFork = winList.get(winList.size()-1);
			mainToFork(esClient, blockMarkBeforeFork);
			//TODO
			System.out.println("Reorganization happen after height: "+blockMarkBeforeFork.getHeight());
			log.info("Reorganization happen after height: "+blockMarkBeforeFork.getHeight());

		//new RollBacker().rollback(esClient.esClient,blockMarkBeforeFork.getHeight());
			writeWinListToEs(esClient,winList);
			System.out.println("Reorganized. Fork: "+PreparerTest.forkList.size()+"BlockId before fork: "+blockMarkBeforeFork.getBlockId()+" Height: "+blockMarkBeforeFork.getHeight());
	}
	private boolean isRepeatBlcokIgnore(BlockMark blockMark) {

		if(PreparerTest.mainList==null || PreparerTest.mainList.isEmpty())return false;
		Iterator<BlockMark> iter = PreparerTest.mainList.iterator();
		while(iter.hasNext()) {
			if(blockMark.getBlockId().equals(iter.next().getBlockId())){
				//TODO
				System.out.println("Repeat block..." );
				log.info("Repeat block..." );
				return true;
			}
		}
		return false;
	}
	private boolean isLinkToMainChainWriteItToEs(ElasticsearchClient esClient, BlockMark blockMark1, byte[] blockBytes) throws Exception {

		BlockMark blockMark = blockMark1;
		
		if(blockMark.getPreBlockId().equals(PreparerTest.BestHash)){
			blockMark.setStatus(PreparerTest.MAIN);
			long newHeight = PreparerTest.BestHeight+1;
			blockMark.setHeight(newHeight);
			ReadyBlock rawBlock = parseBlock(blockBytes,blockMark);
			//ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient.esClient, rawBlock);
			//new BlockWriter().writeIntoEs(esClient.esClient, readyBlock,opReFile);
			dropOldFork(newHeight);
			return true;
		}
		return false;
	}
	private void dropOldFork(long newHeight) {

		Iterator<BlockMark> iter = PreparerTest.forkList.iterator();
		while(iter.hasNext()) {
			BlockMark bm = iter.next();
			if(bm.getHeight() < newHeight-30) {
				iter.remove();
			}
		}
	}

	private boolean isNewForkAddMarkToEs(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException {

		//Iterator<BlockMark> iter = PreparerTest.mainList.iterator();
		//while(iter.hasNext()) {
		for(int i=PreparerTest.mainList.size()-1;i>=PreparerTest.mainList.size()-31;i--) {
			BlockMark bm = PreparerTest.mainList.get(i);
			if(blockMark.getPreBlockId().equals(bm.getBlockId())){
				blockMark.setHeight(bm.getHeight()+1);
				blockMark.setStatus(PreparerTest.FORK);
				writeBlockMark(esClient, blockMark);
				PreparerTest.forkList.add(blockMark);
				
				//TODO
				System.out.println("New fork block. Height: "+blockMark.getHeight());
				log.info("New fork block. Height: "+blockMark.getHeight());
				return true;
			}
		}
		//}
		return false;
	}
	private boolean isLinkedToForkWriteMarkToEs(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException {

		Iterator<BlockMark> iter = PreparerTest.forkList.iterator();
		while(iter.hasNext()) {
			BlockMark bm = iter.next();
			if(blockMark.getPreBlockId().equals(bm.getBlockId())){
				blockMark.setHeight(bm.getHeight()+1);
				blockMark.setStatus(PreparerTest.FORK);
				writeBlockMark(esClient, blockMark);
				PreparerTest.forkList.add(blockMark);
				//TODO
				System.out.println("Linked tofork block. Height: "+blockMark.getHeight());
				log.info("Linked tofork block. Height: "+blockMark.getHeight());
				return true;
			}
		}
		
		return false;
	}

	private boolean isAnyForkOverMain(BlockMark blockMark) {
		for(BlockMark bm:PreparerTest.forkList) {	
			if(bm.getHeight()>PreparerTest.BestHeight) return true;
		}
		return false;
	}
	private ArrayList<BlockMark> findTheBlockBeforeFork(BlockMark blockMark1) {

		BlockMark blockMark = blockMark1;
		
		String preId = blockMark.getPreBlockId();
		BlockMark mainBlock = new BlockMark();
		BlockMark forkBlock = new BlockMark();

		ArrayList<BlockMark> findList = new ArrayList<BlockMark>();
		blockMark.setStatus(PreparerTest.MAIN);
		findList.add(blockMark);
		
		boolean found = false;
		
		while(true) {
			found = false;
			for(BlockMark bm : PreparerTest.forkList) {
				if(bm.getBlockId().equals(preId)) {
					bm.setStatus(PreparerTest.MAIN);
					findList.add(forkBlock);
					
					for(int i=PreparerTest.mainList.size()-2; i>=PreparerTest.mainList.size()-31; i--) {
						mainBlock = PreparerTest.mainList.get(i);
						
						if(bm.getPreBlockId().equals(mainBlock.getBlockId())){
							findList.add(mainBlock);
							return findList;
						}
					}
					found = true;
					preId = bm.getPreBlockId();
				}
				if(found)break;
			}
			if(!found) {
				PreparerTest.forkList.removeAll(findList);
				return null;
			}
		}
	}
	//TODO
	//private static void mainToFork(ElasticsearchClient esClient.esClient,BlockMark blockMarkBeforeFork) throws Exception {
	private void mainToFork(ElasticsearchClient esClient,BlockMark blockMarkBeforeFork) throws Exception {
	// TODO Auto-generated method stub
		
		BulkRequest.Builder br = new BulkRequest.Builder();
		
		for(int i=PreparerTest.mainList.size()-1; i>=PreparerTest.mainList.size()-31; i--) {
			BlockMark mainBlockMark = PreparerTest.mainList.get(i);
			
			ArrayList<BlockMark> mainToForkList = new ArrayList<BlockMark>();
			
			if(blockMarkBeforeFork.getBlockId().equals(mainBlockMark.getBlockId())) {
				
				esClient.bulk(br.build());
				
				PreparerTest.forkList.addAll(mainToForkList);
				PreparerTest.mainList.removeAll(mainToForkList);
				return;
			}
			mainBlockMark.setStatus(PreparerTest.FORK);
			br.operations(op->op.index(in->in
					.index(BLOCK_MARK)
					.id(mainBlockMark.getBlockId())
					.document(mainBlockMark)));	
			mainToForkList.add(mainBlockMark);
		}

		log.error("The fork block is not found in mainBlockMarkList!!!");
		throw new Exception("The fork block is not found in mainBlockMarkList!!!");
	}

	private boolean writeWinListToEs(ElasticsearchClient esClient, ArrayList<BlockMark> winList) throws Exception {
		// TODO Auto-generated method stub
		for(int i=winList.size()-2;i>=0;i--) {
			BlockMark blockMark = winList.get(i);
			byte[] blockBytes = getBlockBytes(blockMark);
			ReadyBlock rawBlock = parseBlock(blockBytes,blockMark);

			//ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient.esClient, rawBlock);
			//new BlockWriter().writeIntoEs(esClient.esClient, readyBlock,opReFile);
		}
		//TODO
		System.out.println("Reorganization finished. from: "+ winList.get(0).getHeight()+" to "+winList.get(winList.size()-1).getHeight());
		log.info("Reorganization finished. from: "+ winList.get(0).getHeight()+" to "+winList.get(winList.size()-1).getHeight());
		dropOldFork(winList.get(0).getHeight());
		return false;
	}
	private byte[] getBlockBytes(BlockMark bm) throws IOException {
		
		File file = new File(PreparerTest.Path, BlockFileTools.getFileNameWithOrder(bm.get_fileOrder()));
		FileInputStream fis = new FileInputStream(file);
		fis.skip(bm.get_pointer()+8);
		byte[] blockBytes = new byte[(int) bm.getSize()];
		fis.read(blockBytes);
		fis.close();
		return blockBytes;
	}

	private void writeOrphanMark(ElasticsearchClient esClient,BlockMark blockMark) throws ElasticsearchException, IOException {

		blockMark.setStatus(PreparerTest.ORPHAN);
		writeBlockMark(esClient, blockMark);
		PreparerTest.orphanList.add(blockMark);
	}
	private void recheckOrphans(ElasticsearchClient esClient) throws Exception {
		//TODO
		System.out.println("rechekOrphans");
		//与fork或main重复的要删除

		boolean found = false;
		
		BlockMark bestBlockMark = new BlockMark();;
		
		while(!found) {	
			//If linked to main;
			
			Iterator<BlockMark> iter = PreparerTest.orphanList.iterator();
			while(iter.hasNext()) {
				BlockMark blockMark = iter.next();
				if(blockMark.getPreBlockId().equals(PreparerTest.BestHash)) {
					blockMark.setHeight(PreparerTest.BestHeight+1);
					blockMark.setStatus(PreparerTest.MAIN);
					byte[] blockBytes = getBlockBytes(blockMark);
					
					ReadyBlock rawBlock = parseBlock(blockBytes,blockMark);
					//ReadyBlock readyBlock = new BlockMaker().makeReadyBlock(esClient.esClient, rawBlock);
					//writeIntoEs(esClient.esClient, readyBlock,opReFile);
					PreparerTest.mainList.add(blockMark);
					if (PreparerTest.mainList.size() > EsTools.READ_MAX) {
						PreparerTest.mainList.remove(0);
					}
					PreparerTest.BestHash = blockMark.getBlockId();
					PreparerTest.BestHeight = blockMark.getHeight();
					//
					
					
					//TODO
					bestBlockMark = PreparerTest.mainList.get(PreparerTest.mainList.size()-1);
					if(bestBlockMark.getBlockId()!= PreparerTest.BestHash) {
						System.out.println("BestHash "+PreparerTest.BestHash+" is not the same as mainList:"+bestBlockMark.getBlockId());
						throw new Exception("BestHash "+PreparerTest.BestHash+" is not the same as mainList:"+bestBlockMark.getBlockId());
					}
					deleteMarkFromEs(esClient,blockMark);
					iter.remove();
					found = true;
					continue;
				}
				//If linked to a fork;
				Iterator<BlockMark> iterf = PreparerTest.forkList.iterator();
				while(iterf.hasNext()) {
					BlockMark fm = iterf.next();
					if(blockMark.getPreBlockId().equals(fm.getBlockId())) {
						blockMark.setHeight(fm.getHeight()+1);
						blockMark.setStatus(PreparerTest.FORK);
						PreparerTest.forkList.add(blockMark);
						
						deleteMarkFromEs(esClient,blockMark);
						iterf.remove();
						
						if(isAnyForkOverMain(blockMark)) {
							reorganize(esClient,blockMark);
						}
						found = true;
						break;
					}	
				}
			}
			if(!found)break;
		}			
	}

	private void deleteMarkFromEs(ElasticsearchClient esClient, BlockMark blockMark) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		//esClient.esClient.delete(d->d.index(Indices.BlockMarkIndex).id(blockMark.getId()));
	}

	private ReadyBlock parseBlock(byte[] blockBytes, BlockMark blockMark) {
		// TODO Auto-generated method stub
		System.out.println("Block parsed. file:"+ PreparerTest.CurrentFile
				+ " pointer: "+ PreparerTest.Pointer
				+ " blockId:"+ blockMark.getBlockId()
				+ " main" + PreparerTest.mainList.size()
				+ " orphan:"+ PreparerTest.orphanList.size()
				+ " fork" + PreparerTest.forkList.size()
				+ " id"+blockMark.getBlockId()
				+ " Height:"+blockMark.getHeight());
		
		PreparerTest.mainList.add(blockMark);
		if (PreparerTest.mainList.size() > EsTools.READ_MAX) {
			PreparerTest.mainList.remove(0);
		}
		PreparerTest.BestHash = blockMark.getBlockId();
		PreparerTest.BestHeight = blockMark.getHeight();
		return null;
	}
}
