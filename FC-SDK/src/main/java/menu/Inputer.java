package menu;

import fcTools.ParseTools;
import javaTools.BytesTools;
import keyTools.KeyTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.Console;

public class Inputer {

public static byte[] inputPassword() {
    Console console = System.console();
    if (console == null) {
        System.out.println("Couldn't get Console instance. Maybe you're running this from within an IDE, which doesn't support Console.");
        return null;
    }

    char[] passwordChars = console.readPassword("Enter your password: ");
    byte[] passwordBytes = new String(passwordChars).getBytes();
    // Clear the password characters immediately after using them for security reasons.
    Arrays.fill(passwordChars, (char) 0);
    return passwordBytes;
}


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


    public static String inputGoodFid(BufferedReader br,String ask)  {

        String fid;
        while (true) {
            System.out.println(ask);
            fid = inputString(br);
            if(fid == null)return null;
            if("".equals(fid))return "";
            if ("d".equals(fid)) return "d";
            if (!keyTools.KeyTools.isValidFchAddr(fid)) {
                System.out.println("It's not a valid FID. Try again.");
                continue;
            }
            return fid;
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
        ArrayList<String> itemList = new ArrayList<String>();
        System.out.println(ask);
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


    public static Map<String,String> inputStringStringMap(BufferedReader br, String askKey, String askValue) {
        Map<String,String> stringStringMap = new HashMap<>();
        while(true) {
            System.out.println(askKey);
            String key =Inputer.inputString(br);
            if(key.equals(""))break;
            System.out.println(askValue);
            String value = inputString(br);
            stringStringMap.put(key,value);
        }
        return stringStringMap;
    }

    public static String inputShare(BufferedReader br, String share)  {
        float flo;
        String str;
        while(true) {
            System.out.println("Input the "+share+ " if you need. Enter to ignore:");
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

    public static String inputIntegerStr(BufferedReader br, String ask) {
        String str;
        int num = 0;
        while(true) {
            System.out.println(ask);
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
            }else return "";
        }
    }

    public static int inputInteger(BufferedReader br, String ask,int maximum) {
        String str;
        int num = 0;
        while(true) {
            System.out.println(ask);
            try {
                str = br.readLine();
            } catch (IOException e) {
                System.out.println("BufferReader wrong.");
                return 0;
            }
            if(!("".equals(str))) {
                try {
                    num = Integer.parseInt(str);
                    if(maximum>0){
                        if(num>maximum){
                            System.out.println("It's bigger than "+maximum+".");
                            continue;
                        }
                    }
                    return num;
                }catch(Exception e) {
                    System.out.println("It isn't a integer. Input again:");
                }
            }else return 0;
        }
    }

    public static char[] input32BytesKey(BufferedReader br, String ask)  {
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

    public static char[] inputPassword(BufferedReader br, String ask)  {
        System.out.println(ask);
        char[] input = new char[64];
        int num = 0;
        try {
            num = br.read(input);
        } catch (IOException e) {
            System.out.println("BufferReader wrong.");
            return null;
        }
        if(num==0)return null;
        char[] password = new char[num-1];
        System.arraycopy(input, 0, password, 0, num-1);
        return password;
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
