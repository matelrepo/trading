package io.matel.app.tools;

public class  Utils {

    public static double round(double value, int decimals) {
        double base = Math.pow(10, decimals);
        return Math.round(value * base) / base;
    }
}
