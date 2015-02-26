
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;
import static java.awt.event.InputEvent.*;
import java.util.jar.*;
import javax.swing.*;

public class MyFrame extends JFrame {

    private int winLength;
    private int winHeight;
    private int row;
    private int col;
    private int mines;

    public static int BOX_SIZE = 21;

    private Minefield mineField = new Minefield();
    private MineBoard mineBoard = new MineBoard(mineField);
    private Solver solver = new Solver();

    private class MineBoard extends Canvas {
        Minefield mf;
        int r = 0;
        int c = 0;

        /**
         *  0: blank
         *  1~8: number
         *  9: mine
         *  10: flag
         */
        BufferedImage[] images;

        void generateImages() {
            images = new BufferedImage[15];
            try {
                /*
                JarFile jf = new JarFile(System.getProperty("java.class.path"));
                images[0] = ImageIO.read(jf.getInputStream(jf.getEntry("Blank.gif")));
                images[1] = ImageIO.read(jf.getInputStream(jf.getEntry("1.gif")));
                images[2] = ImageIO.read(jf.getInputStream(jf.getEntry("2.gif")));
                images[3] = ImageIO.read(jf.getInputStream(jf.getEntry("3.gif")));
                images[4] = ImageIO.read(jf.getInputStream(jf.getEntry("4.gif")));
                images[5] = ImageIO.read(jf.getInputStream(jf.getEntry("5.gif")));
                images[6] = ImageIO.read(jf.getInputStream(jf.getEntry("6.gif")));
                images[7] = ImageIO.read(jf.getInputStream(jf.getEntry("7.gif")));
                images[8] = ImageIO.read(jf.getInputStream(jf.getEntry("8.gif")));
                images[9] = ImageIO.read(jf.getInputStream(jf.getEntry("MineOver.gif")));
                images[10] = ImageIO.read(jf.getInputStream(jf.getEntry("Flag.gif")));
                images[11] = ImageIO.read(jf.getInputStream(jf.getEntry("Block.gif")));
                images[12] = ImageIO.read(jf.getInputStream(jf.getEntry("IsMine.gif")));
                images[13] = ImageIO.read(jf.getInputStream(jf.getEntry("MarkOver.gif")));
                for (int i=0; i<14; i++) {
                    BufferedImage newImage = new BufferedImage(BOX_SIZE, BOX_SIZE, BufferedImage.TYPE_INT_RGB);
                    Graphics g = newImage.getGraphics();
                    g.drawImage(images[i], 0, 0, BOX_SIZE, BOX_SIZE, null);
                    images[i] = newImage;
                }
                */
                for (int i = 0; i < 14; i++) {
                    images[i] = new BufferedImage(BOX_SIZE, BOX_SIZE, BufferedImage.TYPE_INT_RGB);
                    Graphics g = images[i].getGraphics();
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, BOX_SIZE, BOX_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(0, 0, BOX_SIZE - 1, BOX_SIZE - 1);
                }
                for (int i = 0; i < 9; i++) {
                    Graphics g = images[i].getGraphics();
                    g.setColor(Color.GRAY);
                    g.fillRect(1, 1, BOX_SIZE - 2, BOX_SIZE - 2);
                    g.setColor(Color.BLACK);
                    g.drawString(Integer.toString(i), BOX_SIZE / 2 - 3, BOX_SIZE / 2 + 5);
                }
                Graphics g = images[9].getGraphics();
                g.setColor(Color.RED);
                g.fillRect(1, 1, BOX_SIZE - 2, BOX_SIZE - 2);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(9), BOX_SIZE / 2 - 3, BOX_SIZE / 2 + 5);

                g = images[10].getGraphics();
                g.setColor(Color.PINK);
                g.fillRect(1, 1, BOX_SIZE - 2, BOX_SIZE - 2);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(-1), BOX_SIZE / 2 - 5, BOX_SIZE / 2 + 5);

                g = images[12].getGraphics();
                g.setColor(Color.GRAY);
                g.drawString(Integer.toString(9), BOX_SIZE / 2 - 3, BOX_SIZE / 2 + 5);

                g = images[13].getGraphics();
                g.setColor(Color.RED);
                g.fillRect(1, 1, BOX_SIZE - 2, BOX_SIZE - 2);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(-1), BOX_SIZE / 2 - 5, BOX_SIZE / 2 + 5);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public MineBoard(Minefield mf) {
            generateImages();
            this.mf = mf;
            this.r = mf.getRow();
            this.c = mf.getCol();
            MouseProc mouseProc = new MouseProc();
            addMouseListener(mouseProc);
            addMouseMotionListener(mouseProc);
        }

        public void paint(Graphics g) {
            this.r = mf.getRow();
            this.c = mf.getCol();
            int[][] board = mf.getBoard();
            int[][] mineboard = mf.getMineBoard();
            setSize(c * BOX_SIZE, r * BOX_SIZE);
            if (mf.getGameOver()) {
                //              System.out.println('o');
                for (int x = 0; x < r; x++) {
                    for (int y = 0; y < c; y++) {
                        switch (board[x][y]) {
                        case -1: g.drawImage(mineboard[x][y] == 9 ? images[10] : images[13],
                                                 y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null);
                            break;
                        case -2: g.drawImage(mineboard[x][y] == 9 ? images[12] : images[11],
                                                 y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null);
                            break;
                        default: g.drawImage(images[board[x][y]], y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null); break;
                        }
                    }
                }
            } else {
                for (int x = 0; x < r; x++) {
                    for (int y = 0; y < c; y++) {
                        switch (board[x][y]) {
                        case -1: g.drawImage(images[10], y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null); break;
                        case -2: /*g.drawImage(images[11], y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null); */break;
                        default: g.drawImage(images[board[x][y]], y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null); break;
                        }
                    }
                }
                solver.solve(mf.getBoard(), true, mines);
                solver.calc();
                for (int x = 0; x < r; x++) {
                    for (int y = 0; y < c; y++) {
                        if (board[x][y] == -2) {
                            g.drawImage(images[11], y * BOX_SIZE, x * BOX_SIZE, BOX_SIZE, BOX_SIZE, null);
                            int prob = (int)(solver.getMineProb(x, y) * (BOX_SIZE - 2) * (BOX_SIZE - 2));
                            int lines = prob / (BOX_SIZE - 2);
                            int points = prob % (BOX_SIZE - 2);
                            g.setColor(Color.PINK);
                            if (lines > 0) {
                                g.fillRect(y * BOX_SIZE + 1, x * BOX_SIZE + 1, BOX_SIZE - 2, lines);
                            }
                            if (points > 0) {
                                g.fillRect(y * BOX_SIZE + 1, x * BOX_SIZE + lines + 1, points, 1);
                            }
                        }
                    }
                }

            }
            //          g.setColor(new Color(0, 0, 0));
            //          g.fillRect(0, 0, 100, 100);
            //          g.drawImage(images[1], 50, 50, null);
            //          System.out.println("Paint");
        }

        public void sub() {
            paint(getGraphics());
        }

        private class MouseProc extends MouseAdapter {
            public void mouseReleased(MouseEvent e) {
                int y = e.getX() / BOX_SIZE;
                int x = e.getY() / BOX_SIZE;
                if (e.getButton() == 1 && (e.getModifiersEx() & BUTTON3_DOWN_MASK) == 0) {
                    mf.step(x, y, Minefield.LEFT_CLICK);
                    if (menuGameAuto.getState()) {
                        autoPlay();
                    }
                    sub();
                }
                // System.out.println(e);
            }
            public void mousePressed(MouseEvent e) {
                int y = e.getX() / BOX_SIZE;
                int x = e.getY() / BOX_SIZE;
                if (e.getButton() == 3 && (e.getModifiersEx() & BUTTON1_DOWN_MASK) == 0) {
                    mf.step(x, y, Minefield.RIGHT_CLICK);
                    if (menuGameAuto.getState()) {
                        autoPlay();
                    }
                    sub();
                }
                // System.out.println(e);
            }
            public void mouseDragged(MouseEvent e) {
                int y = e.getX() / BOX_SIZE;
                int x = e.getY() / BOX_SIZE;
                int keys = e.getModifiersEx();
                if ((keys & (BUTTON1_DOWN_MASK | BUTTON3_DOWN_MASK)) == (BUTTON1_DOWN_MASK | BUTTON3_DOWN_MASK)) {

                }
            }
            public void mouseMoved(MouseEvent e) {
                int y = e.getX() / BOX_SIZE;
                int x = e.getY() / BOX_SIZE;
                if (!mf.getGameOver()) {
                    over.setText(Integer.toString((int)Math.round((solver.getMineProb(x, y) * 1000))));
                }
            }
        }
    }

    //Board
    private class Board extends Panel {
        private int row;
        private int col;
        public Label l;

        public int getRow() {
            return row;
        }
        public int getCol() {
            return col;
        }

        public void setL(int s) {
            switch (s) {
            case -2 : l.setBackground(Color.white); l.setText(""); break;
            case -1 : l.setBackground(Color.pink); l.setText(Integer.toString(s)); break;
            case  9 : l.setBackground(Color.red); l.setText(Integer.toString(s)); break;
            default : l.setBackground(Color.gray); l.setText(Integer.toString(s));
            }
        }

        Board(int r, int c) {
            row = r;
            col = c;
            setLayout(null);
            l = new Label("");
            l.setFont(new Font("", 0, BOX_SIZE / 2));
            l.setLocation(1, 1);
            l.setSize(BOX_SIZE - 1, BOX_SIZE - 1);
            l.setAlignment(Label.CENTER);
            l.addMouseListener(new clicked());
            add(l);
        }

        public void paint(Graphics g) {
            g.drawRect(0, 0, BOX_SIZE, BOX_SIZE);
        }
    }

    private Label over = new Label("Play");
    private Label minesLeft = new Label("");
    private final static String MINE_LEFT = "Mines Left: ";

    //Menu
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuGame = new JMenu("Game");
    private JMenuItem menuGameNew = new JMenuItem("New Game");
    private JMenuItem menuGameEasy = new JMenuItem("Easy (9 9 10)");
    private JMenuItem menuGameNormal = new JMenuItem("Normal (16 16 40)");
    private JMenuItem menuGameHard = new JMenuItem("Hard (16 30 99)");
    private JCheckBoxMenuItem menuGameAuto = new JCheckBoxMenuItem("Auto Play", true);
    private JCheckBoxMenuItem menuWin7 = new JCheckBoxMenuItem("Win7 Mode", false);

    //Settings
    private Dialog setMineField;
    private Label setRowLabel = new Label("Row     ");
    private Label setColLabel = new Label("Column  ");
    private Label setMineLabel = new Label("Mine    ");
    private TextField setRowText = new TextField("9", 3);
    private TextField setColText = new TextField("9", 3);
    private TextField setMineText = new TextField("10", 3);
    private Button setMineOK = new Button("  OK  ");
    private Button setMineCancel = new Button("Cancel");

    //Error Messages
    private Dialog error;
    private Button errorOK = new Button("  OK  ");

    //Buttons
    private JButton newGameButton = new JButton("Restart");
    private JButton aiGame = new JButton("AI Step");
    private JButton aiGameWithGuess = new JButton("AI Guess");

    int game_type = Minefield.TYPE_XP;

    //Initial
    MyFrame() {
        super("MineField");
        // setResizable(false);
        setSize(300, 400);
        setLayout(null);

        addWindowListener(new winClose());
        setMineFieldInit();
        menuBarInit();
        over.setSize(50, 30);
        over.setFont(new Font("", 0, 15));
        over.setLocation(0, 0);
        over.setVisible(true);
        over.setAlignment(Label.CENTER);
        add(over);
        minesLeft.setSize(150, 30);
        minesLeft.setFont(new Font("", 0, 15));
        add(minesLeft);

        add(newGameButton);
        add(aiGameWithGuess);
        add(aiGame);

        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                over.setText("Play");
                boardInit();
                mineField.init(row, col, mines, game_type);
                minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
            }
        });

        menuWin7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game_type = menuWin7.getState() ? Minefield.TYPE_WIN7 : Minefield.TYPE_XP;
                over.setText("Play");
                boardInit();
                mineField.init(row, col, mines, game_type);
                minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
            }
        });

        aiGame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autoPlay();
            }
        });

        aiGameWithGuess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mineField.getGameOver()) {
                    return;
                }
                Action[] actions = solver.solve(mineField.getBoard(), true, mines);
                for (int i = 0; i < actions.length; i++) {
                    mineField.step(actions[i].getX(), actions[i].getY(), actions[i].getAct());
                }
                if (menuGameAuto.getState()) {
                    autoPlay();
                }
                refreshBoard();
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                //              System.out.println(BOX_SIZE);
                if (col == 0 || row == 0) {
                    return;
                }
                int width = getWidth();
                int height = getHeight();

                BOX_SIZE = Math.max(10, Math.min((width - 60) / col, (height - 150) / row));
                //              System.out.println(BOX_SIZE);

                minesLeft.setLocation(width - 150, 0);
                mineBoard.setLocation(30 + (width - BOX_SIZE * col - 60) / 2, 30 + (height - BOX_SIZE * row - 150) / 2);
                mineBoard.generateImages();
                newGameButton.setLocation(30, height - 100);
                aiGame.setLocation(width - 130, height - 110);
                aiGameWithGuess.setLocation(width - 130, height - 85);

                refreshBoard();
            }

        });

        //      MyFrame.this.setEnabled(false);
        //      setMineField.setVisible(true);
        setJMenuBar(menuBar);
        setVisible(true);
        menuGameHard.doClick();
    }

    //Window Closing
    private class winClose extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    //Mine Step
    private class clicked extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2 || mineField.getGameOver()) {
                return;
            }
            //System.out.println(e.getButton());
            Label temp = (Label) e.getComponent();
            Board tempBoard = (Board) temp.getParent();
            int operate = (e.getButton() == MouseEvent.BUTTON1) ? Minefield.LEFT_CLICK : Minefield.RIGHT_CLICK;
            mineField.step(tempBoard.getRow(), tempBoard.getCol(), operate);
            refreshBoard();
        }
    }

    //Refresh Board
    private void refreshBoard() {
        int[][] boardState = mineField.getBoard();
        if (mineField.getGameOver()) {
            if (mineField.getWin()) {
                over.setText("Win");
            } else {
                over.setText("Lose");
            }
        }
        minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
        mineBoard.sub();
        //      revalidate();
    }

    //Create Menu
    private void menuBarInit() {
        menuBar.add(menuGame);
        menuGame.add(menuGameNew);
        menuGameNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MyFrame.this.setEnabled(false);
                setMineField.setVisible(true);
            }
        });

        menuGame.add(menuGameEasy);
        menuGameEasy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                over.setText("Play");
                row = 9;
                col = 9;
                mines = 10;
                boardInit();
                mineField.init(row, col, mines, game_type);
                minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
            }
        });

        menuGame.add(menuGameNormal);
        menuGameNormal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                over.setText("Play");
                row = 16;
                col = 16;
                mines = 40;
                boardInit();
                mineField.init(row, col, mines, game_type);
                minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
            }
        });

        menuGame.add(menuGameHard);
        menuGameHard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                over.setText("Play");
                row = 16;
                col = 30;
                mines = 99;
                boardInit();
                mineField.init(row, col, mines, game_type);
                minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
            }
        });
        menuGame.add(menuGameAuto);
        menuGame.add(menuWin7);
    }

    void autoPlay() {
        if (!mineField.getGameOver()) {
            Action[] actions;
            do {
                actions = solver.solve(mineField.getBoard(), false, mines);
                for (int i = 0; i < actions.length; i++) {
                    mineField.step(actions[i].getX(), actions[i].getY(), actions[i].getAct());
                }
            } while (actions.length != 0 && !mineField.getGameOver() && menuGameAuto.getState());
        }
        refreshBoard();
    }

    //Create Settings
    private void setMineFieldInit() {
        setMineField = new Dialog(MyFrame.this, "Settings");
        setMineField.setSize(300, 400);
        setMineField.setResizable(false);
        setMineField.setLayout(new FlowLayout(FlowLayout.LEFT, 50, 50));
        setMineField.setFont(new Font("monospaced", Font.PLAIN, 15));
        setMineField.add(setRowLabel); setMineField.add(setRowText);
        setMineField.add(setColLabel); setMineField.add(setColText);
        setMineField.add(setMineLabel); setMineField.add(setMineText);
        setMineField.add(setMineOK); setMineField.add(setMineCancel);

        setMineOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String tempRow = setRowText.getText();
                String tempCol = setColText.getText();
                String tempMines = setMineText.getText();
                if (!checkNum(tempRow) || !checkNum(tempCol) || !checkNum(tempMines)) {
                    errorMsg("Invalid Settings");
                    return;
                }
                int tempRowInt = Integer.parseInt(tempRow);
                int tempColInt = Integer.parseInt(tempCol);
                int tempMinesInt = Integer.parseInt(tempMines);
                if (tempRowInt < 8 || tempColInt < 8) {
                    errorMsg("Too Small Board");
                    return;
                }
                if (tempMinesInt < 10) {
                    errorMsg("Too Few Mines");
                    return;
                }
                if (tempMinesInt >= tempRowInt * tempColInt) {
                    errorMsg("Too Many Mines");
                    return;
                }
                over.setText("Play");
                row = tempRowInt;
                col = tempColInt;
                mines = tempMinesInt;

                MyFrame.this.setEnabled(true);
                setMineField.setVisible(false);
                boardInit();
                mineField.init(row, col, mines, game_type);
                minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
            }
        });

        setMineCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MyFrame.this.setEnabled(true);
                setMineField.setVisible(false);
            }
        });

        setMineField.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                MyFrame.this.setEnabled(true);
                setMineField.setVisible(false);
            }
        });
    }

    //Draw Board
    private void boardInit() {
        winLength = BOX_SIZE * col + 30 + 30;
        winHeight = BOX_SIZE * row + 50 + 100;
        over.setLocation(30, 0);
        minesLeft.setLocation(winLength - 150, 0);
        setSize(winLength, winHeight);

        mineBoard.setSize(1, 1);
        mineBoard.setLocation(30, 30);
        mineBoard.setVisible(true);
        add(mineBoard);
        mineBoard.requestFocus();
        newGameButton.setSize(100, 30);
        newGameButton.setLocation(30, winHeight - 100);
        newGameButton.setMnemonic(KeyEvent.VK_1);
        aiGame.setSize(100, 25);
        aiGame.setLocation(winLength - 130, winHeight - 110);
        aiGame.setMnemonic(KeyEvent.VK_2);
        aiGameWithGuess.setSize(100, 25);
        aiGameWithGuess.setLocation(winLength - 130, winHeight - 85);
        aiGameWithGuess.setMnemonic(KeyEvent.VK_3);
    }

    //Check Number
    private boolean checkNum(String s) {
        if (s.length() == 0) {
            return false;
        }
        byte[] sChar = s.getBytes();
        for (int i = 0; i < s.length(); i++) {
            if ((sChar[i] < 0x30) || (sChar[i] > 0x39)) {
                return false;
            }
        }
        return true;
    }

    //Error Messages
    private void errorMsg(String msg) {
        setMineField.setEnabled(false);
        error = new Dialog(MyFrame.this, "Error!");
        error.setSize(200, 150);
        error.setResizable(false);
        error.setLayout(new FlowLayout(FlowLayout.CENTER, 1000, 20));
        error.setLocation(50, 120);
        error.add(new Label(msg));
        error.add(errorOK);
        error.setVisible(true);
        errorOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMineField.setEnabled(true);
                error.dispose();
            }
        });
        error.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setMineField.setEnabled(true);
                error.dispose();
            }
        });
    }

    public static void main(String[] args) {
        MyFrame f = new MyFrame();
    }
}