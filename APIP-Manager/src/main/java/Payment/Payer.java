package Payment;
/*
    - 每次在ES中找到最新的一次该sid的payroll发放记录
    - 在ES备份总支付
    - 获取该记录之后的所有order收入——incomeT
    - 从payParams获取分配参数计算得到payroll
    - 生成payroll表存入tomcat网页
    - 人工获取发放表，发出payroll交易，opreturn标注sid，payroll
 */
public class Payer {

    public void pay(){
        makeIncomeTotal();
        makePayroll();
        sendPayroll();
        backUpPayment();
    }

    private void makeIncomeTotal() {
//        checkLastPay();
//        calcIncome();
    }

    private void makePayroll() {
    }

    private void sendPayroll() {
    }

    private void backUpPayment() {
    }
}
