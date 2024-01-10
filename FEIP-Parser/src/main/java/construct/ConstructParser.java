package construct;

import constants.IndicesNames;
import feipClass.*;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.google.gson.Gson;
import fchClass.OpReturn;
import esTools.EsTools;
import javaTools.JsonTools;
import startFEIP.StartFEIP;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ConstructParser {

	public ProtocolHistory makeProtocol(OpReturn opre, FcInfo feip) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();

		ProtocolData protocolRaw = new ProtocolData();
		try {
		protocolRaw = gson.fromJson(gson.toJson(feip.getData()), ProtocolData.class);
		}catch(com.google.gson.JsonSyntaxException e) {
			e.printStackTrace();
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			return null;
		}
		
		ProtocolHistory protocolHist = new ProtocolHistory();
		
		if(protocolRaw.getOp()==null)return null;
		
		protocolHist.setOp(protocolRaw.getOp());
		
		switch(protocolRaw.getOp()) {

		case "publish":
			if(protocolRaw.getSn()==null|| protocolRaw.getName()==null||"".equals(protocolRaw.getName()))	return null;
            if (opre.getHeight() > StartFEIP.CddCheckHeight && opre.getCdd() < StartFEIP.CddRequired * 100) return null;
			protocolHist.setTxId(opre.getTxId());
			
			protocolHist.setPid(opre.getTxId());
			protocolHist.setHeight(opre.getHeight());
			protocolHist.setIndex(opre.getTxIndex());
			protocolHist.setTime(opre.getTime());
			protocolHist.setSigner(opre.getSigner());
			
			if(protocolRaw.getType()!=null)protocolHist.setType(protocolRaw.getType());
			if(protocolRaw.getSn()!=null)protocolHist.setSn(protocolRaw.getSn());
			if(protocolRaw.getVer()!=null)protocolHist.setVer(protocolRaw.getVer());
			if(protocolRaw.getDid()!=null)protocolHist.setDid(protocolRaw.getDid());
			if(protocolRaw.getName()!=null)protocolHist.setName(protocolRaw.getName());
			if(protocolRaw.getDesc()!=null)protocolHist.setDesc(protocolRaw.getDesc());
			if(protocolRaw.getLang()!=null)protocolHist.setLang(protocolRaw.getLang());
			if(protocolRaw.getFileUrls()!=null)protocolHist.setFileUrls(protocolRaw.getFileUrls());
			if(protocolRaw.getPreDid()!=null)protocolHist.setPrePid(protocolRaw.getPreDid());
			if(protocolRaw.getWaiters()!=null)protocolHist.setWaiters(protocolRaw.getWaiters());

			break;	
			
		case "update":
			
			if(protocolRaw.getPid()==null|| protocolRaw.getSn()==null|| protocolRaw.getName()==null||"".equals(protocolRaw.getName()))	return null;
			protocolHist.setTxId(opre.getTxId());
			protocolHist.setHeight(opre.getHeight());
			protocolHist.setIndex(opre.getTxIndex());
			protocolHist.setTime(opre.getTime());
			protocolHist.setSigner(opre.getSigner());
			
			protocolHist.setPid(protocolRaw.getPid());

			if(protocolRaw.getType()!=null)protocolHist.setType(protocolRaw.getType());
			if(protocolRaw.getSn()!=null)protocolHist.setSn(protocolRaw.getSn());
			if(protocolRaw.getVer()!=null)protocolHist.setVer(protocolRaw.getVer());
			if(protocolRaw.getDid()!=null)protocolHist.setDid(protocolRaw.getDid());
			if(protocolRaw.getName()!=null)protocolHist.setName(protocolRaw.getName());
			if(protocolRaw.getDesc()!=null)protocolHist.setDesc(protocolRaw.getDesc());
			if(protocolRaw.getLang()!=null)protocolHist.setLang(protocolRaw.getLang());
			if(protocolRaw.getFileUrls()!=null)protocolHist.setFileUrls(protocolRaw.getFileUrls());
			if(protocolRaw.getPreDid()!=null)protocolHist.setPrePid(protocolRaw.getPreDid());
			if(protocolRaw.getWaiters()!=null)protocolHist.setWaiters(protocolRaw.getWaiters());
			
			break;	
		case "stop":
			case "recover":
				if(protocolRaw.getPid()==null)return null;
			protocolHist.setPid(protocolRaw.getPid());

			protocolHist.setTxId(opre.getTxId());
			protocolHist.setHeight(opre.getHeight());
			protocolHist.setIndex(opre.getTxIndex());
			protocolHist.setTime(opre.getTime());
			protocolHist.setSigner(opre.getSigner());
			break;

			case "rate":
			if(protocolRaw.getPid()==null)return null;
			if (opre.getCdd() < StartFEIP.CddRequired) return null;
			protocolHist.setPid(protocolRaw.getPid());
			protocolHist.setRate(protocolRaw.getRate());
			protocolHist.setCdd(opre.getCdd());
			
			protocolHist.setTxId(opre.getTxId());
			protocolHist.setHeight(opre.getHeight());
			protocolHist.setIndex(opre.getTxIndex());
			protocolHist.setTime(opre.getTime());
			protocolHist.setSigner(opre.getSigner());
			break;
		case "close":
			if(protocolRaw.getPid()==null)return null;
			protocolHist.setPid(protocolRaw.getPid());
			if(protocolHist.getCloseStatement()!=null)protocolHist.setCloseStatement(null);
			
			protocolHist.setTxId(opre.getTxId());
			protocolHist.setHeight(opre.getHeight());
			protocolHist.setIndex(opre.getTxIndex());
			protocolHist.setTime(opre.getTime());
			protocolHist.setSigner(opre.getSigner());
			break;
		default:
			return null;
		}
		return protocolHist;
	}

	public ServiceHistory makeService(OpReturn opre, FcInfo feip) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		ServiceData serviceRaw = new ServiceData();
		
		try {
			serviceRaw = gson.fromJson(gson.toJson(feip.getData()), ServiceData.class);
		}catch(com.google.gson.JsonSyntaxException e) {
			return null;
		}
		
		ServiceHistory serviceHist = new ServiceHistory();
		
		if(serviceRaw.getOp()==null)return null;
		
		serviceHist.setOp(serviceRaw.getOp());

		switch(serviceRaw.getOp()) {
		case "publish":
			if(serviceRaw.getStdName()==null||"".equals(serviceRaw.getStdName()))return null;
			if(serviceRaw.getSid()!=null) return null;
            if (opre.getHeight() > StartFEIP.CddCheckHeight && opre.getCdd() < StartFEIP.CddRequired * 100) return null;
			serviceHist.setTxId(opre.getTxId());
			serviceHist.setSid(opre.getTxId());
			serviceHist.setHeight(opre.getHeight());
			serviceHist.setIndex(opre.getTxIndex());
			serviceHist.setTime(opre.getTime());
			serviceHist.setSigner(opre.getSigner());

			if(serviceRaw.getStdName()!=null)serviceHist.setStdName(serviceRaw.getStdName());
			if(serviceRaw.getLocalNames()!=null)serviceHist.setLocalNames(serviceRaw.getLocalNames());
			if(serviceRaw.getDesc()!=null)serviceHist.setDesc(serviceRaw.getDesc());
			if(serviceRaw.getTypes()!=null)serviceHist.setTypes(serviceRaw.getTypes());
			if(serviceRaw.getUrls()!=null)serviceHist.setUrls(serviceRaw.getUrls());
			if(serviceRaw.getWaiters()!=null)serviceHist.setWaiters(serviceRaw.getWaiters());
			if(serviceRaw.getProtocols()!=null)serviceHist.setProtocols(serviceRaw.getProtocols());
			if(serviceRaw.getParams()!=null) {
				serviceHist.setParams(serviceRaw.getParams());
			}
			break;
		case "update":
			if(serviceRaw.getSid()==null)	return null;
			if(serviceRaw.getStdName()==null||"".equals(serviceRaw.getStdName()))	return null;
			
			serviceHist.setTxId(opre.getTxId());
			serviceHist.setSid(serviceRaw.getSid());
			serviceHist.setHeight(opre.getHeight());
			serviceHist.setIndex(opre.getTxIndex());
			serviceHist.setTime(opre.getTime());
			serviceHist.setSigner(opre.getSigner());

			if(serviceRaw.getStdName()!=null)serviceHist.setStdName(serviceRaw.getStdName());
			if(serviceRaw.getLocalNames()!=null)serviceHist.setLocalNames(serviceRaw.getLocalNames());
			if(serviceRaw.getDesc()!=null)serviceHist.setDesc(serviceRaw.getDesc());
			if(serviceRaw.getTypes()!=null)serviceHist.setTypes(serviceRaw.getTypes());
			if(serviceRaw.getUrls()!=null)serviceHist.setUrls(serviceRaw.getUrls());
			if(serviceRaw.getWaiters()!=null)serviceHist.setWaiters(serviceRaw.getWaiters());
			if(serviceRaw.getProtocols()!=null)serviceHist.setProtocols(serviceRaw.getProtocols());
			if(serviceRaw.getParams()!=null) {
				serviceHist.setParams(serviceRaw.getParams());
			}
			break;	
		case "stop", "recover":
			if(serviceRaw.getSid()==null)return null;
			serviceHist.setSid(serviceRaw.getSid());
			serviceHist.setTxId(opre.getTxId());
			serviceHist.setHeight(opre.getHeight());
			serviceHist.setIndex(opre.getTxIndex());
			serviceHist.setTime(opre.getTime());
			serviceHist.setSigner(opre.getSigner());
			break;
			case "close":
			if(serviceRaw.getSid()==null)return null;
			serviceHist.setSid(serviceRaw.getSid());
			if(serviceHist.getCloseStatement()!=null)serviceHist.setCloseStatement(null);
			
			serviceHist.setTxId(opre.getTxId());
			serviceHist.setHeight(opre.getHeight());
			serviceHist.setIndex(opre.getTxIndex());
			serviceHist.setTime(opre.getTime());
			serviceHist.setSigner(opre.getSigner());
			break;
		case "rate":
			if(serviceRaw.getSid()==null)return null;
			if(serviceRaw.getRate()<0 ||serviceRaw.getRate()>5)return null;
            if (opre.getCdd() < StartFEIP.CddRequired) return null;
			serviceHist.setSid(serviceRaw.getSid());
			serviceHist.setTxId(opre.getTxId());
			serviceHist.setHeight(opre.getHeight());
			serviceHist.setIndex(opre.getTxIndex());
			serviceHist.setTime(opre.getTime());
			serviceHist.setSigner(opre.getSigner());
			serviceHist.setRate(serviceRaw.getRate());
			serviceHist.setCdd(opre.getCdd());
			break;
		default:
			return null;
		}
		return serviceHist; 
	}
	public AppHistory makeApp(OpReturn opre, FcInfo feip) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		
		AppData appRaw = new AppData();
		
		try {
			appRaw = gson.fromJson(gson.toJson(feip.getData()), AppData.class);
		}catch(com.google.gson.JsonSyntaxException e) {
			return null;
		}
		
		AppHistory appHist = new AppHistory();
		
		if(appRaw.getOp()==null)return null;
		appHist.setOp(appRaw.getOp());

		switch(appRaw.getOp()) {
		
		case "publish":
			if(appRaw.getStdName()==null||"".equals(appRaw.getStdName()))	return null;
			if(appRaw.getAid()!=null) return null;
            if (opre.getHeight() > StartFEIP.CddCheckHeight && opre.getCdd() < StartFEIP.CddRequired * 100) return null;
			appHist.setTxId(opre.getTxId());
			appHist.setAid(opre.getTxId());
			appHist.setHeight(opre.getHeight());
			appHist.setIndex(opre.getTxIndex());
			appHist.setTime(opre.getTime());
			appHist.setSigner(opre.getSigner());

			if(appRaw.getStdName()!=null)appHist.setStdName(appRaw.getStdName());
			if(appRaw.getLocalNames()!=null)appHist.setLocalNames(appRaw.getLocalNames());
			if(appRaw.getDesc()!=null)appHist.setDesc(appRaw.getDesc());
			if(appRaw.getTypes()!=null)appHist.setTypes(appRaw.getTypes());
			if(appRaw.getUrls()!=null)appHist.setUrls(appRaw.getUrls());
			if(appRaw.getDownloads()!=null)appHist.setDownloads(appRaw.getDownloads());
			if(appRaw.getWaiters()!=null)appHist.setWaiters(appRaw.getWaiters());
			if(appRaw.getProtocols()!=null)appHist.setProtocols(appRaw.getProtocols());
			if(appRaw.getServices() !=null)appHist.setServices(appRaw.getServices());

			break;	
			
		case "update":
			if(appRaw.getAid()==null)	return null;
			if(appRaw.getStdName()==null||"".equals(appRaw.getStdName()))	return null;
			
			appHist.setAid(appRaw.getAid());
			appHist.setTxId(opre.getTxId());
			appHist.setHeight(opre.getHeight());
			appHist.setIndex(opre.getTxIndex());
			appHist.setTime(opre.getTime());
			appHist.setSigner(opre.getSigner());

			if(appRaw.getStdName()!=null)appHist.setStdName(appRaw.getStdName());
			if(appRaw.getLocalNames()!=null)appHist.setLocalNames(appRaw.getLocalNames());
			if(appRaw.getDesc()!=null)appHist.setDesc(appRaw.getDesc());
			if(appRaw.getTypes()!=null)appHist.setTypes(appRaw.getTypes());
			if(appRaw.getUrls()!=null)appHist.setUrls(appRaw.getUrls());
			if(appRaw.getDownloads()!=null)appHist.setDownloads(appRaw.getDownloads());
			if(appRaw.getWaiters()!=null)appHist.setWaiters(appRaw.getWaiters());
			if(appRaw.getProtocols()!=null)appHist.setProtocols(appRaw.getProtocols());
			if(appRaw.getServices() !=null)appHist.setServices(appRaw.getServices());

			break;	
			
		case "stop":
			if(appRaw.getAid()==null)return null;
			
			appHist.setAid(appRaw.getAid());
			
			appHist.setTxId(opre.getTxId());
			appHist.setHeight(opre.getHeight());
			appHist.setIndex(opre.getTxIndex());
			appHist.setTime(opre.getTime());
			appHist.setSigner(opre.getSigner());
			break;
		case "recover":
			if(appRaw.getAid()==null)return null;
			
			appHist.setAid(appRaw.getAid());
			
			appHist.setTxId(opre.getTxId());
			appHist.setHeight(opre.getHeight());
			appHist.setIndex(opre.getTxIndex());
			appHist.setTime(opre.getTime());
			appHist.setSigner(opre.getSigner());
			break;
		case "close":
			if(appRaw.getAid()==null)return null;
			appHist.setAid(appRaw.getAid());
			if(appHist.getCloseStatement()!=null)appHist.setCloseStatement(null);
			
			appHist.setTxId(opre.getTxId());
			appHist.setHeight(opre.getHeight());
			appHist.setIndex(opre.getTxIndex());
			appHist.setTime(opre.getTime());
			appHist.setSigner(opre.getSigner());
			break;
		case "rate":
			if(appRaw.getAid()==null)return null;
			if(appRaw.getRate()<0 ||appRaw.getRate()>5)return null;
            if (opre.getCdd() < StartFEIP.CddRequired) return null;
			appHist.setAid(appRaw.getAid());
			appHist.setRate(appRaw.getRate());
			appHist.setCdd(opre.getCdd());
			
			appHist.setTxId(opre.getTxId());
			appHist.setHeight(opre.getHeight());
			appHist.setIndex(opre.getTxIndex());
			appHist.setTime(opre.getTime());
			appHist.setSigner(opre.getSigner());
			break;
		default:
			return null;
		}
		return appHist; 
	}
	public CodeHistory makeCode(OpReturn opre, FcInfo feip) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		CodeData codeRaw = new CodeData();
		
		try {
			codeRaw = gson.fromJson(gson.toJson(feip.getData()), CodeData.class);
		}catch(com.google.gson.JsonSyntaxException e) {
			return null;
		}
		
		CodeHistory codeHist = new CodeHistory();
		
		if(codeRaw.getOp()==null)return null;
		
		codeHist.setOp(codeRaw.getOp());

		switch(codeRaw.getOp()) {
		case "publish":
			if(codeRaw.getName()==null||"".equals(codeRaw.getName()))	return null;
			if(codeRaw.getCodeId()!=null) return null;
            if (opre.getHeight() > StartFEIP.CddCheckHeight && opre.getCdd() < StartFEIP.CddRequired * 100) return null;
			codeHist.setTxId(opre.getTxId());
			codeHist.setCodeId(opre.getTxId());
			codeHist.setHeight(opre.getHeight());
			codeHist.setIndex(opre.getTxIndex());
			codeHist.setTime(opre.getTime());
			codeHist.setSigner(opre.getSigner());

			if(codeRaw.getName()!=null)codeHist.setName(codeRaw.getName());
			if(codeRaw.getVersion()!=null)codeHist.setVersion(codeRaw.getVersion());
			if(codeRaw.getDid()!=null)codeHist.setDid(codeRaw.getDid());
			if(codeRaw.getDesc()!=null)codeHist.setDesc(codeRaw.getDesc());
			if(codeRaw.getUrls()!=null)codeHist.setUrls(codeRaw.getUrls());
			if(codeRaw.getLangs()!=null)codeHist.setLangs(codeRaw.getLangs());
			if(codeRaw.getProtocols()!=null)codeHist.setProtocols(codeRaw.getProtocols());
			if(codeRaw.getWaiters()!=null)codeHist.setWaiters(codeRaw.getWaiters());

			break;
		case "update":
			if(codeRaw.getCodeId()==null)	return null;
			if(codeRaw.getName()==null||"".equals(codeRaw.getName()))return null;
			
			codeHist.setTxId(opre.getTxId());
			codeHist.setCodeId(codeRaw.getCodeId());
			codeHist.setHeight(opre.getHeight());
			codeHist.setIndex(opre.getTxIndex());
			codeHist.setTime(opre.getTime());
			codeHist.setSigner(opre.getSigner());

			if(codeRaw.getName()!=null)codeHist.setName(codeRaw.getName());
			if(codeRaw.getVersion()!=null)codeHist.setVersion(codeRaw.getVersion());
			if(codeRaw.getDid()!=null)codeHist.setDid(codeRaw.getDid());
			if(codeRaw.getDesc()!=null)codeHist.setDesc(codeRaw.getDesc());
			if(codeRaw.getUrls()!=null)codeHist.setUrls(codeRaw.getUrls());
			if(codeRaw.getLangs()!=null)codeHist.setLangs(codeRaw.getLangs());
			if(codeRaw.getProtocols()!=null)codeHist.setProtocols(codeRaw.getProtocols());
			if(codeRaw.getWaiters()!=null)codeHist.setWaiters(codeRaw.getWaiters());
			break;	
		case "stop":
			if(codeRaw.getCodeId()==null)	return null;
			
			codeHist.setCodeId(codeRaw.getCodeId());
			codeHist.setTxId(opre.getTxId());
			codeHist.setHeight(opre.getHeight());
			codeHist.setIndex(opre.getTxIndex());
			codeHist.setTime(opre.getTime());
			codeHist.setSigner(opre.getSigner());
			break;
		case "recover":
			if(codeRaw.getCodeId()==null)	return null;
			
			codeHist.setCodeId(codeRaw.getCodeId());
			codeHist.setTxId(opre.getTxId());
			codeHist.setHeight(opre.getHeight());
			codeHist.setIndex(opre.getTxIndex());
			codeHist.setTime(opre.getTime());
			codeHist.setSigner(opre.getSigner());

			break;
		case "close":
			if(codeRaw.getCodeId()==null)return null;
			codeHist.setCodeId(codeRaw.getCodeId());
			if(codeHist.getCloseStatement()!=null)codeHist.setCloseStatement(null);
			codeHist.setTxId(opre.getTxId());
			codeHist.setHeight(opre.getHeight());
			codeHist.setIndex(opre.getTxIndex());
			codeHist.setTime(opre.getTime());
			codeHist.setSigner(opre.getSigner());
			break;
		case "rate":
			if(codeRaw.getCodeId()==null)	return null;
			if(codeRaw.getRate()<0 ||codeRaw.getRate()>5)return null;
            if (opre.getCdd() < StartFEIP.CddRequired) return null;
			codeHist.setCodeId(codeRaw.getCodeId());
			codeHist.setTxId(opre.getTxId());
			codeHist.setHeight(opre.getHeight());
			codeHist.setIndex(opre.getTxIndex());
			codeHist.setTime(opre.getTime());
			codeHist.setSigner(opre.getSigner());
			codeHist.setRate(codeRaw.getRate());
			codeHist.setCdd(opre.getCdd());
			break;
		default:
			return null;
		}
		return codeHist; 
	}

	public boolean parseProtocol(ElasticsearchClient esClient, ProtocolHistory protocolHist) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		boolean isValid = false;
		Protocol protocol;
		switch (protocolHist.getOp()) {
			case "publish" -> {
				protocol = EsTools.getById(esClient, IndicesNames.PROTOCOL, protocolHist.getPid(), Protocol.class);
				if (protocol == null) {
					protocol = new Protocol();
					protocol.setPid(protocolHist.getPid());
					protocol.setType(protocolHist.getType());
					protocol.setSn(protocolHist.getSn());
					protocol.setVer(protocolHist.getVer());
					protocol.setDid(protocolHist.getDid());
					protocol.setName(protocolHist.getName());

					protocol.setLang(protocolHist.getLang());
					protocol.setDesc(protocolHist.getDesc());
					protocol.setPrePid(protocolHist.getPrePid());
					protocol.setFileUrls(protocolHist.getFileUrls());

					protocol.setTitle(protocolHist.getType() + protocolHist.getSn() + "V" + protocolHist.getVer() + "_" + protocolHist.getName() + "(" + protocolHist.getLang() + ")");
					protocol.setOwner(protocolHist.getSigner());

					protocol.setBirthTxId(protocolHist.getTxId());
					protocol.setBirthTime(protocolHist.getTime());
					protocol.setBirthHeight(protocolHist.getHeight());

					protocol.setLastTxId(protocolHist.getTxId());
					protocol.setLastTime(protocolHist.getTime());
					protocol.setLastHeight(protocolHist.getHeight());

					protocol.setActive(true);

					Protocol protocol1 = protocol;

					esClient.index(i -> i.index(IndicesNames.PROTOCOL).id(protocolHist.getPid()).document(protocol1));
					isValid = true;
				} else {
					isValid = false;
				}
			}
			case "stop" -> {
				protocol = EsTools.getById(esClient, IndicesNames.PROTOCOL, protocolHist.getPid(), Protocol.class);
				if (protocol == null) {
					isValid = false;
					break;
				}
				if (protocol.isClosed()) {
					isValid = false;
					break;
				}
				if (!protocol.getOwner().equals(protocolHist.getSigner())) {
					isValid = false;
					break;
				}
				if (protocol.isActive()) {
					Protocol protocol1 = protocol;
					protocol1.setActive(false);
					protocol1.setLastTxId(protocolHist.getTxId());
					protocol1.setLastTime(protocolHist.getTime());
					protocol1.setLastHeight(protocolHist.getHeight());
					esClient.index(i -> i.index(IndicesNames.PROTOCOL).id(protocolHist.getPid()).document(protocol1));
					isValid = true;
				} else isValid = false;
			}
			case "recover" -> {
				protocol = EsTools.getById(esClient, IndicesNames.PROTOCOL, protocolHist.getPid(), Protocol.class);
				if (protocol == null) {
					isValid = false;
					break;
				}
				if (protocol.isClosed()) {
					isValid = false;
					break;
				}
				if (!protocol.getOwner().equals(protocolHist.getSigner())) {
					isValid = false;
					break;
				}
				if (!protocol.isActive()) {
					Protocol protocol1 = protocol;
					protocol1.setActive(true);
					protocol1.setLastTxId(protocolHist.getTxId());
					protocol1.setLastTime(protocolHist.getTime());
					protocol1.setLastHeight(protocolHist.getHeight());
					esClient.index(i -> i.index(IndicesNames.PROTOCOL).id(protocolHist.getPid()).document(protocol1));
					isValid = true;
				} else isValid = false;
			}
			case "update" -> {
				protocol = EsTools.getById(esClient, IndicesNames.PROTOCOL, protocolHist.getPid(), Protocol.class);
				if (protocol == null) {
					isValid = false;
					break;
				}
				if (protocol.isClosed()) {
					isValid = false;
					break;
				}
				if (!protocol.getOwner().equals(protocolHist.getSigner())) {
					isValid = false;
					break;
				}
				if (!protocol.isActive()) {
					isValid = false;
					break;
				}
				protocol.setType(protocolHist.getType());
				protocol.setSn(protocolHist.getSn());
				protocol.setVer(protocolHist.getVer());
				protocol.setDid(protocolHist.getDid());
				protocol.setName(protocolHist.getName());
				protocol.setLang(protocolHist.getLang());
				protocol.setDesc(protocolHist.getDesc());
				protocol.setPrePid(protocolHist.getPrePid());
				protocol.setFileUrls(protocolHist.getFileUrls());
				protocol.setTitle(protocolHist.getType() + protocolHist.getSn() + "V" + protocolHist.getVer() + "_" + protocolHist.getName() + "(" + protocolHist.getLang() + ")");
				protocol.setLastTxId(protocolHist.getTxId());
				protocol.setLastTime(protocolHist.getTime());
				protocol.setLastHeight(protocolHist.getHeight());
				Protocol protocol2 = protocol;
				esClient.index(i -> i.index(IndicesNames.PROTOCOL).id(protocolHist.getPid()).document(protocol2));
				isValid = true;
			}
			case "close" -> {
				protocol = EsTools.getById(esClient, IndicesNames.PROTOCOL, protocolHist.getPid(), Protocol.class);
				if (protocol == null) {
					isValid = false;
					break;
				}
				if (protocol.isClosed()) {
					isValid = false;
					break;
				}
				if (!protocol.getOwner().equals(protocolHist.getSigner())) {
					Cid resultCid = EsTools.getById(esClient, IndicesNames.CID, protocolHist.getSigner(), Cid.class);
					if (resultCid.getMaster() != null) {
						if (!resultCid.getMaster().equals(protocolHist.getSigner())) {
							isValid = false;
							break;
						}
					} else {
						isValid = false;
						break;
					}
				}
				Protocol protocol1 = protocol;
				protocol1.setClosed(true);
				protocol1.setActive(false);
				protocol1.setLastTxId(protocolHist.getTxId());
				protocol1.setLastTime(protocolHist.getTime());
				protocol1.setLastHeight(protocolHist.getHeight());
				esClient.index(i -> i.index(IndicesNames.PROTOCOL).id(protocolHist.getPid()).document(protocol1));
				isValid = true;
			}
			case "rate" -> {
				protocol = EsTools.getById(esClient, IndicesNames.PROTOCOL, protocolHist.getPid(), Protocol.class);
				if (protocol == null) {
					isValid = false;
					break;
				}
				if (protocol.getOwner().equals(protocolHist.getSigner())) {
					isValid = false;
					break;
				}
				if (protocol.gettCdd() + protocolHist.getCdd() == 0) {
					protocol.settRate(0);
				} else {
					protocol.settRate(
							(protocol.gettRate() * protocol.gettCdd() + protocolHist.getRate() * protocolHist.getCdd())
									/ (protocol.gettCdd() + protocolHist.getCdd())
					);
				}
				protocol.settCdd(protocol.gettCdd() + protocolHist.getCdd());
				protocol.setLastTxId(protocolHist.getTxId());
				protocol.setLastTime(protocolHist.getTime());
				protocol.setLastHeight(protocolHist.getHeight());
				Protocol protocol3 = protocol;
				esClient.index(i -> i.index(IndicesNames.PROTOCOL).id(protocolHist.getPid()).document(protocol3));
				isValid = true;
			}
		}
		
		return isValid;
	}
	public boolean parseService(ElasticsearchClient esClient, ServiceHistory serviceHist) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		boolean isValid = false;
		Service service;
		switch(serviceHist.getOp()) {
		case "publish":
			service = EsTools.getById(esClient, IndicesNames.SERVICE, serviceHist.getSid(), Service.class);
			if(service==null) {
				service = new Service();
				service.setSid(serviceHist.getTxId());
				service.setStdName(serviceHist.getStdName());
				service.setLocalNames(serviceHist.getLocalNames());
				service.setDesc(serviceHist.getDesc());
				service.setTypes(serviceHist.getTypes());
				service.setUrls(serviceHist.getUrls());
				service.setWaiters(serviceHist.getWaiters());
				service.setProtocols(serviceHist.getProtocols());
				service.setParams(serviceHist.getParams());
				service.setOwner(serviceHist.getSigner());
	
				service.setLastTxId(serviceHist.getTxId());
				service.setLastTime(serviceHist.getTime());
				service.setLastHeight(serviceHist.getHeight());
				
				service.setBirthTime(serviceHist.getTime());
				service.setBirthHeight(serviceHist.getHeight());
				
				service.setActive(true);
				
				Service service1 = service;
				esClient.index(i->i.index(IndicesNames.SERVICE).id(serviceHist.getSid()).document(service1));
				isValid = true;
			}else {
				isValid=false;
			}
			break;
		case "stop":
			service = EsTools.getById(esClient, IndicesNames.SERVICE, serviceHist.getSid(), Service.class);
			
			if(service==null) {
				isValid = false;
				break;
			}
			
			if(service.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! service.getOwner().equals(serviceHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(service.isActive()) {
				Service service2 = service;
				service2.setActive(false);
				service2.setLastTxId(serviceHist.getTxId());
				service2.setLastTime(serviceHist.getTime());
				service2.setLastHeight(serviceHist.getHeight());
				esClient.index(i->i.index(IndicesNames.SERVICE).id(serviceHist.getSid()).document(service2));
				isValid = true;
			}else isValid = false;

			break;
			
		case "recover":
			service = EsTools.getById(esClient, IndicesNames.SERVICE, serviceHist.getSid(), Service.class);
			
			if(service==null) {
				isValid = false;
				break;
			}
			
			if(service.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! service.getOwner().equals(serviceHist.getSigner())) {
				isValid = false;
				break;
			}
			
			
			if(!service.isActive()) {
				Service service3 = service;
				service3.setActive(true);
				service3.setLastTxId(serviceHist.getTxId());
				service3.setLastTime(serviceHist.getTime());
				service3.setLastHeight(serviceHist.getHeight());
				esClient.index(i->i.index(IndicesNames.SERVICE).id(serviceHist.getSid()).document(service3));
				isValid = true;
			}else isValid = false;

			break;
			
		case "update":
			service = EsTools.getById(esClient, IndicesNames.SERVICE, serviceHist.getSid(), Service.class);
			
			if(service==null) {
				isValid = false;
				break;
			}
			
			if(service.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! (service.getOwner().equals(serviceHist.getSigner()))) {
				isValid = false;
				break;
			}

			service.setStdName(serviceHist.getStdName());
			service.setLocalNames(serviceHist.getLocalNames());
			service.setDesc(serviceHist.getDesc());
			service.setTypes(serviceHist.getTypes());
			service.setUrls(serviceHist.getUrls());
			service.setWaiters(serviceHist.getWaiters());
			service.setProtocols(serviceHist.getProtocols());
			service.setParams(serviceHist.getParams());

			service.setLastTxId(serviceHist.getTxId());
			service.setLastTime(serviceHist.getTime());
			service.setLastHeight(serviceHist.getHeight());
			
			Service service4 = service;

			esClient.index(i->i.index(IndicesNames.SERVICE).id(serviceHist.getSid()).document(service4));
			isValid = true;
			break;
			
		case "close":	
			service = EsTools.getById(esClient, IndicesNames.SERVICE, serviceHist.getSid(), Service.class);
			
			if(service==null) {
				isValid = false;
				break;
			}
			
			if(service.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! service.getOwner().equals(serviceHist.getSigner())) {
				Cid resultCid = EsTools.getById(esClient, IndicesNames.CID, serviceHist.getSigner(), Cid.class);
				if(resultCid.getMaster()!=null) {
					if(! resultCid.getMaster().equals(serviceHist.getSigner())) {
					isValid = false;
					break;
					}
				}else {
					isValid = false;
					break;
				}
			}

			
			Service service1 = service;
			service1.setClosed(true);
			service1.setActive(false);
			service1.setLastTxId(serviceHist.getTxId());
			service1.setLastTime(serviceHist.getTime());
			service1.setLastHeight(serviceHist.getHeight());
			esClient.index(i->i.index(IndicesNames.SERVICE).id(serviceHist.getSid()).document(service1));
			isValid = true;

			break;
		case "rate":
			service = EsTools.getById(esClient, IndicesNames.SERVICE, serviceHist.getSid(), Service.class);

			if(service==null) {
				isValid = false;
				break;
			}

			if(service.getOwner().equals(serviceHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(service.gettCdd()+serviceHist.getCdd()==0) {
				service.settRate(0);
			}else {
				service.settRate(
						(service.gettRate()*service.gettCdd()+serviceHist.getRate()*serviceHist.getCdd())
						/(service.gettCdd()+serviceHist.getCdd())
						);
			}
			service.settCdd(service.gettCdd()+serviceHist.getCdd());
			service.setLastTxId(serviceHist.getTxId());
			service.setLastTime(serviceHist.getTime());
			service.setLastHeight(serviceHist.getHeight());
			
			Service service5 = service;
			
			esClient.index(i->i.index(IndicesNames.SERVICE).id(serviceHist.getSid()).document(service5));
			isValid = true;

			break;
		}
		return isValid;
	}
	public boolean parseApp(ElasticsearchClient esClient, AppHistory appHist) throws ElasticsearchException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		boolean isValid = false;
		App app;
		switch(appHist.getOp()) {
		case "publish":
			app = EsTools.getById(esClient, IndicesNames.APP, appHist.getAid(), App.class);
			if(app==null) {
				app = new App();
				app.setAid(appHist.getTxId());
				app.setStdName(appHist.getStdName());
				app.setLocalNames(appHist.getLocalNames());
				app.setDesc(appHist.getDesc());
				app.setUrls(appHist.getUrls());
				app.setDownloads(appHist.getDownloads());
				app.setWaiters(appHist.getWaiters());
				app.setOwner(appHist.getSigner());
				app.setProtocols(appHist.getProtocols());
				app.setServices(appHist.getServices());
				
				app.setBirthTime(appHist.getTime());
				app.setBirthHeight(appHist.getHeight());
				
				app.setLastTxId(appHist.getTxId());
				app.setLastTime(appHist.getTime());
				app.setLastHeight(appHist.getHeight());
	
				app.setActive(true);
				
				App app1=app;
				esClient.index(i->i.index(IndicesNames.APP).id(appHist.getAid()).document(app1));
				isValid = true;
			}else {
				isValid = false;
			}
			break;
			
		case "stop":
			
			app = EsTools.getById(esClient, IndicesNames.APP, appHist.getAid(), App.class);
			
			if(app==null) {
				isValid = false;
				break;
			}
			
			if(app.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! app.getOwner().equals(appHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(app.isActive()) {
				App app2 = app;
				app2.setActive(false);
				app2.setLastTxId(appHist.getTxId());
				app2.setLastTime(appHist.getTime());
				app2.setLastHeight(appHist.getHeight());
				esClient.index(i->i.index(IndicesNames.APP).id(appHist.getAid()).document(app2));
				isValid = true;
			}else isValid = false;

			break;
			
		case "recover":
			
			app = EsTools.getById(esClient, IndicesNames.APP, appHist.getAid(), App.class);
			
			if(app==null) {
				isValid = false;
				break;
			}
			
			if(app.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! app.getOwner().equals(appHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(!app.isActive()) {
				App app2 = app;
				app2.setActive(true);
				app2.setLastTxId(appHist.getTxId());
				app2.setLastTime(appHist.getTime());
				app2.setLastHeight(appHist.getHeight());
				esClient.index(i->i.index(IndicesNames.APP).id(appHist.getAid()).document(app2));
				isValid = true;
			}else isValid = false;

			break;
			
		case "update":	
			app = EsTools.getById(esClient, IndicesNames.APP, appHist.getAid(), App.class);
			
			if(app==null) {
				isValid = false;
				break;
			}
			
			if(app.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! app.getOwner().equals(appHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(!app.isActive()) {
				isValid = false;
				break;
			}
				
			app.setStdName(appHist.getStdName());
			app.setLocalNames(appHist.getLocalNames());
			app.setDesc(appHist.getDesc());
			app.setUrls(appHist.getUrls());
			app.setDownloads(appHist.getDownloads());
			app.setWaiters(appHist.getWaiters());
			app.setOwner(appHist.getSigner());
			app.setProtocols(appHist.getProtocols());
			app.setServices(appHist.getServices());
			
			app.setLastTxId(appHist.getTxId());
			app.setLastTime(appHist.getTime());
			app.setLastHeight(appHist.getHeight());
			
			App app2 = app;
			
			esClient.index(i->i.index(IndicesNames.APP).id(appHist.getAid()).document(app2));
			isValid = true;
			break;
			
		case "close":	
			app = EsTools.getById(esClient, IndicesNames.APP, appHist.getAid(), App.class);
			
			if(app==null) {
				isValid = false;
				break;
			}
			
			if(app.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! app.getOwner().equals(appHist.getSigner())) {
				Cid resultCid = EsTools.getById(esClient, IndicesNames.CID, appHist.getSigner(), Cid.class);
				if(resultCid.getMaster()!=null) {
					if(! resultCid.getMaster().equals(appHist.getSigner())) {
					isValid = false;
					break;
					}
				}else {
					isValid = false;
					break;
				}
			}

			
			App app1 = app;
			app1.setClosed(true);
			app1.setActive(false);
			app1.setLastTxId(appHist.getTxId());
			app1.setLastTime(appHist.getTime());
			app1.setLastHeight(appHist.getHeight());
			esClient.index(i->i.index(IndicesNames.APP).id(appHist.getAid()).document(app1));
			isValid = true;

			break;
			
		case "rate":
			app = EsTools.getById(esClient, IndicesNames.APP, appHist.getAid(), App.class);
			
			if(app==null) {
				isValid = false;
				break;
			}
			
			if(app.getOwner().equals(appHist.getSigner())) {
				isValid = false;
				break;
			}

			if(app.gettCdd()+appHist.getCdd()==0) {
				app.settRate(0);
			}else {
				app.settRate(
						(app.gettRate()*app.gettCdd()+appHist.getRate()*appHist.getCdd())
						/(app.gettCdd()+appHist.getCdd())
						);
			}
			app.settCdd(app.gettCdd()+appHist.getCdd());
			app.setLastTxId(appHist.getTxId());
			app.setLastTime(appHist.getTime());
			app.setLastHeight(appHist.getHeight());
			
			App app3 = app;
			
			esClient.index(i->i.index(IndicesNames.APP).id(appHist.getAid()).document(app3));
			isValid = true;
			break;
		}
		return isValid;
	}
	public boolean parseCode(ElasticsearchClient esClient, CodeHistory codeHist) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		boolean isValid = false;
		Code code;
		switch(codeHist.getOp()) {
		case "publish":
			code = EsTools.getById(esClient, IndicesNames.CODE, codeHist.getCodeId(), Code.class);
			if(code==null) {
				code = new Code();
				code.setCodeId(codeHist.getTxId());
				code.setName(codeHist.getName());
				code.setVer(codeHist.getVersion());
				code.setDid(codeHist.getDid());
				code.setDesc(codeHist.getDesc());
				code.setLangs(codeHist.getLangs());
				code.setUrls(codeHist.getUrls());
				code.setProtocols(codeHist.getProtocols());
				code.setWaiters(codeHist.getWaiters());
				
				code.setOwner(codeHist.getSigner());
				code.setBirthTime(codeHist.getTime());
				code.setBirthHeight(codeHist.getHeight());
				
				code.setLastTxId(codeHist.getTxId());
				code.setLastTime(codeHist.getTime());
				code.setLastHeight(codeHist.getHeight());
	
				code.setActive(true);
				
				Code code1=code;
				esClient.index(i->i.index(IndicesNames.CODE).id(codeHist.getCodeId()).document(code1));
				isValid = true;
			}else {
				isValid = false;
			}
			break;
			
		case "stop":
			
			code = EsTools.getById(esClient, IndicesNames.CODE, codeHist.getCodeId(), Code.class);
			
			if(code==null) {
				isValid = false;
				break;
			}
			
			if(code.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! code.getOwner().equals(codeHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(code.isActive()) {
				Code app2 = code;
				app2.setActive(false);
				app2.setLastTxId(codeHist.getTxId());
				app2.setLastTime(codeHist.getTime());
				app2.setLastHeight(codeHist.getHeight());
				esClient.index(i->i.index(IndicesNames.CODE).id(codeHist.getCodeId()).document(app2));
				isValid = true;
			}else isValid = false;

			break;
			
		case "recover":
			
			code = EsTools.getById(esClient, IndicesNames.CODE, codeHist.getCodeId(), Code.class);
			
			if(code==null) {
				isValid = false;
				break;
			}
			
			if(code.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! code.getOwner().equals(codeHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(!code.isActive()) {
				Code code2 = code;
				code2.setActive(true);
				code2.setLastTxId(codeHist.getTxId());
				code2.setLastTime(codeHist.getTime());
				code2.setLastHeight(codeHist.getHeight());
				esClient.index(i->i.index(IndicesNames.CODE).id(codeHist.getCodeId()).document(code2));
				isValid = true;
			}else isValid = false;

			break;
			
		case "update":	
			code = EsTools.getById(esClient, IndicesNames.CODE, codeHist.getCodeId(), Code.class);
			
			if(code==null) {
				isValid = false;
				break;
			}
			
			if(code.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! code.getOwner().equals(codeHist.getSigner())) {
				isValid = false;
				break;
			}
			
			if(!code.isActive()) {
				isValid = false;
				break;
			}		

			code.setName(codeHist.getName());
			code.setVer(codeHist.getVersion());
			code.setDid(codeHist.getDid());
			code.setDesc(codeHist.getDesc());
			code.setLangs(codeHist.getLangs());
			code.setUrls(codeHist.getUrls());
			code.setProtocols(codeHist.getProtocols());
			code.setWaiters(codeHist.getWaiters());
			
			code.setLastTxId(codeHist.getTxId());
			code.setLastTime(codeHist.getTime());
			code.setLastHeight(codeHist.getHeight());

			
			Code app2 = code;
			
			esClient.index(i->i.index(IndicesNames.CODE).id(codeHist.getCodeId()).document(app2));
			isValid = true;
			break;
		case "close":	
			code = EsTools.getById(esClient, IndicesNames.CODE, codeHist.getCodeId(), Code.class);
			
			if(code==null) {
				isValid = false;
				break;
			}
			
			if(code.isClosed()) {
				isValid = false;
				break;
			}
			
			if(! code.getOwner().equals(codeHist.getSigner())) {
				Cid resultCid = EsTools.getById(esClient, IndicesNames.CID, codeHist.getSigner(), Cid.class);
				if(resultCid.getMaster()!=null) {
					if(! resultCid.getMaster().equals(codeHist.getSigner())) {
					isValid = false;
					break;
					}
				}else {
					isValid = false;
					break;
				}
			}
			
			Code code1 = code;
			code1.setClosed(true);
			code1.setActive(false);
			code1.setLastTxId(codeHist.getTxId());
			code1.setLastTime(codeHist.getTime());
			code1.setLastHeight(codeHist.getHeight());
			esClient.index(i->i.index(IndicesNames.CODE).id(codeHist.getCodeId()).document(code1));
			isValid = true;
			
			break;
		case "rate":
			code = EsTools.getById(esClient, IndicesNames.CODE, codeHist.getCodeId(), Code.class);
			
			if(code==null) {
				isValid = false;
				break;
			}
			
			if(code.getOwner().equals(codeHist.getSigner())) {
				isValid = false;
				break;
			}

			if(code.gettCdd()+codeHist.getCdd()==0) {
				code.settRate(0);
			}else {
				code.settRate(
						(code.gettRate()*code.gettCdd()+codeHist.getRate()*codeHist.getCdd())
						/(code.gettCdd()+codeHist.getCdd())
						);
			}
			code.settCdd(code.gettCdd()+codeHist.getCdd());
			code.setLastTxId(codeHist.getTxId());
			code.setLastTime(codeHist.getTime());
			code.setLastHeight(codeHist.getHeight());
			
			Code code3 = code;
			
			esClient.index(i->i.index(IndicesNames.CODE).id(codeHist.getCodeId()).document(code3));
			isValid = true;
			break;
		}
		return isValid;
	}
}
