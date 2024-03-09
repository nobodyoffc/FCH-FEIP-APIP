package appUtils;

import eccAes256K1P7.EccAes256K1P7;
import fcTools.Base58;
import fcTools.ParseTools;
import javaTools.BytesTools;
import keyTools.KeyTools;
import org.bitcoinj.core.ECKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.Console;
import java.util.concurrent.*;

import static FeipClient.IdentityFEIPs.setMasterOnChain;


public class Inputer {

    private static char[] inputPassword(String ask) {
        System.out.println(ask);
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance. Maybe you're running this from within an IDE, which doesn't support Console.");
            return null;
        }
        return console.readPassword(ask);
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

    public static String inputString(BufferedReader br) {
        String input = null;
        try {
            input = br.readLine();
        } catch (IOException e) {
            System.out.println("BufferedReader is wrong. Can't read.");
        }
        return input;
    }
    public static String inputString(BufferedReader br,String ask) {
        System.out.println(ask);
        return inputString(br);
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

    public static String[] inputFidArray(BufferedReader br, String ask, int len) {
        ArrayList<String> itemList = new ArrayList<String>();
        System.out.println(ask);
        while(true) {
            String item =Inputer.inputString(br);
            if(item.equals(""))break;
            if(!KeyTools.isValidFchAddr(item)){
                System.out.println("Invalid FID. Try again.");
                continue;
            }
            if(item.startsWith("3")){
                System.out.println("Multi-sign FID can not used to make new multi-sign FID. Try again.");
                continue;
            }
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

    public static char[] inputPriKeyWif(BufferedReader br)  {
        char[] priKey = new char[52];
        int num = 0;
        try {
            num = br.read(priKey);

            if(num!=52 || !Base58.isBase58Encoded(priKey)){
                System.out.println("The key should be 52 characters and Base58 encoded.");
                return null;
            }
            br.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return priKey;
    }

    public static String inputMsg(BufferedReader br) {
        System.out.println("Input the plaintext:");
        String msg = null;
        try {
            msg = br.readLine();
        } catch (IOException e) {
            System.out.println("BufferedReader wrong.");
            return null;
        }
        return msg;
    }

    public static byte[] getPasswordBytes(BufferedReader br) {
        String ask = "Input password:";
        char[] password = inputPassword(br, ask);
        byte[] passwordBytes = BytesTools.utf8CharArrayToByteArray(password);
        BytesTools.clearCharArray(password);
        return passwordBytes;
    }

    @NotNull
    public static byte[] inputAndCheckNewPassword(BufferedReader br) {
        byte[] passwordBytesNew;
        while(true){
            System.out.print("Set the new password. ");
            passwordBytesNew = getPasswordBytes(br);
            System.out.print("Recheck the new password.");
            byte[] checkPasswordByte = getPasswordBytes(br);
            if(Arrays.equals(passwordBytesNew, checkPasswordByte))break;
            System.out.println("They are not the same. Try again.");
        }
        return passwordBytesNew;
    }

//    public static String inputStringMultiLine(BufferedReader br) {
//        StringBuilder input = new StringBuilder();
//
//        String line;
//
//        while (true) {
//            try {
//                line = br.readLine();
//            } catch (IOException e) {
//                System.out.println("BufferReader wrong.");
//                return null;
//            }
//            if("".equals(line)){
//                break;
//            }
//            input.append(line).append("\n");
//        }
//
//        // Access the complete input as a string
//        String text = input.toString();
//
//        if(text.endsWith("\n")) {
//            text = text.substring(0, input.length()-1);
//        }
//        return text;
//    }
    public static String inputStringMultiLine(BufferedReader br) {
        StringBuilder input = new StringBuilder();
        String line;

        while (true) {
            try {
                line = br.readLine();
            } catch (IOException e) {
                System.out.println("BufferReader wrong.");
                return null;
            }

            // Check for a special delimiter or condition
            if (line == null || line.trim().isEmpty()) {
                break;
            }

            input.append(line).append("\n");
        }

        // Remove the last newline character if present
        if (input.length() > 0 && input.charAt(input.length() - 1) == '\n') {
            input.deleteCharAt(input.length() - 1);
        }

        return input.toString();
    }

    public static boolean askIfYes(BufferedReader br, String ask) {
        System.out.println(ask);
        String input;
        try {
            input = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "y".equals(input);
    }

    @Nullable
    public static ECKey inputPriKey(BufferedReader br) {
        byte[] priKey32;
        String input = inputString(br, "Generate a new private key? y/n");
        if("y".equals(input)){
            return KeyTools.genNewFid(br);
        }else {
            priKey32=KeyTools.inputCipherGetPriKey(br);
            if(priKey32!=null) return ECKey.fromPrivate(priKey32);
        }
        return null;
    }

    public static String inputPriKeyCipher(BufferedReader br, byte[] initSymKey) {
        ECKey ecKey = inputPriKey(br);
        if(ecKey==null)return null;
        byte[] priKeyBytes = ecKey.getPrivKeyBytes();
        return EccAes256K1P7.encryptKeyWithSymKey(priKeyBytes,initSymKey);
    }

    public static String[] promptAndSet(BufferedReader reader, String fieldName, String[] currentValue) throws IOException {
        String ask = "Enter " + fieldName + " (Press Enter to skip): ";
        String[] newValue = inputStringArray(reader,ask,0);
        return newValue.length==0 ? currentValue : newValue;
    }

    public static String promptAndSet(BufferedReader reader, String fieldName, String currentValue) throws IOException {
        System.out.print("Enter " + fieldName + " (Press Enter to skip): ");
        String newValue = reader.readLine();
        return newValue.isEmpty() ? currentValue : newValue;
    }

    public static long promptForLong(BufferedReader reader, String fieldName, long currentValue) throws IOException {
        System.out.print("Enter " + fieldName + " (Press Enter to skip): ");
        String newValue = reader.readLine();
        return newValue.isEmpty() ? currentValue : Long.parseLong(newValue);
    }

    public static String[] promptAndUpdate(BufferedReader reader, String fieldName, String[] currentValue) throws IOException {
        System.out.println(fieldName + " current value: " + Arrays.toString(currentValue));
        System.out.print("Do you want to update it? (y/n): ");

        if ("y".equalsIgnoreCase(reader.readLine())) {
            String ask = "Enter new values for " + fieldName + ": ";
            return inputStringArray(reader,ask,0);
        }
        return currentValue;
    }

    public static String promptAndUpdate(BufferedReader reader, String fieldName, String currentValue) throws IOException {
        System.out.println(fieldName + " current value: " + currentValue);
        System.out.print("Do you want to update it? (y/n): ");

        if ("y".equalsIgnoreCase(reader.readLine())) {
            String ask = "Enter new values for " + fieldName + ": ";
            return inputString(reader,ask);
        }
        return currentValue;
    }
}
