import java.util.*;

public class Test implements Runnable {

    Random r = null;

    public Test(Random r) {
        this.r = r;
    }

    static int wincnt = 0;
    static int overcnt = 0;
    static int tot_guess = 0;
    static int fail_open = 0;

    final static int ZTHRE = 0;
    final static int OTHRE = 0;

    public void run() {
        Minefield mf = new Minefield();
        mf.setRandom(r);
        Solver solver = new Solver();
        int N_COL = 30, N_ROW = 16, N_MINES = 99;
        // int N_COL = 16, N_ROW = 16, N_MINES = 40;
        // int N_COL = 8, N_ROW = 8, N_MINES = 10;
        // int N_COL = 9, N_ROW = 9, N_MINES = 10;
        // int N_COL = 24, N_ROW = 30, N_MINES = 149;
        // int N_COL = 4, N_ROW = 4, N_MINES = 4;
        int TYPE = Minefield.TYPE_XP;

        int rclk = 0;
        int cclk = 0;

        Action[] actions;

        long t = System.currentTimeMillis();

        //      int[] tot_interval = new int[1001];
        //      int[] err_interval = new int[1001];

        while (true) {
            //System.out.println(wincnt + overcnt);

            mf.init(N_ROW, N_COL, N_MINES, TYPE);
            synchronized (Test.class) {
                mf.step(rclk, cclk, Minefield.LEFT_CLICK);
            }
            // int[][] mb = mf.getMineBoard();
            // for (int i=0; i<N_ROW; i++) {
            //  for (int j=0; j<N_COL; j++) {
            //      if (mb[i][j] == 0) {
            //          mf.step(i, j, Minefield.LEFT_CLICK);
            //      }
            //  }
            // }
            int n_guess = 0;
            while (true) {
                actions = solver.solve(mf.getBoard(), true, N_MINES);
                if (actions.length == 0) {
                    System.out.println("error");
                    mf.showBoard();
                }
                // int cur_interval = (int) Math.round(solver.errRate * 1000);
                // tot_interval[cur_interval]++;
                if (solver.isGuess()) {
                    n_guess++;
                    // mf.showBoard();
                }
                for (int i = 0; i < actions.length; i++) {
                    mf.step(actions[i].getX(), actions[i].getY(), actions[i].getAct());
                }
                if (mf.getGameOver()) {
                    synchronized (Test.class) {

                        if (mf.getWin()) {
                            wincnt++;
                        } else if (mf.getGameOver()) {
                            if (solver.n_zero == 0) {
                                fail_open++;
                            }
                            // err_interval[cur_interval]++;
                            if (solver.n_zero >= ZTHRE && solver.n_zero + solver.opened_cnt >= OTHRE) {
                                overcnt++;
                                if (!solver.isGuess()) {
                                    System.out.println("Error");
                                    mf.showBoard();
                                }
                            }
                            // mf.showBoard();
                            // if (solver.minErrRate != 0.5) {
                            //  mf.showBoard();
                            // }
                        }
                        tot_guess += n_guess;
                        if ((wincnt + overcnt) % 1000 == 0) {
                            double p = wincnt * 1.0 / (wincnt + overcnt);
                            System.out.println(String.format("%d\t%d\t%.2f±%.2f%%\t%.2f%%\t%.4f\t%d",
                                                             wincnt, wincnt + overcnt, wincnt * 100.0 / (wincnt + overcnt),
                                                             196 * Math.sqrt(p * (1 - p) / (wincnt + overcnt)),
                                                             100.0 * fail_open / (wincnt + overcnt),
                                                             tot_guess * 1.0 / (wincnt + overcnt), System.currentTimeMillis() - t));
                            // for (int i=0; i<1000; i++) {
                            //  if (err_interval[i] >= 10) {
                            //      System.out.println(i + "\t" + tot_interval[i] + "\t" + err_interval[i]);
                            //  }
                            // }
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void main(String args[]) {
        Random r = new ec.util.MersenneTwister(1L);
        int N_THREADS = 7;
        for (int i = 0; i < N_THREADS; i++) {
            new Thread(new Test(r)).start();
        }
    }
}