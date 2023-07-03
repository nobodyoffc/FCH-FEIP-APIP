package startFCH;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import FchClass.Block;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.ChainParser;
import parser.OpReFileTools;
import parser.Preparer;
import servers.NewEsClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class StartFCH {

    private static final Logger log = LoggerFactory.getLogger(StartFCH.class);

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    static NewEsClient newEsClient = new NewEsClient();
    public static void main(String[] args) throws Exception {
        ElasticsearchClient esClient = null;

        log.debug("FchParser is start...");

        ConfigFCH configFCH = new ConfigFCH();
        while (true) {
            configFCH= configFCH.getClassInstanceFromFile(br, ConfigFCH.class);
            if (configFCH.getEsIp() == null||configFCH.getBlockFilePath()==null ) configFCH.config(br);

            String path = configFCH.getBlockFilePath();

            esClient = newEsClient.checkEsClient(esClient, configFCH);
            if (esClient == null) {
                log.debug("Creating ES client failed.");
                newEsClient.shutdownClient();
                return;
            }

            Menu menu = new Menu();

            ArrayList<String> menuItemList = new ArrayList<>();
            menuItemList.add("Start new parse");
            menuItemList.add("Restart from interruption");
            menuItemList.add("Manually start from a height");
            menuItemList.add("Config");

            menu.add(menuItemList);

            System.out.println(" << FCH parser >> \n");
            menu.show();

            int choice = menu.choose(br);

            long bestHeight;
            switch (choice) {
                case 1:
                    System.out.println("Start from 0, all indices and opreturn*.byte will be deleted. Do you want? y or n:");
                    String delete = br.readLine();
                    if (delete.equals("y")) {
                        System.out.println("Do you sure? y or n:");
                        delete = br.readLine();
                        if (delete.equals("y")) {
                            File blk = new File(configFCH.getBlockFilePath(), "blk00000.dat");
                            if (!blk.exists()) {
                                System.out.println("blk00000.dat isn't found in " + configFCH.getBlockFilePath() + ". Config the path:");
                                break;
                            }

                            deleteOpReFiles();

                            bestHeight = -1;

                            IndicesFCH.deleteAllIndices(esClient);

                            java.util.concurrent.TimeUnit.SECONDS.sleep(3);
                            IndicesFCH.createAllIndices(esClient);
                            new Preparer().prepare(esClient, path, bestHeight);
                            break;
                        } else break;
                    } else break;

                case 2:
                    System.out.println("Do you sure to restart from bestHeight in ES? y or n:");
                    String restart = br.readLine();

                    if (restart.equals("y")) {

                        Block bestBlock = ChainParser.getBestBlock(esClient);
                        bestHeight = bestBlock.getHeight();

                        log.debug("Restarting from BestHeight: " + (bestHeight - 1) + " ...");

                        path = configFCH.getBlockFilePath();
                        bestHeight = bestHeight - 1;

                        new Preparer().prepare(esClient, path, bestHeight);
                        break;
                    } else break;

                case 3:
                    path = configFCH.getBlockFilePath();
                    while (true) {
                        System.out.println("Input the height you want to rolling back to:");
                        String input = br.readLine();
                        try{
                            bestHeight = Long.parseLong(input);
                            break;
                        }catch (Exception e){
                            System.out.println("Input the height you want to rolling back to:");
                        }
                    }
                    new Preparer().prepare(esClient, path, bestHeight);
                    break;
                case 4:
                    configFCH.config(br);
                    break;
                case 0:
                    //startMyEsClient.esClient.esClient.shutdown();
                    if (esClient != null) newEsClient.shutdownClient();
                    log.debug("FchParser closed by user.");
                    System.out.println("Exited, see you again.");
                    br.close();
                    return;
            }
        }
    }

    private static void deleteOpReFiles() {

        String fileName = ChainParser.OpRefileName;
        File file;

        while (true) {
            file = new File(fileName);
            if (file.exists()) {
                file.delete();
                fileName = OpReFileTools.getNextFile(fileName);
            } else break;
        }
    }
//
//
//
//	private static ElasticsearchClient creatClient(StartClient startMyEsClient) {
//
//		ElasticsearchClient esClient.esClient = startMyEsClient.getEsClient();
//
//		if(esClient.esClient!=null) {
//			System.out.println("There has been a client:"+esClient.esClient.toString()+"\nPress any key return...");
//			sc.nextLine();
//			return null;
//		}
//		if(startMyEsClient.getHost()==null) {
//			System.out.println("No IP. Choose 6 to config first. Press any key return...");
//			sc.nextLine();
//			return null;
//		}
//		if(startMyEsClient.getPort()==0) {
//			System.out.println("No port. Choose 6 to config first. Press any key return...\"");
//			sc.nextLine();
//			return null;
//		}
//
//		try {
//			esClient.esClient = startMyEsClient.getClientHttp();
//			System.out.println("ES client for HTTP created.");
//			return esClient.esClient;
//		} catch (ElasticsearchException e) {
//			log.error("Create esClient.esClient failed.",e);
//			System.out.println("Create esClient.esClient failed. See log...\n");
//			return null;
//		} catch (IOException e) {
//			log.error("Create esClient.esClient failed.",e);
//			System.out.println("Create esClient.esClient failed. See log...\n");
//			return null;
//		}
//	}
//	private static ElasticsearchClient creatSslClient(StartClient startMyEsClient) {
//
//		ElasticsearchClient esClient.esClient = startMyEsClient.getEsClient();
//
//		if(esClient.esClient!=null) {
//			System.out.println("There has been a client:"+esClient.esClient.toString()+"\nPress any key return...");
//			sc.nextLine();
//			return null;
//		}
//		if(startMyEsClient.getHost()==null) {
//			System.out.println("No IP. Choose 6 to config first. Press any key return...");
//			sc.nextLine();
//			return null;
//		}
//		if(startMyEsClient.getPort()==0) {
//			System.out.println("No port. Choose 6 to config first. Press any key return...\"");
//			sc.nextLine();
//			return null;
//		}
//		if(startMyEsClient.getUsername()==null) {
//			System.out.println("No userName. Choose 6 to config first. Press any key return...\"");
//			sc.nextLine();
//			return null;
//		}
//
//		System.out.println("Input the password:");
//		String password = sc.next();
//
//		try {
//			esClient.esClient = startMyEsClient.getClientHttps(password);
//		} catch (KeyManagementException | ElasticsearchException | NoSuchAlgorithmException | IOException e) {
//			log.error("Create esClient.esClient failed.",e);
//			System.out.println("Create esClient.esClient failed. See log...\n");
//		}
//		return esClient.esClient;
//
//	}
//

}
