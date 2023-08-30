package parser;

import fchClass.*;
import javaTools.BytesTools;
import cryptoTools.SHA;
import keyTools.KeyTools;
import fcTools.ParseTools;
import fcTools.ParseTools.VarintResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BlockParser {
	public ReadyBlock parseBlock(byte[] blockBytes, BlockMark blockMark) throws IOException {
		

		Block block = new Block();
		block.setBlockId(blockMark.getBlockId());
		block.setPreBlockId(blockMark.getPreBlockId());
		block.setHeight(blockMark.getHeight());
		block.setSize(blockMark.getSize());

		ByteArrayInputStream blockInputStream = new ByteArrayInputStream(blockBytes);
		
		byte[] blockHeadBytes = new byte[80];
		blockInputStream.read(blockHeadBytes);
		
		byte[] blockBodyBytes = new byte[(int) (block.getSize() - 80)];
		blockInputStream.read(blockBodyBytes);

		block = parseBlockHead(blockHeadBytes, block);

		ReadyBlock readyBlock = parseBlockBody(blockBodyBytes, block);
		
		readyBlock.setBlockMark(blockMark);

		return readyBlock;
	}

	private Block parseBlockHead(byte[] blockHeadBytes, Block block1) {
		
		Block block = block1;

		int offset = 0;
		// Read 4 bytes of the block version
		// 读取4字节版本号
		byte[] b4Ver = Arrays.copyOfRange(blockHeadBytes, offset, offset + 4);
		offset += 4;
		block.setVersion(BytesTools.bytesToHexStringLE(b4Ver));

		// Read 32 bytes of the father block hash
		// 读取32字节前区块哈希
		byte[] b32PreId = Arrays.copyOfRange(blockHeadBytes, offset, offset + 32);
		offset += 32;
		String preId = BytesTools.bytesToHexStringLE(b32PreId);
		block.setPreBlockId(preId);

		// Read 32 bytes of the merkle root
		// 读取32字节默克尔根
		byte[] b32MklR = Arrays.copyOfRange(blockHeadBytes, offset, offset + 32);
		offset += 32;
		block.setMerkleRoot(BytesTools.bytesToHexStringLE(b32MklR));

		// Read 4 bytes of the time stamp.
		// 读取4字节时间戳：
		byte[] b4Time = Arrays.copyOfRange(blockHeadBytes, offset, offset + 4);
		offset += 4;
		block.setTime(BytesTools.bytes4ToLongLE(b4Time));

		// Read 4 bytes of difficulty.
		// 读取4字节挖矿难度：
		byte[] b4Bits = Arrays.copyOfRange(blockHeadBytes, offset, offset + 4);
		offset += 4;
		block.setDiffTarget(BytesTools.bytes4ToLongLE(b4Bits));

		// Read 4 bytes of nonce
		// 读取4字节挖矿随机数：
		byte[] b4Nonce = Arrays.copyOfRange(blockHeadBytes, offset, offset + 4);
		offset += 4;
		block.setNonce(BytesTools.bytes4ToLongLE(b4Nonce));

		return block;
	}

	private ReadyBlock parseBlockBody(byte[] blockBodyBytes, Block block1) throws IOException {
		
		ReadyBlock readyBlock = new ReadyBlock();
		Block block = block1;
		ByteArrayInputStream blockInputStream = new ByteArrayInputStream(blockBodyBytes);

		long txCount = ParseTools.parseVarint(blockInputStream).number;
		block.setTxCount((int) txCount);

		ArrayList<Tx> txList = new ArrayList<Tx>();
		ArrayList<TxHas> txHasList = new ArrayList<TxHas>();
		ArrayList<Cash> inList = new ArrayList<Cash>();
		ArrayList<Cash> outList = new ArrayList<Cash>();
		ArrayList<OpReturn> opReturnList = new ArrayList<OpReturn>();

		TxResult txResult = new TxResult();
		txResult = parseCoinbase(blockInputStream, block);

		txList.add(txResult.tx);
		outList.addAll(txResult.outList);

		for (int i = 1; i < txCount; i++) {
			txResult = parseTx(blockInputStream, block, i);

			txList.add(txResult.tx);
			inList.addAll(txResult.inList);
			outList.addAll(txResult.outList);
			if (txResult.opReturn != null)
				opReturnList.add(txResult.opReturn);
		}

		readyBlock.setBlock(block);
		readyBlock.setTxList(txList);
		readyBlock.setTxHasList(txHasList);
		if (inList != null)
			readyBlock.setInList(inList);
		readyBlock.setOutList(outList);
		if (opReturnList != null)
			readyBlock.setOpReturnList(opReturnList);
		
		return readyBlock;
	}

	private TxResult parseCoinbase(ByteArrayInputStream blockInputStream, Block block) throws IOException {

		ArrayList<byte[]> bytesList = new ArrayList<byte[]>();

		Tx tx = new Tx();

		byte[] b4Version = new byte[4];
		blockInputStream.read(b4Version);
		bytesList.add(b4Version);

		tx.setTxIndex(0);
		tx.setVersion(BytesTools.bytesToIntLE(b4Version));
		tx.setBlockTime(block.getTime());
		tx.setBlockId(block.getBlockId());
		tx.setHeight(block.getHeight());

		byte[] b37SkipInput = new byte[1 + 32 + 4];// Skip input count(1) + preHash(32 '00') + input index (4 'ff').
		blockInputStream.read(b37SkipInput);
		bytesList.add(b37SkipInput);

		byte[] b1ScriptLength = new byte[1]; // Input script length.
		blockInputStream.read(b1ScriptLength);
		bytesList.add(b1ScriptLength);

		int scriptLength = b1ScriptLength[0];
		byte[] bvScript = new byte[scriptLength]; // coinbase (input script).
		blockInputStream.read(bvScript);
		bytesList.add(bvScript);

		byte[] b4Sequence = new byte[4];
		blockInputStream.read(b4Sequence); // sequence of ffffffff.
		bytesList.add(b4Sequence);

		tx.setCoinbase(new String(bvScript));
		tx.setInCount(0);

		// Parse Outputs./解析输出。
		ParseTxOutResult parseTxOutResult = parseOut(blockInputStream, tx);
		bytesList.add(parseTxOutResult.rawBytes);
		tx = parseTxOutResult.tx;
		ArrayList<Cash> rawOutList = parseTxOutResult.rawOutList;

		// Read lock time.
		// 读取输出时间锁
		byte[] b4LockTime = new byte[4];
		blockInputStream.read(b4LockTime);
		bytesList.add(b4LockTime);
		tx.setLockTime(BytesTools.bytes4ToLongLE(b4LockTime));

		tx.setTxId(BytesTools.bytesToHexStringLE(SHA.Sha256x2(BytesTools.bytesMerger(bytesList))));
		ArrayList<Cash> outList = makeOutList(tx.getTxId(), rawOutList);

		TxHas txHas = new TxHas();
		txHas.setTxId(tx.getTxId());
		txHas.setHeight(tx.getHeight());

		TxResult txResult = new TxResult();
		txResult.tx = tx;
		txResult.outList = outList;
		return txResult;
	}

	private class TxResult {
		Tx tx;
		ArrayList<Cash> inList;
		ArrayList<Cash> outList;
		OpReturn opReturn;
	}

	private ParseTxOutResult parseOut(ByteArrayInputStream blockInputStream, Tx tx1) throws IOException {
		Tx tx = tx1;
		ArrayList<byte[]> rawBytesList = new ArrayList<byte[]>(); // For returning raw bytes.
		ArrayList<Cash> rawOutList = new ArrayList<Cash>();// For returning outputs without

		String opReturnStr = "";

		// Parse output count.
		// 解析输出数量。
		VarintResult varintParseResult = new VarintResult();
		varintParseResult = ParseTools.parseVarint(blockInputStream);
		long outputCount = (long) varintParseResult.number;
		byte[] b0 = (byte[]) varintParseResult.rawBytes;
		rawBytesList.add(b0);

		tx.setOutCount((int) outputCount);

		// Starting operators in output script.
		// 输出脚本中的起始操作码。
		final byte OP_DUP = (byte) 0x76;
		final byte OP_HASH160 = (byte) 0xa9;
		final byte OP_RETURN = (byte) 0x6a;
		byte b1Script = 0x00; // For get the first byte of output script./接收脚本中的第一个字节。

		for (int j = 0; j < outputCount; j++) {
			// Start one output.
			// 开始解析一个输出。
			Cash out = new Cash();
			out.setBirthIndex(j);
			out.setBirthTxIndex(tx.getTxIndex());
			out.setBirthHeight(tx.getHeight());
			out.setBirthBlockId(tx.getBlockId());
			out.setBirthTime(tx.getBlockTime());

			// Read the value of this output in satoshi.
			// 读取该输出的金额，以聪为单位。
			byte[] b8Value = new byte[8];
			blockInputStream.read(b8Value);
			rawBytesList.add(b8Value);
			out.setValue(BytesTools.bytes8ToLong(b8Value,true));

			// Parse the length of script.
			// 解析脚本长度。
			varintParseResult = ParseTools.parseVarint(blockInputStream);
			long scriptSize = varintParseResult.number;
			byte[] b2 = (byte[]) varintParseResult.rawBytes;
			rawBytesList.add(b2);

			byte[] bScript = new byte[(int) scriptSize];
			blockInputStream.read(bScript);
			rawBytesList.add(bScript);

			b1Script = bScript[0];

			switch (b1Script) {
			case OP_DUP:
				out.setType("P2PKH");
				out.setLockScript(BytesTools.bytesToHexStringBE(bScript));
				byte[] hash160Bytes = Arrays.copyOfRange(bScript, 3, 23);
				out.setFid(KeyTools.hash160ToFCHAddr(hash160Bytes));
				break;
			case OP_RETURN:
				out.setType("OP_RETURN");
				opReturnStr = new String(Arrays.copyOfRange(bScript, 2, bScript.length));
				out.setFid("OpReturn");

				out.setValid(false);
				if (tx.getTxIndex() != 0) {
					if (opReturnStr.length() <= 30) {
						tx.setOpReBrief(opReturnStr);
					} else {
						tx.setOpReBrief(opReturnStr.substring(0, 29));
					}
				}
				break;
			case OP_HASH160:
				out.setType("P2SH");
				out.setLockScript(BytesTools.bytesToHexStringBE(bScript));
				byte[] hash160Bytes1 = Arrays.copyOfRange(bScript, 2, 22);
				out.setFid(KeyTools.hash160ToMultiAddr(hash160Bytes1));
				break;
			default:
				out.setType("Unknown");
				out.setLockScript(BytesTools.bytesToHexStringBE(bScript));
				out.setFid("Unknown");
			}

			// Add block and tx information to output./给输出添加区块和交易信息。
			// Add information where it from/添加来源信息
			out.setValid(true);
			out.setBirthTime(tx.getBlockTime());
			out.setBirthTxIndex(tx.getTxIndex());

			// Add this output to List.
			// 添加输出到列表。
			rawOutList.add(out);
		}
		byte[] rawBytes = (byte[]) BytesTools.bytesMerger(rawBytesList);

		ParseTxOutResult parseTxOutResult = new ParseTxOutResult();
		parseTxOutResult.rawBytes = rawBytes;
		parseTxOutResult.rawOutList = rawOutList;
		if (tx.getTxIndex() != 0)
			parseTxOutResult.opReturnStr = opReturnStr;
		parseTxOutResult.tx = tx;

		return parseTxOutResult;
	}

	private class ParseTxOutResult {
		Tx tx;
		ArrayList<Cash> rawOutList;
		byte[] rawBytes;
		String opReturnStr;
	}

	private ArrayList<Cash> makeOutList(String txId, ArrayList<Cash> rawOutList1) {
		ArrayList<Cash> rawOutList = rawOutList1;
		for (int j = 0; j < rawOutList.size(); j++) {
			rawOutList.get(j).setBirthTxId(txId);
			rawOutList.get(j).setCashId(ParseTools.calcTxoId(txId, j));
		}
		return rawOutList;
	}

	private TxResult parseTx(ByteArrayInputStream blockInputStream, Block block, int i) throws IOException {

		ArrayList<byte[]> bytesList = new ArrayList<byte[]>();
		Tx tx = new Tx();

		// Read tx version/读取交易的版本
		byte[] b4Version = new byte[4];
		blockInputStream.read(b4Version);
		bytesList.add(b4Version);

		tx.setVersion(BytesTools.bytesToIntLE(b4Version));
		tx.setTxIndex(i);
		tx.setBlockTime(block.getTime());
		tx.setBlockId(block.getBlockId());
		tx.setHeight(block.getHeight());

		// Read inputs /读输入
		// ParseTxOutResult parseTxOutResult
		ParseTxInResult parseTxInResult = parseInput(blockInputStream, tx);

		bytesList.add(parseTxInResult.rawBytes);
		tx = parseTxInResult.tx;
		ArrayList<Cash> rawInList = parseTxInResult.rawInList;

		// Read outputs /读输出
		// Parse Outputs./解析输出。
		ParseTxOutResult parseTxOutResult = parseOut(blockInputStream, tx);
		bytesList.add(parseTxOutResult.rawBytes);
		tx = parseTxOutResult.tx;
		ArrayList<Cash> rawOutList = parseTxOutResult.rawOutList;

		// Read lock time.
		// 读取输出时间锁
		byte[] b4LockTime = new byte[4];
		blockInputStream.read(b4LockTime);
		bytesList.add(b4LockTime);
		tx.setLockTime(BytesTools.bytesToIntLE(b4LockTime));

		tx.setTxId(BytesTools.bytesToHexStringLE(SHA.Sha256x2(BytesTools.bytesMerger(bytesList))));
		ArrayList<Cash> inList = makeInList(tx.getTxId(), rawInList);
		ArrayList<Cash> outList = makeOutList(tx.getTxId(), rawOutList);
		
		OpReturn opReturn = new OpReturn();
		if(parseTxOutResult.opReturnStr!=null && !"".equals(parseTxOutResult.opReturnStr)) {
			opReturn.setOpReturn(parseTxOutResult.opReturnStr);
			opReturn.setTxId(tx.getTxId());
			opReturn.setTxIndex(tx.getTxIndex());
			opReturn.setHeight(tx.getHeight());
		}
		TxResult txResult = new TxResult();
		txResult.tx = tx;
		if(parseTxOutResult.opReturnStr!=null && !"".equals(parseTxOutResult.opReturnStr)) 
			txResult.opReturn = opReturn;
		txResult.inList = inList;
		txResult.outList = outList;

		return txResult;
	}

	private ParseTxInResult parseInput(ByteArrayInputStream blockInputStream, Tx tx1) throws IOException {
		ArrayList<byte[]> rawBytesList = new ArrayList<byte[]>(); // For returning raw bytes
		ArrayList<Cash> rawInList = new ArrayList<Cash>();// For returning inputs without

		Tx tx = tx1;
		// Get input count./获得输入数量
		VarintResult varintParseResult = new VarintResult();
		varintParseResult = ParseTools.parseVarint(blockInputStream);
		long inputCount = varintParseResult.number;
		tx.setInCount((int) inputCount);

		byte[] bvVarint = varintParseResult.rawBytes;
		rawBytesList.add(bvVarint);

		// Read inputs /读输入
		for (int j = 0; j < inputCount; j++) {
			Cash input = new Cash();

			input.setSpendTime(tx.getBlockTime());
			input.setSpendTxIndex(tx.getTxIndex());
			input.setBirthBlockId(tx.getBlockId());
			input.setSpendHeight(tx.getHeight());
			input.setSpendIndex(j);
			input.setValid(false);

			// Read preTXHash and preOutIndex./读取前交易哈希和输出索引。
			byte[] b36PreTxIdAndIndex = new byte[32 + 4];
			blockInputStream.read(b36PreTxIdAndIndex);
			rawBytesList.add(b36PreTxIdAndIndex);

			input.setCashId(ParseTools.calcTxoIdFromBytes(b36PreTxIdAndIndex));

			// Read the length of script./读脚本长度。
			varintParseResult = ParseTools.parseVarint(blockInputStream);
			long scriptLength = varintParseResult.number;
			byte[] bvVarint1 = varintParseResult.rawBytes;
			rawBytesList.add(bvVarint1);

			// Get script./获取脚本。
			byte[] bvScript = new byte[(int) scriptLength];
			blockInputStream.read(bvScript);
			rawBytesList.add(bvScript);
			input.setUnlockScript(BytesTools.bytesToHexStringBE(bvScript));

			// Parse sigHash.
			// 解析sigHash。
			int sigLen = Byte.toUnsignedInt(bvScript[0]);// Length of signature;
			// Skip signature/跳过签名。
			byte sigHash;
			if(sigLen!=0){								// No multiSig
				sigHash = bvScript[sigLen];				// 签名类型标志
				setInputSigHash(input,sigHash);
			}

			// Get sequence./获取sequence。
			byte[] b4Sequence = new byte[4];
			blockInputStream.read(b4Sequence);
			rawBytesList.add(b4Sequence);
			input.setSequence(BytesTools.bytesToHexStringBE(b4Sequence));

			rawInList.add(input);
		}
		byte[] rawBytes = BytesTools.bytesMerger(rawBytesList);

		ParseTxInResult parseTxInResult = new ParseTxInResult();

		parseTxInResult.rawBytes = rawBytes;
		parseTxInResult.rawInList = rawInList;
		parseTxInResult.tx = tx;

		return parseTxInResult;
	}

	private void setInputSigHash(Cash input, byte sigHash) {
		switch (sigHash) {
			case 0x41:
				input.setSigHash("ALL");
				break;
			case 0x42:
				input.setSigHash("NONE");
				break;
			case 0x43:
				input.setSigHash("SINGLE");
				break;
			case (byte) 0xc1:
				input.setSigHash("ALLIANYONECANPAY");
				break;
			case (byte) 0xc2:
				input.setSigHash("NONEIANYONECANPAY");
				break;
			case (byte) 0xc3:
				input.setSigHash("SINGLEIANYONECANPAY");
				break;
			default:
				input.setSigHash(null);
		}
	}

	private class ParseTxInResult {
		Tx tx;
		ArrayList<Cash> rawInList;
		byte[] rawBytes;
	}

	private ArrayList<Cash> makeInList(String txId, ArrayList<Cash> rawInList) {
		
		ArrayList<Cash> inList = rawInList;
		for (Cash in : inList) {
			in.setSpendTxId(txId);
		}
		return inList;
	}
}
