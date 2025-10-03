package ru.job4j.pojo;

public class Library {

    public static void main(String[] args) {
        Book test1 = new Book("Clean code", 120);
        Book test2 = new Book("java mdl", 190);
        Book test3 = new Book("java sn", 1000);
        Book test4 = new Book("java GOD", 10000);
        Book [] books = new Book[4];
        books[0] = test1;
        books[1] = test2;
        books[2] = test3;
        books[3] = test4;

        for (int i = 0; i < books.length; i++) {

            Book book = books[i];
            System.out.printf("Наименование книги: %-20s Количество страниц: %d%n",
                    book.getName(), book.getCount());
        }

        System.out.println("Перестановка 0 и 3 ");

        Book tmp = books[3];
        books[3] = books[0];
        books[0] = tmp;

        for (int i = 0; i < books.length; i++) {

            Book book = books[i];
            System.out.println("Наименование книги: "  +  book.getName()+ "\t  Количество страниц: " + book.getCount());

        }

        System.out.println("Поиск книги по названию");
        for (int i = 0; i < books.length; i++) {

            Book book = books[i];
            if (book.getName().equals("Clean code")){
                System.out.println("Наименование книги: "  +  book.getName()+ "\t  Количество страниц: " + book.getCount());
            }
        }

    }

}
