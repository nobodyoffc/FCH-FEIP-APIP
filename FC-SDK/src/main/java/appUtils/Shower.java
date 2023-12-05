package appUtils;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;

public class Shower {

    public static String showDataTable(String title, String[]fields, int[]widths, List<List<Object>> valueListList){
        if(fields==null||valueListList==null||fields.length==0||valueListList.size()==0){
            System.out.println("Empty fields.");
            return null;
        }

        if(widths==null ||widths.length == 0){
            widths= new int[fields.length];
            Arrays.fill(widths,20);
        }

        if(fields.length!=widths.length || fields.length!=valueListList.get(0).size()){
            System.out.println("Wrong fields number.");
            return null;
        }

        int totalWidth=0;
        for(int width:widths)totalWidth+=(width+2);

        System.out.println("\n<"+title+">");
        printUnderline(totalWidth);
        for (int i=0;i<fields.length;i++){
            System.out.print(formatString(fields[i],widths[i]+2));
        }
        System.out.println();
        printUnderline(totalWidth);

        for(List<Object> valueList: valueListList){
            for(int i=0;i<valueList.size();i++){
                System.out.print(formatString(String.valueOf(valueList.get(i)),widths[i]+2));
            }
            System.out.println();
        }
        printUnderline(totalWidth);
        return null;
    }

    public static void printUnderline(int num) {
        for(int i=0; i<num; i++) {
            System.out.print("_");
        }
        System.out.println();
    }

    public static String formatString(String str, int length) {
        return String.format("%-" + length + "s", str);
    }

    public static int choose(BufferedReader br,int min,int max)  {
        System.out.println("\nInput the number to choose what you want to do:\n");
        int choice = 0;
        while (true) {
            String input = null;
            try {
                input = br.readLine();
                choice = Integer.parseInt(input);
            }catch (Exception e){
            }
            if (choice <= max && choice >= min) break;
            System.out.println("\nInput an integer within:"+min+"~"+max+".");
        }
        return choice;
    }
}
