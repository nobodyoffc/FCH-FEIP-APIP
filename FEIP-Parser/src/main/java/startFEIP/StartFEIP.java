package startFEIP;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.google.gson.Gson;
import config.ConfigFEIP;
import constants.IndicesNames;
import fcTools.ParseTools;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servers.NewEsClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static startFEIP.IndicesFEIP.createAllIndices;
import static startFEIP.IndicesFEIP.deleteAllIndices;

public class StartFEIP {
	public static long CddCheckHeight=2000000;
	public static long CddRequired=1;

	private static final Logger log = LoggerFactory.getLogger(StartFEIP.class);
	public static NewEsClient newEsClient = new NewEsClient();
	
	public static void main(String[] args)throws Exception{
		
		log.info("Start.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		boolean end = false;
		ElasticsearchClient esClient = null;

		ConfigFEIP configFEIP = new ConfigFEIP();
		while(!end) {
			configFEIP = configFEIP.getClassInstanceFromFile(br,ConfigFEIP.class);
			if (configFEIP.getEsIp() == null||configFEIP.getOpReturnFilePath() == null)
				configFEIP.config(br);
			String opReturnJsonPath = configFEIP.getOpReturnFilePath();

			esClient = newEsClient.getElasticSearchClient(br, configFEIP, null);
			if (esClient == null) {
				log.debug("Creating ES client failed.");
				newEsClient.shutdownClient();
				return;
			}

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();
			menuItemList.add("Start New Parse from file");
			menuItemList.add("Restart from interruption");
			menuItemList.add("Manual start from a height");
			menuItemList.add("Reparse ID list");
			menuItemList.add("Config");

			menu.add(menuItemList);

			System.out.println(" << FEIP parser >> \n");
			menu.show();
			int choice = menu.choose(br);

			long bestHeight = 0;
			switch (choice) {
				case 1 -> {
					System.out.println("Start from 0, all indices will be deleted. Do you want? y or n:");
					String delete = br.readLine();
					if (delete.equals("y")) {
						System.out.println("Do you sure? y or n:");
						delete = br.readLine();
						if (delete.equals("y")) {

							System.out.println("Deleting indices...");
							deleteAllIndices(esClient);
							TimeUnit.SECONDS.sleep(3);

							System.out.println("Creating indices...");
							createAllIndices(esClient);
							TimeUnit.SECONDS.sleep(2);

							end = startNewFromFile(esClient, opReturnJsonPath);

						}
					}
				}
				case 2 -> end = restartFromFile(esClient, opReturnJsonPath);
				case 3 -> {
					System.out.println("Input the height that parsing begin with: ");
					while (true) {
						String input = br.readLine();
						try{
							bestHeight = Long.parseLong(input);
							break;
						}catch (Exception e){
							System.out.println("\nInput the number of the height:");
						}
					}
					end = manualRestartFromFile(esClient, opReturnJsonPath, bestHeight);
				}
				case 4 -> {
					System.out.println("Input the name of ES index:");
					String index = br.readLine();
					System.out.println("Input the ID list in compressed Json string:");
					String idListJsonStr = br.readLine();
					Gson gson = new Gson();
					List<String> idList = gson.fromJson(idListJsonStr, ArrayList.class);
					FileParser fileParser = new FileParser();
					fileParser.reparseIdList(esClient, index, idList);
				}
				case 5 -> configFEIP.config(br);
				case 0 -> {
					newEsClient.shutdownClient();
					System.out.println("Exited, see you again.");
					end = true;
				}
				default -> {
				}
			}
		}
		newEsClient.shutdownClient();
		br.close();
	}
	
	private static boolean startNewFromFile(ElasticsearchClient esClient, String path) throws Exception {
		
		System.out.println("startNewFromFile.");
		
		FileParser fileParser = new FileParser();
		
		fileParser.setPath(path);
		fileParser.setFileName("opreturn0.byte");
		fileParser.setPointer(0);
		fileParser.setLastHeight(0);
		fileParser.setLastIndex(0);
		
		boolean isRollback = false;
		return fileParser.parseFile(esClient,isRollback);
		// TODO Auto-generated method stub
	}

	private static boolean restartFromFile(ElasticsearchClient esClient, String path) throws Exception {
		
		SearchResponse<ParseMark> result = esClient.search(s->s
				.index(IndicesNames.FEIP_MARK)
				.size(1)
				.sort(s1->s1
						.field(f->f
								.field("lastIndex").order(SortOrder.Desc)
								.field("lastHeight").order(SortOrder.Desc)
								)
						)
				, ParseMark.class);

		ParseMark parseMark = result.hits().hits().get(0).source();

		if (parseMark == null) throw new AssertionError();
		ParseTools.gsonPrint(parseMark);
		
		FileParser fileParser = new FileParser();
		
		fileParser.setPath(path);
		fileParser.setFileName(parseMark.getFileName());
		fileParser.setPointer(parseMark.getPointer());
		fileParser.setLength(parseMark.getLength());
		fileParser.setLastHeight(parseMark.getLastHeight());
		fileParser.setLastIndex(parseMark.getLastIndex());
		fileParser.setLastId(parseMark.getLastId());
		
		boolean isRollback = false;
		boolean error = fileParser.parseFile(esClient,isRollback);
		
		System.out.println("restartFromFile.");
		return error;
	}

	private static boolean manualRestartFromFile(ElasticsearchClient esClient, String path, long height) throws Exception {
		
		SearchResponse<ParseMark> result = esClient.search(s->s
				.index(IndicesNames.FEIP_MARK)
				.query(q->q.range(r->r.field("lastHeight").lte(JsonData.of(height))))
				.size(1)
				.sort(s1->s1
						.field(f->f
								.field("lastIndex").order(SortOrder.Desc)
								.field("lastHeight").order(SortOrder.Desc)))
				, ParseMark.class);

		if (result.hits().total() == null) throw new AssertionError();
		if(result.hits().total().value()==0) {
			return restartFromFile(esClient,path);
		}
		
		ParseMark parseMark = result.hits().hits().get(0).source();
		
		FileParser fileParser = new FileParser();
		
		fileParser.setPath(path);
		if (null == parseMark) throw new AssertionError();
		fileParser.setFileName(parseMark.getFileName());
		fileParser.setPointer(parseMark.getPointer());
		fileParser.setLength(parseMark.getLength());
		fileParser.setLastHeight(parseMark.getLastHeight());
		fileParser.setLastIndex(parseMark.getLastIndex());
		fileParser.setLastId(parseMark.getLastId());
		
		boolean isRollback = true;
		
		boolean error = fileParser.parseFile(esClient,isRollback);
		
		System.out.println("manualRestartFromFile");
		return error;
		// TODO Auto-generated method stub
		
	}
}

