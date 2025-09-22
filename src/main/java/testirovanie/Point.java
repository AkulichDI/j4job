package testirovanie;
// 3 задание
public class Point {
    public static double distance(int x1, int y1, int x2, int y2) {
        double x21 = Math.pow( x2 - x1 , 2 );
        double y21 = Math.pow ( y2 - y1, 2 );
        return  Math.sqrt ( x21 + y21);

    }

    public static void main(String[] args) {
        double result = Point.distance(0, 0, 2, 0);
        System.out.println("result (0, 0) to (2, 0) " + result);
    }
}
