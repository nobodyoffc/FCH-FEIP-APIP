package apipClient;

import apipClass.ApipParamsForClient;
import apipClass.Fcdsl;
import apipClass.SignInData;
import apipTools.ApipTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import constants.ApiNames;
import constants.IndicesNames;
import constants.Strings;
import cryptoTools.Hash;
import eccAes256K1P7.EccAes256K1P7;
import fcTools.ParseTools;
import javaTools.BytesTools;
import javaTools.ImageTools;
import keyTools.KeyTools;
import menu.Inputer;
import menu.Menu;
import org.jetbrains.annotations.Nullable;
import walletTools.SendTo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static constants.Constants.APIP_PARAMS_JSON;

public class StartApipClient {
    public static final int DEFAULT_SIZE = 20;

    public static ApipParamsForClient initApipParamsForClient;

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        byte[] sessionKey;
        byte[] symKey;
        while(true) {
            Menu.printUnderline(20);
            System.out.println("\nWelcome to the Freeverse with APIP Client.");
            Menu.printUnderline(20);
            System.out.println("Confirm or set your password...");
            byte[] passwordBytes = Inputer.getPasswordBytes(br);
            symKey = Hash.Sha256x2(passwordBytes);
            try {
                initApipParamsForClient = ApipParamsForClient.checkApipParams(br, passwordBytes.clone());
                if(initApipParamsForClient ==null)return;
                sessionKey = initApipParamsForClient.decryptSessionKey(Hash.Sha256x2(passwordBytes));

                if(sessionKey ==null)continue;
                BytesTools.clearByteArray(passwordBytes);
                break;
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Wrong password, try again.");
            }
        }

        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();
        menuItemList.add("Example");
        menuItemList.add("FreeGet");
        menuItemList.add("OpenAPI");
        menuItemList.add("Blockchain");
        menuItemList.add("Identity");
        menuItemList.add("Organize");
        menuItemList.add("Construct");
        menuItemList.add("Personal");
        menuItemList.add("Publish");
        menuItemList.add("Wallet");
        menuItemList.add("CryptoTool");
        menuItemList.add("Settings");

        menu.add(menuItemList);

        while (true) {
            System.out.println(" << APIP Client>>");
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> showExample(sessionKey,br);
                case 2 -> freeGet(br);
                case 3 -> openAPI(sessionKey, symKey,br);
                case 4 -> blockchain(sessionKey, br);
                case 5 -> identity(sessionKey, br);
                case 6 -> organize(sessionKey, br);
                case 7 -> construct(sessionKey, br);
                case 8 -> personal(sessionKey, br);
                case 9 -> publish(sessionKey, br);
                case 10 -> wallet(sessionKey, br);
                case 11 -> cryptoTools(sessionKey,br);
                case 12 -> setting(sessionKey,symKey,br);
                case 0 -> {
                    BytesTools.clearByteArray(sessionKey);
                    return;
                }
            }
        }
    }
    public static void showExample(byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.addNewQuery().addNewTerms().addNewFields("owner","issuer").addNewValues("FEk41Kqjar45fLDriztUDTUkdki7mmcjWK");
        fcdsl.getQuery().addNewRange().addNewFields("cd").addGt("1").addLt("100");
        fcdsl.addNewFilter().addNewPart().addNewFields("issuer").addNewValue("FEk41Kqjar45fLDriztUDTUkdki7mmcjWK");
        fcdsl.addNewExcept().addNewEquals().addNewFields("cd").addNewValues("1","2");
        fcdsl.addNewSort("cd","desc").addSize(2).addNewAfter("56");
        if(!fcdsl.checkFcdsl())return;
        System.out.println("Java code:");
        Menu.printUnderline(20);
        String code = """
                public static void showExample(byte[] sessionKey, BufferedReader br) {
                \tFcdsl fcdsl = new Fcdsl();
                \tfcdsl.addNewQuery().addNewTerms().addNewFields("owner","issuer").addNewValues("FEk41Kqjar45fLDriztUDTUkdki7mmcjWK");
                \tfcdsl.getQuery().addNewRange().addNewFields("cd").addGt("1").addLt("100");
                \tfcdsl.addNewFilter().addNewPart().addNewFields("issuer").addNewValue("FEk41Kqjar45fLDriztUDTUkdki7mmcjWK");
                \tfcdsl.addNewExcept().addNewEquals().addNewFields("cd").addNewValues("1","2");
                \tfcdsl.addNewSort("cd","desc").addSize(2).addNewAfter("56");
                \tif(!fcdsl.checkFcdsl())return;
                \tOpenAPIs openAPIs = new OpenAPIs();
                \tApipClient apipClient =openAPIs.generalPost(IndicesNames.CASH,initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
                \tGson gson = new GsonBuilder().setPrettyPrinting().create();
                \tSystem.out.println("Request header:\\n"+ParseTools.gsonString(apipClient.getRequestHeaderMap()));
                \tSystem.out.println("Request body:\\n"+gson.toJson(apipClient.getRequestBody()));
                \tSystem.out.println("Response header:\\n"+ParseTools.gsonString(apipClient.getResponseHeaderMap()));
                \tSystem.out.println("Response body:\\n"+gson.toJson(apipClient.getResponseBody()));
                }""";
        System.out.println(code);
        System.out.println("Requesting ...");
        OpenAPIs openAPIs = new OpenAPIs();
        ApipClient apipClient =openAPIs.generalPost(IndicesNames.CASH,initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String responseBodyJson = gson.toJson(apipClient.getResponseBody());
        Menu.printUnderline(20);
        System.out.println("Request header:\n"+ParseTools.gsonString(apipClient.getRequestHeaderMap()));
        Menu.printUnderline(20);
        System.out.println("Request body:\n"+gson.toJson(apipClient.getRequestBody()));
        Menu.printUnderline(20);
        System.out.println("Response header:\n"+ParseTools.gsonString(apipClient.getResponseHeaderMap()));
        Menu.printUnderline(20);
        System.out.println("Response body:");
        Menu.printUnderline(20);
        System.out.println(responseBodyJson);
        Menu.printUnderline(20);

        Menu.anyKeyToContinue(br);
    }

    public static void freeGet(BufferedReader br) {
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.FreeGetAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> broadcast(br);
                case 2 -> getApps(br);
                case 3 -> getServices(br);
                case 4 -> getAvatar(br);
                case 5 -> getCashes(br);
                case 6 -> getFidCid(br);
                case 7 -> getFreeService(br);
                case 8 -> getService(br);
                case 9-> getTotals(br);
                case 0 -> {
                    return;
                }
            }
        }
    }
    public static void broadcast(BufferedReader br) {
        System.out.println("Input the rawTx:");
        String rawTx = Inputer.inputString(br);
        System.out.println("Broadcasting...");
        ApipClient apipClient = new FreeGetAPIs().broadcast(initApipParamsForClient.getUrlHead(),rawTx);
        System.out.println(apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void getCashes(BufferedReader br) {
        String id = Inputer.inputGoodFid(br,"Input the FID:");
        System.out.println("Getting cashes...");
        ApipClient apipClient = new FreeGetAPIs().getCashes(initApipParamsForClient.getUrlHead(),id);
        System.out.println(apipClient.getResponseBodyStr());
        ParseTools.gsonPrint(ApipDataGetter.getCashList(apipClient.getResponseBody().getData()));
        Menu.anyKeyToContinue(br);
    }
    public static void getApps(BufferedReader br) {
        System.out.println("Input the aid or enter to ignore:");
        String id = Inputer.inputString(br);
        if("".equals(id))id=null;
        System.out.println("Getting APPs...");
        ApipClient apipClient = new FreeGetAPIs().getApps(initApipParamsForClient.getUrlHead(),id);
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }
    public static void getServices(BufferedReader br) {
        System.out.println("Input the sid or enter to ignore:");
        String id = Inputer.inputString(br);
        if("".equals(id))id=null;
        System.out.println("Getting services...");
        ApipClient apipClient = new FreeGetAPIs().getServices(initApipParamsForClient.getUrlHead(),id);
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }
    public static void getTotals(BufferedReader br) {
        System.out.println("Getting totals...");
        ApipClient apipClient = new FreeGetAPIs().getTotals(initApipParamsForClient.getUrlHead());
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }
    public static void getFreeService(BufferedReader br) {
        System.out.println("Getting the free service and the sessionKey...");
        ApipClient apipClient = new FreeGetAPIs().getFreeService(initApipParamsForClient.getUrlHead());
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }
    public static void getService(BufferedReader br) {
        System.out.println("Getting the default service information...");
        ApipClient apipClient = new OpenAPIs().getService(initApipParamsForClient.getUrlHead());
        System.out.println(apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void openAPI(byte[] sessionKey, byte[] symKey, BufferedReader br) {
        System.out.println("OpenAPI...");
        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();

        menuItemList.add("getService");
        menuItemList.add("SignInPost");
        menuItemList.add("SignInEccPost");
        menuItemList.add("TotalsGet");
        menuItemList.add("TotalsPost");
        menuItemList.add("generalPost");



        menu.add(menuItemList);

        while (true) {
            System.out.println(" << Maker manager>>");
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> getService(br);
                case 2 -> sessionKey=signInPost(symKey.clone(),null);
                case 3 -> sessionKey = signInEccPost(symKey.clone(),null);
                case 4 -> totalsGet(br);
                case 5 -> totalsPost(sessionKey, br);
                case 6 -> generalPost(sessionKey, br);
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void generalPost(byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = new Fcdsl();
        System.out.println("Input the index name. Enter to exit:");
        String input = Inputer.inputString(br);
        if("".equals(input))return;
        fcdsl.setIndex(input);

        fcdsl.promoteInput(br);

        if(!fcdsl.checkFcdsl()){
            System.out.println("Fcdsl wrong:");
            System.out.println(ParseTools.gsonString(fcdsl));
            return;
        }
        System.out.println(ParseTools.gsonString(fcdsl));
        Menu.anyKeyToContinue(br);
        System.out.println("Requesting ...");
        ApipClient apipClient =new OpenAPIs().generalPost(fcdsl.getIndex(),initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }

    public static void totalsGet(BufferedReader br) {
        System.out.println("Get request for totals...");
        ApipClient apipClient = new OpenAPIs().totalsGet(initApipParamsForClient.getUrlHead());
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }

    public static void totalsPost(byte[] sessionKey, BufferedReader br) {
        System.out.println("Post request for totals...");
        ApipClient apipClient =new OpenAPIs().totalsPost(initApipParamsForClient.getUrlHead(), initApipParamsForClient.getVia(), sessionKey);
        System.out.println(apipClient.getResponseBodyStr());;
        Menu.anyKeyToContinue(br);
    }

    public static byte[] signInEccPost(byte[] symKey, String mode) {
        byte [] priKey = EccAes256K1P7.decryptKeyWithSymKey(initApipParamsForClient.getApipBuyerPriKeyCipher(), symKey.clone());
        if(priKey==null)return null;
        System.out.println("Sign in for EccAES256K1P7 encrypted sessionKey...");
        ApipClient apipClient =new OpenAPIs().signInEccPost(initApipParamsForClient.getUrlHead(), initApipParamsForClient.getVia(),priKey.clone(),mode);

        System.out.println(apipClient.getResponseBodyStr());

        SignInData signInData = (SignInData) apipClient.getResponseBody().getData();
        String sessionKeyCipherFromApip = signInData.getSessionKeyCipher();
        byte[] newSessionKey = ApipTools.decryptSessionKeyWithPriKey(sessionKeyCipherFromApip, priKey.clone());

        updateSession(symKey.clone(), signInData, newSessionKey);

        return newSessionKey;
    }

    public static byte[] signInPost(byte[] symKey, String mode) {
        byte [] priKey = EccAes256K1P7.decryptKeyWithSymKey(initApipParamsForClient.getApipBuyerPriKeyCipher(), symKey.clone());
        if(priKey==null)return null;
        OpenAPIs sinIn = new OpenAPIs();
        System.out.println("Sign in...");
        ApipClient apipClient = sinIn.signInPost(initApipParamsForClient.getUrlHead(), initApipParamsForClient.getVia(), priKey, mode);
        System.out.println(ParseTools.gsonString(apipClient.getResponseBody()));
        SignInData signInData = OpenAPIs.makeSignInData(apipClient.getResponseBodyStr());
        byte[] newSessionKey = HexFormat.of().parseHex(signInData.getSessionKey());

        updateSession(symKey.clone(), signInData, newSessionKey);
        return newSessionKey;
    }

    public static void updateSession(byte[] symKey, SignInData signInData, byte[] newSessionKey) {
        String newSessionKeyCipher = ApipParamsForClient.encrypt32BytesKeyWithSymKeyBytes(newSessionKey, symKey.clone());
        signInData.setSessionKeyCipher(newSessionKeyCipher);
        initApipParamsForClient.setSessionKeyCipher(newSessionKeyCipher);
        initApipParamsForClient.setSessionExpire(signInData.getExpireTime());
        String newSessionName= ApipTools.getSessionName(newSessionKey);
        initApipParamsForClient.setSessionName(newSessionName);
        System.out.println("SessionName:"+ newSessionName);
        System.out.println("SessionKeyCipher: "+ signInData.getSessionKeyCipher());
        ApipParamsForClient.writeApipParamsToFile(initApipParamsForClient,APIP_PARAMS_JSON);
    }

    public static void blockchain(byte[] sessionKey, BufferedReader br) {
        System.out.println("Blockchain...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.BlockchainAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> blockByIds(sessionKey,br);
                case 2 -> blockSearch(DEFAULT_SIZE,"height:desc->blockId:asc",sessionKey,br);
                case 3 -> cashByIds(sessionKey,br);
                case 4 -> cashSearch(DEFAULT_SIZE,"valid:desc->birthHeight:desc->cashId:asc",sessionKey,br);
                case 5 -> cashValid(DEFAULT_SIZE,"cd:asc->value:desc->cashId:asc",sessionKey,br);
                case 6 -> fidByIds(sessionKey,br);
                case 7 -> fidSearch(DEFAULT_SIZE,"lastHeight:desc->fid:asc",sessionKey,br);
                case 8 -> opReturnByIds(sessionKey,br);
                case 9 -> opReturnSearch(DEFAULT_SIZE,"height:desc->txIndex:desc->txId:asc",sessionKey,br);
                case 10-> p2shByIds(sessionKey,br);
                case 11-> p2shSearch(DEFAULT_SIZE,"birthHeight:desc->fid:asc",sessionKey,br);
                case 12-> txByIds(sessionKey,br);
                case 13-> txSearch(DEFAULT_SIZE,"height:desc->txId:asc",sessionKey,br);
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void blockByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input blockIds:",0);
        System.out.println("Requesting blockByIds...");
        ApipClient apipClient =new BlockchainAPIs().blockByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void blockSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting blockSearch...");
        ApipClient apipClient =new BlockchainAPIs().blockSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void cashByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input cashIds:",0);
        System.out.println("Requesting cashByIds...");
        ApipClient apipClient =new BlockchainAPIs().cashByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void cashValid(int defaultSize, String defaultSort,byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting cashValid...");
        ApipClient apipClient =new BlockchainAPIs().cashValidPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void cashSearch(int defaultSize, String defaultSort,byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting cashSearch...");
        ApipClient apipClient =new BlockchainAPIs().cashSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void fidByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input FIDs:",0);
        System.out.println("Requesting fidByIds...");
        ApipClient apipClient =new BlockchainAPIs().fidByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void fidSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting fidSearch...");
        ApipClient apipClient =new BlockchainAPIs().fidSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void opReturnByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input opReturnIds:",0);
        System.out.println("Requesting opReturnByIds...");
        ApipClient apipClient =new BlockchainAPIs().opReturnByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void opReturnSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting opReturnSearch...");
        ApipClient apipClient =new BlockchainAPIs().opReturnSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }
    public static void p2shByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input p2shIds:",0);
        System.out.println("Requesting p2shByIds...");
        ApipClient apipClient =new BlockchainAPIs().p2shByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void p2shSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting p2shSearch...");
        ApipClient apipClient =new BlockchainAPIs().p2shSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }
    public static void txByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input txIds:",0);
        System.out.println("Requesting txByIds...");
        ApipClient apipClient =new BlockchainAPIs().txByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    public static void txSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting txSearch...");
        ApipClient apipClient =new BlockchainAPIs().txSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
    }

    @Nullable
    public static Fcdsl inputFcdsl(int defaultSize, String defaultSort, BufferedReader br) {
        Fcdsl fcdsl = new Fcdsl();

        fcdsl.promoteSearch(defaultSize, defaultSort, br);

        if(!fcdsl.checkFcdsl()){
            System.out.println("Fcdsl wrong:");
            System.out.println(ParseTools.gsonString(fcdsl));
            return null;
        }
        System.out.println("fcdsl:\n"+ParseTools.gsonString(fcdsl));

        Menu.anyKeyToContinue(br);
        return fcdsl;
    }


    public static void identity(byte[] sessionKey, BufferedReader br) {
        System.out.println("Identity...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.IdentityAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> cidInfoByIds(sessionKey,br);
                case 2 -> cidInfoSearch(DEFAULT_SIZE,"nameTime:desc->fid:asc",sessionKey,br);
                case 3 -> cidHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 4 -> fidCidSeek(DEFAULT_SIZE,"lastHeight:desc->cashId:asc",sessionKey,br);
                case 5 -> getFidCid(br);
                case 6 -> nobodyByIds(sessionKey,br);
                case 7 -> nobodySearch(DEFAULT_SIZE,"deathHeight:desc->deathTxIndex:desc",sessionKey,br);
                case 8 -> homepageHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 9 -> noticeFeeHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 10-> reputationHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 11-> avatars(sessionKey,br);
                case 12-> getAvatar(br);
                case 0 -> {
                    return;
                }
            }
        }
    }
    public static void cidInfoByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input FIDs:",0);
        System.out.println("Requesting cidInfoByIds...");
        ApipClient apipClient =new IdentityAPIs().cidInfoByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void cidInfoSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().cidInfoSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void fidCidSeek(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().fidCidSeekPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void getFidCid(BufferedReader br) {
        System.out.println("Input FID or CID:");
        String id = Inputer.inputString(br);
        System.out.println("Requesting ...");
        ApipClient apipClient = new FreeGetAPIs().getFidCid(initApipParamsForClient.getUrlHead(),id);
        System.out.println(apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void nobodyByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input FIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().nobodyByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void nobodySearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().nobodySearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void cidHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().cidHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void homepageHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().homepageHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void noticeFeeHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().noticeFeeHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void reputationHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().reputationHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void avatars(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input FIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new IdentityAPIs().avatarsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void getAvatar(BufferedReader br) {
        String fid = Inputer.inputGoodFid(br,"Input FID:");
        System.out.println("Requesting ...");
        ApipClient apipClient = new FreeGetAPIs().getAvatar(initApipParamsForClient.getUrlHead(),fid);
        byte[] imageBytes = apipClient.getResponseBodyBytes();
        ImageTools.displayPng(imageBytes);
        ImageTools.savePng(imageBytes,"test.png");
        Menu.anyKeyToContinue(br);
    }

    public static void organize(byte[] sessionKey, BufferedReader br) {
        System.out.println("Organize...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.OrganizeAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> groupByIds(sessionKey,br);
                case 2 -> groupSearch(DEFAULT_SIZE,"tCdd:desc->gid:asc",sessionKey,br);
                case 3 -> groupMembers(sessionKey,br);
                case 4 -> groupOpHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 5 -> myGroups(sessionKey,br);
                case 6 -> teamByIds(sessionKey,br);
                case 7 -> teamSearch(DEFAULT_SIZE,"active:desc->tRate:desc->tid:asc",sessionKey,br);
                case 8 -> teamMembers(sessionKey,br);
                case 9 -> teamExMembers(sessionKey,br);
                case 10-> teamOpHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 11-> teamRateHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 12-> teamOtherPersons(sessionKey,br);
                case 13-> myTeams(sessionKey,br);
                case 0 -> {
                    return;
                }
           }
        }
    }

    public static void groupByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input GIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().groupByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void groupSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().groupSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void groupMembers(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input GIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().groupMembersPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void groupOpHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        ApipClient apipClient =new OrganizeAPIs().groupOpHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void myGroups(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the FID. Enter to exit:");
        String id = Inputer.inputString(br);
        if("".equals(id))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().myGroupsPost(initApipParamsForClient.getUrlHead(),id, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void teamByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input TIDs:",0);
        ApipClient apipClient =new OrganizeAPIs().teamByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void teamSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().teamSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void teamMembers(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input TIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().teamMembersPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void teamExMembers(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input TIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().teamExMembersPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void teamRateHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().teamRateHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void teamOpHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().teamOpHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void teamOtherPersons(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input TIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().teamOtherPersonsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void myTeams(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the FID. Enter to exit:");
        String id = Inputer.inputString(br);
        if("".equals(id))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new OrganizeAPIs().myTeamsPost(initApipParamsForClient.getUrlHead(),id, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void construct(byte[] sessionKey, BufferedReader br) {
        System.out.println("Construct...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.ConstructAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> protocolByIds(sessionKey,br);
                case 2 -> protocolSearch(DEFAULT_SIZE,"active:desc->tRate:desc->pid:asc",sessionKey,br);
                case 3 -> protocolOpHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 4 -> protocolRateHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 5 -> codeByIds(sessionKey,br);
                case 6 -> codeSearch(DEFAULT_SIZE,"active:desc->tRate:desc->codeId:asc",sessionKey,br);
                case 7 -> codeOpHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 8 -> codeRateHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 9 -> serviceByIds(sessionKey,br);
                case 10-> serviceSearch(DEFAULT_SIZE,"active:desc->tRate:desc->sid:asc",sessionKey,br);
                case 11-> serviceOpHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 12-> serviceRateHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 13-> appByIds(sessionKey,br);
                case 14-> appSearch(DEFAULT_SIZE,"active:desc->tRate:desc->aid:asc",sessionKey,br);
                case 15-> appOpHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 16-> appRateHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 0 -> {
                    return;
                }
            }
        }
    }
    public static void protocolByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input PIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().protocolByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void protocolSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().protocolSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void protocolRateHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().protocolRateHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void protocolOpHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().protocolOpHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    } public static void codeByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input codeIds:",0);
        ApipClient apipClient =new ConstructAPIs().codeByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void codeSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().codeSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void codeRateHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().codeRateHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void codeOpHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().codeOpHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void serviceByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input SIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().serviceByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void serviceSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().serviceSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void serviceRateHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().serviceRateHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void serviceOpHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().serviceOpHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void appByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input AIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().appByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void appSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().appSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void appRateHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().appRateHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void appOpHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new ConstructAPIs().appOpHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void personal(byte[] sessionKey, BufferedReader br) {
        System.out.println("Personal...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.PersonalAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> boxByIds(sessionKey,br);
                case 2 -> boxSearch(DEFAULT_SIZE,"lastHeight:desc->bid:asc",sessionKey,br);
                case 3 -> boxHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 4 -> contactByIds(sessionKey,br);
                case 5 -> contacts(DEFAULT_SIZE,"birthHeight:desc->contactId:asc",sessionKey,br);
                case 6 -> contactsDeleted(DEFAULT_SIZE,"lastHeight:desc->contactId:asc",sessionKey,br);
                case 7 -> secretByIds(sessionKey,br);
                case 8 -> secrets(DEFAULT_SIZE,"birthHeight:desc->secretId:asc",sessionKey,br);
                case 9 -> secretsDeleted(DEFAULT_SIZE,"lastHeight:desc->secretId:asc",sessionKey,br);
                case 10 -> mailByIds(sessionKey,br);
                case 11 -> mails(DEFAULT_SIZE,"birthHeight:desc->mailId:asc",sessionKey,br);
                case 12 -> mailsDeleted(DEFAULT_SIZE,"lastHeight:desc->mailId:asc",sessionKey,br);
                case 13 -> mailThread(DEFAULT_SIZE,"birthHeight:desc->mailId:asc",sessionKey,br);
                case 0 -> {
                    return;
                }
            }
        }
    }
    public static void boxByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input BIDs:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().boxByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void boxSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().boxSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void boxHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().boxHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void contactByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input contactIds:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().contactByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void contacts(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().contactsPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void contactsDeleted(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().contactsDeletedPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void secretByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input secretIds:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().secretByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void secrets(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().secretsPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void secretsDeleted(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().secretsDeletedPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void mailByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input mailIds:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().mailByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void mails(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().mailsPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void mailsDeleted(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().mailsDeletedPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void mailThread(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PersonalAPIs().mailThreadPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void publish(byte[] sessionKey, BufferedReader br) {
        System.out.println("Publish...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.PublishAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> proofByIds(sessionKey,br);
                case 2 -> proofSearch(DEFAULT_SIZE,"lastHeight:desc->bid:asc",sessionKey,br);
                case 3 -> proofHistory(DEFAULT_SIZE,"height:desc->index:desc",sessionKey,br);
                case 4 -> statementByIds(sessionKey,br);
                case 5 -> statementSearch(DEFAULT_SIZE,"birthHeight:desc->contactId:asc",sessionKey,br);
                case 6 -> nidSearch(DEFAULT_SIZE,"birthHeight:desc->nameId:asc",sessionKey,br);
                case 0 -> {
                    return;
                }
            }
        }
    }
    public static void proofByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input proofIds:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new PublishAPIs().proofByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void proofSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PublishAPIs().proofSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void proofHistory(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PublishAPIs().proofHistoryPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void statementByIds(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input statementIds:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new PublishAPIs().statementByIdsPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void statementSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PublishAPIs().statementSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void nidSearch(int defaultSize, String defaultSort, byte[] sessionKey, BufferedReader br) {
        Fcdsl fcdsl = inputFcdsl(defaultSize, defaultSort, br);
        if (fcdsl == null) return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new PublishAPIs().nidSearchPost(initApipParamsForClient.getUrlHead(),fcdsl, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void wallet(byte[] sessionKey, BufferedReader br) {
        System.out.println("Wallet...");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.WalletAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> broadcastTx(sessionKey,br);
                case 2 -> decodeRawTx(sessionKey,br);
                case 3 -> cashValidForPay(sessionKey,br);
                case 4 -> cashValidForCd(sessionKey,br);
                case 5 -> unconfirmed(sessionKey,br);
                case 0 -> {
                    return;
                }
            }
        }
    }
    public static void broadcastTx(byte[] sessionKey, BufferedReader br) {
        String txHex;
        while (true) {
            System.out.println("Input the hex of the TX:");
            txHex = Inputer.inputString(br);
            if (BytesTools.isHexString(txHex)) {
                System.out.println("It's not a hex. Try again.");
                break;
            }
        }
        System.out.println("Requesting ...");
        ApipClient apipClient =new WalletAPIs().broadcastTxPost(initApipParamsForClient.getUrlHead(),txHex, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void decodeRawTx(byte[] sessionKey, BufferedReader br) {
        String txHex;
        while (true) {
            System.out.println("Input the hex of the raw TX:");
            txHex = Inputer.inputString(br);
            if (BytesTools.isHexString(txHex)) break;
            System.out.println("It's not a hex. Try again.");
        }
        System.out.println("Requesting ...");
        ApipClient apipClient =new WalletAPIs().decodeRawTxPost(initApipParamsForClient.getUrlHead(),txHex, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void cashValidForPay(byte[] sessionKey, BufferedReader br) {
        String fid;

        while (true) {
            System.out.println("Input the sender's FID:");
            fid = Inputer.inputString(br);
            if (KeyTools.isValidFchAddr(fid)) break;
            System.out.println("It's not a FID. Try again.");
        }
        Double amount = Inputer.inputDouble(br, "Input the amount:");
        if(amount==null)return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new WalletAPIs().cashValidForPayPost(initApipParamsForClient.getUrlHead(),fid,amount, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void cashValidForCd(byte[] sessionKey, BufferedReader br) {
        String fid;

        while (true) {
            System.out.println("Input the sender's FID:");
            fid = Inputer.inputString(br);
            if (KeyTools.isValidFchAddr(fid)) break;
            System.out.println("It's not a FID. Try again.");
        }
        int cd = Inputer.inputInteger(br, "Input the required CD:",0);
        System.out.println("Requesting ...");
        ApipClient apipClient =new WalletAPIs().cashValidForCdPost(initApipParamsForClient.getUrlHead(),fid,cd, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void unconfirmed(byte[] sessionKey, BufferedReader br) {
        String[] ids = Inputer.inputStringArray(br,"Input FIDs:",0);
        ApipClient apipClient =new WalletAPIs().unconfirmedPost(initApipParamsForClient.getUrlHead(),ids, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void cryptoTools(byte[] sessionKey,BufferedReader br) {
        System.out.println("CryptoTools");
        while(true){
            Menu menu = new Menu();
            menu.add(ApiNames.CryptoToolsAPIs);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> addresses(sessionKey,br);
                case 2 -> encrypt(sessionKey,br);
                case 3 -> verify(sessionKey,br);
                case 4 -> sha256(sessionKey,br);
                case 5 -> sha256x2(sessionKey,br);
                case 6 -> sha256Bytes(sessionKey,br);
                case 7 -> sha256x2Bytes(sessionKey,br);
                case 8 -> offLineTx(sessionKey,br);
                case 9 -> offLineTxByCd(sessionKey,br);
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void addresses(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the address or public key. Enter to exit:");
        String addrOrKey = Inputer.inputString(br);
        if("".equals(addrOrKey))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().addressesPost(initApipParamsForClient.getUrlHead(),addrOrKey, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void encrypt(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the text. Enter to exit:");
        String msg = Inputer.inputString(br);
        if("".equals(msg))return;
        System.out.println("Requesting ...");
        System.out.println("Input the pubKey or symKey. Enter to exit:");
        String key = Inputer.inputString(br);
        if("".equals(key))return;

        ApipClient apipClient =new CryptoToolAPIs().encryptPost(initApipParamsForClient.getUrlHead(),key,msg, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void verify(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the signature. Enter to exit:");
        String signature = Inputer.inputString(br);
        if("".equals(signature))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().verifyPost(initApipParamsForClient.getUrlHead(),signature, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void sha256(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the text. Enter to exit:");
        String text = Inputer.inputString(br);
        if("".equals(text))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().sha256Post(initApipParamsForClient.getUrlHead(),text, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }
    public static void sha256x2(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the text. Enter to exit:");
        String text = Inputer.inputString(br);
        if("".equals(text))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().sha256x2Post(initApipParamsForClient.getUrlHead(),text, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void sha256Bytes(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the text. Enter to exit:");
        String text = Inputer.inputString(br);
        if("".equals(text))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().sha256BytesPost(initApipParamsForClient.getUrlHead(),text, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void sha256x2Bytes(byte[] sessionKey, BufferedReader br) {
        System.out.println("Input the text. Enter to exit:");
        String text = Inputer.inputString(br);
        if("".equals(text))return;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().sha256x2BytesPost(initApipParamsForClient.getUrlHead(),text, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void offLineTx(byte[] sessionKey, BufferedReader br) {

        String fid = Inputer.inputGoodFid(br,"Input the sender's FID:");

        List<SendTo> sendToList = SendTo.inputSendToList(br);

        System.out.println("Input the text of OpReturn. Enter to skip:");
        String msg = Inputer.inputString(br);
        if("".equals(msg))msg=null;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().offLineTxPost(initApipParamsForClient.getUrlHead(),fid,sendToList,msg, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void offLineTxByCd(byte[] sessionKey, BufferedReader br) {

        String fid = Inputer.inputGoodFid(br,"Input the FID:");

        int cd = Inputer.inputInteger(br,"Input the required CD:",0);

        List<SendTo> sendToList = SendTo.inputSendToList(br);

        System.out.println("Input the text of OpReturn. Enter to skip:");
        String msg = Inputer.inputString(br);
        if("".equals(msg))msg=null;
        System.out.println("Requesting ...");
        ApipClient apipClient =new CryptoToolAPIs().offLineTxByCdPost(initApipParamsForClient.getUrlHead(),fid,sendToList,msg,cd, initApipParamsForClient.getVia(), sessionKey);
        System.out.println("apipClient:\n"+apipClient.getResponseBodyStr());
        Menu.anyKeyToContinue(br);
    }

    public static void setting(byte[] sessionKey, byte[] symKey, BufferedReader br) {
        System.out.println("setting...");
        while(true){
            Menu menu = new Menu();
            menu.add("Check APIP", "Reset APIP","Refresh SessionKey","Change password");
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> checkApip(sessionKey,br);
                case 2 -> resetApip(br);
                case 3 -> sessionKey = refreshSessionKey(symKey);
                case 4 -> {
                    byte[] symKeyNew = resetPassword(br);
                    if(symKeyNew==null)break;
                    symKey = symKeyNew;

                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static byte[] resetPassword(BufferedReader br) {

        byte[] passwordBytesOld;
        while(true) {
            System.out.print("Check password. ");

            passwordBytesOld = Inputer.getPasswordBytes(br);
            byte[] sessionKey = initApipParamsForClient.decryptSessionKey(Hash.Sha256x2(passwordBytesOld));
            if (sessionKey != null) break;
            System.out.println("Wrong password. Try again.");
        }

        byte[] passwordBytesNew;
        passwordBytesNew = Inputer.inputAndCheckNewPassword(br);

        byte[] symKeyOld = Hash.Sha256x2(passwordBytesOld);

        byte[] sessionKey = initApipParamsForClient.decryptSessionKey(symKeyOld.clone());
        byte [] priKey = EccAes256K1P7.decryptKeyWithSymKey(initApipParamsForClient.getApipBuyerPriKeyCipher(), symKeyOld.clone());

        byte[] symKeyNew= Hash.Sha256x2(passwordBytesNew);
        String buyerPriKeyCipherNew = ApipParamsForClient.encrypt32BytesKeyWithSymKeyBytes(priKey, symKeyNew.clone());
        if(buyerPriKeyCipherNew==null){
            System.out.println("Encrypt buyer's priKey with new password wrong.");
            return passwordBytesOld;
        }
        String sessionKeyCipherNew = EccAes256K1P7.encryptKey(sessionKey, symKeyNew.clone());
        if(sessionKeyCipherNew.contains("Error")){
            System.out.println("Get sessionKey wrong:"+sessionKeyCipherNew);
        }
        initApipParamsForClient.setSessionKeyCipher(sessionKeyCipherNew);
        initApipParamsForClient.setApipBuyerPriKeyCipher(buyerPriKeyCipherNew);

        ApipParamsForClient.writeApipParamsToFile(initApipParamsForClient, APIP_PARAMS_JSON);
        return symKeyNew;
    }

    public static byte[] refreshSessionKey(byte[] symKey) {
        System.out.println("Refreshing ...");
        return signInEccPost(symKey, Strings.REFRESH);
    }

    public static void checkApip(byte[] sessionKey, BufferedReader br) {
        Menu.printUnderline(20);
        System.out.println("Apip Service:");
        String urlHead = initApipParamsForClient.getUrlHead();
        String[] ids = new String[]{initApipParamsForClient.getSid()};
        String via = initApipParamsForClient.getVia();

        System.out.println("Requesting ...");
        ApipClient apipClient = new ConstructAPIs().serviceByIdsPost(urlHead, ids, via, sessionKey);
        System.out.println(apipClient.getResponseBodyStr());

        Menu.printUnderline(20);
        System.out.println("User Params:");
        System.out.println(ParseTools.gsonString(initApipParamsForClient));
        Menu.printUnderline(20);
        Menu.anyKeyToContinue(br);
    }

    public static void resetApip(BufferedReader br) {
        byte[] passwordBytes = Inputer.getPasswordBytes(br);
        initApipParamsForClient.updateApipParams(br, passwordBytes);
    }
}
