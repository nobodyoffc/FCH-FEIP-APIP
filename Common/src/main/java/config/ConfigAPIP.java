package config;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import static constants.Constants.UserDir;
import static constants.Strings.*;

public class ConfigAPIP extends ConfigService {

    private String avatarBasePath = System.getProperty(UserDir)+"/avatar/elements";
    private String avatarPngPath = System.getProperty(UserDir)+"/avatar/png";

    @Override
    public void config(BufferedReader br) throws IOException {
        System.out.println("Config is needed. The content of "+ configFileName+" :");
        printConfigJson();

        setAvatarPath(br);
        setBlockFilePath(br);
        setOpReturnFilePath(br);
        setListenPath(br);
        setTomcatBasePath(br);
        setServiceName(br);
        setWindowTime(br);
        setCheckOrderOpReturn(br);
        setForbidFreeGet(br);
        setScanMempool(br);
        setConfigFilePath(br);
        setEsIp(br);
        setEsPort(br);
        setEsUsername(br);
        setRpcIp(br);
        setRpcPort(br);
        setRpcUsername(br);
        setRpcPassword(br);

        writeConfigToFile();

        System.out.println("Config set. The content is :");
        printConfigJson();
    }

    @Override
    public boolean loadConfigToRedis(Jedis jedis){
        try {
            jedis.hset(CONFIG, ES_IP, esIp);
            jedis.hset(CONFIG, ES_PORT, String.valueOf(esPort));
            jedis.hset(CONFIG, CONFIG_FILE_PATH, configFilePath);
            jedis.hset(CONFIG, OP_RETURN_FILE_PATH, this.getOpReturnFilePath());
            jedis.hset(CONFIG, LISTEN_PATH, this.getListenPath());
            jedis.hset(CONFIG, SERVICE_NAME, serviceName);
            jedis.hset(CONFIG, FORBID_FREE_GET, String.valueOf(forbidFreeGet));
            jedis.hset(CONFIG, CHECK_ORDER_OPRETURN, String.valueOf(checkOrderOpReturn));
            jedis.hset(CONFIG, SCAN_MEMPOOL, String.valueOf(scanMempool));
            jedis.hset(CONFIG, WINDOW_TIME,String.valueOf(this.windowTime));
            jedis.hset(CONFIG, AVATAR_BASE_PATH,avatarBasePath);
            jedis.hset(CONFIG, AVATAR_PNG_PATH, avatarPngPath);

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void setAvatarPath(BufferedReader br) throws IOException {
        setAvatarBasePath(br);
        setAvatarPngPath(br);
    }

    private void setAvatarBasePath(BufferedReader br) throws IOException {

        while (true) {
            if (this.avatarBasePath != null)
                System.out.println("The base path of avatar making is: " + this.avatarBasePath);
            System.out.println("Input the base path of avatar making. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)) return;
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (file.exists()) {
                this.avatarBasePath = str1;
                System.out.println("\nThe base path of avatar making was set.");
                return;
            } else {
                System.out.println("\nPath doesn't exist. ");
            }
        }
    }


    private void setAvatarPngPath(BufferedReader br) throws IOException {

        while (true) {
            if (this.avatarPngPath != null)
                System.out.println("The path for generated png files is: " + this.avatarPngPath);
            System.out.println("Input the path for generated png files. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)) return;
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (!file.exists()) {
                System.out.println("\nPath doesn't exist. ");
            } else {
                this.avatarPngPath = str1;
                System.out.println("\nThe path for generated png files was set.");
                return;
            }
        }
    }
}
