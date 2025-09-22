package org.example.importclass;

public class Math {
    public static double sum(double first, double second) {
        return first + second;
    }
    public static double multiply(double first, double second) {
        return first * second;
    }
    public static double summl(double first, double second) {
        return sum(first, second) + multiply(first, second) / first + second ;
    }

    public static double sub (double first, double second){
        return first - second;
    }

}
