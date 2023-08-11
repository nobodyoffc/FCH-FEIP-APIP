import fcTools.ParseTools;
import reward.RewardInfo;
import reward.RewardState;

import java.util.HashMap;
import java.util.Map;

public class Tester {
    public static void main(String[] args) {
//        RewardInfo rewardInfo = new RewardInfo();
//        rewardInfo.setRewardId("eifls");
//        rewardInfo.setState(RewardState.unpaid);
//        ParseTools.gsonPrint(rewardInfo);

        Map<String, Long> testMap = new HashMap<>();
        testMap.put("1",134l);
        System.out.println(testMap.get("2"));
    }
}
