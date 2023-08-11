package startAPIP;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import config.ConfigAPIP;
import constants.ApiNames;
import constants.Strings;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static constants.Constants.FALSE;
import static constants.Constants.TRUE;
import static constants.Strings.*;

public class Settings {

    private static final Logger log = LoggerFactory.getLogger(Settings.class);
    private final Jedis jedis;
    private final ElasticsearchClient esClient;
    private final BufferedReader br;
    private final ConfigAPIP configAPIP;

    public Settings(Jedis jedis, ElasticsearchClient esClient, BufferedReader br, ConfigAPIP configAPIP ) {
        this.jedis = jedis;
        this.esClient = esClient;
        this.br = br;
        this.configAPIP = configAPIP;
    }

    public void menu()  {


        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();
        menuItemList.add("Set windowTime");
        menuItemList.add("List APIs and set nPrice");
        menuItemList.add("Set public sessionKey");
        menuItemList.add("Switch free get APIs");

        menu.add(menuItemList);
        while(true) {
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> setWindowTime(br, jedis, configAPIP);
                case 2 -> setNPrices(jedis, br);
                case 3 -> setPublicSessionKey(br, jedis);
                case 4 -> switchForbidFreeGet(br, jedis, configAPIP);
                case 5 -> configAndLoadToRedis(br, jedis, configAPIP);

                case 0 -> {
                    return;
                }
            }
        }
    }
    static void configAndLoadToRedis(BufferedReader br, Jedis jedis, ConfigAPIP configAPIP) {
        try {
            configAPIP.config(br);
        } catch (IOException e) {
            log.error("Config wrong.",e);
        }
        configAPIP.loadConfigToRedis(jedis);
    }

    private static void switchForbidFreeGet(BufferedReader br, Jedis jedis, ConfigAPIP configAPIP) {
        String freeGetForbidden;
        try {
            freeGetForbidden = jedis.hget(CONFIG, FORBID_FREE_GET);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Read forbidFreeGet failed.");
            return;
        }
        System.out.println("Forbid free get APIs: " + freeGetForbidden + ". Change it? 'y' to change, others to keep it:");
        String input;
        try {
            input = br.readLine();
        } catch (IOException e) {
            System.out.println("br.readLine() wrong.");
            return;
        }
        if (!("y".equals(input))) return;
        if (TRUE.equals(freeGetForbidden)) {
            jedis.hset(CONFIG, FORBID_FREE_GET, FALSE);
            configAPIP.setForbidFreeGet(false);
            System.out.println("ForbidFreeGet is false now.");
        } else if (FALSE.equals(freeGetForbidden)) {
            jedis.hset(CONFIG, FORBID_FREE_GET, TRUE);
            configAPIP.setForbidFreeGet(true);
            System.out.println("ForbidFreeGet is true now.");
        }


        configAPIP.writeConfigToFile();

        Menu.anyKeyToContinue(br);
    }

    static void setPublicSessionKey(BufferedReader br, Jedis jedis) {
        setPublicSessionKey(jedis);
        String balance = jedis.hget(StartAPIP.serviceName+"_"+Strings.FID_BALANCE, PUBLIC);
        System.out.println("The balance of public session is: " + balance + ". Would you reset it? Input a number satoshi to set. Enter to skip.");
        while (true) {
            try {
                String num = br.readLine();
                if("".equals(num))return;
                Long.parseLong(num);
                jedis.hset(StartAPIP.serviceName+"_"+Strings.FID_BALANCE, PUBLIC, num);
                break;
            } catch (Exception ignore) {
                System.out.println("It's not a integer. Input again:");
            }
        }
    }

    private static void setWindowTime(BufferedReader br, Jedis jedis, ConfigAPIP configAPIP)  {
        String windowTimeStr = jedis.hget(CONFIG, Strings.WINDOW_TIME);
        if (windowTimeStr == null) {
            System.out.println("WindowTime is not set yet. Input a long integer to set it in millisecond. Any other to cancel:");
        }
        System.out.println("Input the windowTime: ");
        try {
            windowTimeStr = br.readLine();
        } catch (IOException e) {
            System.out.println("br.readLine() wrong.");
            return;
        }
        long windowTime;
        try {
            windowTime = Long.parseLong(windowTimeStr);
        } catch (Exception e) {
            System.out.println("It's not a integer. ");
            return;
        }
        jedis.hset(CONFIG, Strings.WINDOW_TIME, windowTimeStr);
        configAPIP.setWindowTime(windowTime);

        configAPIP.writeConfigToFile();

        log.debug("The windowTime was set to " + jedis.hget(CONFIG, Strings.WINDOW_TIME));
        Menu.anyKeyToContinue(br);
    }

    private static void setPublicSessionKey(Jedis jedis) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String sessionKey = HexFormat.of().formatHex(randomBytes);
        String oldSession = null;
        try{
            oldSession=jedis.hget(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME,PUBLIC);
        }catch (Exception ignore){}

        jedis.hset(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME,PUBLIC,sessionKey.substring(0,12));

        jedis.select(1);
        try{
            jedis.del(oldSession);
        }catch (Exception ignore){}

        jedis.hset(sessionKey.substring(0,12),SESSION_KEY,sessionKey);
        jedis.hset(sessionKey.substring(0,12),FID,PUBLIC);
        jedis.select(0);
        System.out.println("Public session key set into redis: "+sessionKey);
    }

    static void setNPrices(Jedis jedis, BufferedReader br) {
        Map<Integer, String> apiMap = loadAPIs();
        showAllAPIs(apiMap);
        while (true) {
            System.out.println("""
                    Set nPrices:
                    \t'a' to set all nPrices,
                    \t'one' to set all nPrices by 1,
                    \t'zero' to set all nPrices by 0,
                    \tan integer to set the corresponding API,
                    \tor 'q' to quit.\s""");
            String str = null;
            try {
                str = br.readLine();
                if ("".equals(str)) str = br.readLine();
                if (str.equals("q")) return;
                if (str.equals("a")) {
                    setAllNPrices(apiMap, jedis, br);
                    System.out.println("Done.");
                    return;
                }
            }catch (Exception e){
                log.error("Set nPrice wrong. ",e);
            }
            if(str==null){
                log.error("Set nPrice failed. ");
            }

            if (str.equals("one")) {
                for (int i = 0; i < apiMap.size(); i++) {
                    jedis.hset(StartAPIP.serviceName+"_"+Strings.N_PRICE, apiMap.get(i + 1), "1");
                }
                System.out.println("Done.");
                return;
            }
            if (str.equals("zero")) {
                for (int i = 0; i < apiMap.size(); i++) {
                    jedis.hset(StartAPIP.serviceName+"_"+Strings.N_PRICE, apiMap.get(i + 1), "0");
                }
                System.out.println("Done.");
                return;
            }
            try {
                int i = Integer.parseInt(str);
                if (i > apiMap.size()) {
                    System.out.println("The integer should be no bigger than " + apiMap.size());
                } else {
                    setNPrice(i, apiMap, jedis, br);
                    System.out.println("Done.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Wrong input.");
            }
        }
    }

    private static void setAllNPrices(Map<Integer, String> apiMap, Jedis jedis, BufferedReader br) throws IOException {
        for (int i : apiMap.keySet()) {
            setNPrice(i, apiMap, jedis, br);
        }
    }

    private static void setNPrice(int i, Map<Integer, String> apiMap, Jedis jedis, BufferedReader br) throws IOException {
        String apiName = apiMap.get(i);
        while (true) {
            System.out.println("Input the multiple number of API " + apiName + ":");
            String str = br.readLine();
            try {
                int n = Integer.parseInt(str);
                jedis.hset(StartAPIP.serviceName+"_"+Strings.N_PRICE, apiName, String.valueOf(n));
                return;
            } catch (Exception e) {
                System.out.println("Wong input.");
            }
        }
    }

    private static void showAllAPIs(Map<Integer, String> apiMap) {
        System.out.println("API list:");
        for (int i = 1; i <= apiMap.size(); i++) {
            System.out.println(i + ". " + apiMap.get(i));
        }
    }

    private static Map<Integer, String> loadAPIs() {

        ArrayList<String> apiList = ApiNames.apiList;


        Map<Integer, String> apiMap = new HashMap<>();
        for (int i = 0; i < apiList.size(); i++) apiMap.put(i + 1, apiList.get(i));
        return apiMap;
    }

}
