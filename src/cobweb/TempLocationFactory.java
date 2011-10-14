package cobweb;

/**
 * A generator of temporary locations.
 * @author Daniel Kats
 */
public class TempLocationFactory {
	/**
	 * Size of the x-axis.
	 */
	private int X_AXIS_SIZE;

	/**
	 * Size of the y-axis.
	 */
	private int Y_AXIS_SIZE;

	public TempLocationFactory(int xAxisSize, int yAxisSize) {
		this.X_AXIS_SIZE = xAxisSize;
		this.Y_AXIS_SIZE = yAxisSize;
	}

	public int getSize(int axis) {
		switch(axis) {
			case Environment.AXIS_X:
				return this.X_AXIS_SIZE;
			case Environment.AXIS_Y:
				return this.Y_AXIS_SIZE;
			default:
				return 0;
		}
	}

	private int verticalFlippedX(TempLocation loc) {
		int addOn = (Math.abs(loc.v[0] - X_AXIS_SIZE / 2));
		int new_x = 0;

		if(loc.v[0] > X_AXIS_SIZE / 2) {
			new_x = X_AXIS_SIZE / 2 - addOn;
		} else {
			new_x = X_AXIS_SIZE / 2 + addOn;
		}

		return new_x;
	}

	private TempLocation getFlippedDown(TempLocation loc) {
		int new_x = this.verticalFlippedX(loc);
		int new_y = -1 * loc.v[1] - 1;

		return new TempLocation(new_x, new_y);
	}

	private TempLocation getFlippedUp(TempLocation loc) {
		int new_x = this.verticalFlippedX(loc);
		int new_y = 2 * Y_AXIS_SIZE - loc.v[1];

		return new TempLocation(new_x, new_y);
	}

	private TempLocation getFlippedRight(TempLocation loc) {
		int new_x = X_AXIS_SIZE + loc.v[0];

		return new TempLocation(new_x, loc.v[1]);
	}

	private TempLocation getFlippedLeft(TempLocation loc) {
		int new_x = -1 * (X_AXIS_SIZE - loc.v[0]);

		return new TempLocation(new_x, loc.v[1]);
	}

	/**
	 * Return a random location.
	 * @return A random location.
	 */
	public TempLocation getRandomLocation() {
		int x = cobweb.globals.random.nextInt(this.X_AXIS_SIZE);
		int y = cobweb.globals.random.nextInt(this.Y_AXIS_SIZE);

		return new TempLocation(x, y);
	}

	public int distanceSquaredTo(TempLocation start, TempLocation other) {
		TempLocation[] flips = new TempLocation[6];
		flips[0] = other;
		flips[1] = this.getFlippedUp(other);
		flips[2] = this.getFlippedDown(other);
		flips[3] = this.getFlippedLeft(other);
		flips[4] = this.getFlippedRight(other);

		//		flips[5] = this;


		//		int bestIndex = 0;

		int minDist = 0, dist;

		for(int i = 0; i < flips.length - 1; i++) {
			dist = start.naiveDistanceSquaredTo(flips[i]);

			if(i == 0 || dist < minDist) {
				minDist = dist;
				//				bestIndex = i;
			}
		}

		//		TempLocation.printPoints(flips);
		//
		//		System.out.println();
		//
		//		System.out.println("Best flip direction was " + (FlipDir.values()[bestIndex]).toString());
		return minDist;
	}

	/**
	 * For testing.
	 * Convert a number into a number spanning 3 chars.
	 * @param j The int.
	 * @return String spanning 3 chars.
	 */
	private static String printNum(int j) {
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

	/**
	 * For testing. Print the grid with all the given points on it.
	 * @param points The wrapped points.
	 */
	private void printPoints(TempLocation[] points) {
		TempLocation current;

		System.out.print("   ");

		for(int i = -1 * this.X_AXIS_SIZE + 1; i < 2 * this.X_AXIS_SIZE; i++) {
			System.out.print(TempLocationFactory.printNum(i));
		}

		System.out.println();

		for(int j = 2 * this.Y_AXIS_SIZE - 1; j >= -1 * this.Y_AXIS_SIZE; j--) {
			System.out.print(TempLocationFactory.printNum(j));

			for(int i = -1 * this.X_AXIS_SIZE + 1; i < 2 * this.X_AXIS_SIZE; i++) {

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
}
