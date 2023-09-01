package menu;

import fcTools.ParseTools;
import javaTools.BytesTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Inputer {

    public static String inputString(BufferedReader br) {
        String input = null;
        try {
            input = br.readLine();
        } catch (IOException e) {
            System.out.println("BufferedReader is wrong. Can't read.");
        }
        return input;
    }

    public static Double inputGoodShare(BufferedReader br) {
        while (true) {
            String ask = "Input the number. Enter to quit.";
            Double share = inputDouble(br,ask);
            if(share==null)return null;
            if (share > 1) {
                System.out.println("A share should less than 1. ");
                continue;
            }
            return ParseTools.roundDouble4(share);
        }
    }

    public static Double inputDouble(BufferedReader br,String ask)  {

        while(true) {
            System.out.println(ask);
            String inputStr;
            double input;
            try {
                inputStr = br.readLine();
            } catch (IOException e) {
                System.out.println("br.readLine() wrong.");
                return null;
            }
            if("".equals(inputStr))return null;
            try {
                input = Double.parseDouble(inputStr);
                return input;
            } catch (Exception e) {
                System.out.println("Input a number. Try again.");
            }
        }
    }

    public static String inputDoubleAsString(BufferedReader br,String ask)  {

        while(true) {
            System.out.println(ask);
            String inputStr;
            try {
                inputStr = br.readLine();
            } catch (IOException e) {
                System.out.println("br.readLine() wrong.");
                return null;
            }
            if("".equals(inputStr))return null;
            try {
                Double.parseDouble(inputStr);
                return inputStr;
            } catch (Exception e) {
                System.out.println("Input a number. Try again.");
            }
        }
    }

    public static Map<String,String> inputGoodFidValueStrMap(BufferedReader br, String mapName, boolean checkFullShare)  {
        Map<String,String> map = new HashMap<>();

        while(true) {

            while(true) {
                System.out.println("Set " + mapName + ". 'y' to input. 'q' to quit. 'i' to quit ignore all changes.");
                String input;
                try {
                    input = br.readLine();
                } catch (IOException e) {
                    System.out.println("br.readLine() wrong.");
                    return null;
                }
                if("y".equals(input))break;
                if("q".equals(input)){
                    System.out.println(mapName + " is set.");
                    return map;
                }
                if("i".equals(input))return null;
                System.out.println("Invalid input. Try again.");
            }

            String key;
            while (true) {
                System.out.println("Input FID. 'q' to quit:");
                key = inputString(br);
                if(key == null)return null;
                if ("q".equals(key)) break;

                if (!keyTools.KeyTools.isValidFchAddr(key)) {
                    System.out.println("It's not a valid FID. Try again.");
                    continue;
                }
                break;
            }
            Double value = null;

            if(!"q".equals(key)) {
                if (checkFullShare) {
                    value = inputGoodShare(br);
                } else {
                    String ask = "Input the number. Enter to quit.";
                    value = inputDouble(br,ask);
                }
            }

            if(value!=null){
                map.put(key,String.valueOf(value));
            }
        }
    }

    public static String[] inputStringArray(BufferedReader br, String ask, int len) {
        System.out.println(ask);
        ArrayList<String> itemList = new ArrayList<String>();
        while(true) {
            String item =Inputer.inputString(br);
            if(item.equals(""))break;
            if(len>0) {
                if(item.length()!=len) {
                    System.out.println("The length does not match.");
                    continue;
                }
            }
            itemList.add(item);
            System.out.println("Input next item if you want or enter to end:");
        }
        if(itemList.isEmpty())return new String [0];

        String[] items = itemList.toArray(new String[itemList.size()]);

        return items;
    }

    public static String inputShare(BufferedReader br, String share)  {
        float flo;
        String str;
        System.out.println("Input the "+share+ " if you need. Enter to ignore:");
        while(true) {
            str = Inputer.inputString(br);
            if("".equals(str)) return null;
            try {
                flo = Float.valueOf(str);
                if(flo>1){
                    System.out.println("A share should less than 1. Input again:");
                    continue;
                }
                flo = (float) ParseTools.roundDouble4(flo);
                return String.valueOf(flo);
            }catch(Exception e) {
                System.out.println("It isn't a number. Input again:");
            }
        }
    }

    public static String inputInteger(BufferedReader br, String ask) {
        String str;
        System.out.println(ask);
        int num = 0;
        while(true) {
            try {
                str = br.readLine();
            } catch (IOException e) {
                System.out.println("BufferReader wrong.");
                return null;
            }
            if(!("".equals(str))) {
                try {
                    num = Integer.parseInt(str);
                    return String.valueOf(num);
                }catch(Exception e) {
                    System.out.println("It isn't a integer. Input again:");
                }
            }
        }
    }

    public static char[] inputKey(BufferedReader br, String ask)  {
        System.out.println(ask);
        char[] symKey = new char[64];
        int num = 0;
        try {
            num = br.read(symKey);

            if(num!=64 || !BytesTools.isHexCharArray(symKey)){
                System.out.println("The key should be 32 bytes in hex.");
                return null;
            }
            br.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return symKey;
    }

    public static String inputMsg(BufferedReader br) {
        System.out.println("Input the plaintext:");
        String msg = null;
        char[] symKey;
        try {
            msg = br.readLine();
        } catch (IOException e) {
            System.out.println("BufferedReader wrong.");
            return null;
        }
        return msg;
    }
}
