import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * UI for 2048 Game
 * @author Justin Huynh
 */

class GameUI {
    private Game game = new Game();
    private boolean gamePaused = false;
    private boolean fullscreen = false;
    private boolean colorMode = false;
    private boolean got2048 = false;
    private boolean lostAfter2048 = false;
    private final String titleText = "2048";
    private final int squareSizeScale = 6;
    private final float titleFontScale = 8.25f;
    private int squareSize;
    private final Color background;
    private final Color emptySquare;
    private final Color twoFourFontColor;
    private final Color fontColor;
    private final String fontName;
    private final Font titleFont;
    private final Font font;
    private final JFrame frame;
    private final JLabel score;
    private final JLabel time;
    private final JLabel title;
    private final JLabel winState;
    private final JLabel pauseState;
    private final Timer timer;
    private int secs;
    private Dimension dimension;
    private Point location;
    private Grid board;

    private GameUI() {
        /* Minimum Dimensions */
        final int minWindowHeight = 700;
        final int minWindowWidth = 600;
        squareSize = minWindowHeight / squareSizeScale;

        /* Colors */
        background = new Color(187, 173, 160);
        emptySquare = new Color(205, 193, 181);
        fontColor = new Color(244, 230, 219);
        twoFourFontColor = new Color(117, 107, 97);
        Color buttonColor = new Color(143, 122, 102);

        /* Fonts */
        fontName = "Clear Sans";
        titleFont = new Font(fontName, Font.BOLD, 0); // will get scaled anyway
        font = new Font(fontName, Font.BOLD, 18);

        /* Main JFrame */
        frame = new JFrame();
        frame.setMinimumSize(new Dimension(minWindowWidth, minWindowHeight));
        frame.pack();
        frame.setTitle(titleText);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setFocusable(true); // the only component that is focusable!
        frame.setBackground(background);

        // Resize listener
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleOnResize(e.getComponent().getBounds().getSize().getHeight());
            }
        });

        /* Icon */
        URL iconURL = this.getClass().getClassLoader().getResource("icon.png");
        if (iconURL != null) {
            frame.setIconImage(new ImageIcon(iconURL).getImage());
        }

        /* New Game JButton */
        JButton reset = new JButton("<html><center>New<br>Game</center></html>");
        reset.setPreferredSize(new Dimension(80, 65));
        reset.setFont(font);
        reset.setFocusPainted(false);
        reset.setContentAreaFilled(false);
        reset.setBackground(buttonColor);
        reset.setForeground(fontColor);
        reset.setOpaque(true);
        reset.setFocusable(false);
        reset.setBorder(new LineBorder(background, 2));
        reset.addActionListener(actionEvent -> resetGame());

        /* JPanels */
        // Holds scoreTimePause panel on right side
        JPanel topPanel = new JPanelCustom();
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBorder(new CompoundBorder(topPanel.getBorder(),
                           new EmptyBorder(0, 0, 15, 15)));

        // Holds reset button on left
        JPanel buttonPanel = new JPanelCustom();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Holds win/lose message in center
        JPanel winPanel = new JPanelCustom();
        winPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        winPanel.setBorder(new CompoundBorder(winPanel.getBorder(),
                           new EmptyBorder(0, 0, -15, 0)));

        // Holds 2048 logo text in center
        JPanel titlePanel = new JPanelCustom();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 4 && !gamePaused) {
                    colorMode = true;
                    board.paintTile();
                }
            }
        });

        // Holds score, time, pause JLabels
        JPanel scoreTimePausePanel = new JPanelCustom();
        scoreTimePausePanel.setLayout(new GridLayout(3, 1));
        scoreTimePausePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                togglePause();
            }
        });

        // Holds buttonPanel, titlePanel, topPanel
        JPanel mainPanel = new JPanelCustom();
        mainPanel.setLayout(new GridLayout());

        /* JLabels */
        title = new JLabelCustom(titleText);
        title.setFont(titleFont);

        score = new JLabelCustom("Score: 0 ");
        time = new JLabelCustom("Time: 00:00 ");
        pauseState = new JLabelCustom(" ");

        winState = new JLabelCustom(null);
        clearWinText();
        winState.setFont(new Font(fontName, Font.BOLD, 20));
        winState.setBorder(new EmptyBorder(0, 0, 25, 0));

        /* Add components */
        // add title
        titlePanel.add(title);

        // add score, time pause labels
        scoreTimePausePanel.add(score);
        scoreTimePausePanel.add(time);
        scoreTimePausePanel.add(pauseState);
        topPanel.add(scoreTimePausePanel);

        // add reset button
        buttonPanel.add(reset);

        // add everything to main holder
        mainPanel.add(buttonPanel);
        mainPanel.add(titlePanel);
        mainPanel.add(topPanel);

        // add win message
        winPanel.add(winState);

        // Panels to frame
        frame.add(mainPanel, BorderLayout.NORTH);
        frame.add(winPanel, BorderLayout.SOUTH);
        frame.add(board = new Grid());

        /* Stopwatch */
        secs = 0;
        timer = new Timer(1000, e -> {
            secs++;
            time.setText(String.format("Time: %02d:%02d ", (secs % 3600) / 60, (secs % 60)));
        });
        timer.start();

        /* Fullscreen Mode vars */
        dimension = frame.getContentPane().getSize();
        location = frame.getLocation();

        /* Keyboard Listener */
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        togglePause();
                        break;
                    case KeyEvent.VK_F11:
                        toggleFullscreen();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        resetGame();
                        break;
                }
                if (!game.checkLost() && !gamePaused && !lostAfter2048) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            game.shift(0);
                            break;
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            game.shift(2);
                            break;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            game.shift(1);
                            break;
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            game.shift(3);
                            break;
                    }
                    updateAfterMove();
                }
            }
        });

        /* Touchscreen listener */
        frame.addMouseListener(new MouseAdapter() {
            float lastY = 0f;
            float lastX = 0f;

            @Override
            public void mouseReleased(MouseEvent me) {
                if (!game.checkLost() && !gamePaused && !lostAfter2048) {
                    float x = me.getX() - lastX;
                    float y = me.getY() - lastY;
                    if (Math.abs(y) > Math.abs(x)) {
                        if (y > 0) { // down
                            game.shift(1);
                        } else { // up
                            game.shift(0);
                        }
                    } else {
                        if (x > 0) { // right
                            game.shift(3);
                        } else { // left
                            game.shift(2);
                        }
                    }
                    updateAfterMove();
                }
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                updateXY(me);
            }

            @Override
            public void mousePressed(MouseEvent me) {
                updateXY(me);
            }

            private void updateXY(MouseEvent me) {
                lastY = me.getY();
                lastX = me.getX();
            }
        });
        frame.setVisible(true);
    }

    /**
     * Update score text, repaint tiles
     * Check if game won/lost, start timer if needed
     */
    private void updateAfterMove() {
        board.paintTile();
        score.setText(String.format("Score: %d ", game.getScore()));
        if (!got2048) {
            checkWinGame();
        } else {
            clearWinText();
            timer.start();
            if (!lostAfter2048) {
                checkLostAfter2048();
            }
        }
    }

    private void setFullscreen() {
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.dispose();
        frame.setUndecorated(true);
        frame.setVisible(true);
    }

    private void unFullscreen() {
        frame.dispose();
        frame.setUndecorated(false);
        frame.setExtendedState(Frame.NORMAL);
        frame.setSize(dimension);
        frame.setLocation(location);
        frame.setVisible(true);
    }

    private void toggleFullscreen() {
        if (fullscreen) {
            unFullscreen();
        } else {
            if (frame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
                dimension = frame.getContentPane().getSize();
                location = frame.getLocation();
            }
            setFullscreen();
        }
        frame.requestFocus();
        fullscreen = !fullscreen;
    }

    /**
     * Scales font and square sizes on window resize
     * @param nHeight the updated window height
     */
    private void scaleOnResize(double nHeight) {
        int height = (int) nHeight;
        title.setFont(this.titleFont.deriveFont(height / titleFontScale));
        squareSize = height / squareSizeScale;
        if (!colorMode) {
            board.paintTile();
        }
    }

    /**
     * Resets and redraws game
     */
    private void resetGame() {
        if (!gamePaused) {
            frame.remove(board);
            colorMode = false;
            got2048 = false;
            lostAfter2048 = false;
            game = new Game();
            board = new Grid();
            frame.add(board);
            board.refresh();
            secs = 0;
            score.setText("Score: 0 ");
            clearWinText();
            timer.stop();
            time.setText("Time: 00:00 ");
            timer.start();
        }
    }

    /**
     * Pauses game by stopping timer and adding text
     */
    private void pauseGame() {
        pauseState.setText("Paused");
        timer.stop();
    }

    /**
     * Unpauses game by continuing timer and removing text
     */
    private void unPauseGame() {
        pauseState.setText(" ");
        if (!game.checkLost() && !got2048) {
            timer.start();
        }
    }

    private void togglePause() {
        if (gamePaused) {
            unPauseGame();
        } else {
            pauseGame();
        }
        gamePaused = !gamePaused;
    }

    /**
     * Adds win text if game won
     */
    private void checkWinGame() {
        if (game.checkWon()) {
            setWinText();
            got2048 = true;
            timer.stop();
        } else if (game.checkLost()) {
            setLoseText();
            timer.stop();
        }

    }

    private void checkLostAfter2048() {
        if (game.checkLost()) {
            setLoseText();
            timer.stop();
        }
    }

    private void setLoseText() {
        winState.setText("<html><center>Game Over!<br>Press ESC to play again.<center><html>");
    }

    private void setWinText() {
        winState.setText("<html><center>You won!<br>Press any key to continue playing.<center><html>");
    }

    private void clearWinText() {
        winState.setText("<html><center>&nbsp;<br>&nbsp;<center><html>");
    }

    /**
     * JLabel with universal font, alignment, focusability, color
     */
    private class JLabelCustom extends JLabel {
        private JLabelCustom(String text) {
            super(text);
            this.setFocusable(false);
            this.setFont(font);
            this.setForeground(fontColor);
            this.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    /**
     * JPanel with universal background color and focusability
     */
    private class JPanelCustom extends JPanel {
        private JPanelCustom() {
            super();
            this.setFocusable(false);
            this.setBackground(background);
        }
    }

    private class Grid extends JPanel {
        private final Square[][] cells = new Square[4][4];
        private final int borderWidthScale = 19;

        private Grid() {
            this.setBackground(background);
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    gbc.gridx = col;
                    gbc.gridy = row;
                    Square square = new Square(row, col, null);
                    cells[row][col] = square;
                    add(square, gbc);
                }
            }
            paintTile();
        }

        /**
         * Updates color and number of all Squares
         */
        private void paintTile() {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    if (game.getBoard()[row][col] != null) {
                        Color tileColor = game.getBoard()[row][col].getColor();
                        if (colorMode) {
                            tileColor = new Color((int) (Math.random() * 0x1000000));
                        }
                        cells[row][col].setBackground(tileColor);
                        cells[row][col].setNum(game.getBoard()[row][col].toString());
                    } else {
                        cells[row][col].setBackground(emptySquare);
                    }
                    cells[row][col].setBorder(new LineBorder(background, squareSize / borderWidthScale));
                    cells[row][col].repaint();
                    this.repaint();
                }
            }
        }

        /**
         * Repaints and revalidates each Square
         */
        private void refresh() {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    cells[row][col].revalidate();
                    cells[row][col].repaint();
                }
            }
        }
    }

    public class Square extends JPanel {
        private final float fontScale = 0.40f; // font is 40% of square size for tiles between 1 and 3 digits
        private final float fontDecrease = 0.06f; // remove 6% for each increase in number of digits past 3
        private final int row;
        private final int col;
        private String num;

        private Square(int row, int col, String num) {
            this.row = row;
            this.col = col;
            this.num = num;
        }

        private void setNum(String num) {
            this.num = num;
        }

        /**
         * Paints numbers in middle
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            if (game.getBoard()[row][col] != null) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

                /* Scale font */
                float fontScale = this.fontScale;
                if (num.length() > 3) {
                    fontScale -= (num.length() - 3) * fontDecrease;
                }
                g2.setFont(new Font(fontName, Font.BOLD, (int) (squareSize * fontScale)));

                // change font color if it's a two or four
                g2.setColor(twoFourFontColor);
                if (game.getBoard()[row][col].getPow() > 2) {
                    g2.setColor(fontColor);
                }

                /* Place in center and draw */
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(num, g2);
                int x = (this.getWidth() - (int) r.getWidth()) / 2;
                int y = (this.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2.drawString(num, x, y);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(squareSize, squareSize);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameUI::new);
    }
}