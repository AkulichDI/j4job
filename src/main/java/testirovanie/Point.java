package testirovanie;
// 3 задание

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Point {
    private int x;
    private int y;
    private int z;

    public Point(int first, int second) {
        this.x = first;
        this.y = second;
    }


    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void info() {

        System.out.println("Point[" + this.x + ", " + this.y + "]");

    }

    public double distance(Point that) {

        return sqrt(pow(this.x - that.x, 2) + pow(this.y - that.y, 2));
    }
    public double distance3d(Point that) {
        return sqrt(pow(this.x - that.x, 2) + pow(this.y - that.y, 2) + pow(this.z - that.z, 2));
    }


    public static void main(String[] args) {
        /*Point a = new Point(25, 0);
        Point b = new Point(0, 30);
        System.out.println(a.distance(b));
        a.info();
        Point point1 = new Point(1, 2, 3);
        Point point2 = new Point(4, 6, 8);
        System.out.println("3D distance: " + point1.distance3d(point2));
    */}
}