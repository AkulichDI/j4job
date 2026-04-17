package gc.cache;

import java.util.Scanner;

public class Emulator {

    private static DirFileCache cache;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {

            System.out.println("1. Указать кэшируемую директорию");
            System.out.println("2. Загрузить содержимое файла в кэш");
            System.out.println("3. Получить содержимое файла из кэша");
            System.out.println("4. Выход");
            System.out.print("Выберите опцию: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                    case 1:
                    System.out.print("Введите путь к кэшируемой директории: ");
                    String dir = scanner.nextLine();
                    cache = new DirFileCache(dir);
                    break;
                    case 2:
                        System.out.print("Введите имя файла для загрузки в кэш: ");
                        String fileToLoad = scanner.nextLine();
                        if (cache != null) {
                            cache.get(fileToLoad);
                            System.out.println("Файл загружен в кэш.");
                        } else {
                            System.out.println("Сначала укажите кэшируемую директорию.");
                        }
                        break;
                    case 3:
                        System.out.print("Введите имя файла для получения из кэша: ");
                        String fileToGet = scanner.nextLine();
                        if (cache != null) {
                            String content = cache.get(fileToGet);
                            System.out.println("Содержимое файла: "  +  content);
                        } else {
                            System.out.println("Сначала укажите кэшируемую директорию.");
                        }
                        break;
                    case 4:
                        running = false;
                        break;
                    default:
                        System.out.println("Неверный выбор, попробуйте снова.");

        }

        }




    }





}
