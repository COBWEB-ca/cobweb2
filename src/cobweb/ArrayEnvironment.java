package cobweb;


public class ArrayEnvironment {

	private int width;

	private int height;

	private int[][] locationBits;

	public ArrayEnvironment(int w, int h) {
		width = w;
		height = h;
		locationBits = new int[w][h];
	}

	public ArrayEnvironment(int w, int h, ArrayEnvironment a) {
		width = w;
		height = h;
		locationBits = cobweb.ArrayUtilities.resizeArray(a.getBitArray(), w, h);
	}

	public int getAxisCount() {
		return 2;
	}

	public int[][] getBitArray() {
		return locationBits;
	}

	public int getLocationBits(cobweb.Environment.Location l) {
		return locationBits[l.v[0]][l.v[1]];
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

	public void setLocationBits(cobweb.Environment.Location l, int bits) {
		locationBits[l.v[0]][l.v[1]] = bits;
	}
}
