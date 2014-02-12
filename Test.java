import java.util.*;

public class Test {
	public static void main(String args[]) {
		Minefield mf = new Minefield();
		Solver solver = new Solver();
		int N_COL = 30, N_ROW = 16, N_MINES = 99;
//		int N_COL = 16, N_ROW = 16, N_MINES = 40;
//		int N_COL = 8, N_ROW = 8, N_MINES = 10;
//		int N_COL = 20, N_ROW = 20, N_MINES = 50;
		Random r = new Random();
		int TYPE = Minefield.TYPE_XP;
		
		int rclk = 0;
		int cclk = 0;
		
		Action[] actions;
		int wincnt = 0;
		int overcnt = 0;

		int ZTHRE = 0;
		int OTHRE = 0;

		long t = System.currentTimeMillis();

		int tot_guess = 0;

		int[] tot_interval = new int[1001];
		int[] err_interval = new int[1001];

		while (true) {
			//System.out.println(wincnt + overcnt);
			mf.init(N_ROW, N_COL, N_MINES, TYPE);
			mf.step(rclk, cclk, Minefield.LEFT_CLICK);
			int n_guess = 0;
			while (true) {
				actions = solver.solve(mf.getBoard(), true, N_MINES);
				if (actions.length == 0) {
					System.out.println("error");
					mf.showBoard();
				}
				int cur_interval = (int) Math.round(solver.errRate * 1000);
				tot_interval[cur_interval]++;
				if (solver.isGuess()) {
					n_guess++;
//					mf.showBoard();
				}
				for (int i = 0; i < actions.length; i++) {
					mf.step(actions[i].getX(), actions[i].getY(), actions[i].getAct());
				}
				if (mf.getWin()) {
					wincnt++;
					break;
				} else if (mf.getGameOver()) {
					err_interval[cur_interval]++;
					if (solver.n_zero < ZTHRE || solver.n_zero + solver.opened_cnt < OTHRE) {
						//game over when initialization
					} else {
						overcnt++;
						if (!solver.isGuess()) {
							System.out.println("Error");
							mf.showBoard();
						}
					}
//					mf.showBoard();
//					System.out.println(solver.minprob);
					break;
				}
			}
			tot_guess += n_guess;
			if ((wincnt + overcnt) % 100 == 0) {
				System.out.println(String.format("%d\t%d\t%f\t%f\t%d", 
					wincnt, wincnt + overcnt, wincnt * 1.0 / (wincnt + overcnt), tot_guess * 1.0 / (wincnt + overcnt), System.currentTimeMillis() - t));
				for (int i=0; i<1000; i++) {
					if (err_interval[i] >= 10) {
						System.out.println(i + "\t" + tot_interval[i] + "\t" + err_interval[i]);
					}
				}
			}
		}
	}
}