package fcTools;

import org.bitcoinj.core.VarInt;


public class TxTool {

    public static void main(String[] args) {
        System.out.println("0:"+VarInt.sizeOf(0));
        System.out.println("1:"+VarInt.sizeOf(1));
        System.out.println("253:"+VarInt.sizeOf(253));
        System.out.println("65536:"+VarInt.sizeOf(65536));
        System.out.println("4294967296:"+VarInt.sizeOf(4294967296L));


        long inputNum=1;
        long outputNum=1;
        long opLen=4;
        long length = 10 + (long) 141 * inputNum + (long) 34 * (outputNum + 1) + (opLen + VarInt.sizeOf(opLen) + 1 + VarInt.sizeOf(opLen + VarInt.sizeOf(opLen) + 1) + 8);
        System.out.println("4:"+length);


        System.out.println(calcFee(1,1,4));
    }

    public static long calcFee(int inputNum, int outputNum, int opLen) {
        long priceInSatoshi =1;
        long length = 0 ;
        if(opLen==0) {
            length = 10+ 141 * (long)inputNum + (long) 34 *(outputNum+1);
        }else{
            length= 10+ (long)141*inputNum + (long) 34 *(outputNum+1)+ (opLen+VarInt.sizeOf(opLen)+1+VarInt.sizeOf(opLen+VarInt.sizeOf(opLen)+1)+8);
        }
        long fee = priceInSatoshi*length;
        return fee;
    }
}
