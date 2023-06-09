package initial;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class FileCopier {

    public static void main(String[] args){
        String sourceFile = "/Users/liuchangyong/ideaProjects/APIP/a.txt";
        String destinationFile = "/Users/liuchangyong/fc/b.txt";
        copyFile(sourceFile,destinationFile);
    }

    public static void copyFile(String sourceFile, String destinationFile){
        try {
            Path sourcePath = Paths.get(sourceFile);
            Path destinationPath = Paths.get(destinationFile);
            Files.copy(sourcePath, destinationPath);
            System.out.println("File copied successfully!");
        } catch (FileAlreadyExistsException e) {
            System.err.println("File already exists.");
        } catch (IOException e) {
            System.err.println("Something wrong when copying files.");
        }
    }
}
