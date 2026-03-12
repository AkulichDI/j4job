package ru.job4j.condition;

public class Point {


    public static double distance ( int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

    }

    public static void main(String[] args) {

        double result = Point.distance(10,-20,2,0);
        System.out.println("result (0,0) to ( 2, 0 ) " + result);

    }




}
