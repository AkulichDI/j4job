package org.example.ru.job4j.calculator;

import org.example.importclass.Math;


public class Importclasstest {

    public static double sumAndMultiply(double first, double second) {
        return Math.sum(first, second) + Math.multiply(first, second);
    }

    public static void main(String[] args) {
        System.out.println("Результат расчета равен: " + sumAndMultiply(10, 20));
    }



}
