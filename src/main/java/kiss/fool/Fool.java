package kiss.fool;

import java.util.Scanner;

public class Fool {

    static String answer(int number) {
        if (number % 15 == 0) {
            return "FizzBuzz";
        }
        if (number % 3 == 0) {
            return "Fizz";
        }
        if (number % 5 == 0) {
            return "Buzz";
        }
        return String.valueOf(number);
    }

    public static void main(String[] args) {
        System.out.println("Игра FizzBuzz.");

        int startAt = 1;
        Scanner input = new Scanner(System.in);

        while (startAt < 100) {
            System.out.println(answer(startAt));
            startAt++;

            String userAnswer = input.nextLine();

            if (!answer(startAt).equals(userAnswer)) {
                System.out.println("Ошибка. Начинай снова.");
                startAt = 1;
                continue;
            }

            startAt++;
        }
    }
}