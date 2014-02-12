import java.util.*;

public class Solver {
	private static int[] y_near = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
	private static int[] x_near = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

	private static double[] fact = new double[3320];
	private static double EPS = 1e-8;
	private static int[][] Cnk = new int[9][9];
	private static double Cnkx(int n, int k) {
		return n < k ? 0 : Math.exp(fact[n] - fact[k] - fact[n-k] - 300);
	}
	static {
		fact[0] = 0.0;
		for (int i=1; i<3320; i++) {
			fact[i] = fact[i-1] + Math.log(i);
		}
		for (int i=0; i<9; i++) {
			Cnk[i][0] = Cnk[i][i] = 1;
			for (int j=1; j<i; j++) {
				Cnk[i][j] = Cnk[i-1][j-1] + Cnk[i-1][j];
			}
		}
	}

	int[][] board;
	int h;
	int w;
	ArrayList<Action> ret;
	double errRate;
	int n_zero;

	boolean isGuess() {
		return errRate > EPS;
	}

	int unopened_cnt = 0;
	int opened_cnt = 0;
	int remain_mines = 0;

	Action[] solve(int[][] board, boolean guess, int remain_mines) {
		this.errRate = 0.0;
		this.h = board.length;
		this.w = board[0].length;
		this.board = new int[h][w];
		this.ret = new ArrayList<Action>();
		this.n_zero = 0;
		this.remain_mines = remain_mines;
		for (int i=0; i<h; i++) {
			for (int j=0; j<w; j++) {
				this.board[i][j] = board[i][j];
			}
		}

		opened_cnt = 0;
		unopened_cnt = 0;

		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				int val = getValue(x, y);
				if (val == 0) {
					n_zero++;
					setValue(x, y, -3);
				} else if (val == -2) {
					unopened_cnt++;
				} else if (isNumber(val)) {
					opened_cnt++;
				}
			}
		}

		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				if (getValue(x, y) == -1) {
					deleteMines(x, y);
				}
			}
		}

		int retLength = 0;
		do {
			retLength = ret.size();
			
			proc0();

			if (retLength != ret.size()) {
				continue;
			}

			proc1();

			if (ret.size() != 0) {
				continue;
			}

			proc2();

		} while (retLength != ret.size());

		if (ret.size() == 0) {
			calc();
		}

		if (ret.size() == 0 && guess) {
			if (minErrPoint != null) {
				this.errRate = minErrRate;
				return new Action[]{new Action(minErrPoint.x, minErrPoint.y, Action.LEFT_CLICK)};
			}
			double p_space = 0.0;
			Point minsval = null;
			for (int x=0; x<h; x++) {
				for (int y=0; y<w; y++) {
					if (getValue(x, y) == -2 && info[x][y] == 0) {
						double cur_p = getSpaceProb(x, y, 0xff);
						if (cur_p >= p_space) {
							p_space = cur_p;
							minsval = new Point(x, y);
						}
					}
				}
			}
			if (minsval != null) {
				this.errRate = p_blind;
				return new Action[]{new Action(minsval.x, minsval.y, Action.LEFT_CLICK)};
			}
			System.out.println('e');
			showBoard(board);
		}
		return ret.toArray(new Action[0]);
	}

	void proc0() {
		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				int val = getValue(x, y);
				if (val == 0) {
					openNear(x, y, 0xff);
					setValue(x, y, -3);
				} else if (val > 0 && val < 9 && val == unopenedBlock(x, y, 0xff)) {
					markNear(x, y, 0xff);
					setValue(x, y, -3);
				}
			}
		}		
		if (remain_mines == 0) {
			for (int x=0; x<h; x++) {
				for (int y=0; y<w; y++) {
					if (getValue(x, y) == -2) {
						openMines(x, y);
					}
				}
			}
		}
	}

	void proc1() {
		//check (x, y) and (x+1, y), (x, y) and (x, y+1)
		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				procpair(x, y, x, y+1, 0x07, 0xe0);
				procpair(x, y, x+1, y, 0x29, 0x94);
			}
		}
	}

	void proc2() {
		//check (x-1, y) and (x+1, y)
		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				if (!isNumber(getValue(x, y))) {
					continue;
				}
				procpair(x, y, x  , y+2, 0x1f, 0xf8);
				procpair(x, y, x+1, y+1, 0x2f, 0xf4);
				procpair(x, y, x+2, y  , 0x6b, 0xd6);
				procpair(x, y, x+1, y+2, 0x3f, 0xfc);
				procpair(x, y, x+2, y+1, 0x6f, 0xf6);
				procpair(x, y, x+2, y+2, 0x7f, 0xfe);
				procpair(x, y, x-1, y+1, 0x97, 0xe9);
				procpair(x, y, x-1, y+2, 0x9f, 0xf9);
				procpair(x, y, x-2, y+1, 0xd7, 0xeb);
				procpair(x, y, x-2, y+2, 0xdf, 0xfb);
			}
		}		
	}

	void procpair(int x1, int y1, int x2, int y2, int mask1, int mask2) {
		int val1 = getValue(x1, y1);
		int val2 = getValue(x2, y2);
		if (!isNumber(val1) || !isNumber(val2)) {
			return;
		}
		int mine1 = unopenedBlock(x1, y1, mask1);
		int mine2 = unopenedBlock(x2, y2, mask2);
		if (mine2 == val2 - val1) {
			openNear(x1, y1, mask1);
			markNear(x2, y2, mask2);
		}
		if (mine1 == val1 - val2) {
			openNear(x2, y2, mask2);						
			markNear(x1, y1, mask1);
		}
		if (mine1 == 0 && mine2 == 0) {
			setValue(x2, y2, -3);
		}
	}

	/**
	 *	get the value at (x, y)
	 *	-1: marked mine
	 *	-2: unopened
	 *	-3: unavailable
	 */
	int getValue(int x, int y) {
		if (x < 0 || x >= h || y < 0 || y >= w) {
			return -3;
		}
		return board[x][y];
	}

	void setValue(int x, int y, int val) {
		if (x < 0 || x >= h || y < 0 || y >= w) {
			return;
		}
		board[x][y] = val;	
	}

	boolean isNumber(int val) {
		return val > 0 && val < 9;
	}

	int unopenedBlock(int x, int y, int mask) {
		int ret = 0;
		for (int i=0; i<8; i++, mask >>= 1) {
			if ((mask & 1) == 1 && getValue(x + x_near[i], y + y_near[i]) == -2) {
				ret++;
			}
		}
		return ret;
	}

	int openedBlock(int x, int y, int mask) {
		int ret = 0;
		for (int i=0; i<8; i++, mask >>= 1) {
			if ((mask & 1) == 1 && isNumber(getValue(x + x_near[i], y + y_near[i]))) {
				ret++;
			}
		}
		return ret;
	}

	void markMines(int x, int y) {
		if (getValue(x, y) == -2) {
			ret.add(new Action(x, y, Action.RIGHT_CLICK));
			deleteMines(x, y);
		}
	}

	void openMines(int x, int y) {
		if (getValue(x, y) == -2) {
			ret.add(new Action(x, y, Action.LEFT_CLICK));
			setValue(x, y, -3);
		}
	}

	void markNear(int x, int y, int mask) {
		for (int i=0; i<8; i++, mask >>= 1) {
			if ((mask & 1) == 1) {
				markMines(x + x_near[i], y + y_near[i]);
			}
		}		
	}

	void openNear(int x, int y, int mask) {
		for (int i=0; i<8; i++, mask >>= 1) {
			if ((mask & 1) == 1) {
				openMines(x + x_near[i], y + y_near[i]);
			}
		}		
	}

	void deleteMines(int x, int y) {
		for (int i=0; i<8; i++) {
			int val = getValue(x + x_near[i], y + y_near[i]);
			if (val > 0 && val < 9) {
				setValue(x + x_near[i], y + y_near[i], val - 1);
			}
		}
		setValue(x, y, -3);
		remain_mines--;
	}

	int[][] info;
	int[][] masks;
	double[][] probability;
	Point[][] parent;

	int markinfo(int x, int y, boolean mines, ArrayList<Point> ret_mines, ArrayList<Point> ret_numbers) {
		int val = getValue(x, y);
		if (mines ? val != -2 : !isNumber(val)) {
			return 0;
		}
		if (info[x][y] != 0) {
			return 1;
		}
		info[x][y] = -1;
		(mines ? ret_mines : ret_numbers).add(new Point(x, y));
		int mask = masks[x][y] & 0xff;
		for (int i=0; mask!=0; i++, mask>>=1) {
			if ((mask & 1) == 1) {
				markinfo(x + x_near[i], y + y_near[i], !mines, ret_mines, ret_numbers);
			}
		}

		return 1;
	}


	double minErrRate = 1.0;
	Point minErrPoint = null;

	int n_blind = 0;
	double p_blind = 0.0;

	int getMask(int x, int y) {
		int val = getValue(x, y);
		boolean mine;
		if (val == -2) {
			mine = true;
		} else if (isNumber(val)) {
			mine = false;
		} else {
			return 0;
		}
		int ret = mine ? 0x100 : 0;
		for (int i=0; i<8; i++) {
			int v = getValue(x + x_near[i], y + y_near[i]);
			if (mine ? isNumber(v) : v == -2) {
				ret |= 1 << i;
			}
		}
		return ret;
	}

	void procSimilar(int x1, int y1, int x2, int y2, int mask1, int mask2) {
		if (getValue(x2, y2) != -2 || masks[x2][y2] == 0) {
			return;
		}
		if ((masks[x1][y1] & mask1) != 0 || (masks[x2][y2] & mask2) != 0) {
			return;
		}
		masks[x1][y1] += masks[x2][y2] & 0xffffff00;
		if (parent[x2][y2] != null) {
			System.out.println("e_parent");
		}
		parent[x2][y2] = new Point(x1, y1);
		masks[x2][y2] = 0;
	}

	void procInfo() {
		info = new int[h][w];
		masks = new int[h][w];
		parent = new Point[h][w];
		n_blind = 0;
		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				masks[x][y] = getMask(x, y);
				if (masks[x][y] == 0x100) {
					n_blind++;
					masks[x][y] = 0;
				}
			}
		}

		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				if (getValue(x, y) != -2 || masks[x][y] == 0) {
					continue;
				}
				procSimilar(x, y, x, y+1, 0x07, 0xe0);
				procSimilar(x, y, x+1, y, 0x29, 0x94);
				procSimilar(x, y, x  , y+2, 0x1f, 0xf8);
				procSimilar(x, y, x+1, y+1, 0x2f, 0xf4);
				procSimilar(x, y, x+2, y  , 0x6b, 0xd6);
				procSimilar(x, y, x+1, y+2, 0x3f, 0xfc);
				procSimilar(x, y, x+2, y+1, 0x6f, 0xf6);
				procSimilar(x, y, x+2, y+2, 0x7f, 0xfe);
				procSimilar(x, y, x-1, y+1, 0x97, 0xe9);
				procSimilar(x, y, x-1, y+2, 0x9f, 0xf9);
				procSimilar(x, y, x-2, y+1, 0xd7, 0xeb);
				procSimilar(x, y, x-2, y+2, 0xdf, 0xfb);
			}
		}
	}

	double getSpaceProb(int x, int y, int mask) {
		double ret = 1.0 - getMineProb(x, y);
		for (int i=0; i<8; i++) {
			ret *= 1.0 - getMineProb(x + x_near[i], y + y_near[i]);
		}
		return ret;
	}

	double getMineProb(int x, int y) {
		if (getValue(x, y) != -2) {
			return (x < 0 || x >= h || y < 0 || y >= w) ? 0.0 : probability[x][y];
		}
		if (info[x][y] == 0) {
			return p_blind;
		}
		if (parent[x][y] != null) {
			probability[x][y] = getMineProb(parent[x][y].x, parent[x][y].y);
			parent[x][y] = null;
		}
		return probability[x][y];
	}

	void calc() {
		minErrPoint = null;
		procInfo();
		probability = new double[h][w];
		p_blind = 0.0;
		ArrayList<Point> ret_mines = new ArrayList<Point>();
		ArrayList<Point> ret_numbers = new ArrayList<Point>();
		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				if (getValue(x, y) == -2 && masks[x][y] != 0 && info[x][y] == 0) {
					markinfo(x, y, true, ret_mines, ret_numbers);
				}
			}
		}
		Point[] mines = ret_mines.toArray(new Point[0]);
		Point[] numbers = ret_numbers.toArray(new Point[0]);
		prob_sum = 0.0;
		doSearch(mines, numbers, 0, remain_mines, 1);
		p_blind = p_blind / prob_sum;
		for (int x=0; x<h; x++) {
			for (int y=0; y<w; y++) {
				if (info[x][y] == -1) {
					probability[x][y] /= prob_sum;
				}
			}
		}		
		minErrRate = n_blind == 0 ? 1.0 : p_blind;
		if (p_blind < EPS || p_blind > 1.0 - EPS) {
			for (int x=0; x<h; x++) {
				for (int y=0; y<w; y++) {
					if (getValue(x, y) == -2 && info[x][y] == 0) {
						ret.add(new Action(x, y, p_blind < EPS ? Action.LEFT_CLICK : Action.RIGHT_CLICK));
					}
				}
			}			
		}
		for (int i=0; i<ret.size(); i++) {
			Action act = ret.get(i);
			if (act.getAct() == Action.RIGHT_CLICK) {
				probability[act.getX()][act.getY()] = 1.0;
			}
		}
		for (int i=0; i<mines.length; i++) {
			if ((masks[mines[i].x][mines[i].y] >> 8) == 0) {
				continue;
			}
			double curprob = probability[mines[i].x][mines[i].y];
			if (curprob < EPS) {
				ret.add(new Action(mines[i].x, mines[i].y, Action.LEFT_CLICK));
			} else if (curprob > 1.0 - EPS) {
				ret.add(new Action(mines[i].x, mines[i].y, Action.RIGHT_CLICK));
			}
			if (curprob < minErrRate) {
				minErrRate = curprob;
				minErrPoint = mines[i];
			}
		}
	}

	double prob_sum = 0;

	void doSearch(Point[] mines, Point[] numbers, int idx, int n_mines, double mult) {
		if (idx == mines.length) {
			if (n_blind < n_mines) {
				return;
			}
			double prob = Cnkx(n_blind, n_mines) * mult;
			prob_sum += prob;
			p_blind += prob * n_mines / n_blind;
			for (int i=0; i<idx; i++) {
				int val = info[mines[i].x][mines[i].y];
				if (val != 0) {
					probability[mines[i].x][mines[i].y] += prob * val / (masks[mines[i].x][mines[i].y] >> 8);
				}
			}
		} else {
			int x = mines[idx].x;
			int y = mines[idx].y;
			int max_mines = masks[x][y] >> 8;
			for (int i=0; i<=Math.min(max_mines, n_mines); i++) {
				info[x][y] = i;
				if (isValidMine(x, y)) {
					doSearch(mines, numbers, idx + 1, n_mines - i, mult * Cnk[max_mines][i]);
				}
			}
			info[x][y] = -1;
		}
	}

	boolean isValidMine(int x, int y) {
		int mask = masks[x][y] & 0xff;
		for (int i=0; mask!=0; i++, mask>>=1) {
			if (((mask & 1) == 1) && !isValidNumber(x + x_near[i], y + y_near[i])) {
				return false;
			}
		}
		return true;
	}

	boolean isValidNumber(int x, int y) {
		int blank = 0;
		int number = board[x][y];
		int mask = masks[x][y] & 0xff;
		for (int i=0; mask!=0; i++, mask>>=1) {
			if ((mask & 1) == 0) {
				continue;
			}
			int val = info[x + x_near[i]][y + y_near[i]];
			if (val != -1) {
				number -= val;
			} else {
				blank += masks[x + x_near[i]][y + y_near[i]] >> 8;
			}
		}
		return number >= 0 && number <= blank;
	}


	public void showBoard(int[][] board) {
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				switch (board == masks ? (board[i][j] >> 8) : board[i][j]) {
				case -3 : System.out.print("X"); break;
				case -2 : System.out.print("-"); break;
				case -1 : System.out.print("o"); break;
			//	case 9  : System.out.print("*"); break;
				case 0	: System.out.print(" "); break;
				default : System.out.print(Integer.toString(board == masks ? (board[i][j] >> 8) : board[i][j]));
				}
			}
			System.out.println();
		}
		for (int i = 0; i < 1; i++) {
			System.out.println();
		}
	}


	class Point {
		int x, y;
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}