package javaTools;

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
}
