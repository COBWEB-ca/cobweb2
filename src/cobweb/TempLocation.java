package cobweb;


public class TempLocation {
	public static void main(String args[]) {
		//create a new location...	

		TempLocation a, b;

		a = TempLocation.randomGen();
		b = a;

		while(b.equals(a)) {
			b = TempLocation.randomGen();
		}

		System.out.println("Point a was " + a.toString());
		System.out.println("Point b was " + b.toString());

		TempLocation.printLegend();

		int dist = a.distanceSquaredTo(b);
		System.out.println("Min square distance is " + dist);
	}

	public static void printLegend() {
		FlipDir[] vals = FlipDir.values();

		for(int i = 0; i < vals.length; i++) {
			System.out.println(i + " - " + vals[i].toString());
		}
	}

	public static String printNum(int j) {
		StringBuilder s = new StringBuilder();

		if(j < -9) {
			s.append(j);
		} else if(j > -10 && j < 0) {
			s.append("-0" + Math.abs(j));
		} else if(j < 10) {
			s.append("+0" + j);
		} else {
			s.append("+" + j);
		}

		return s.toString();
	}

	public static void printPoints(TempLocation[] points) {
		TempLocation current;

		System.out.print("   ");

		for(int i = -1 * TempLocation.X_AXIS_SIZE + 1; i < 2 * TempLocation.X_AXIS_SIZE; i++) {
			System.out.print(TempLocation.printNum(i));
		}

		System.out.println();

		for(int j = 2 * TempLocation.Y_AXIS_SIZE - 1; j >= -1 * TempLocation.Y_AXIS_SIZE; j--) {
			System.out.print(TempLocation.printNum(j));

			for(int i = -1 * TempLocation.X_AXIS_SIZE + 1; i < 2 * TempLocation.X_AXIS_SIZE; i++) {

				//looking at coordinates (i, j)
				current = new TempLocation(i, j);

				int equals = -1;

				for(int a = 0; a < points.length; a++) {
					if(current.equals(points[a])) {
						equals = a;
					} 
				}

				char c = '.';

				if(equals == 0) {
					c = 'B';
				} else if (equals == 5) {
					c = 'A';
				} else if(equals > 0) {
					c = Integer.toString(equals).toCharArray()[0];
				}

				System.out.print(" " + c + " ");
			}

			System.out.println();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TempLocation)) {
			return false;
		}

		TempLocation otherCoord = (TempLocation) other;

		return otherCoord.v[0] == this.v[0] && otherCoord.v[1] == this.v[1];
	}

	@Override
	public String toString() {
		return "(" + this.v[0] + ", " + this.v[1] + ")";
	}

	public static TempLocation randomGen() {
		int x = (int) (Math.random() * TempLocation.X_AXIS_SIZE);
		int y = (int) (Math.random() * TempLocation.Y_AXIS_SIZE);

		return new TempLocation(x, y);
	}

	public int[] v;

	private static final int X_AXIS_SIZE = 10;

	private static final int Y_AXIS_SIZE = 10;

	private static enum FlipDir {
		B_NONE,
		UP,
		DOWN,
		LEFT,
		RIGHT,
		A_NONE
	}

	public TempLocation(int x, int y) {
		this.v = new int[2];
		v[0] = x;
		v[1] = y;
	}

	public int distanceSquaredTo(TempLocation other) {
		TempLocation[] flips = new TempLocation[6];
		flips[0] = other;
		flips[1] = other.getFlippedUp();
		flips[2] = other.getFlippedDown();
		flips[3] = other.getFlippedLeft();
		flips[4] = other.getFlippedRight();

		flips[5] = this;


		int bestIndex = 0;
		int minDist = 0, dist;

		for(int i = 0; i < flips.length - 1; i++) {
			dist = this.naiveDistanceSquaredTo(flips[i]);

			if(i == 0 || dist < minDist) {
				minDist = dist;
				bestIndex = i;
			}
		}

		TempLocation.printPoints(flips);

		System.out.println();

		System.out.println("Best flip direction was " + (FlipDir.values()[bestIndex]).toString());

		return minDist;
	}

	private int naiveDistanceSquaredTo(TempLocation other) {
		return (int) (Math.pow(other.v[0] - this.v[0], 2) + Math.pow(other.v[1] - this.v[1], 2));
	}

	private TempLocation getFlippedDown() {
		int new_x = this.verticalFlippedX();
		int new_y = -1 * this.v[1] - 1;

		return new TempLocation(new_x, new_y);
	}

	private int verticalFlippedX() {
		int addOn = (Math.abs(this.v[0] - X_AXIS_SIZE / 2));
		int new_x = 0;

		if(this.v[0] > X_AXIS_SIZE / 2) {
			new_x = X_AXIS_SIZE / 2 - addOn;
		} else {
			new_x = X_AXIS_SIZE / 2 + addOn;
		}

		return new_x;
	}

	private TempLocation getFlippedUp() {
		int new_x = this.verticalFlippedX();
		int new_y = 2 * Y_AXIS_SIZE - this.v[1];

		return new TempLocation(new_x, new_y);
	}

	private TempLocation getFlippedRight() {
		int new_x = X_AXIS_SIZE + this.v[0];

		return new TempLocation(new_x, this.v[1]);
	}

	private TempLocation getFlippedLeft() {
		int new_x = -1 * (X_AXIS_SIZE - this.v[0]);

		return new TempLocation(new_x, this.v[1]);
	}
}
