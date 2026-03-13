package regex;

import javax.xml.transform.Source;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExample {


    public static void main(String[] args) {

       /* Pattern pattern = Pattern.compile("Я учусь на Job4J");

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

        Pattern pattern = Pattern.compile("Job4j");
        Matcher matcher = pattern.matcher("Job4j1 и Job4j2 и Job4j3");

        while (matcher.find()) {
            System.out.println("Совпадение: " + matcher.group());
            System.out.println("start = " + matcher.start());
            System.out.println("end = " + matcher.end());
        }




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
