package javaTools;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class JsonSort {

    public static String sortJson(String json) {
        Gson g = new Gson();
        JsonParser p = new JsonParser();
        JsonElement e = p.parse(json);
        sort(e);
        return g.toJson(e);
    }

    private static Comparator<String> getComparator() {
        return (s1, s2) -> s1.compareTo(s2);
    }

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
}
