package javaTools;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberTools {
    public static double roundDouble8(double raw){
        BigDecimal bd = new BigDecimal(raw);
        bd = bd.setScale(8, RoundingMode.FLOOR); // Choose the rounding mode as needed
        return bd.doubleValue();
    }

    public static boolean isInt(String numberStr) {
        try{
            Integer.parseInt(numberStr);
        }catch (Exception ignore){
            return false;
        }
        return true;
    }

    public static boolean isBoolean(String boolStr, boolean strictly) {
        if(strictly) {
            return boolStr.equals("true") || boolStr.equals("false");
        }

        try{
            Boolean.parseBoolean(boolStr);
        }catch (Exception ignore){
            return false;
        }
        return true;
    }

    @Test
    public void test(){
        double a = 1.23;
        double b = 0.12322000;
        double c = 0.0;
        String d = "1.23";
        System.out.println(getDecimalPlaces(a));
        System.out.println(getDecimalPlaces(b));
        System.out.println(getDecimalPlaces(c));

        String t1 = "True";
        String t2 = "False";
        String t3 = "trUe";
        System.out.println(isBoolean(t1,true));
        System.out.println(isBoolean(t1,false));
        System.out.println(isBoolean(t2,false));
        System.out.println(isBoolean(t3,false));


    }
    public static int getDecimalPlaces(double number) {
        if (number == (long) number) {
            // The number has no significant decimal places
            return 0;
        } else {
            String numberAsString = Double.toString(Math.abs(number));
            // Remove the integer part and the decimal point
            String decimalPart = numberAsString.substring(numberAsString.indexOf('.') + 1);
            // Remove trailing zeros
            String significantDecimalPart = decimalPart.replaceAll("0*$", "");
            return significantDecimalPart.length();
        }
    }

    public static double roundDouble16(double raw){
        BigDecimal bd = new BigDecimal(raw);
        bd = bd.setScale(16, RoundingMode.FLOOR); // Choose the rounding mode as needed
        return bd.doubleValue();
    }

    public static double roundDouble4(double raw){
        BigDecimal bd = new BigDecimal(raw);
        bd = bd.setScale(4, RoundingMode.FLOOR); // Choose the rounding mode as needed
        return bd.doubleValue();
    }

    public static double roundDouble(double number,int decimal, RoundingMode mode){
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(decimal, mode); // Choose the rounding mode as needed
        return bd.doubleValue();
    }

    public static String numberToPlainString(String number,String deci){
        BigDecimal bigDecimal = new BigDecimal(number);

        // Get a NumberFormat instance for formatting numbers with commas
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);

        // Set the maximum number of fraction digits to avoid unnecessary decimal places
        // This is important if your BigDecimal value has non-zero fraction part
        if(deci!=null)formatter.setMaximumFractionDigits(Integer.parseInt(deci));

        // Format the BigDecimal number with commas
        return formatter.format(bigDecimal);
    }
}
