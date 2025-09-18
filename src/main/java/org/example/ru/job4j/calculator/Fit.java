package org.example.ru.job4j.calculator;

public class Fit {

    public static double manWeight(short height) {
        double result = (height- 100) * 1.15;
        return result;
    }

    public static double womanWeight(short height) {
        double result = (height -100) * 1.15;
        return result;
    }

    public static void main(String[] args) {
        short height = 187;
        double man = Fit.manWeight(height);
        System.out.println("Man 187 is " + man);
    }

}