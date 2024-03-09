import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BackupToMultiSign {

    public static void main(String[] args) {
        System.out.println(calculateCombination(16,15));
        doIt(7,4,32);
    }

    public static void doIt(int n, int m, int length){
        System.out.println("n="+n);
        System.out.println("m="+m);

        char[] msg = new char[length];
        for(int i=0;i<length;i++){
            msg[i]= (char)(48+i);
        }
        System.out.println("msg = "+Arrays.toString(msg));
        System.out.println("msg length = "+length);

        long group = calculateCombination(n,m-1);
        System.out.println("group = "+group);
        if(length<group) {
            System.out.println("Length has to be larger than groups.");
            return;
        }

        int copy = n-(m-1);
        System.out.println("copy="+copy);

        long remain = length % group;
        long perGroup = remain==0?length/group:length/group+1;

        System.out.println("perGroup="+perGroup);

        long perPerson = (group*copy)%n==0?(group*copy)/n:(group*copy)/n+1;
        System.out.println("perPerson="+perPerson);

        char[][] groups = makeGroups(msg, group, remain, perGroup);
        System.out.println(groups.length+" groups. "+groups[0].length+" elements per group.");

        char[][][] copyGroups = new char[copy][groups.length][groups[0].length];

        for(int i=0;i<copy;i++){
            char[][] newGroups = new char[groups.length][groups[0].length];
            for(int j=0;j<groups.length;j++){
                for(int k=0;k<groups[0].length;k++){
                    newGroups[j][k]=groups[j][k];
                }
                copyGroups[i]=newGroups;
            }
        }

        char[][][] result =
                new char[n]
                        [(int) perPerson]
                        [(int) perGroup];

    }

    public static long factorial(int number) {
        long result = 1;
        for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }
        return result;
    }

    // Method to calculate combinations (n choose k)
    public static long calculateCombination(int n, int k) {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }


    @NotNull
    private static char[][] makeGroups(char[] msg, long group, long remain, long perGroup) {
        char[][] groups = new char[(int) group][(int) perGroup];
        int i=0;
        int j=0;
        long remainCount = remain;
        for(char c : msg){
            groups[i][j]=c;
            j++;
            if(remain ==0){
                if(j== perGroup) {
                    System.out.println(Arrays.toString(groups[i]));
                    i++;
                    j=0;
                }
            }else {
                if(remainCount>0) {
                    if (j == perGroup) {
                        System.out.println(Arrays.toString(groups[i]));
                        i++;
                        j = 0;
                        remainCount--;
                    }
                }else{
                    if (j == perGroup -1) {
                        System.out.println(Arrays.toString(groups[i]));
                        i++;
                        j = 0;
                    }
                }
            }
        }
        return groups;
    }
}
