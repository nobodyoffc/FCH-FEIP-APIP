package reward;

import static constants.Strings.REWARD;

public class RewardData {
    private String rewardId;
    private String op = REWARD;

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
