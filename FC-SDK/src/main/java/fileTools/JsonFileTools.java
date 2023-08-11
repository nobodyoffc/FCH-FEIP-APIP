package fileTools;

import com.google.gson.Gson;
import fcTools.ParseTools;
import feipClass.Cid;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JsonFileTools {

    public static void main(String[] args) throws IOException {
        Cid cid = new Cid();
        cid.setFid("fffff");
        cid.setHot(10000);
        String fileName = "cid.json";
        writeObjectToJsonFile(cid,fileName,true,Cid.class);
        cid.setFid("FFFFFF");
        writeObjectToJsonFile(cid,fileName,true,Cid.class);

        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);

        System.out.println("begin read...");
        while(true) {
            try {
                Cid cid1 = readObjectFromJsonFile(fis,Cid.class);
                if(cid1!=null) {
                    ParseTools.gsonPrint(cid1);
                }else return;

            } catch (Exception e) {
                System.out.println("Read file wrong.");
                e.printStackTrace();
                fis.close();
                return;
            }
        }

    }

    public static <T> T readObjectFromJsonFile(String filePath, String fileName, Class<T> tClass) throws IOException {
        File file = new File(filePath,fileName);
        Gson gson = new Gson();
        FileInputStream fis = new FileInputStream(file);
        byte[] configJsonBytes = new byte[fis.available()];
        fis.read(configJsonBytes);

        String configJson = new String(configJsonBytes);
        T t = gson.fromJson(configJson, tClass);
        fis.close();
        return t;
    }

    public static  <T> void writeObjectToJsonFile(T obj,String fileName, boolean append,Class<T> tClass) {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(fileName,append)) {
            gson.toJson(obj, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T readObjectFromJsonFile(FileInputStream fis, Class<T> tClass) throws IOException {
        Gson gson = new Gson();
        T t;
        byte[] jsonBytes;
        ArrayList<Integer> jsonByteList = new ArrayList<>();

        int tip = 0;

        boolean counting = false;
        boolean ignore;
        int b;
        while(true){
            b = fis.read();
            if(b<0)return null;

            ignore = (char) b == '\\';

            if(ignore){
                jsonByteList.add(b);
                continue;
            }

            if((char)b == '{'){
                counting = true;
                tip++;
            }else {
                if ((char) b == '}'&&counting) tip--;
            }

            jsonByteList.add(b);

            if(counting && tip==0){
                jsonBytes = new byte[jsonByteList.size()];
                int i=0;
                for(int b1 : jsonByteList){
                    jsonBytes[i]= (byte) b1;
                    i++;
                }
                counting=false;
                break;
            }
        }
        if(counting)return null;

        try {
            String json = new String(jsonBytes,StandardCharsets.UTF_8);
            t = gson.fromJson(json, tClass);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return t;
    }
}
