package fc;

import javaTools.BytesTools;

public class isBase64 {

    public static void main(String[] args) {
        System.out.println(BytesTools.isBase64Encoded("Y2hhckFycmF5TmFtZQ==".toCharArray())); // true
        System.out.println(BytesTools.isBase64Encoded("Y2hhckFycmF5TmFtZQ".toCharArray()));  // true
        System.out.println(BytesTools.isBase64Encoded("Y2hhckFycmF5TmFtZQ===".toCharArray())); // false
        System.out.println(BytesTools.isBase64Encoded("Y2hhckFycmF5TmFtZQ==Q".toCharArray())); // false
        System.out.println(BytesTools.isBase64Encoded("Y2hhckFycmF5TmFtZQ&=".toCharArray())); // false
    }

}
