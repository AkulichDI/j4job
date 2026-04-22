package kiss.fool;

import java.util.Scanner;

public class Fool {

    public static void main(String[] args) {
        8
        System.out.println("Игра FizzBuzz.");
        9
        var startAt = 1;
        10
        var input = new Scanner(System.in);
        11
        while (startAt < 100) {
            12
            if (startAt % 3 == 0 && startAt % 5 == 0) {
                13
                System.out.println("FizzBuzz");
                14
            } else if (startAt % 3 == 0) {
                15
                System.out.println("Fizz");
                16
            } else if (startAt % 5 == 0) {
                17
                System.out.println("Buzz");
                18
            } else {
                19
                System.out.println(startAt);
                20
            }
            21
            startAt++;
            22
            var answer = input.nextLine();
            23
            if (startAt % 3 == 0 && startAt % 5 == 0) {
                24
                if (!"FizzBuzz".equals(answer)) {
                    25
                    System.out.println("Ошибка. Начинай снова.");
                    26
                    startAt = 0;
                    27
                }
                28
            } else if (startAt % 3 == 0) {
                29
                if (!"Fizz".equals(answer)) {
                    30
                    System.out.println("Ошибка. Начинай снова.");
                    31
                    startAt = 0;
                    32
                }
                33
            } else if (startAt % 5 == 0) {
                34
                if (!"Buzz".equals(answer)) {
                    35
                    System.out.println("Ошибка. Начинай снова.");
                    36
                    startAt = 0;
                    37
                }
                38
            } else {
                39
                if (!String.valueOf(startAt).equals(answer)) {
                    40
                    System.out.println("Ошибка. Начинай снова.");
                    41
                    startAt = 0;
                    42
                }
                43
            }
            44
            startAt++;
            45
        }
        46
    }



}
