package converter;

import javax.xml.crypto.Data;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ConsoleChat {

    /**
     * Задание.
     *
     * 1. В этом задании необходимо создать программу 'Консольный чат'. Некоторое описание:
     *
     * - пользователь вводит слово-фразу, программа берет случайную фразу из текстового файла и выводит в ответ.
     * - программа замолкает если пользователь вводит слово «стоп», при этом он может продолжать отправлять сообщения в чат.
     * - если пользователь вводит слово «продолжить», программа снова начинает отвечать.
     * - при вводе слова «закончить» программа прекращает работу.
     * - запись диалога, включая слова-команды стоп/продолжить/закончить должны быть записаны в текстовый лог.
     * - текстовые файлы с ответами бота и лог размещайте в ранее созданном каталоге data в корне проекта.
     *
     *Т.е. класс принимает в конструктор 2 параметра - имя файла, в который будет записан весь диалог между ботом и пользователем, и имя файла в котором находятся строки с ответами, которые будет использовать бот. Вам нужно реализовать методы:
     *
     *  - run(), содержит логику чата;
     *  - readPhrases(), читает фразы из файла;
     *  - saveLog(), сохраняет лог чата в файл.
     *
     * Небольшая рекомендация:
     * Так делать не надо:
     *  while (true) { - консольный чат должен явно выходить из цикла, не делайте вечный цикл.
     *
     */




    private static final String OUT = "закончить";
    private static final String STOP = "стоп";
    private static final String CONTINUE = "продолжить";
    private static final String MENU = "меню";
    private final String path;
    private final String botAnswers;

    public ConsoleChat(String path, String botAnswers) {
        this.path = path;
        this.botAnswers = botAnswers;
    }


    public void run() {

        List<String> phrases = readPhrases();
        List<String> log = new ArrayList<>();
        boolean botActive = true;
        Random random = new Random();
        String userInput = "";
        System.out.println("Привет! Я Олех \nДавайте познакомимся!");
        System.out.println("Чтобы ознакомиться с функционалом напиши \"меню\"");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            while ( !OUT.equals(userInput)){

                userInput = reader.readLine();
                log.add("Пользователь: " + userInput + " Дата: " + LocalDateTime.now());

                if (STOP.equals(userInput)){
                    botActive = false;
                } else if (CONTINUE.equals(userInput)) {
                    botActive = true;
                } else if (OUT.equals(userInput)) {
                    break;
                } else if (MENU.equals(userInput)) {
                    if ( botActive) {
                        System.out.println("Меню:" + "\n" + "1. Стоп" + "\n" + "2. Продолжить" + "\n" + "3. Закончить");
                        log.add("Меню:" + "\n" + "1. Стоп" + "\n" + "2. Продолжить" + "\n" + "3. Закончить");
                    }
                } else if (botActive) {
                    if (botActive) {
                        String answer = phrases.get(random.nextInt(0, phrases.size() - 1));
                        System.out.println(answer);
                        log.add("Бот: " + answer + " Дата: " + LocalDateTime.now());
                        saveLog(log);
                    }
                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private List<String> readPhrases() {


        List<String> result = new ArrayList<>();

        try(BufferedReader read = new BufferedReader(new FileReader(this.botAnswers, StandardCharsets.UTF_8 ))){

            String line;
            while ( (line = read.readLine()) != null ){
                ValidatorLines.validateLine(line);
                result.add(line);
            }
        }catch (IOException e ){
            e.printStackTrace();
        }

        return result;
    }

    private void saveLog(List<String> log) {

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.path, StandardCharsets.UTF_8, true)))){
            for ( String line : log ){
                out.println(line);
            }

        }catch ( IOException e ){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ConsoleChat consoleChat = new ConsoleChat("./data/logFileChat.txt", "./data/dataResult.txt");
        consoleChat.run();
    }



}
