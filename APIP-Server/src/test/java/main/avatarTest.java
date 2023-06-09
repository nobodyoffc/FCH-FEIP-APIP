package main;

import java.io.IOException;

public class avatarTest {
    public static void main(String[] args) throws IOException {
        String[] addrs = new String[6];
        addrs[0] = "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX";
        addrs[1] = "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv";
        addrs[2] = "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK";
        addrs[3] = "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW";
        addrs[4] = "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U";
        addrs[5] = "FHbcD4nsJwncGytZLw6Z5SWAVJFJA88iM4";

        String basePath = "/Users/liuchangyong/Desktop/eclipse-web-workspace/APIP-server/avatar/elements/";
        String filePath = "/Users/liuchangyong/Desktop/eclipse-web-workspace/APIP-server/avatar/png/";

        APIP17V1_Avatar.AvatarMaker.getAvatars(addrs,basePath,filePath);

    }
}
