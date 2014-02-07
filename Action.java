public class Action {
	private int x;
	private int y;
	private int act;

	public static int LEFT_CLICK = 0;
	public static int RIGHT_CLICK = 1;

	public Action(int x, int y, int act) {
		this.x = x;
		this.y = y;
		this.act = act;
//		System.out.println("" + x + "\t" + y + "\t" + act);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getAct() {
		return act;
	}
}