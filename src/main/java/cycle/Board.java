package cycle;

public class Board {

    public static void paint(int width, int height) {
        for (int i = 0; i<=height; i++ ) {
            for (int j = 0; j<= width; j++ ) {
                /* условие проверки, что писать пробел или X */
                /* Выше в задании мы определили закономерность, когда нужно проставлять X */
                if ((j + i)  % 2== 0) {
                    System.out.print("X");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        paint(3, 3);
        System.out.println();
        paint(4, 4);
    }

}
