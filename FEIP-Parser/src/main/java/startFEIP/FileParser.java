package startFEIP;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import construct.*;
import fcTools.ParseTools;
import identity.CidHist;
import identity.IdentityParser;
import identity.IdentityRollbacker;
import identity.RepuHist;
import opReturn.Feip;
import opReturn.OpReFileTools;
import opReturn.OpReturn;
import opReturn.opReReadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import organize.GroupHistory;
import organize.OrganizationParser;
import organize.OrganizationRollbacker;
import organize.TeamHistory;
import personal.BoxHistory;
import personal.PersonalParser;
import personal.PersonalRollbacker;
import publish.ProofHistory;
import publish.PublishParser;
import publish.PublishRollbacker;
import servers.EsTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileParser {

	private String path = null;
	private  String fileName = null;
	private long pointer =0;
	private int length =0;
	private long lastHeight = 0;
	private int lastIndex = 0;
	private String lastId = null;

	enum FEIP_NAME{
		CID,ABANDON,MASTER,HOMEPAGE,NOTICE_FEE,REPUTATION,SERVICE,PROTOCOL,APP,CODE,NID, CONTACT,MAIL,SAFE,STATEMENT,GROUP,TEAM, BOX,PROOF
	}

	private static final Logger log = LoggerFactory.getLogger(FileParser.class);

	public boolean parseFile(ElasticsearchClient esClient, boolean isRollback) throws Exception {

		IdentityRollbacker cidRollbacker = new IdentityRollbacker();
		IdentityParser cidParser = new IdentityParser();

		ConstructParser constructParser = new ConstructParser();
		ConstructRollbacker constructRollbacker = new ConstructRollbacker();

		PersonalParser personalParser = new PersonalParser();
		PersonalRollbacker personalRollbacker = new PersonalRollbacker();

		PublishParser publishParser = new PublishParser();
		PublishRollbacker publishRollbacker = new PublishRollbacker();

		OrganizationParser organizationParser = new OrganizationParser();
		OrganizationRollbacker organizationRollbacker = new OrganizationRollbacker();

		if(isRollback) {
			cidRollbacker.rollback(esClient, lastHeight);
			constructRollbacker.rollback(esClient, lastHeight);
			personalRollbacker.rollback(esClient, lastHeight);
			organizationRollbacker.rollback(esClient, lastHeight);
			publishRollbacker.rollback(esClient, lastHeight);
		}

		FileInputStream fis;

		pointer += length;

		System.out.println("Start parse "+fileName+ " form "+pointer);
		log.info("Start parse {} from {}",fileName,pointer);

		TimeUnit.SECONDS.sleep(2);

		boolean error = false;

		while(!error) {
			fis = openFile();
			fis.skip(pointer);
			opReReadResult readOpResult = OpReFileTools.readOpReFromFile(fis);
			fis.close();
			length = readOpResult.getLength();
			pointer += length;

			boolean isValid= false;

			if(readOpResult.isFileEnd()) {
				if(pointer>251658240) {
					fileName = OpReFileTools.getNextFile(fileName);
					while(!new File(fileName).exists()) {
						System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
						System.out.println(" Waiting 30 seconds for new file ...");
						TimeUnit.SECONDS.sleep(30);
					}
					pointer = 0;
					fis = openFile();
					continue;
				}else {
					System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
					System.out.println(" Waiting for new item ...");
					fis.close();
					ParseTools.waitForNewItemInFile(path+fileName);
					fis = openFile();
					fis.skip(pointer);
					continue;
				}
			}


			if(readOpResult.isRollback()) {
				cidRollbacker.rollback(esClient,readOpResult.getOpReturn().getHeight());
				constructRollbacker.rollback(esClient, readOpResult.getOpReturn().getHeight());
				continue;
			}

			OpReturn opre = readOpResult.getOpReturn();

			lastHeight = opre.getHeight();
			lastIndex = opre.getTxIndex();
			lastId = opre.getTxId();

            if (opre.getHeight() > StartFEIP.CddCheckHeight && opre.getCdd() < StartFEIP.CddRequired) continue;

			Feip feip = parseFeip(opre);
			if(feip==null)continue;
			if(feip.getType()==null)continue;
			if(!feip.getType().equals("FEIP"))continue;

			FEIP_NAME feipName = checkFeipSn(feip);
			if(feipName == null)continue;

			switch(feipName) {

				case CID:
					System.out.println("Cid.");
					CidHist identityHist = cidParser.makeCid(opre,feip);
					if(identityHist==null)break;
					isValid = cidParser.parseCidInfo(esClient,identityHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.CidHistIndex).id(identityHist.getTxId()).document(identityHist));
					break;
				case ABANDON:
					System.out.println("abandon.");
					CidHist identityHist4 = cidParser.makeNobody(opre,feip);
					if(identityHist4==null)break;
					isValid = cidParser.parseCidInfo(esClient,identityHist4);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.CidHistIndex).id(identityHist4.getTxId()).document(identityHist4));
					break;
				case MASTER:
					System.out.println("master.");
					CidHist identityHist1 = cidParser.makeMaster(opre,feip);
					if(identityHist1==null)break;
					isValid = cidParser.parseCidInfo(esClient,identityHist1);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.CidHistIndex).id(identityHist1.getTxId()).document(identityHist1));
					break;
				case HOMEPAGE:
					System.out.println("homepage.");
					CidHist identityHist2 = cidParser.makeHomepage(opre,feip);
					if(identityHist2==null)break;
					isValid = cidParser.parseCidInfo(esClient,identityHist2);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.CidHistIndex).id(identityHist2.getTxId()).document(identityHist2));
					break;
				case NOTICE_FEE:
					System.out.println("notice fee.");
					CidHist identityHist3 = cidParser.makeNoticeFee(opre,feip);
					if(identityHist3==null)break;
					isValid = cidParser.parseCidInfo(esClient,identityHist3);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.CidHistIndex).id(identityHist3.getTxId()).document(identityHist3));
					break;
				case REPUTATION:
					System.out.println("reputation.");
					RepuHist repuHist = cidParser.makeReputation(opre,feip);
					if(repuHist==null)break;
					isValid = cidParser.parseReputation(esClient,repuHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.RepuHistIndex).id(repuHist.getTxId()).document(repuHist));
					break;
				case PROTOCOL:
					System.out.println("Protocol.");
					ProtocolHistory freeProtocolHist = constructParser.makeProtocol(opre,feip);
					if(freeProtocolHist==null)break;
					isValid = constructParser.parseFreeProtocol(esClient,freeProtocolHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.ProtocolHistIndex).id(freeProtocolHist.getTxId()).document(freeProtocolHist));
					break;
				case SERVICE:
					System.out.println("Service.");
					ServiceHistory serviceHist = constructParser.makeService(opre,feip);
					if(serviceHist==null)break;
					isValid = constructParser.parseService(esClient,serviceHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.ServiceHistIndex).id(serviceHist.getTxId()).document(serviceHist));
					break;
				case APP:
					System.out.println("APP.");
					AppHistory appHist = constructParser.makeApp(opre,feip);
					if(appHist==null)break;
					isValid = constructParser.parseApp(esClient,appHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.AppHistIndex).id(appHist.getTxId()).document(appHist));
					break;
				case CODE:
					System.out.println("Code.");
					CodeHistory codeHist = constructParser.makeCode(opre,feip);
					if(codeHist==null)break;
					isValid = constructParser.parseCode(esClient,codeHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.CodeHistIndex).id(codeHist.getTxId()).document(codeHist));
					break;
				case NID:
					System.out.println("Nid.");
					isValid = publishParser.parseNid(esClient,opre,feip);
					break;
				case CONTACT:
					System.out.println("Contact.");
					isValid = personalParser.parseContact(esClient,opre,feip);
					break;
				case MAIL:
					System.out.println("Mail.");
					isValid = personalParser.parseMail(esClient,opre,feip);
					break;
				case SAFE:
					System.out.println("Safe.");
					isValid = personalParser.parseSecret(esClient,opre,feip);
					break;
				case STATEMENT:
					System.out.println("Statement.");
					isValid = publishParser.parseStatement(esClient,opre,feip);
					break;
				case GROUP:
					System.out.println("Group.");
					GroupHistory groupHist = organizationParser.makeGroup(opre,feip);
					if(groupHist==null)break;
					isValid = organizationParser.parseGroup(esClient,groupHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.GroupHistIndex).id(groupHist.getTxId()).document(groupHist));
					break;
				case TEAM:
					System.out.println("Team.");
					TeamHistory teamHist = organizationParser.makeTeam(opre,feip);
					if(teamHist==null)break;
					isValid = organizationParser.parseTeam(esClient,teamHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.TeamHistIndex).id(teamHist.getTxId()).document(teamHist));
					break;
				case BOX:
					System.out.println("Box.");
					BoxHistory boxHist = personalParser.makeBox(opre,feip);
					if(boxHist==null)break;
					isValid = personalParser.parseBox(esClient,boxHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.BoxHistIndex).id(boxHist.getTxId()).document(boxHist));
					break;
				case PROOF:
					System.out.println("Proof.");
					ProofHistory proofHist = publishParser.makeProof(opre,feip);
					if(proofHist==null)break;
					isValid = publishParser.parseProof(esClient,proofHist);
					if(isValid)esClient.index(i->i.index(IndicesFEIP.ProofHistIndex).id(proofHist.getTxId()).document(proofHist));
					break;
				default:
					break;
			}
			if(isValid)writeParseMark(esClient,readOpResult.getLength());
		}
		return error;
	}

	private void writeParseMark(ElasticsearchClient esClient, int length) throws IOException {

		ParseMark parseMark= new ParseMark();

		parseMark.setFileName(fileName);
		parseMark.setPointer(pointer-length);
		parseMark.setLength(length);
		parseMark.setLastHeight(lastHeight);
		parseMark.setLastIndex(lastIndex);
		parseMark.setLastId(lastId);

		esClient.index(i->i.index(IndicesFEIP.ParseMarkIndex).id(parseMark.getLastId()).document(parseMark));
	}

	private FileInputStream openFile() throws FileNotFoundException {

		File file = new File(path,fileName);
		return new FileInputStream(file);
	}

	private Feip parseFeip(OpReturn opre) {

		if(opre.getOpReturn()==null)return null;

		String protStr = ParseTools.strToJson(opre.getOpReturn());

		Feip feip = null;
		try {
			feip = new Gson().fromJson(protStr, Feip.class);
		}catch(JsonSyntaxException e) {
			System.out.println("Invalid opReturn content. Check the JSON string of FEIP:\n"+opre.getOpReturn());
		}
		return  feip;
	}

	private FEIP_NAME checkFeipSn(Feip feip) {

		String sn = feip.getSn();
		if(sn.equals("1"))return FEIP_NAME.PROTOCOL;
		if(sn.equals("2"))return FEIP_NAME.CODE;
		if(sn.equals("3"))return FEIP_NAME.CID;
		if(sn.equals("4"))return FEIP_NAME.ABANDON;
		if(sn.equals("5"))return FEIP_NAME.SERVICE;
		if(sn.equals("6"))return FEIP_NAME.MASTER;
		if(sn.equals("7"))return FEIP_NAME.MAIL;
		if(sn.equals("8"))return FEIP_NAME.STATEMENT;
		if(sn.equals("9"))return FEIP_NAME.HOMEPAGE;
		if(sn.equals("10"))return FEIP_NAME.NOTICE_FEE;
		if(sn.equals("11"))return FEIP_NAME.NID;
		if(sn.equals("12"))return FEIP_NAME.CONTACT;

		if(sn.equals("13"))return FEIP_NAME.BOX;
		if(sn.equals("14"))return FEIP_NAME.PROOF;
		if(sn.equals("15"))return FEIP_NAME.APP;
		if(sn.equals("16"))return FEIP_NAME.REPUTATION;
		if(sn.equals("17"))return FEIP_NAME.SAFE;
		if(sn.equals("18"))return FEIP_NAME.TEAM;
		if(sn.equals("19"))return FEIP_NAME.GROUP;


		return null;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getPointer() {
		return pointer;
	}

	public void setPointer(long pointer) {
		this.pointer = pointer;
	}

	public long getLastHeight() {
		return lastHeight;
	}

	public void setLastHeight(long lastHeight) {
		this.lastHeight = lastHeight;
	}

	public long getLastIndex() {
		return lastIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	public String getLastId() {
		return lastId;
	}

	public void setLastId(String lastId) {
		this.lastId = lastId;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void reparseIdList(ElasticsearchClient esClient, String index, List<String> idList) throws Exception {

		if(idList==null || idList.isEmpty())return;
		switch (index) {
			case IndicesFEIP.CidIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.CidIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);

				ArrayList<CidHist> reparseCidList = getReparseHistList(esClient, IndicesFEIP.CidHistIndex,idList,"signer", CidHist.class);

				for(CidHist idHist: reparseCidList) {
					new IdentityParser().parseCidInfo(esClient,idHist);
				}
				new IdentityRollbacker().reviseCidRepuAndHot(esClient, (ArrayList<String>) idList);
				break;
			case IndicesFEIP.ProtocolIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.ProtocolIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);

				ArrayList<ProtocolHistory> reparseFreeProtocolList = getReparseHistList(esClient, IndicesFEIP.ProtocolHistIndex,idList,"pid", ProtocolHistory.class);

				for(ProtocolHistory idHist: reparseFreeProtocolList) {
					new ConstructParser().parseFreeProtocol(esClient, idHist);
				}
				break;
			case IndicesFEIP.CodeIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.CodeIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);

				ArrayList<CodeHistory> reparseCodeList = getReparseHistList(esClient, IndicesFEIP.CodeHistIndex,idList,"coid",CodeHistory.class);

				for(CodeHistory idHist: reparseCodeList) {
					new ConstructParser().parseCode(esClient, idHist);
				}
				break;
			case IndicesFEIP.AppIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.AppIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);

				ArrayList<AppHistory> reparseAppList = getReparseHistList(esClient, IndicesFEIP.AppHistIndex,idList,"aid",AppHistory.class);

				for(AppHistory idHist: reparseAppList) {
					new ConstructParser().parseApp(esClient, idHist);
				}
				break;
			case IndicesFEIP.ServiceIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.ServiceIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);
				ArrayList<ServiceHistory> reparseServiceList = getReparseHistList(esClient, IndicesFEIP.ServiceHistIndex,idList,"sid",ServiceHistory.class);

				for(ServiceHistory idHist: reparseServiceList) {
					new ConstructParser().parseService(esClient, idHist);
				}
				break;
			case IndicesFEIP.GroupIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.GroupIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);
				ArrayList<GroupHistory> reparseGroupList = getReparseHistList(esClient, IndicesFEIP.GroupHistIndex,idList,"gid",GroupHistory.class);

				for(GroupHistory idHist: reparseGroupList) {
					new OrganizationParser().parseGroup(esClient, idHist);
				}
				break;
			case IndicesFEIP.TeamIndex:
				EsTools.bulkDeleteList(esClient, IndicesFEIP.TeamIndex, (ArrayList<String>) idList);
				TimeUnit.SECONDS.sleep(2);
				ArrayList<TeamHistory> reparseTeamList = getReparseHistList(esClient, IndicesFEIP.TeamHistIndex,idList,"tid",TeamHistory.class);

				for(TeamHistory idHist: reparseTeamList) {
					new OrganizationParser().parseTeam(esClient, idHist);
				}
				break;
			default:
				break;
		}
	}

	private <T>ArrayList<T> getReparseHistList(ElasticsearchClient esClient, String histIndex,
											   List<String> idList, String idField, Class<T> clazz)
			throws ElasticsearchException, IOException {

		List<FieldValue> fieldValueList = new ArrayList<FieldValue>();
		for(String id:idList) {
			fieldValueList.add(FieldValue.of(id));
		}

		SearchResponse<T> result = esClient.search(s->s
						.index(histIndex)
						.query(q->q
								.terms(t->t
										.field(idField)
										.terms(t1->t1.value(fieldValueList))))
				, clazz);
		if(result.hits().total().value()==0)return null;
		List<Hit<T>> hitList = result.hits().hits();
		ArrayList <T> reparseList = new ArrayList<T>();
		for(Hit<T> hit:hitList) {
			reparseList.add(hit.source());
		}
		return reparseList;
	}
}
