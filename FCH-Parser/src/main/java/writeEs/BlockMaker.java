package writeEs;

import fchClass.*;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import keyTools.KeyTools;
import fcTools.ParseTools;
import parser.ReadyBlock;
import fcTools.WeightMethod;
import esTools.EsTools;
import esTools.EsTools.MgetResult;

import java.util.*;

import static constants.IndicesNames.ADDRESS;
import static constants.IndicesNames.CASH;

public class BlockMaker {

	public ReadyBlock makeReadyBlock(ElasticsearchClient esClient, ReadyBlock rawBlock) throws Exception {
		//TODO
		//System.out.println("[makeReadyBloc]Address 0 in rawBlock: "+ rawBlock.getOutList().get(0).getAddr());

		if (rawBlock.getInList() == null || rawBlock.getInList().isEmpty()) {

			ReadyBlock txTxHasOpMadeBlock = makeTxTxHasOpReturn(rawBlock);
			ReadyBlock blockBlockHasMadeBlock = makeBlockBlockHas(txTxHasOpMadeBlock);
			ReadyBlock addrMadeBlock = makeAddress(esClient, blockBlockHasMadeBlock);
			addrMadeBlock.setOutWriteList(addrMadeBlock.getOutList());
			return addrMadeBlock;

		} else {
			ReadyBlock inputMadeBlock = makeInputList(esClient, rawBlock);
			inputMadeBlock.setOutList(rawBlock.getOutList());
			ReadyBlock txTxHasOpMadeBlock = makeTxTxHasOpReturn(inputMadeBlock);
			ReadyBlock blockBlockHasMadeBlock = makeBlockBlockHas(txTxHasOpMadeBlock);

			return makeAddress(esClient, blockBlockHasMadeBlock);
		}

	}

	private ReadyBlock makeInputList(ElasticsearchClient esClient, ReadyBlock rawBlock) throws Exception {

		ReadyBlock inListMadeBlock = rawBlock;

		ArrayList<Cash> inList = inListMadeBlock.getInList();
		Map<String, Cash> inMap = new HashMap<String, Cash>();
		List<String> inStrList = new ArrayList<String>();
		for (Cash in : inList) {
			inMap.put(in.getCashId(), in);
			inStrList.add(in.getCashId());
		}

		ArrayList<Cash> outList = inListMadeBlock.getOutList();
		Map<String, Cash> outMap = new HashMap<String, Cash>();
		for (Cash out : outList) {
			outMap.put(out.getCashId(), out);
		}

		MgetResult<Cash> inMgetResult = EsTools.getMultiByIdList(esClient, CASH, inStrList, Cash.class);
		ArrayList<Cash> inOldList = (ArrayList<Cash>) inMgetResult.getResultList();
		List<String> inNewIdList = inMgetResult.getMissList();

		ArrayList<Cash> inMadeList = new ArrayList<Cash>();
		ArrayList<Cash> outWriteList = new ArrayList<Cash>();

		for (Cash out : inOldList) {
			Cash in = inMap.get(out.getCashId());
			in.setIssuer(out.getIssuer());
			in.setOwner(out.getOwner());
			in.setBirthIndex(out.getBirthIndex());
			in.setType(out.getType());
			in.setValue(out.getValue());
			in.setLockScript(out.getLockScript());
			in.setBirthTxId(out.getBirthTxId());
			in.setBirthTxIndex(out.getBirthTxIndex());
			in.setBirthTime(out.getBirthTime());
			in.setBirthHeight(out.getBirthHeight());
			in.setCdd(ParseTools.cdd(in.getValue(), in.getBirthTime(), in.getSpendTime()));

			inMadeList.add(in);
		}

		for (String id : inNewIdList) {
			Cash in = inMap.get(id);
			Cash out = outMap.get(id);
			in.setIssuer(out.getIssuer());
			in.setOwner(out.getOwner());
			in.setBirthIndex(out.getBirthIndex());
			in.setType(out.getType());
			in.setValue(out.getValue());
			in.setLockScript(out.getLockScript());
			in.setBirthTxId(out.getBirthTxId());
			in.setBirthTxIndex(out.getBirthTxIndex());
			in.setBirthTime(out.getBirthTime());
			in.setBirthHeight(out.getBirthHeight());
			in.setCdd(ParseTools.cdd(in.getValue(), in.getBirthTime(), in.getSpendTime()));
			outMap.remove(id);
			inMadeList.add(in);
		}

		Set<String> idSet = outMap.keySet();
		for (String id : idSet) {
			outWriteList.add(outMap.get(id));
		}

		inListMadeBlock.setInList(inMadeList);
		inListMadeBlock.setOutWriteList(outWriteList);
		inListMadeBlock.setOutList(outList);
		return inListMadeBlock;
	}

	private ReadyBlock makeTxTxHasOpReturn(ReadyBlock blockForMaking) {

		ArrayList<Cash> outList = blockForMaking.getOutList();
		ArrayList<Tx> txList = blockForMaking.getTxList();
		ArrayList<OpReturn> opList = blockForMaking.getOpReturnList();

		Map<String, Tx> txMap = new HashMap<String, Tx>();
		Map<String, TxHas> txHasMap = new HashMap<String, TxHas>();

		for (Tx tx : txList) {
			txMap.put(tx.getTxId(), tx);

			TxHas txHas = new TxHas();
			ArrayList<CashMark> inMarks = new ArrayList<CashMark>();
			ArrayList<CashMark> outMarks = new ArrayList<CashMark>();
			txHas.setTxId(tx.getTxId());
			txHas.setRawTx(tx.getRawTx());
			txHas.setHeight(tx.getHeight());
			txHas.setInMarks(inMarks);
			txHas.setOutMarks(outMarks);
			txHasMap.put(tx.getTxId(), txHas);
		}

		if (blockForMaking.getInList() != null)
			for (Cash in : blockForMaking.getInList()) {
				long value = in.getValue();
				long cdd = in.getCdd();

				Tx tx = txMap.get(in.getSpendTxId());
				tx.setInValueT(tx.getInValueT() + value);
				tx.setCdd(tx.getCdd() + cdd);

				TxHas txHas = txHasMap.get(in.getSpendTxId());
				CashMark inMark = new CashMark();
				inMark.setCashId(in.getCashId());
				inMark.setOwner(in.getOwner());
				inMark.setValue(in.getValue());
				inMark.setCdd(in.getCdd());

				txHas.getInMarks().add(inMark);
			}

		for (Cash out : outList) {
			long value = out.getValue();

			Tx tx = txMap.get(out.getBirthTxId());
			tx.setOutValueT(tx.getOutValueT() + value);

			TxHas txHas = txHasMap.get(out.getBirthTxId());
			CashMark outMark = new CashMark();
			outMark.setCashId(out.getCashId());
			outMark.setOwner(out.getOwner());
			outMark.setValue(out.getValue());

			txHas.getOutMarks().add(outMark);

			if(txHas.getInMarks().size()>0){
				out.setIssuer(txHas.getInMarks().get(0).getOwner());
			}else {
				out.setIssuer("coinbase");
			}
		}

		if (opList != null && !opList.isEmpty()) {

			//TODO
			Iterator<OpReturn> iterOp = opList.iterator();
			OpReturn op = new OpReturn();
			while(iterOp.hasNext()) {
				op = iterOp.next();
				if("".equals(op.getOpReturn()))iterOp.remove();
			}

			for (OpReturn opReturn : opList) {
				String txId = opReturn.getTxId();

				Tx tx = txMap.get(txId);
				opReturn.setCdd(tx.getCdd());
				opReturn.setTime(tx.getBlockTime());

				TxHas txhas = txHasMap.get(txId);
				String signer = txhas.getInMarks().get(0).getOwner();
				opReturn.setSigner(signer);

				for (CashMark txoB : txhas.getOutMarks()) {
					String addr = txoB.getOwner();
					if (!addr.equals(signer) && !addr.equals("unknown") && !addr.equals("OpReturn")) {
						opReturn.setRecipient(addr);
						break;
					}
				}
				if (opReturn.getRecipient() == null)
					opReturn.setRecipient("nobody");
			}
		}

		Iterator<Tx> iterTx = txList.iterator();
		ArrayList<Tx> txGoodList = new ArrayList<Tx>();
		while (iterTx.hasNext()) {
			Tx tx = txMap.get(iterTx.next().getTxId());
			if (tx.getInCount() != 0)
				tx.setFee(tx.getInValueT() - tx.getOutValueT());
			txGoodList.add(tx);
		}

		ArrayList<TxHas> txHasGoodList = new ArrayList<TxHas>();
		Iterator<Tx> itertxhas = txList.iterator();
		while (itertxhas.hasNext()) {
			txHasGoodList.add(txHasMap.get(itertxhas.next().getTxId()));
		}

		ReadyBlock txAndTxHasMadeBlock = blockForMaking;

		txAndTxHasMadeBlock.setTxList(txGoodList);
		txAndTxHasMadeBlock.setTxHasList(txHasGoodList);
		txAndTxHasMadeBlock.setOpReturnList(opList);

		return txAndTxHasMadeBlock;
	}

	private ReadyBlock makeBlockBlockHas(ReadyBlock txAndTxHasMadeBlock) {

		ArrayList<Tx> txList = txAndTxHasMadeBlock.getTxList();
		Block block = txAndTxHasMadeBlock.getBlock();
		BlockHas blockHas = new BlockHas();
		blockHas.setBlockId(block.getBlockId());
		blockHas.setHeight(block.getHeight());
		blockHas.setTxMarks(new ArrayList<TxMark>());

		for (Tx tx : txList) {
			block.setInValueT(block.getInValueT() + tx.getInValueT());
			block.setOutValueT(block.getOutValueT() + tx.getOutValueT());
			block.setFee(block.getFee() + tx.getFee());
			block.setCdd(block.getCdd() + tx.getCdd());

			TxMark txMark = new TxMark();
			txMark.setTxId(tx.getTxId());
			txMark.setOutValue(tx.getOutValueT());

			if (tx.getInCount() != 0) {
				long fee = tx.getFee();
				txMark.setFee(fee);
				txMark.setCdd(tx.getCdd());
			}
			blockHas.getTxMarks().add(txMark);
		}

		ReadyBlock blockMadeBlock = txAndTxHasMadeBlock;

		blockMadeBlock.setBlock(block);
		blockMadeBlock.setBlockHas(blockHas);

		return blockMadeBlock;
	}

	private ReadyBlock makeAddress(ElasticsearchClient esClient, ReadyBlock readyBlock) throws Exception {

		List<String> addrStrList = getAddrStrList(readyBlock);

		ArrayList<Address> addrList = readAddrListFromEs(esClient, addrStrList);

		Map<String, Address> addrMap = new HashMap<String, Address>();

		for (Address addr : addrList) {
			addrMap.put(addr.getFid(), addr);
		}

		ArrayList<TxHas> txHasList = readyBlock.getTxHasList();

		for (TxHas txHas : txHasList) {

			if (txHas.getInMarks() != null && !txHas.getInMarks().isEmpty()) {
				for (CashMark inb : txHas.getInMarks()) {
					String inAddr = inb.getOwner();
					long inValue = inb.getValue();
					long cdd = inb.getCdd();

					Address addr = addrMap.get(inAddr);
					addr.setExpend(addr.getExpend() + inValue);
					addr.setBalance(addr.getBalance() - inValue);
					addr.setCdd(addr.getCdd() + cdd);
					addr.setWeight((long) (addr.getWeight()+(cdd* WeightMethod.cddPercentInWeight)/100));
					addr.setLastHeight(txHas.getHeight());
					addr.setCash(addr.getCash() - 1);

					if (addr.getPubKey() == null) {
						ArrayList<Cash> inList = readyBlock.getInList();
						Iterator<Cash> iter = inList.iterator();
						while (iter.hasNext()) {
							Cash in = iter.next();
							if (in.getOwner().equals(addr.getFid()) && in.getType().equals("P2PKH")) {
								setPKAndMoreAddrs(addr, in.getUnlockScript());
								break;
							}
							if (in.getOwner().equals(addr.getFid()) && in.getType().equals("P2SH")) {
								P2SH p2sh = new P2SH();
								p2sh.parseP2SH(esClient,in);
								break;
							}
						}
					}
				}
			}
			for (CashMark outb : txHas.getOutMarks()) {
				String outAddr = outb.getOwner();
				long outValue = outb.getValue();
				Address addr = addrMap.get(outAddr);
				addr.setIncome(addr.getIncome() + outValue);
				addr.setBalance(addr.getBalance() + outValue);
				addr.setLastHeight(txHas.getHeight());
				addr.setCash(addr.getCash() + 1);

				if (addr.getBirthHeight() == 0 && (!addr.getFid().equals("FTqiqAyXHnK7uDTXzMap3acvqADK4ZGzts")))
					addr.setBirthHeight(txHas.getHeight());

				if (addr.getGuide() == null) {
					if (txHas.getInMarks() != null && !txHas.getInMarks().isEmpty()) {
						addr.setGuide(txHas.getInMarks().get(0).getOwner());
					} else
						addr.setGuide("coinbase");
				}
			}
		}

		Collection<Address> addrs = addrMap.values();
		ArrayList<Address> readyAddrList = new ArrayList<Address>();

		Iterator<Address> iter = addrs.iterator();
		while (iter.hasNext()) {
			readyAddrList.add(iter.next());
		}

		ReadyBlock addrMadeBlock = readyBlock;
		addrMadeBlock.setAddrList(readyAddrList);

		return addrMadeBlock;
	}

	private List<String> getAddrStrList(ReadyBlock readyBlock) {

		ArrayList<Cash> inList = readyBlock.getInList();
		ArrayList<Cash> outList = readyBlock.getOutList();
		Set<String> addrStrSet = new HashSet<String>();

		for (Cash in : inList)
			addrStrSet.add(in.getOwner());
		for (Cash out : outList)
			addrStrSet.add(out.getOwner());

		List<String> addrStrList = new ArrayList<String>(addrStrSet);

		return addrStrList;
	}

	private ArrayList<Address> readAddrListFromEs(ElasticsearchClient esClient, List<String> addrStrList)
			throws Exception {
		MgetResult<Address> addrMgetResult = EsTools.getMultiByIdList(esClient, ADDRESS, addrStrList,
				Address.class);

		ArrayList<Address> addrOldList = (ArrayList<Address>) addrMgetResult.getResultList();

		List<String> addrNewStrList = addrMgetResult.getMissList();
		ArrayList<Address> addrNewList = new ArrayList<Address>();

		for (String addrStr : addrNewStrList) {
			Address addr = new Address();
			addr.setFid(addrStr);
			addrNewList.add(addr);
		}

		ArrayList<Address> addrList = new ArrayList<Address>();
		addrList.addAll(addrOldList);
		addrList.addAll(addrNewList);
		return addrList;
	}

	private void setPKAndMoreAddrs(Address addr, String unLockScript) {

		String pk = KeyTools.parsePkFromUnlockScript(unLockScript);

		addr.setPubKey(pk);
		addr.setBtcAddr(KeyTools.pubKeyToBtcAddr(pk));
		addr.setEthAddr(KeyTools.pubKeyToEthAddr(pk));
		addr.setLtcAddr(KeyTools.pubKeyToLtcAddr(pk));
		addr.setDogeAddr(KeyTools.pubKeyToDogeAddr(pk));
		addr.setTrxAddr(KeyTools.pubKeyToTrxAddr(pk));
	}
}
