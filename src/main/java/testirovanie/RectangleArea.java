package testirovanie;

public class RectangleArea {
    public static double square(double p, double k) {
        double h = p / (2 * (k + 1));
        double l = h * k;
        double result = l * h;
        return result;
    }

    public static void main(String[] args) {
        double result = org.example.ru.job4j.calculator.RectangleArea.square(-2, -1);
        System.out.println(" p = 4, k = 1, s = 1, real = " + result);
    }
}
