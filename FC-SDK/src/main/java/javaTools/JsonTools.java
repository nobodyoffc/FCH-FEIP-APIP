package javaTools;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import feipClass.Cid;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonTools {

    private static void sort(JsonElement e) {
        if (e.isJsonNull() || e.isJsonPrimitive()) {
            return;
        }

        if (e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            Iterator<JsonElement> it = a.iterator();
            it.forEachRemaining(i -> sort(i));
            return;
        }

        if (e.isJsonObject()) {
            Map<String, JsonElement> tm = new TreeMap<>(getComparator());
            for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
                tm.put(en.getKey(), en.getValue());
            }

            String key;
            JsonElement val;
            for (Map.Entry<String, JsonElement> en : tm.entrySet()) {
                key = en.getKey();
                val = en.getValue();
                e.getAsJsonObject().remove(key);
                e.getAsJsonObject().add(key, val);
                sort(val);
            }
        }
    }
    public static Map<String, String> getStringStringMap(String json) {
        Gson gson = new Gson();
        // Define the type of the map
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        // Parse the JSON string back into a map
        Map<String, String> map = gson.fromJson(json, mapType);
        return map;
    }
    public static <T,E> Type getMapType(Class<T> t, Class<E> e) {
        return new TypeToken<Map<T, E>>() {}.getType();
    }
    public static <T>Type getArrayListType(Class<T> t) {
        return TypeToken.getParameterized(ArrayList.class, t).getType();
//        return new TypeToken<ArrayList<T>>() {}.getType();
    }

    public static String getNiceString(Object ob) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(ob);
    }

    public static String getString(Object ob) {
        return new Gson().toJson(ob);
    }

    public static void  gsonPrint(Object ob) {
        System.out.println("***********\n" + ob.getClass().toString() + ": " + getNiceString(ob) + "\n***********");
        return;
    }

    public static String strToJson(String rawStr) {
        if(!rawStr.contains("{")) return null;
        if(!rawStr.contains("}")) return null;
        int begin = rawStr.indexOf("{");
        int end = rawStr.lastIndexOf("}");
        rawStr = rawStr.substring(begin,end+1);
        return rawStr.replaceAll("[\r\n\t]", "");
    }

    public static Map<String, Object> getStringObjectMap(String json) {
        Gson gson = new Gson();
        // Define the type of the map
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        // Parse the JSON string back into a map
        Map<String, Object> map = gson.fromJson(json, mapType);
        return map;
    }

    @Test
    public void strTest (){
        String str = "{\n\t\"f\":\"v\"\n}";
        byte[] b = str.getBytes();
        byte[] b1 = new byte[b.length+1];
        for(int i=0; i<b.length;i++  ){
            b1[i] = b[i];
        }
        b1[b.length]=0x00;

        String ns = new String(b1,StandardCharsets.UTF_8);
        System.out.println(str);
        System.out.println(strToJson(str));
        System.out.println(ns);
        System.out.println(ns.trim());
        System.out.println(strToJson(ns));
    }
    private static Comparator<String> getComparator() {
        return (s1, s2) -> s1.compareTo(s2);
    }

    public static void main(String[] args) throws IOException {
        Cid cid = new Cid();
        cid.setFid("fffff");
        cid.setHot(10000);
        String fileName = "cid.json";
        writeObjectToJsonFile(cid,fileName,true);
        cid.setFid("FFFFFF");
        writeObjectToJsonFile(cid,fileName,true);

        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);

        System.out.println("begin read...");
        while(true) {
            try {
                Cid cid1 = readObjectFromJsonFile(fis,Cid.class);
                if(cid1!=null) {
                    gsonPrint(cid1);
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

    public static  <T> void writeObjectToJsonFile(T obj, String fileName, boolean append) {
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
            String json = new String(jsonBytes, StandardCharsets.UTF_8);
            t = gson.fromJson(json, tClass);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return t;
    }

    public static String removeEscapes(String input) {
        StringBuilder result = new StringBuilder();
        boolean escape = false;

        for (char c : input.toCharArray()) {
            if (escape) {
                switch (c) {
                    case 'n' -> result.append('\n');
                    case 't' -> result.append('\t');
                    case 'r' -> result.append('\r');
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case '\\' -> result.append('\\');
                    default -> result.append(c);
                }
                escape = false;
            } else {
                if (c == '\\') {
                    escape = true;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }


    @Test
    public void testRemoveEscapes() {
        String input = "{\\n  \\\"type\\\": \\\"APIP\\\",\\n  \\\"sn\\\": \\\"0\\\",\\n  \\\"ver\\\": \\\"1\\\",\\n  \\\"name\\\": \\\"OpenAPI\\\",\\n  \\\"pid\\\": \\\"\\\",\\n  \\\"data\\\": {\\n    \\\"op\\\": \\\"buy\\\",\\n    \\\"sid\\\": \\\"46c1df926598cf0b881f0f1ab2ac6340826a5f954dd690786459c36388d6c131\\\"\\n  }\\n}";
        String cleanedString = removeEscapes(input);
        System.out.println("Original: " + input);
        System.out.println("Cleaned:  " + cleanedString);
    }
    public static  <T> void writeObjectListToJsonFile(List<T> objList, String fileName, boolean append) {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(fileName,append)) {
            objList.forEach(t -> gson.toJson(t, writer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static <T> List<T> readJsonObjectListFromFile(String fileName, Class<T> clazz) {
        List<T> objects = new ArrayList<>();
        Gson gson = new Gson();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder jsonBuilder = new StringBuilder();
            int braceCount = 0;
            int ch;

            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                jsonBuilder.append(c);

                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;

                    if (braceCount == 0) {
                        T obj = gson.fromJson(jsonBuilder.toString(), clazz);
                        objects.add(obj);
                        jsonBuilder = new StringBuilder();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return objects;
    }
}
