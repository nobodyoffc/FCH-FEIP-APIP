package balance;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import constants.Strings;
import fcTools.ParseTools;
import menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import startAPIP.StartAPIP;
import startAPIP.UserAPIP;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import static constants.Strings.BALANCE;
import static startAPIP.IndicesAPIP.balanceMappingJsonStr;
import static startAPIP.IndicesAPIP.recreateApipIndex;

public class BalanceManager {
    private static final Logger log = LoggerFactory.getLogger(BalanceManager.class);
    private final Jedis jedis;
    private final ElasticsearchClient esClient;
    private final BufferedReader br;


    public BalanceManager(Jedis jedis, ElasticsearchClient esClient, BufferedReader br) {


        this.jedis = jedis;
        this.esClient = esClient;
        this.br = br;
    }

    public void menu()  {

        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();


        menuItemList.add("Find user balance");
        menuItemList.add("Backup user balance to ES");
        menuItemList.add("Recover user balance from ES");
        menuItemList.add("Recreate balance index");

        menu.add(menuItemList);
        while (true) {
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {

                case 1 -> findUsers(br);
                case 2 -> BalanceInfo.backupUserBalanceToEs(esClient, jedis);
                case 3 -> BalanceInfo.recoverUserBalanceFromEs(esClient, jedis);
                case 4 -> recreateApipIndex(br, esClient, jedis, BALANCE, balanceMappingJsonStr);
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void findUsers(BufferedReader br) {
        System.out.println("Input user's fch address or session name. Press enter to list all users:");
        String str;
        try {
            str = br.readLine();
        } catch (IOException e) {
            System.out.println("br.readLine() wrong.");
            return;
        }

        Jedis jedis0Common = new Jedis();
        Jedis jedis1Session = new Jedis();

        jedis1Session.select(1);

        if ("".equals(str)) {
            Set<String> addrSet = jedis0Common.hkeys(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME);
            for (String addr : addrSet) {
                UserAPIP user = getUser(addr, jedis0Common, jedis1Session);
                System.out.println(ParseTools.gsonString(user));
            }
        } else {
            if (jedis0Common.hget(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME, str) != null) {
                UserAPIP user = getUser(str, jedis0Common, jedis1Session);
                System.out.println(ParseTools.gsonString(user));
            } else if (jedis1Session.hgetAll(str) != null) {
                UserAPIP user = getUser(jedis1Session.hget(str, "addr"), jedis0Common, jedis1Session);
                System.out.println(ParseTools.gsonString(user));
            }
        }
        Menu.anyKeyToContinue(br);
    }

    private static UserAPIP getUser(String addr, Jedis jedis0Common, Jedis jedis1Session) {
        UserAPIP user = new UserAPIP();
        user.setAddress(addr);
        user.setBalance(jedis0Common.hget(StartAPIP.serviceName+"_"+Strings.FID_BALANCE, addr));
        String sessionName = jedis0Common.hget(StartAPIP.serviceName+"_"+Strings.FID_SESSION_NAME, addr);
        user.setSessionName(sessionName);
        user.setSessionKey(jedis1Session.hget(sessionName, "sessionKey"));

        long timestamp = System.currentTimeMillis() + jedis1Session.expireTime(sessionName); // example timestamp in milliseconds
        Date date = new Date(timestamp); // create a new date object from the timestamp

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // define the date format
        String formattedDate = sdf.format(date); // format the date object to a string

        user.setExpireAt(formattedDate);

        return user;
    }


}