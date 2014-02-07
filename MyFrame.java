
import java.awt.*;
import java.awt.event.*;

public class MyFrame extends Frame {
	
	private int winLength;
	private int winHeight;
	private int row;
	private int col;
	private int mines;
	
	public final static int BOX_SIZE = 20;
	
	private Minefield mineField = new Minefield();
	private Solver solver = new Solver();
	
	//Board
	private class Board extends Panel {
		private int row;
		private int col;
		public Label l;
		
		public int getRow() { return row; }
		public int getCol() { return col; }
		
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
			l.setFont(new Font("", 0, BOX_SIZE/2));
			l.setLocation(1, 1);
			l.setSize(BOX_SIZE-1, BOX_SIZE-1);
			l.setAlignment(Label.CENTER);
			l.addMouseListener(new clicked());
			add(l);
		}
		
		public void paint(Graphics g) {
			g.drawRect(0, 0, BOX_SIZE, BOX_SIZE);
		}
	}
	
	private Board[][] board;
	private Label over = new Label("Play");
	private Label minesLeft = new Label("");
	private final static String MINE_LEFT = "Mines Left: ";
	
	//Menu
	private MenuBar menuBar = new MenuBar();
	private Menu menuGame = new Menu("Game");
	private MenuItem menuGameNew = new MenuItem("New Game");
	private MenuItem menuGameEasy = new MenuItem("Easy (9 9 10)");
	private MenuItem menuGameNormal = new MenuItem("Normal (16 16 40)");
	private MenuItem menuGameHard = new MenuItem("Hard (16 30 99)");
	
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
	private Button newGameButton = new Button("Restart");
	private Button aiGame = new Button("AI Step");
	private Button aiGameWithGuess = new Button("AI Guess");
	
	//Initial
	MyFrame() {
		super("MineField");
		setResizable(false);
		setSize(300, 400);
		setLayout(null);
		
		addWindowListener(new winClose());
		setMineFieldInit();
		menuBarInit();
		over.setSize(50, 50);
		over.setFont(new Font("", 0, 15));
		over.setLocation((winLength/2)-10, 30);
		over.setVisible(true);
		over.setAlignment(Label.CENTER);
		add(over);
		minesLeft.setSize(100, 50);
		minesLeft.setFont(new Font("", 0, 15));
		add(minesLeft);
		
		add(newGameButton);
		add(aiGameWithGuess);
		add(aiGame);
		
		newGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						remove(board[i][j]);
					}
				}
				over.setText("Play");
				boardInit();
				mineField.init(row, col, mines, Minefield.TYPE_XP);
				minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
			}
		});
		
		aiGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mineField.getGameOver()) {
					return;
				}
				Action[] actions;
				do {
					actions = solver.solve(mineField.getBoard(), false, mines);
					for (int i = 0; i < actions.length; i++) {
						mineField.step(actions[i].getX(), actions[i].getY(), actions[i].getAct());
					}
				} while (actions.length != 0);
				refreshBoard();
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
				refreshBoard();
			}
		});
		
		MyFrame.this.setEnabled(false);
		setMineField.setVisible(true);
		setMenuBar(menuBar);
		setVisible(true);
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
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				board[i][j].setL(boardState[i][j]);
			}
		}
		if (mineField.getGameOver()) {
			if (mineField.getWin()) {
				over.setText("Win");
			}
			else {
				over.setText("Lose");
			}
		}
		minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
		revalidate();
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
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						remove(board[i][j]);
					}
				}
				over.setText("Play");
				row = 9;
				col = 9;
				mines = 10;
				boardInit();
				mineField.init(row, col, mines, Minefield.TYPE_XP);
				minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
			}
		});
		
		menuGame.add(menuGameNormal);
		menuGameNormal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						remove(board[i][j]);
					}
				}
				over.setText("Play");
				row = 16;
				col = 16;
				mines = 40;
				boardInit();
				mineField.init(row, col, mines, Minefield.TYPE_XP);
				minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
			}
		});
		
		menuGame.add(menuGameHard);
		menuGameHard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						remove(board[i][j]);
					}
				}
				over.setText("Play");
				row = 16;
				col = 30;
				mines = 99;
				boardInit();
				mineField.init(row, col, mines, Minefield.TYPE_XP);
				minesLeft.setText(MINE_LEFT + (mineField.getMinesLeft()));
			}
		});
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
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						remove(board[i][j]);
					}
				}
				over.setText("Play");
				row = tempRowInt;
				col = tempColInt;
				mines = tempMinesInt;
				
				MyFrame.this.setEnabled(true);
				setMineField.setVisible(false);
				boardInit();
				mineField.init(row, col, mines, Minefield.TYPE_XP);
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
		winLength = BOX_SIZE * col + 2 * (col+1) + 50 + 50;
		winHeight = BOX_SIZE * row + 2 * (row+1) + 100 + 150;
		over.setLocation(50, 50);
		minesLeft.setLocation(winLength-150, 50);
		setSize(winLength, winHeight);
		board = new Board[row][col];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				board[i][j] = new Board(i, j);
				board[i][j].setSize(BOX_SIZE+1, BOX_SIZE+1);
				board[i][j].setLocation(50+(BOX_SIZE+1)*j, 100+(BOX_SIZE+1)*i);
				//board[i][j].addMouseListener(new clicked());
				board[i][j].setVisible(true);
				add(board[i][j]);
			}
		}
		newGameButton.setSize(70, 30);
		newGameButton.setLocation(70, winHeight-100);
		aiGame.setSize(70, 30);
		aiGame.setLocation(winLength-140, winHeight-100);
		aiGameWithGuess.setSize(70, 30);
		aiGameWithGuess.setLocation(winLength-240, winHeight-100);
	}
	
	//Check Number
	private boolean checkNum(String s) {
		if (s.length() == 0) {
			return false;
		}
		byte[] sChar = s.getBytes();
		for (int i = 0; i < s.length(); i++) {
			if ((sChar[i]<0x30) || (sChar[i]>0x39)) {
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
		for (int i=0; i<34; i++) {
			f.mineField.init(16, 30, 99, Minefield.TYPE_XP);
			f.mineField.step(0, 0, Minefield.LEFT_CLICK);
		}
	}
}