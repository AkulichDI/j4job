package converter;

public class Converter {


    public static float rubleToEuro(float value) {
        return value / 70;
    }


    public static float rubleToDollar(float value) {
        return value / 60;

    }

    public static void main(String[] args) {
        float input = 110;
        float expected = 1;
        float output = Converter.rubleToEuro(input);
        float output1 = Converter.rubleToDollar(input);
        System.out.println(output1);
        boolean passed = expected == output;
        boolean passed1 = expected == output1;/*
        System.out.println("140 rubles are 2. Test result : " + passed);
        System.out.println("110 rubles are 1. Test result : " + passed1);
        */

        System.out.printf("%.15f%n", 23.123f);
    }
}
