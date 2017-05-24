import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Random;
import java.util.Arrays;

/**
 * 2048 board logic
 * @author Alan Huynh
 * @author Justin Huynh
 */

class Game {
    enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private final Tile[][] board;
    private final Tile[][] oldBoard;
    private final Random randy;
    private int score;
    private int hiScore;
    private static final String scoreLocation = "src/high_score.txt";

    Game() {
        board = new Tile[4][4];
        oldBoard = new Tile[4][4];
        score = 0;
        randy = new Random();
        initializeBoard();

        if (!new File(scoreLocation).exists()) {
            saveHighScore(); // if the file doesn't exist, then create it
        }
        loadHighScore();
    }

    /**
     * Writes to 'high_score.txt' to save the high score.
     */
    private void saveHighScore() {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(scoreLocation), "utf-8"))) {
            this.hiScore = score;
            writer.write(String.valueOf(this.hiScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the high score from 'high_score.txt'.
     */
    private void loadHighScore() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(scoreLocation));
            String num = reader.readLine();
            this.hiScore = (num == null) ? 0 : Integer.parseInt(num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getHiScore() {
        return this.hiScore;
    }

    /**
     * Loads two tiles on the board to initialize.
     */
    private void initializeBoard() {
        int toGen = 2;
        while (toGen > 0) {
            int rand = randy.nextInt(16);
            if (board[rand / 4][rand % 4] == null) {
                board[rand / 4][rand % 4] = new Tile(isFour());
                toGen--;
            }
        }
    }

    Tile[][] getBoard() {
        return board;
    }

    int getScore() {
        return score;
    }

    /**
     * Loop through board until you find a space that is 2048.
     * @return won or not
     */
    boolean checkWon() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] != null && board[r][c].getPow() == 11) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if lost: every tile is filled, and there are no two adjacent tiles
     *     of the same rank.
     * @return lost or not
     */
    boolean checkLost() {
        int i;
        int j;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] != null) {
                    i = r + 1;
                    j = c;
                    if (i <= 3 && i >= 0 && j <= 3 && j >= 0) {
                        if (board[i][j] != null && board[r][c].getPow() == board[i][j].getPow()) {
                            return false;
                        }
                    }
                    i = r - 1;
                    if (i <= 3 && i >= 0 && j <= 3 && j >= 0) {
                        if (board[i][j] != null && board[r][c].getPow() == board[i][j].getPow()) {
                            return false;
                        }
                    }
                    i = r;
                    j = c - 1;
                    if (i <= 3 && i >= 0 && j <= 3 && j >= 0) {
                        if (board[i][j] != null && board[r][c].getPow() == board[i][j].getPow()) {
                            return false;
                        }
                    }
                    j = c + 1;
                    if (i <= 3 && i >= 0 && j <= 3 && j >= 0) {
                        if (board[i][j] != null && board[r][c].getPow() == board[i][j].getPow()) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Rotates the board in the desired direction, compress the sub-array,
     * and rotate back to return to the original orientation.
     * @param direction direction to move
     */
    void shift(Direction direction) {
        rotate(direction);
        for (int i = 0; i < 4; i++) {
            board[i] = compress(board[i]);
        }
        rotate(direction);
        if (!checkChanged()) {
            for (int i = 0; i < 4; i++) {
                oldBoard[i] = Arrays.copyOf(board[i], 4);
            }
            spawn();
        }
        if (hiScore <= score) {
            saveHighScore();
        }
    }

    /**
     * Checks if any tiles would actually move if you were to shift.
     * Used to ensure that tiles don't get spawned when they shouldn't.
     * @return true if oldBoard and board would be the same.
     */
    private boolean checkChanged() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] != oldBoard[r][c]) {
                    return false;
                }
                if (board[r][c] != null && oldBoard[r][c] != null) {
                    if (board[r][c].getPow() != oldBoard[r][c].getPow()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * When right shifting, mirror the array.
     * When shifting up, flip around the (x, x) diagonal.
     * When shifting down, flip around the (4-x, x) diagonal.
     * Left shift is default orientation, so don't need to do anything.
     * @param direction direction to move
     */
    private void rotate(Direction direction) {
        Tile temp;
        switch (direction) {
            case UP: // invert around (x, x) axis
                for (int r = 1; r < 4; r++) {
                    for (int c = 0; c < r; c++) {
                        temp = board[r][c];
                        board[r][c] = board[c][r];
                        board[c][r] = temp;
                    }
                }
                break;
            case DOWN: // invert around (4-x, x) axis
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < (3 - r); c++) {
                        temp = board[r][c];
                        board[r][c]= board[3 - c][3 - r];
                        board[3 - c][3 - r] = temp;
                    }
                }
                break;
            case RIGHT:
                for (int r = 0; r < 4; r++) {
                    temp = board[r][0];
                    board[r][0] = board[r][3];
                    board[r][3] = temp;
                    temp = board[r][1];
                    board[r][1] = board[r][2];
                    board[r][2] = temp;
                }
                break;
            case LEFT:
                break;
        }
    }

    /**
     * Remove all the empty spaces between tiles in a sub-array
     * @param sub sub-array to remove spaces from
     * @return shortened array
     */
    private Tile[] shorten(Tile[] sub) {
        for (int j = 0; j < 4; j++) {
            if (sub[j] == null) {
                for (int i = j + 1; i < 4; i++) {
                    if (sub[i] != null) {
                        sub[j] = sub[i];
                        sub[i] = null;
                        break;
                    }
                }
            }
        }
        return sub;
    }

    /**
     * Takes a row of tiles and compresses it in the negative direction.
     * First removes any empty space, combines tiles, and removes space again.
     * @param sub row to compress
     * @return compressed sub-array
     */
    private Tile[] compress(Tile[] sub) {
        sub = shorten(sub);
        for (int i = 0; i < 3; i++) {
            if (sub[i] != null) {
                if (sub[i + 1] != null && sub[i + 1].getPow() == sub[i].getPow()) {
                    sub[i + 1] = null;
                    sub[i].increasePow();
                    score += sub[i].value();
                }
            }
        }
        sub = shorten(sub);
        return sub;
    }

    /**
     * Generates a 10 percent chance
     */
    private boolean isFour() {
        return randy.nextInt(10) == 5;
    }

    /**
     * Spawns a single tile, if possible.
     */
    private void spawn() {
        int empty = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == null) { // count the remaining empty spaces
                    empty++;
                }
            }
        }
        if (empty == 0) { // make sure that it's still possible to spawn space
            return;
        }
        int rand = randy.nextInt(empty);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == null && rand > 0) {
                    rand--;
                }
                if (board[r][c] == null && rand == 0) {
                    board[r][c] = new Tile(isFour());
                    return;
                }
            }
        }
    }
}