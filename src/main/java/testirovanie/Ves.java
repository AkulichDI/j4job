package testirovanie;
// 2 задание
public class Ves {
    public static double manWeight(short height) {
        double result = (height- 100) * 1.15;
        return result;
    }

    public static double womanWeight(short height) {
        double result = (height -100) * 1.15;;
        return result;
    }

    public static void main(String[] args) {
        short height = 187;
        double man = Ves.manWeight(height);
        System.out.println("Man 187 is " + man);
    }
}
