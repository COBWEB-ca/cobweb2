package cobweb;


public class ArrayEnvironment {
	public ArrayEnvironment(int w, int h, ArrayEnvironment a) {
		width = w;
		height = h;
		int[] indices = { w, h };
		locationBits = (long[][]) cobweb.ArrayUtilities.initArray(a
				.getBitArray(), indices, 0);
	}

	public ArrayEnvironment(int w, int h) {
		width = w;
		height = h;
		locationBits = new long[w][h];
	}

	public int getAxisCount() {
		return 2;
	}

	public int getSize(int axis) {
		switch (axis) {
		case cobweb.Environment.AXIS_X:
			return width;
		case cobweb.Environment.AXIS_Y:
			return height;
		default:
			return 0;
		}
	}

	public long getLocationBits(cobweb.Environment.Location l) {
		return locationBits[l.v[0]][l.v[1]];
	}

	public void setLocationBits(cobweb.Environment.Location l, long bits) {
		locationBits[l.v[0]][l.v[1]] = bits;
	}

	public long[][] getBitArray() {
		return locationBits;
	}

	private int width;

	private int height;

	private long[][] locationBits;
}