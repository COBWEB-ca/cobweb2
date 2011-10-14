package cobweb;

/**
 * A temporary location
 * @author Daniel Kats
 */
public class TempLocation {
	/**
	 * For testing.
	 */
	public static void main(String args[]) {
		//create a new location...	

		TempLocationFactory factory = new TempLocationFactory(10, 10);

		TempLocation a, b;

		a = factory.getRandomLocation();
		b = a;

		while(b.equals(a)) {
			b = factory.getRandomLocation();
		}

		System.out.println("Point a was " + a.toString());
		System.out.println("Point b was " + b.toString());

		TempLocation.printLegend();

		//		int dist = a.distanceSquaredTo(b);
		//		System.out.println("Min square distance is " + dist);
	}


	/**
	 * For testing.
	 * Prints the legend.
	 */
	private static void printLegend() {
		FlipDir[] vals = FlipDir.values();

		for(int i = 0; i < vals.length; i++) {
			System.out.println(i + " - " + vals[i].toString());
		}
	}

	/**
	 * Compare one location to another.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TempLocation)) {
			return false;
		}

		TempLocation otherCoord = (TempLocation) other;

		return otherCoord.v[0] == this.v[0] && otherCoord.v[1] == this.v[1];
	}

	/**
	 * Convert the coordinate to a string.
	 */
	@Override
	public String toString() {
		return "(" + this.v[0] + ", " + this.v[1] + ")";
	}

	public int[] v;

	/**
	 * For testing.
	 * Directions to flip.
	 * @author Daniel Kats
	 */
	private static enum FlipDir {
		B_NONE,
		UP,
		DOWN,
		LEFT,
		RIGHT
	}


	/**
	 * Create a new temp. location.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public TempLocation(int x, int y) {
		this.v = new int[2];
		v[0] = x;
		v[1] = y;
	}


	/**
	 * Calculate the native distance squared from this location to the other.
	 * Calculated in the regular Cartesian way.
	 * @param other The other location.
	 * @return The square distance.
	 */
	public int naiveDistanceSquaredTo(TempLocation other) {
		return (int) (Math.pow(other.v[0] - this.v[0], 2) + Math.pow(other.v[1] - this.v[1], 2));
	}	
}
