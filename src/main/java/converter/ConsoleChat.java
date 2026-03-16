package converter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        System.out.println("Привет! Я Олех");
        System.out.println("Давайте познакомимся!");
        System.out.println("Чтобы ознакомиться с функционалом напиши \"меню\"");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            while (!OUT.equals(userInput)) {
                userInput = reader.readLine();
                log.add("Пользователь: " + userInput + " Дата: " + LocalDateTime.now());

                if (STOP.equals(userInput)) {
                    botActive = false;
                } else if (CONTINUE.equals(userInput)) {
                    botActive = true;
                } else if (OUT.equals(userInput)) {
                    break;
                } else if (MENU.equals(userInput)) {
                    String menu = "Меню:\n1. Стоп\n2. Продолжить\n3. Закончить";
                    System.out.println(menu);
                    log.add(menu);
                } else if (botActive) {
                    String answer = phrases.get(random.nextInt(phrases.size()));
                    System.out.println(answer);
                    log.add("Бот: " + answer + " Дата: " + LocalDateTime.now());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveLog(log);
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
        try (PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(this.path, StandardCharsets.UTF_8, false)))) {
            for (String line : log) {
                out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConsoleChat consoleChat = new ConsoleChat("./data/logFileChat.txt", "./data/dataResult.txt");
        consoleChat.run();
    }



}
