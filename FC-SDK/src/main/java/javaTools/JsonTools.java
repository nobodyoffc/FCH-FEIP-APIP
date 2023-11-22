package javaTools;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
    public static <T,E> Type getMapType(Class<T> t, Class<E> e) {
        return new TypeToken<Map<T, E>>() {}.getType();
    }
    public static <T>Type getArrayListType(Class<T> t) {
        return new TypeToken<ArrayList<T>>() {}.getType();
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

        if (!rawStr.contains("{")) return null;

        int begin = rawStr.indexOf("{");

        String goodStr = rawStr.substring(begin);

        goodStr.replaceAll("\r|\n|\t", "");

        return goodStr;
    }

    private static Comparator<String> getComparator() {
        return (s1, s2) -> s1.compareTo(s2);
    }

}
