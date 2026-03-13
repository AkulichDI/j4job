package converter;

import java.util.List;

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
    private final String path;
    private final String botAnswers;

    public ConsoleChat(String path, String botAnswers) {
        this.path = path;
        this.botAnswers = botAnswers;
    }


    public void run() {


    }

    private List<String> readPhrases() {
        return null;
    }

    private void saveLog(List<String> log) {


    }

    public static void main(String[] args) {
        ConsoleChat consoleChat = new ConsoleChat("", "");
        consoleChat.run();
    }



}
