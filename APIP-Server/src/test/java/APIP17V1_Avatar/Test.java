package APIP17V1_Avatar;

import java.io.IOException;
import java.util.Set;

public class Test {
    public static void main(String[] args) throws IOException {
        String basePath = "/Users/liuchangyong/Desktop/IdeaProjects/FCH-FEIP-APIP/APIP-Server/avatar/elements";
        String filePath="/Users/liuchangyong/Desktop/IdeaProjects/FCH-FEIP-APIP/APIP-Server/avatar/png";

        //get2(basePath, filePath);

        //gen58(basePath, filePath);

        getUs(basePath, filePath);
    }

    private static void getUs(String basePath, String filePath) throws IOException {
        String[] addrs = new String[16];
        addrs[0] = "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX";
        addrs[1] = "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv";
        addrs[2] = "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK";
        addrs[3] = "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW";
        addrs[4] = "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U";
        addrs[5] = "FHbcD4nsJwncGytZLw6Z5SWAVJFJA88iM4";
        addrs[6] = "FJFXErHx3H3K6zBJuVA1Ni2s9sycdYmRFe";
        addrs[7] = "FC6hGnRNGSCzcWCF7gwGgLPbeurpuKwmmw";
        addrs[8] = "FN5Q5xchybpJFy4BY4BhSvXHgYxp9qSuQP";
        addrs[9] = "F9FSC3i6BJ9yALbBNRdb4UT13aL1U5rRbH";
        addrs[10] = "FDN3tsToURpGULc6xpap8MXiZkpMfmwang";
        addrs[11] = "FJYN3D7x4yiLF692WUAe7Vfo2nQpYDNrC7";
        addrs[12] = "F8bpsJtNwTvDZuYxjrYhYrtHZx356y3mZZ";
        addrs[13] = "F76LCCRLkX4HEQM8Te6Uo76WWZxENHtvLS";
        addrs[14] = "FLx88wdsbLQyZRmbqtpeXA9u5FG9EyCash";
        addrs[15] = "FG1A35dQd9V5pvJy3fuEEFxbTnroiRCash";




        avatar.AvatarMaker.getAvatars(addrs, basePath, filePath);
    }
    private static void get2(String basePath, String filePath) throws IOException {
        String[]  addrs = new String[]{"FkkkkkkkkkkkkkkkkkkkkkkkkZkkkkkkkkkk","Fkkkkkkkkkkkkkkkkkkkkkkkhzkkkkkkkkkk"};
        avatar.AvatarMaker.getAvatars(addrs, basePath, filePath);
    }

    private static void gen58(String basePath, String filePath) throws IOException {
        Set<String> set = AvatarMaker.data.keySet();
        String[] chars = new String[set.size()];
        System.out.println(set.size());
        int i=0;
        for(String str: set){
            chars[i]=str;
            i++;
        }
        String[] addrs = new String[chars.length];
        System.out.println(addrs.length);
//        for(int j=33;j<chars.length;j++){
        int m = 10;
        for(int j=0;j<chars.length;j++){
                addrs[j]= "F"+m+"kkkkkkkkkkkkkkkkkkkkkk"+chars[j]+"kkkkkkkkk";
                m++;
                System.out.println(addrs[j]);
        }
//        String[] addr1 = new String[25];
//        System.arraycopy(addrs,33,addr1,0,25);
        AvatarMaker.getAvatars(addrs, basePath, filePath);
    }
}
