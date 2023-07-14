package parseBlocks;

import java.util.ArrayList;

import fchClass.*;

public class ReadyBlock {
	
	private BlockMark blockMark;
	private Block block;
	private BlockHas blockHas;
	private ArrayList<Tx> txList;
	private ArrayList<TxHas> txHasList;
	private ArrayList<Cash> inList;
	private ArrayList<Cash> outList;
	private ArrayList<OpReturn> opReturnList;
	
	public BlockMark getBlockMark() {
		return blockMark;
	}
	public void setBlockMark(BlockMark blockMark) {
		this.blockMark = blockMark;
	}
	public Block getBlock() {
		return block;
	}
	public void setBlock(Block block) {
		this.block = block;
	}
	public BlockHas getBlockHas() {
		return blockHas;
	}
	public void setBlockHas(BlockHas blockHas) {
		this.blockHas = blockHas;
	}
	public ArrayList<Tx> getTxList() {
		return txList;
	}
	public void setTxList(ArrayList<Tx> txList) {
		this.txList = txList;
	}
	public ArrayList<TxHas> getTxHasList() {
		return txHasList;
	}
	public void setTxHasList(ArrayList<TxHas> txHasList) {
		this.txHasList = txHasList;
	}
	public ArrayList<Cash> getInList() {
		return inList;
	}
	public void setInList(ArrayList<Cash> inList) {
		this.inList = inList;
	}
	public ArrayList<Cash> getOutList() {
		return outList;
	}
	public void setOutList(ArrayList<Cash> outList) {
		this.outList = outList;
	}
	public ArrayList<OpReturn> getOpReturnList() {
		return opReturnList;
	}
	public void setOpReturnList(ArrayList<OpReturn> opReturnList) {
		this.opReturnList = opReturnList;
	}

	
}
