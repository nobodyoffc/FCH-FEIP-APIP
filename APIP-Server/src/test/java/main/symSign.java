package main;

import javaTools.BytesTools;
import cryptoTools.SHA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class symSign {
    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.println("Input text to sign, press 'q' to exit:");
            String text = br.readLine();
            if(text.equals("q"))return;
            System.out.println("cidOrAddr\n" + getSign(text));
        }
    }


    private static void signCidInfo() {
        String cidOrAddr = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/cidOrAddr\",\"time\":1677673821267,\"nonce\":1,\"fcdsl\":{\"query\":{\"part\":{\"fields\":[\"cid\",\"id\"],\"value\":\"*arm?\"}}}}";
        System.out.println("cidOrAddr\n"+getSign(cidOrAddr));



    }

    private static void signDSL() {
        String after = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/after\",\"time\":1677673821267,\"nonce\":17,\"query\":{\"range\":{\"field\":\"cdd\",\"gt\":\"10\"}},\"filter\":{\"exist\":\"btcAddr\"},\"size\":\"10\",\"sort\":{\"cdd\":\"desc\"},\"after\":[\"1000000\"]}";
        String all ="{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/all\",\"time\":1677673821267,\"nonce\":1}";
        String equals = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/equals\",\"time\":1677673821267,\"nonce\":12,\"query\":{\"equals\":{\"fields\":[\"inCount\",\"outCount\"],\"values\":[\"2\",\"3\"]}}}";
        String exist = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/exist\",\"time\":1677673821267,\"nonce\":10,\"query\":{\"exist\":[\"opReBrief\"]}}";
        String filter = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/filter\",\"time\":1677673821267,\"nonce\":14,\"query\":{\"range\":{\"field\":\"cdd\",\"gt\":\"10\",\"lte\":\"100000\"}},\"filter\":{\"exist\":\"btcAddr\"}}";
        String ids = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/ids\",\"time\":1677673821267,\"nonce\":2,\"ids\":[\"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv\",\"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\"]}";
        String match = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/match\",\"time\":1677673821267,\"nonce\":8,\"fcdsl\":{\"query\":{\"match\":{\"fields\":[\"opReBrief\"],\"value\":\"FEIP sn\"}}}}";
        String part = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/part\",\"time\":1677673821267,\"nonce\":7,\"query\":{\"part\":{\"fields\":[\"guide\",\"id\"],\"value\":\"*mcjW?\"}}}";
        String range = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/range\",\"time\":1677673821267,\"nonce\":13,\"query\":{\"range\":{\"field\":\"cdd\",\"gt\":\"10\",\"lte\":\"100000\"}}}";
        String size = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/size\",\"time\":1677673821267,\"nonce\":15,\"query\":{\"range\":{\"field\":\"cdd\",\"gt\":\"10\"}},\"filter\":{\"exist\":\"btcAddr\"},\"size\":\"3\"}";
        String sort = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/sort\",\"time\":1677673821267,\"nonce\":16,\"query\":{\"range\":{\"field\":\"cdd\",\"gt\":\"10\"}},\"filter\":{\"exist\":\"btcAddr\"},\"size\":\"10\",\"sort\":{\"cd\":\"asc\",\"cdd\":\"desc\"}}";
        String terms = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/terms\",\"time\":1677673821267,\"nonce\":5,\"query\":{\"terms\":{\"fields\":[\"guide\",\"id\"],\"values\":[\"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv\",\"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\"]}}}";
        String unexist = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/unexist\",\"time\":1677673821267,\"nonce\":11,\"query\":{\"unexist\":[\"opReBrief\",\"coinbase\"]}}";
        String part0 = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/part\",\"time\":1677673821267,\"nonce\":3,\"query\":{\"part\":{\"fields\":[\"guide\"],\"value\":\"*mcjW?\"}}}";
        String match0 = "{\"url\":\"http://localhost:8080/APIPserver/apip3/v1/match\",\"time\":1677673821267,\"nonce\":8,\"query\":{\"match\":{\"fields\":[\"id\"],\"value\":\"arm vpAv\"}}}";
        String sum="{\"url\":\"http://localhost:8080/APIPserver/fc_browser/v1/sum\",\"time\":1677673821267,\"nonce\":30}";


        System.out.println("after\n"+getSign(after));
        System.out.println("all\n"+getSign(all));
        System.out.println("equals\n"+getSign(equals));
        System.out.println("exist\n"+getSign(exist));
        System.out.println("filter\n"+getSign(filter));
        System.out.println("ids\n"+getSign(ids));
        System.out.println("match\n"+getSign(match));
        System.out.println("part\n"+getSign(part));
        System.out.println("range\n"+getSign(range));
        System.out.println("size\n"+getSign(size));
        System.out.println("sort\n"+getSign(sort));
        System.out.println("terms\n"+getSign(terms));
        System.out.println("unexist\n"+getSign(unexist));
        System.out.println("part0\n"+getSign(part0));
        System.out.println("match0\n"+getSign(match0));

        System.out.println("sum\n"+getSign(sum));
    }

    static class T1{
        int i;
        Map<String,Long> map;
    }

    private static String getSign(String text) {
        byte[] textBytes = text.getBytes();
        byte[] keyBytes = BytesTools.hexToByteArray("7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08");
        byte[] bytes = BytesTools.bytesMerger(textBytes,keyBytes);
        byte[] signBytes = SHA.Sha256x2(bytes);
        return BytesTools.bytesToHexStringBE(signBytes);
    }
}
