package servers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fcTools.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HexFormat;

import static api.Constant.UserDir;

public class ConfigBase {
    protected String esIp;
    protected int esPort;
    protected String esUsername;
    protected String randomSymKeyHex;

    protected String blockFilePath;

    protected String configFileName = "config.json";
    protected String configFilePath;

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void setConfigFilePath() {
        configFilePath = System.getProperty(UserDir);

    }
    private static final Logger log = LoggerFactory.getLogger(ConfigBase.class);
    public <T> T getClassInstanceFromFile(BufferedReader br, Class<T> tClass) throws IOException {

        T t;
        if(configFilePath==null)configFilePath=System.getProperty("user.dir");
        System.out.println("config path: "+configFilePath+" config name:"+configFileName);
        File configFile = new File(configFilePath,configFileName);
        System.out.println("Path of config.json: "+configFile.getAbsolutePath());
        if (configFile.exists()) {
            t = readObjectFromFile(configFilePath,configFileName,tClass);
            if (t == null) {
                System.out.println("Can't get parameters from config.json. Config again...\n");
                config(br);
                t = readObjectFromFile(configFilePath,configFileName,tClass);
                return t;
            }else{
                System.out.println("Config.json: "+ ParseTools.gsonString(t));
                return t;
            }
        } else {
            createConfigFile(configFilePath,configFileName);
            System.out.println("The file of config.json was not found. Config it now...\n");
            config(br);
            t = readObjectFromFile(configFilePath,configFileName,tClass);
            return t;
        }
    }
    public <T> T getClassInstanceFromFile(Class<T> tClass) throws IOException {

        T t;
        File configFile = new File(configFilePath,configFileName);
        System.out.println("Path of config.json: "+configFile.getAbsolutePath());
        if (configFile.exists()) {
            t = readObjectFromFile(configFilePath,configFileName,tClass);
            if (t == null) {
                System.out.println("Can't get parameters from config.json. Config again...\n");
                t = readObjectFromFile(configFilePath,configFileName,tClass);
                return t;
            }else{
                System.out.println("Config.json: "+ ParseTools.gsonString(t));
                return t;
            }
        } else {
            createConfigFile(configFilePath,configFileName);
            System.out.println("The file of config.json was not found. Config it now...\n");
            t = readObjectFromFile(configFilePath,configFileName,tClass);
            return t;
        }
    }

    public static <T> T readObjectFromFile(String configFilePath, String configFileName,Class<T> tClass) throws IOException {
        File file = new File(configFilePath,configFileName);
        Gson gson = new Gson();
        FileInputStream fis = new FileInputStream(file);
        byte[] configJsonBytes = new byte[fis.available()];
        fis.read(configJsonBytes);

        String configJson = new String(configJsonBytes);
        T t = gson.fromJson(configJson, tClass);
        fis.close();
        return t;
    }

    public static void createConfigFile(String filePath,String fileName) {
        Path path = Paths.get(filePath,fileName);

        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                System.out.println("File '" + fileName + "' created successfully.");
            } else {
                System.out.println("File '" + fileName + "' already exists.");
            }
        } catch (IOException e) {
            System.out.println("Error creating the file: " + fileName);
            log.debug(e.getMessage());
        }
    }

    public void writeConfigToFile() throws IOException {
        Gson gson = new Gson();

        File configFile = new File(configFilePath,configFileName);
        if(!configFile.exists()){
            try {
                configFile.createNewFile();
            }catch (Exception e){
                System.out.println("Wrong file path.");
                log.debug(e.getMessage());
                return;
            }
        }

        FileOutputStream fos = new FileOutputStream(configFile);

        fos.write(gson.toJson(this).getBytes());
        fos.close();
    }

    public void setEs(BufferedReader br) throws IOException {

        setEsIp(br);
        setEsPort(br);
        setEsUsername(br);
        setSymKey();
    }

    public String getEsIp() {
        return this.esIp;
    }

    private void setEsIp(BufferedReader br) throws IOException {
        if (this.esIp != null)
            System.out.println("IP of ES is: " + this.esIp);

        System.out.println("Input the IP of ES server. Press 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)||"".equals(str)) return;
            if (str.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))")) {
                this.esIp = str;
                break;
            }
            System.out.println("It must be a IPaddress, like \"100.102.102.10\". Input again.");
        }
    }

    public void setEsIp(String esIp) {
        this.esIp = esIp;
    }

    public int getEsPort() {
        return esPort;
    }

    private void setEsPort(BufferedReader br) throws IOException {
        if (this.esPort != 0)
            System.out.println("Port of ES is: " + this.esPort);

        System.out.println("Input the port of ES server. Press 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)||"".equals(str)) return;
            int i = 0;
            try {
                i = Integer.parseInt(str);
            } catch (Exception e) {
                System.out.println("It must be a port. It's a integer between 0 and 655350. Input again.\"");
                log.debug(e.getMessage());
            }
            if (i > 0 && i < 65535) {
                this.esPort = i;
                return;
            }
            System.out.println("It has to be between 0 and 655350. Input again.");
        }
    }

    public void setEsPort(int esPort) {
        this.esPort = esPort;
    }

    public String getEsUsername() {
        return esUsername;
    }

    private void setEsUsername(BufferedReader br) throws IOException {
        if (this.esUsername != null)
            System.out.println("Username of ES is: " + this.esUsername);
        System.out.println("Input the username of ES or press 's' to skip, 'd' to delete it:");
        String str = br.readLine();
        if ("d".equals(str)) {
            this.esUsername = null;
            return;
        }
        if ("s".equals(str)||"".equals(str)) return;
        this.esUsername = str;

    }

    public void setEsUsername(String esUsername) {
        this.esUsername = esUsername;
    }

    public String getBlockFilePath() {
        return blockFilePath;
    }

    public String getRandomSymKeyHex() {
        return randomSymKeyHex;
    }

    public void setRandomSymKeyHex(String randomSymKeyHex) {
        this.randomSymKeyHex = randomSymKeyHex;
    }

    public void setBlockFilePath(BufferedReader br) throws IOException {
        while (true) {
            if (this.blockFilePath != null)
                System.out.println("The path of block files is: " + this.blockFilePath);
            System.out.println("Input the path of freecash block data. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)||"".equals(str1)) {
                return;
            }
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (!file.exists()) {
                System.out.println("\nPath doesn't exist. ");
                continue;
            } else {
                this.blockFilePath = str1;
                return;
            }
        }
    }

    public void setConfigFilePath(BufferedReader br) throws IOException {
        while (true) {
            if (this.blockFilePath != null)
                System.out.println("The path of config.json is: " + this.configFilePath);
            System.out.println("Input the path of config.json. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)||"".equals(str1)) {
                return;
            }
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (!file.exists()) {
                System.out.println("\nPath doesn't exist. ");
                continue;
            } else {
                this.configFilePath = str1;
                return;
            }
        }
    }

    public void config(BufferedReader br) throws IOException {
        printConfiger();
        setEs(br);
        setBlockFilePath(br);
        writeConfigToFile();
        setConfigFilePath();
    }

    public void printConfiger() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        System.out.println(gson.toJson(this));
    }
    public void setSymKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        this.randomSymKeyHex = HexFormat.of().formatHex(bytes);
    }
}
