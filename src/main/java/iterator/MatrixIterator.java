package iterator;

import java.util.*;

public class MatrixIterator implements Iterator<Integer> {

    private final int[][] data;
    private int row = 0, col = 0;

    public MatrixIterator(int[][] data) {
        this.data = data;
    }

    private void advance() {
        while (row < data.length && col >= data[row].length) {
            row++;
            col = 0;
        }
    }

    @Override
    public boolean hasNext() {
        advance(); // переходим к следующей непустой строке
        return row < data.length && col < data[row].length;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException(); // конец
        }
        return data[row][col++];
    }

}


 class DemoMatrixIterator {
    public static void main(String[] args) {
        int[][] matrix = {
                {1, 2},
                {},
                {3, 4, 5}
        };
        Iterator<Integer> it = new MatrixIterator(matrix);
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }

    }
}

 class DemoListIteratorEdit {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(List.of("A", "B", "C"));
        ListIterator<String> it = list.listIterator();

        it.next();                // вернул "A"
        String b = it.next();     // вернул "B"
        it.set(b.toLowerCase());  // заменяем "B" на "b"
        it.add("X");              // вставляем "X" перед "C"
        String c = it.next();     // возвращает "C"
        it.remove();              // удаляет "C"

        System.out.println(list);      // [A, b, X]
        System.out.println("Удалили: " + c);  // "C"
    }
 }