package reward;

import fcTools.DataForOffLineTx;
import fchClass.Cash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Payroll {

    RewardInfo rewardInfo;
    String account;
    DataForOffLineTx dataForOffLineTx;
    List<Cash> meetCashList;

    public Payroll(String account,RewardInfo rewardInfo) {
        this.rewardInfo = rewardInfo;
        this.account = account;
    }

    public String makePayrollJson(){

        return null;
    }


}
