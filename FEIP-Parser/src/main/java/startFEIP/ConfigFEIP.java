package startFEIP;

import servers.ConfigBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class ConfigFEIP extends ConfigBase {

    private String opReturnFilePath;

    @Override
    public void config(BufferedReader br) throws IOException {
        printConfiger();
        setEs(br);
        setOpReturnFilePath(br);
        writeConfigToFile();
    }

    public String getOpReturnFilePath() {
        return opReturnFilePath;
    }

    public void setOpReturnFilePath(BufferedReader br) throws IOException {
        while (true) {
            if (this.opReturnFilePath != null)
                System.out.println("The path of OP_Return data files is: " + this.opReturnFilePath);
            System.out.println("Input the path of OP_Return data files. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)) return;
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (!file.exists()) {
                System.out.println("\nPath doesn't exist. ");
                continue;
            } else {
                this.opReturnFilePath = str1;
                return;
            }
        }
    }

    public void setOpReturnFilePath(String opReturnFilePath) {
        this.opReturnFilePath = opReturnFilePath;
    }


}
