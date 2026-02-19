package lambda;


import java.util.Iterator;
import java.util.NoSuchElementException;

    public class MatrixIterator implements Iterator<Integer> {
        private final int[][] data;
        private int row = 0;
        private int col = 0;

        public MatrixIterator(int[][] data) {
            this.data = data;
        }

        // Сдвигаем row/col к ближайшей "валидной" позиции
        private void shiftToNext() {
            while (row < data.length && col >= data[row].length) {
                row++;
                col = 0;
            }
        }

        @Override
        public boolean hasNext() {
            shiftToNext();
            return row < data.length && col < data[row].length;
        }

        @Override
        public Integer next() {
            if (!hasNext()) { // по контракту next() должен падать, если элементов нет
                throw new NoSuchElementException();
            }
            return data[row][col++]; // берём текущий и сдвигаем столбец
        }


}

 class Demo {
    public static void main(String[] args) {
        int[][] m = {
                {1, 2},
                {},
                {3, 4, 5}
        };

        Iterator<Integer> it = new MatrixIterator(m);
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }

    }
}
