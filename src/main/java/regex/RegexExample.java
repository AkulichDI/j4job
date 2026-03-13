package regex;

import javax.xml.transform.Source;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExample {


    public static void main(String[] args) {

        Pattern pattern1 = Pattern.compile("\\S{1,}@\\S{1,}\\.\\S{1,}");

        String text1 = "peter-2022@example.com example65@mail_box.ru 123_45@example-mailbox.com";

        Matcher matcher1 = pattern1.matcher(text1);

        while (matcher1.find()) {

            System.out.println("Найдено совпадение: " + matcher1.group());

        }


        System.out.println();

        Pattern pattern = Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{4}\\b");

        String text = "24.04.1987 11.11.111111 99.99.99991 99.99.9999 99999999 0000.00.00";
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {

            System.out.println("Найдено совпадение: " + matcher.group());

        }

/*
        Pattern pattern = Pattern.compile("11");
        String text = "111111";

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            System.out.println("Найдено совпадение: " + matcher.group());
        }







        Pattern pattern = Pattern.compile("Я учусь на Job4J");

        String textOne = "Я учусь на Job4J";
        Matcher matcherOne = pattern.matcher(textOne);
        boolean isPresentOne = matcherOne.matches();

        System.out.println(isPresentOne);

        String textTwo = "Я учусь";
        Matcher matcherTwo = pattern.matcher(textTwo);
        boolean isPresentTwo = matcherTwo.matches();
        System.out.println(isPresentTwo);


        System.out.println("-");
*/

     /*   Pattern pattern = Pattern.compile("Job4j");
        Matcher matcher = pattern.matcher("Job4j1 и Job4j2 и Job4j3");

        while (matcher.find()) {
            System.out.println("Совпадение: " + matcher.group());
            System.out.println("start = " + matcher.start());
            System.out.println("end = " + matcher.end());
        }
*/



/*
        Pattern pattern = Pattern.compile("Job4j");


        String textOne = "Job4j";
        Matcher matcherOne = pattern.matcher(textOne);
        boolean isPresentOne = matcherOne.matches();
        System.out.println(isPresentOne);



        String textTwo = "job4j";
        Matcher matcherTwo = pattern.matcher(textTwo);
        boolean isPresentTwo = matcherTwo.matches();
        System.out.println(isPresentTwo);
*/
    }


}
