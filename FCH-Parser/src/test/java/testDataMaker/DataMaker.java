package testDataMaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import constants.Constants;
import javaTools.BytesTools;
import fileTools.BlockFileTools;
import cryptoTools.SHA;

public class DataMaker {

	public static void main(String[] args) throws IOException, InterruptedException {
		makeBlockFile();
		System.out.println("Made a new block file.\n");
		readBlockFile() ;
	}
	public static void makeBlockFile() throws IOException {
		
		BlockParts blockParts = new BlockParts();

		File newFile = new File("blk0000.dat");
		FileOutputStream fos = new FileOutputStream(newFile);
		
		int fileOrder = 0;
		long pointer =0;
		//3个初始区块
		for(int i=0;i<3;i++) {
		blockParts = readBlock(fileOrder,pointer);
		pointer += blockParts.getLength();
		writeBlock(blockParts,fos);
		System.out.println("Block "+ getLastId(blockParts)+ " writed. Length:"+ blockParts.getLength()+" Pointer:"+pointer);
		}
		String lastId = getLastId(blockParts);

		//2个valid header fork 区块
		for(int i=0;i<2;i++) {
		blockParts = readBlock(fileOrder, pointer);
		pointer += blockParts.getLength();//pointer += 88;
		//blockParts.setBody(null);
		writeBlock(blockParts,fos);
		System.out.println("Writed fork valid header Block "+ getLastId(blockParts)+" Pointer:"+pointer);
		}
		//1个正常区块
		
		blockParts = readBlock(fileOrder, pointer);
		pointer += blockParts.getLength();
		BlockParts blockParts1 = replacePreId(blockParts,lastId);
		writeBlock(blockParts1,fos);
		System.out.println("Block "+ getLastId(blockParts1)+ " writed. Length:"+ blockParts.getLength()+" Pointer:"+pointer);

		lastId = getLastId(blockParts);
		
		//31815
		blockParts = readBlock(fileOrder, 13766105);
		pointer += blockParts.getLength();
		blockParts = replacePreId(blockParts,lastId);
		writeBlock(blockParts,fos);
		System.out.println("Block "+ getLastId(blockParts)+ " writed. Length:"+ blockParts.getLength());
		
		//填充100个0
		byte[] b0 = new byte[1];
		b0[0]=0;
		for(int i=0;i<100;i++) {
			fos.write(b0);
		}	
		System.out.println("100 ‘0’filled. ");
		
		fos.close();
	}

	public static void readBlockFile() throws IOException, InterruptedException {
		long pointer = 0;
		TimeUnit.SECONDS.sleep(3);
		while(true) {
			FileInputStream fis = new FileInputStream(new File("blk0000.dat"));
			fis.skip(pointer);

			byte []	b8 = new byte[8];
			byte []	b4 = new byte[4];
			byte []	b80 = new byte[8];
			
			int end = fis.read(b8);
			
			if(end == -1) {
				System.out.println("File finished.");
				break;
			}
			b4 = Arrays.copyOfRange(b8, 0, 4);
			if(b4[0]==0) {
				System.out.println("Readed the begining of 0.");
				fis.close();
				return;
			}
	
			b4 = Arrays.copyOfRange(b8, 4, 8);
			int blockSize = (int) BytesTools.bytes4ToLongLE(b4);

			
			System.out.println("blockSize: "+blockSize);
			byte[] blockBytes = new byte[blockSize];
			
			end = fis.read(blockBytes);
			if(end == -1) {
				System.out.println("File finished.");
				break;
			}
			
			b80 = Arrays.copyOfRange(blockBytes, 0, 80);
						
			byte[] blockBodyBytes = new byte[blockSize-80];

			
			System.out.println("Readed new block :"+ BytesTools.bytesToHexStringLE(SHA.Sha256x2(b80)));
			System.out.println("PreId is :"+ BytesTools.bytesToHexStringLE(Arrays.copyOfRange(b80, 4, 4+32)));
			blockBodyBytes = Arrays.copyOfRange(blockBytes, 80, blockSize);
			
			//Check valid header fork
			b4 = Arrays.copyOfRange(blockBodyBytes, 0, 4);
			String b4Hash = BytesTools.bytesToHexStringBE(b4) ;
			if(b4Hash.equals(Constants.MAGIC)) {
				System.out.println("Found valid header fork. Pointer: "+pointer);
				pointer += 88;
			}
			pointer += (blockSize+8);
			fis.close();
		}
	}


	public static String getLastId(BlockParts blockParts) {
		// TODO Auto-generated method stub
		return BytesTools.bytesToHexStringLE(SHA.Sha256x2(blockParts.getB80()));
	}

	public static BlockParts replacePreId(BlockParts blockParts1, String preId) {
		// TODO Auto-generated method stub
		BlockParts blockParts = blockParts1;
		
		System.out.print("PreId replaced from: "+ BytesTools.bytesToHexStringLE(Arrays.copyOfRange(blockParts.getB80(), 4, 4+32)));
		byte[] b32PreId = new byte[32];
		b32PreId = BytesTools.invertArray( BytesTools.hexToByteArray(preId));
		
		byte[] b80BlockHead = blockParts.getB80();
		
		byte[] bHead = Arrays.copyOfRange(b80BlockHead, 0,4);
		byte[] bTail = Arrays.copyOfRange(b80BlockHead, 36,80);
		
		ArrayList<byte[]> b80L = new ArrayList<byte[]>();
		
		b80L.add(bHead);
		b80L.add(b32PreId);
		b80L.add(bTail);
		
		byte[] b80 = BytesTools.bytesMerger(b80L);
		
		blockParts.setB80(b80);
		
		System.out.println(" to: "+ BytesTools.bytesToHexStringLE(Arrays.copyOfRange(b32PreId, 4, 4+32)));
		BytesTools.bytesToHexStringLE(Arrays.copyOfRange(b80, 4, 4+32));
		System.out.println("This blockId is :"+ BytesTools.bytesToHexStringLE(SHA.Sha256x2(blockParts.getB80())));

		return blockParts;
	}

	public static BlockParts readBlock(int fileOrder, long pointer) throws IOException {

		String fileName = BlockFileTools.getFileNameWithOrder(fileOrder);
		
		File inFile = new File("/Users/liuchangyong/fc_data/blocks",fileName);
		FileInputStream fis = new FileInputStream(inFile);
		
		fis.skip(pointer);
		
		BlockParts bp = new BlockParts();
		
		byte []	b8 = new byte[8];
		byte []	b4 = new byte[4];
		byte []	b80 = new byte[8];
		
		fis.read(b8);

		b4 = Arrays.copyOfRange(b8, 4, 8);
		int blockSize = (int) BytesTools.bytes4ToLongLE(b4);
		
		byte[] blockBytes = new byte[blockSize];
		fis.read(blockBytes);
		
		b80 = Arrays.copyOfRange(blockBytes, 0, 80);
		
		bp.setLenght(8+blockSize);
		bp.setB8(b8);
		bp.setB80(b80);
		bp.setBody(Arrays.copyOfRange(blockBytes, 80,blockBytes.length));
		
		System.out.println("Readed block: "+ BytesTools.bytesToHexStringLE(SHA.Sha256x2(b80)));
		
		fis.close();
		return bp;
	}

	private static void writeBlock(BlockParts blockParts, FileOutputStream fos) throws IOException {
		// TODO Auto-generated method stub
		if(blockParts.getB8()!=null) {
			fos.write(blockParts.getB8());
		}
		if(blockParts.getB80()!=null) {
			fos.write(blockParts.getB80());
		}
		if(blockParts.getBody()!=null) {
			fos.write(blockParts.getBody());
		}
		
		fos.flush();
	}
}
