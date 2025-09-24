package array;
//6.7.2
public class MatrixCheck {

    public static boolean monoHorizontal(char[][] board, int row) {
        boolean result = true;
        for (int i = 0; i < board[row].length; i++) {
            char x = 'X';
            if (x != board[row][i]) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean monoVertical(char[][] board, int column) {
        boolean result = true;
        for (int i = 0; i < board.length; i++) {
            if (board[i][column] != 'X') {
                result = false;
                break;
            }
        }
        return result;
    }



    public static char[] extractDiagonal(char[][] board) {
        char[] result = new char[board.length];
        for (int i = 0; i < board.length ; i++) {

            result[i] = board[i][i];
        }
        return result;
    }



}
