package Threads.hw;
import java.io.*;

/**
 * Многопоточность/immutable: избавься от get/set, передавай File в конструктор; сделай класс immutable (все поля final, класс final, без сеттеров). Тогда проблема видимости file исчезает сама — почему? (ответь заодно).
 * IO — незакрытые ресурсы: потоки нигде не закрываются → утечка. Примени try-with-resources.
 * IO — без буфера: чтение/запись по байту — медленно; оберни в BufferedInputStream/BufferedReader (или буферизованный вывод).
 * Баг чтения: while ((data = input.read()) > 0) — почему это неверное условие конца файла? Что реально возвращает read() в конце и что будет на символе с кодом 0? Как правильно?
 * Копипаст → стратегия: getContent() и getContentWithoutUnicode() почти одинаковы. Слей в один метод content(Predicate<Character> filter), а разницу вынеси в фильтр (c -> true и c -> c < 0x80).
 * Не обязательно сразу всё идеально — пришли попытку, разберём по пунктам. Если объём великоват для одного захода — начни с пунктов 1 и 4 (они самые «по теме урока»), остальное добьём следующим шагом.
 *
 * Что скажешь по пункту 1 (почему immutable убивает проблему видимости) и по пункту 4 (баг с read() > 0)? Можешь ответить словами до кода.
 */





public final class ParseFile {
    private final File file;
    public ParseFile(File file) {
        this.file = file;
    }

    public  String getContent() throws IOException {
        try(BufferedReader input = new BufferedReader(new FileReader( file ))) {
            StringBuilder output = new StringBuilder();
            int data;
            while ((data = input.read()) > -1) {
                output.append ((char) data);
            }
            return output.toString();
        }

    }

    public  String getContentWithoutUnicode() throws IOException {
        try(BufferedReader input = new BufferedReader(new FileReader( file ))){
            StringBuilder output = new StringBuilder();
            int data;
            while ((data = input.read()) > -1) {
                if (data < 0x80) {
                    output.append(( char) data);
                }
            }
            return output.toString();

        }
    }

    public  void saveContent(String content) throws IOException {

        try(BufferedWriter o = new  BufferedWriter(new FileWriter( file ))) {

            for (int i = 0; i < content.length(); i++) {
                o.write(content.charAt(i));
            }

        }

    }


    public static void main(String[] args) {

        ParseFile file = new ParseFile(new File("C:\\Users\\Kiosk\\Desktop\\main.txt"));


        try{
            System.out.println(file.getContent());
            System.out.println(file.getContentWithoutUnicode());
        }catch(IOException e){
            e.printStackTrace();
        }

    }








}