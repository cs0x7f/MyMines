
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Minefield {
    private int col;
    private int row;
    private int mines;
    private int minesLeft;
    private int boardLeft;
    private int type;//0 = XP, 1 = Win7
    private int[][] board;
    private int[][] mineBoard;
    private boolean newGame;
    private boolean win;
    private boolean gameOver;
    public final static int RIGHT_CLICK = 1;
    public final static int LEFT_CLICK = 0;
    public final static int TYPE_XP = 0;
    public final static int TYPE_WIN7 = 1;

    private static int[] y_near = new int[] { -1, -1, -1, 0, 0, 1, 1, 1};
    private static int[] x_near = new int[] { -1, 0, 1, -1, 1, -1, 0, 1};

    Random rnd = new ec.util.MersenneTwister();

    public int getCol() {
        return col;
    }
    public int getRow() {
        return row;
    }
    public int getMines() {
        return mines;
    }
    public int getMinesLeft() {
        return minesLeft;
    }
    public int getBoardLeft() {
        return boardLeft;
    }
    public int[][] getBoard() {
        return board;
    }
    public int[][] getMineBoard() {
        return mineBoard;
    }
    public boolean getWin() {
        return win;
    }
    public boolean getGameOver() {
        return gameOver;
    }

    public void setRandom(Random r) {
        rnd = r;
    }

    public void init(int r, int c,  int m, int type) {
        col = c;
        row = r;
        this.type = type;
        int n = col * row;
        mines = m;
        minesLeft = 0;
        boardLeft = n;
        newGame = true;
        win = false;
        gameOver = false;
        board = new int[r][c];
        mineBoard = new int[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                board[i][j] = -2;
            }
        }
    }

    public void showMineBoard() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(mineBoard[i][j]);
            }
            System.out.print('\n');
        }
        System.out.print('\n');
    }

    private static int[] color = new int[] {0, 34, 32, 31, 35, 0, 0, 0, 0, 0};
    private static String fmt = ((char)27) + "[%dm%s" + ((char)27) + "[0m ";

    public void showBoard() {
        showBoard(board, mineBoard, row, col);
        /*      for (int i = 0; i < row; i++) {
                    for (int j = 0; j < col; j++) {
                        switch (board[i][j]) {
                        case -2 : System.out.print(String.format(fmt, 0, mineBoard[i][j] == 9 ? "x" : "-")); break;
                        case -1 : System.out.print(String.format(fmt, 0, mineBoard[i][j] == 9 ? "o" : "b")); break;
                        case 9  : System.out.print(String.format(fmt, 41, "*")); break;
                        case 0  : System.out.print(String.format(fmt, 0, " ")); break;
                        default : System.out.print(String.format(fmt, color[board[i][j]], Integer.toString(board[i][j])));
                        }
                    }
                    System.out.println();
                }
                System.out.println();
        */
    }

    public static synchronized void showBoard(int[][] board, int[][] mineBoard, int row, int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                switch (board[i][j]) {
                case -2 : System.out.print(String.format(fmt, 0, mineBoard[i][j] == 9 ? "x" : "-")); break;
                case -1 : System.out.print(String.format(fmt, 0, mineBoard[i][j] == 9 ? "o" : "b")); break;
                case 9  : System.out.print(String.format(fmt, 41, "*")); break;
                case 0  : System.out.print(String.format(fmt, 0, " ")); break;
                default : System.out.print(String.format(fmt, color[board[i][j]], Integer.toString(board[i][j])));
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public void showBoard2() {
        System.out.println("<br>");
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                switch (board[i][j]) {
                case -2 : System.out.print(mineBoard[i][j] == 9 ? "<img src=\"Mine.gif\">" : "<img src=\"Block.gif\">"); break;
                case -1 : System.out.print(mineBoard[i][j] == 9 ? "<img src=\"Flag.gif\">" : "<img src=\"MarkOver.gif\">"); break;
                case 9  : System.out.print("<img src=\"MineOver.gif\">"); break;
                case 0  : System.out.print("<img src=\"Blank.gif\">"); break;
                default : System.out.print("<img src=\"" + Integer.toString(board[i][j]) + ".gif\">");
                }
            }
            System.out.print("<br>");
        }
        System.out.println("<br>");
    }

    private void mineBoardInit() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (mineBoard[i][j] == 9) {
                    for (int idx = 0; idx < 8; idx++) {
                        try {
                            if (mineBoard[i + x_near[idx]][j + y_near[idx]] != 9) {
                                mineBoard[i + x_near[idx]][j + y_near[idx]]++;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                }
            }
        }
    }

    private class Position {
        int r;
        int c;
        Position(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    private void open(int r, int c) {
        if (r < 0 || c < 0 || r >= row || c >= col || board[r][c] != -2) {
            return;
        }
        board[r][c] = mineBoard[r][c];
        boardLeft--;
        if (board[r][c] == 0) {
            open(r - 1, c - 1);
            open(r - 1, c);
            open(r - 1, c + 1);
            open(r, c - 1);
            open(r, c + 1);
            open(r + 1, c - 1);
            open(r + 1, c);
            open(r + 1, c + 1);
        }
    }

    public void step(int r, int c, int operate) {
        if (gameOver) {
            return;
        }
        if (operate == RIGHT_CLICK) {
            if (board[r][c] == -2) {
                board[r][c] = -1;
                minesLeft--;
                boardLeft--;
            } else if (board[r][c] == -1) {
                board[r][c] = -2;
                minesLeft++;
                boardLeft++;
            }
        }
        if (operate == LEFT_CLICK) {
            if (newGame) {
                newGame = false;
                mineBoard[r][c] = -2;
                if (type == TYPE_WIN7) {
                    for (int i = 0; i < 8; i++) {
                        try {
                            mineBoard[r + x_near[i]][c + y_near[i]] = -2;
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                }
                while (minesLeft < mines) {
                    int pos = rnd.nextInt(col * row);
                    int mineRow = pos / col;
                    int mineCol = pos % col;
                    if (mineBoard[mineRow][mineCol] == 0) {
                        mineBoard[mineRow][mineCol] = 9;
                        minesLeft++;
                    }
                }
                mineBoard[r][c] = 0;
                if (type == TYPE_WIN7) {
                    for (int i = 0; i < 8; i++) {
                        try {
                            mineBoard[r + x_near[i]][c + y_near[i]] = 0;
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                }
                mineBoardInit();
                //              showMineBoard();
            }
            if (mineBoard[r][c] == 9) {
                gameOver = true;
            }
            if (board[r][c] == -2) {
                open(r, c);
            }
        }
        if (minesLeft == boardLeft) {
            win = (mineBoard[r][c] != 9) || (mineBoard[r][c] == 9 && operate == RIGHT_CLICK);
            gameOver = true;
        }
        //      showBoard();
        //      showMineBoard();
    }
}