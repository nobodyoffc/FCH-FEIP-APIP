package fc;

import cryptoTools.SHA;
import cryptoTools.Hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HexFormat;

public class sha256 {
    public static void main(String[] args) throws IOException {
        String s = "aa";
        File file = new File("/Users/liuchangyong/Desktop/a.txt");
        System.out.println("Text:"+s);
        System.out.println("* sha256 utf8:\n"+ HexFormat.of().formatHex(SHA.sha256(s.getBytes())));
        System.out.println("* sha256x2 utf8:\n"+ HexFormat.of().formatHex(SHA.sha256(SHA.sha256(s.getBytes()))));
        System.out.println("* sha256 hex:\n"+ HexFormat.of().formatHex(Hash.sha256(HexFormat.of().parseHex(s))));
        System.out.println("* sha256x2 hex:\n"+ HexFormat.of().formatHex(Hash.Sha256x2(HexFormat.of().parseHex(s))));
        System.out.println("* sha256 file:\n"+Hash.Sha256(file));
        System.out.println("* sha256x2 file:\n"+Hash.Sha256x2(file));
//        sha256 hex:6999b8ad43d9d15e937a6b9ef322f65a10f3fda0fe8a455894bf1333772b6706
//        sha256x2 hex:376b1df6500b74971c4c2e738134b0f8d7b8190e33b1dda687646b1ca24d536e

        File file1 = new File("/Users/liuchangyong/Desktop/CV.pdf");
        FileInputStream fis = new FileInputStream(file1);
        byte[] cipherBytes = new byte[fis.available()];
        System.out.println(fis.read(cipherBytes));
        fis.close();
        FileInputStream fis1 = new FileInputStream(file1);
        System.out.println(fis1.readAllBytes().length);

    }
}
