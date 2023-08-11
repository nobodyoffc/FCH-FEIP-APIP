package fileTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FileTools {
    public static File getAvailableFile(BufferedReader br) {
        String input;
        while(true) {
            System.out.println("Input the full path.'s' to skip:");
            try {
                input = br.readLine();
            } catch (IOException e) {
                System.out.println("BufferedReader wrong:"+e.getMessage());
                return null;
            }

            if ("s".equals(input)) return null;

            File file = new File(input);
            if (!file.exists()) {
                System.out.println("\nPath doesn't exist. Input again.");
            } else {
                return file;
            }
        }
    }

    public static File getNewFile(String filePath,String fileName) {
        File file = new File(filePath,fileName);
        if (!file.exists()) {
            try {
                if(file.createNewFile()){
                    return file;
                }else {
                    System.out.println("Create new file "+ fileName +" failed.");
                }
            } catch (IOException e) {
                System.out.println("Create new file "+ fileName +" wrong:"+e.getMessage());
            }
        } else {
            System.out.println("File has existed. It will be rewritten.");
            return file;
        }
        return null;
    }
}
