package menu;

import fcTools.ParseTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static constants.Strings.ANY_KEY;

public class Menu {
    private Map<Integer, String> itemMap = new HashMap<>();
    private int itemNum = 0;

    public static void anyKeyToContinue(BufferedReader br) {
        System.out.println(ANY_KEY);
        try {
            br.read();
        } catch (IOException ignored) {}
    }

    public static boolean askIfNotToDo(String x, BufferedReader br)  {
        System.out.println(x+" 'y' to do it. Other key to quit:");

        String input = Inputer.inputString(br);

        return !"y".equals(input);
    }

    public static void printUnderline(int num) {
        for(int i=0; i<num; i++) {
            System.out.print("_");
        }
        System.out.println();
    }

    public static boolean isFullShareMap(Map<String, String> map) {
        long sum = 0;
        for(String key: map.keySet()){
            String valueStr = map.get(key);
            Double valueDb;
            try{
                valueDb = Double.parseDouble(valueStr);
                valueDb = ParseTools.roundDouble8(valueDb);
                sum += ((long)(valueDb*10000));
            }catch (Exception ignore){}
        }
        System.out.println("The sum of shares is "+sum/100 +"%");
        if(sum!=10000){
            System.out.println("Builder shares didn't sum up to 100%. Reset it.");
            return false;
        }
        return true;
    }

    public void add(ArrayList<String> itemList) {
        for (int i = 1; i <= itemList.size(); i++) {
            this.itemMap.put(i, itemList.get(i - 1));
        }
    }

    public void show() {
        System.out.println(
                "	-----------------------------\n"
                        + "	Menu\n"
                        + "	-----------------------------");

        List<Integer> sortedKeys = new ArrayList<>(itemMap.keySet());

        Collections.sort(sortedKeys, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return itemMap.get(o1).compareTo(itemMap.get(o2));
            }
        });

        for (int i = 1; i <= itemMap.size(); i++) {
            System.out.println("	" + i + " " + itemMap.get(i) + "");
        }

        System.out.println(
                "	0 Exit\n"
                        + "	-----------------------------");
        this.itemNum = itemMap.size();
    }

    public int choose(BufferedReader br)  {
        System.out.println("\nInput the number to choose what you want to do:\n");
        int choice = 0;
        while (true) {
            String input = null;
            try {
                input = br.readLine();
                choice = Integer.parseInt(input);
            }catch (Exception e){
            }
            if (choice <= this.itemNum && choice >= 0) break;
            System.out.println("\nInput one of the integers shown above.");
        }
        return choice;
    }
}
