package javaTools;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class OtherTools {
    public static Map<String, Integer> makeStrMapToIntegerMap(Map<String, String> strMap) {
        Map<String,Integer> integerMap = new HashMap<>();
        if(strMap !=null){
            for(String key: strMap.keySet()){
                try{
                    integerMap.put(key, Integer.parseInt(strMap.get(key)));
                }catch (Exception ignore){}
            }
        }
        return integerMap;
    }

    public static String millisecondToDataTime(long milliTime) {

        Instant instant = Instant.ofEpochMilli(milliTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}
