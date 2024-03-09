package main;
import fcTools.ParseTools;

import static javaTools.NumberTools.numberToPlainString;

public class BitcoinDifficulty {

    public static void main(String[] args) {
        long bits = 436240696L; // Your "bits" value
        double difficulty = ParseTools.bitsToDifficulty(bits);
        System.out.println("Difficulty: " + difficulty);
        double hashRate = ParseTools.difficultyToHashRate(difficulty);
        System.out.println("Hash power: " + hashRate);

        System.out.println(numberToPlainString(String.valueOf(difficulty),null));
        System.out.println(numberToPlainString(String.valueOf(hashRate),null));
        System.out.println(numberToPlainString(String.valueOf(hashRate/1000000000),null));
        System.out.println(numberToPlainString(String.valueOf(hashRate/1000000000),"12"));
        System.out.println(numberToPlainString(String.valueOf(hashRate/10000000),"3"));
    }

}


