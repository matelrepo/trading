package io.matel.app.tools;

import java.text.SimpleDateFormat;

public class  Utils {

//    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss");
    public static SimpleDateFormat formatter = new SimpleDateFormat("d/M/yyyy HH:mm:ss");


    public static double round(double value, int decimals) {
        double base = Math.pow(10, decimals);
        return Math.round(value * base) / base;
    }
}
