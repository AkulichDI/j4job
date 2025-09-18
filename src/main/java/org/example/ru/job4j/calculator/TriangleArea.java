package org.example.ru.job4j.calculator;

public class TriangleArea {
    public static double area(double a, double b, double c) {
        double p = (a + b + c) / 2 ;


        return Math.sqrt ( p*(p-a)*(p-b)*(p-c) );
    }

    public static void main(String[] args) {
        double result = TriangleArea.area(2, 2, 2);
        System.out.println("area (2, 2, 2) = " + result);
    }

}
