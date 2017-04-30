import java.util.Random;
import java.util.Arrays;

/**
 * 2048 board logic
 * @author Alan Huynh
 * @author Justin Huynh
 */


class Game {
    private final Tile[][] board;
    private final Tile[][] oldBoard;
    private final Random randy;
    private int score;
    Game() {
        board = new Tile[4][4];
        oldBoard = new Tile[4][4];
        score = 0;
        randy = new Random();
        for (int i = 0; i < 2; i++) {
            initializeBoard();
        }
    }

    private void initializeBoard() {
        int rand = randy.nextInt(16);
        if (board[rand / 4][rand % 4] == null) {
            board[rand / 4][rand % 4] = isFour() ? new Tile(true) : new Tile(false);
        } else {
            initializeBoard();
        }
    }

    Tile[][] getBoard() {
        return board;
    }

    int getScore() {
        return score;
    }

    boolean checkWon() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != null && board[i][j].getPow() == 11) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean checkLost() {
        int i1;
        int j1;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != null) {
                    i1 = i + 1;
                    j1 = j;
                    if (i1 <= 3 && i1 >= 0 && j1 <= 3 && j1 >= 0) {
                        if (board[i1][j1] != null && board[i][j].getPow() == board[i1][j1].getPow()) {
                            return false;
                        }
                    }
                    i1 = i - 1;
                    if (i1 <= 3 && i1 >= 0 && j1 <= 3 && j1 >= 0) {
                        if (board[i1][j1] != null && board[i][j].getPow() == board[i1][j1].getPow()) {
                            return false;
                        }
                    }
                    i1 = i;
                    j1 = j - 1;
                    if (i1 <= 3 && i1 >= 0 && j1 <= 3 && j1 >= 0) {
                        if (board[i1][j1] != null && board[i][j].getPow() == board[i1][j1].getPow()) {
                            return false;
                        }
                    }
                    j1 = j + 1;
                    if (i1 <= 3 && i1 >= 0 && j1 <= 3 && j1 >= 0) {
                        if (board[i1][j1] != null && board[i][j].getPow() == board[i1][j1].getPow()) {
                            return false;
                        }
                    }
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    void shift(int direction) {
        rotate(direction);
        for (int i = 0; i < 4; i++) {
            board[i] = compress(board[i]);
        }
        rotate(direction);
        if (!compare()) {
            for (int i = 0; i < 4; i++) {
                oldBoard[i] = Arrays.copyOf(board[i], 4);
            }
            fill();
        }
    }

    private boolean compare() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != oldBoard[i][j]) {
                    return false;
                }
                if (board[i][j] != null && oldBoard[i][j] != null) {
                    if (board[i][j].getPow() != oldBoard[i][j].getPow()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * [x][y] = array of size x of y-arrays
     * [3][4] =[1, 2, 3, 4]
     * [5, 6, 7, 8]
     * [9, 0, 1, 2]
     * @param direction:
     *  0 = up
     *  1 = down
     *  2 = left
     *  3 = right
     */
    private void rotate(int direction) {
        Tile temp;
        switch (direction) {
            case 0: //invert around (x, x) axis
                for (int i = 1; i < 4; i++) {
                    for (int j = 0; j < i; j++) {
                        temp = board[i][j];
                        board[i][j] = board[j][i];
                        board[j][i] = temp;
                    }
                }
                break;
            case 1: //invert around (4-x, x) axis
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < (3 - i); j++) {
                        temp = board[i][j];
                        board[i][j]= board[3 - j][3 - i];
                        board[3 - j][3 - i] = temp;
                    }
                }
                break;
            case 3:
                for (int i = 0; i < 4; i++) {
                    temp = board[i][0];
                    board[i][0] = board[i][3];
                    board[i][3] = temp;
                    temp = board[i][1];
                    board[i][1] = board[i][2];
                    board[i][2] = temp;
                }
                break;
        }
    }

    private Tile[] shorten(Tile[] vec) {
        for (int j = 0; j < 4; j++) {
            if (vec[j] == null) {
                for (int i = j + 1; i < 4; i++) {
                    if (vec[i] != null) {
                        vec[j] = vec[i];
                        vec[i] = null;
                        break;
                    }
                }
            }
        }
        return vec;
    }

    private Tile[] compress(Tile[] vec) {
        vec = shorten(vec);
        for (int i = 0; i < 3; i++) {
            if (vec[i] != null) {
                if (vec[i + 1] != null && vec[i + 1].getPow() == vec[i].getPow()) {
                    vec[i + 1] = null;
                    vec[i].increasePow();
                    score += vec[i].value();
                }
            }
        }
        vec = shorten(vec);
        return vec;
    }

    private boolean isFour() {
        // Generate 10% chance
        return randy.nextInt(10) == 5;
    }

    private void fill() {
        int empty = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == null) {
                    empty++;
                }
            }
        }
        if (empty == 0) {
            return;
        }
        int c = randy.nextInt(empty);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == null && c > 0) {
                    c--;
                }
                if (board[i][j] == null && c == 0) {
                    board[i][j] = isFour() ? new Tile(true) : new Tile(false);
                    return;
                }
            }
        }
    }
}