import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class FileReader {

    public static void main(String[] args) throws IOException {


/*        byte[] file  = Files.readAllBytes(Path.of("pom.xml"));
        String cont = new String(file, StandardCharsets.UTF_8);



        System.out.println(cont);
         final List<String> results = new ArrayList<>();
        Optional<String> optionalValue = evaluate(50);
        final Optional<Boolean> added1 = optionalValue.map(results::add);
        optionalValue.ifPresent(results::add);
        optionalValue.map(results::add);

        optionalValue = evaluate(101);


        optionalValue.ifPresent(results::add);
        optionalValue.map(results::add);

        final Optional<Boolean> added = optionalValue.map(results::add);
        System.out.println(added);
        System.out.println(added1);
        System.out.println(results);*/

        String txt = "Объясни понятным языком основные концепции [выбранной темы в программировании], включая следующие аспекты:\n" +
                    "Определение и важность темы.\n" +
                    "Основные термины и понятия.\n" +
                    "Историческое развитие и применение.\n" +
                    "Пошаговое объяснение ключевых концепций с примерами кода.\n" +
                    "Практическое применение и реальные примеры использования.\n" +
                    "Возможные сложности и способы их преодоления.\n" +
                    "Рекомендации по дальнейшему изучению и полезным ресурсам.\n" +
                    "Используй простые и доступные слова, избегай сложных технических терминов без объяснений. Структурируй информацию с помощью подзаголовков и списков для лучшего восприятия. Приведи конкретные примеры кода, чтобы проиллюстрировать теоретические концепции.\n" +
                    "Если требуется, добавь визуальные элементы (схемы, диаграммы) для наглядности. Учти, что аудитория может иметь разный уровень подготовки, поэтому старайся делать объяснения универсальными и понятными для всех.";

        Optional<String> dateTest = Optional.of(txt);


        Optional<String> data = Optional.ofNullable(null);
        if ( data.isEmpty()){
            data = Optional.of("Hello");
            System.out.println(data.get());
        }else {
            System.out.println(data);
        }


    }
    public static Optional<String> evaluate(int i) {

        if (i > 100 ){
            return Optional.of("OK");
        }else{
            return Optional.ofNullable(null);
        }
    }








}


     class testim {
         public static void main(String[]args) {
/*
             String mercury = "Меркурий";
             String venus = "Венера";
             String earth = "Земля";
             String mars = "Марс";
             String jupiter = "Юпитер";
             String saturn = "Сатурн";
             String uranus = "Уран";
             String neptune = "Нептун";

             ArrayList<String> solarSystem = new ArrayList<>(Arrays.asList(neptune, venus, earth, mars
                     , jupiter, saturn, uranus, mercury));
             System.out.println(solarSystem);

             Collections.swap(solarSystem, solarSystem.indexOf(mercury), solarSystem.indexOf(neptune));
             System.out.println(solarSystem);



             ArrayList<String> solarSystemPart1 = new ArrayList<>(Arrays.asList(mercury, venus, earth, mars));
             ArrayList<String> solarSystemPart2 = new ArrayList<>(Arrays.asList(jupiter, saturn, uranus, neptune));

             System.out.println(Collections.disjoint(solarSystemPart1, solarSystemPart2));
 */




            /* Function<String, String> trim = String::trim;
             Function<String, Integer> length = String::length;

             Function<String, Integer> trimThenLen = trim.andThen(length);

             System.out.println(trimThenLen.apply("  java  ")); // 4

             Iterable<String> it = List.of("a", "b", "c");
             Iterator<String> iterator = it.iterator(); // ключевой мостик

             while (iterator.hasNext()){
                 String data = iterator.next();
                 System.out.println(data);
             }



             List<String> names = new ArrayList<>(List.of(" Ann ", " Petr ", " Alexander "," Data ", " Oleg   "));
             Iterator<String> it = names.iterator();

             List<String> name = new ArrayList<>();

             while (it.hasNext()) {
                 String s = it.next();
                 s = s.trim();

                 it.remove();

                 name.add(s);

             }
             System.out.println(name);
             System.out.println(names);
*/

/*
             Supplier<Long> now = System::currentTimeMillis;
             System.out.println(now.get());

             System.out.println(now.get());
*/




             List<String> names = new ArrayList<>(List.of(" Ann ", " Petr ", " Alexander "," Data ", " Oleg   "));
             Iterator<String> it = names.iterator();

             List<String> name = new ArrayList<>();
             names.replaceAll(String::trim);
             names.replaceAll(String::toUpperCase);
             System.out.println(names);

             UnaryOperator<String> unar = x -> x.substring(0, 1);

             Consumer<String> print = System.out::println;
             Function<String, String> upper = String::toUpperCase;

             List<String> dataa =  names.stream().map(unar).filter(x -> x.equals("A")).collect(Collectors.toList());

             System.out.println(dataa + " aaaaaaa");


             names.replaceAll(unar);
             System.out.println(names);


         }


    }

