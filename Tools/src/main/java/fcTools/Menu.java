package fcTools;

import java.io.BufferedReader;
import java.util.*;

public class Menu {
    private Map<Integer, String> itemMap = new HashMap<>();
    private int itemNum = 0;

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
