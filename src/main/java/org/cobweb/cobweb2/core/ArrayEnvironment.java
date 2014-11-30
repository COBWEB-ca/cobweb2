package org.cobweb.cobweb2.core;


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
		locationBits = org.cobweb.util.ArrayUtilities.resizeArray(a.getBitArray(), w, h);
	}

	public int getAxisCount() {
		return 2;
	}

	public int[][] getBitArray() {
		return locationBits;
	}

	public int getLocationBits(org.cobweb.cobweb2.core.Location l) {
		return locationBits[l.v[0]][l.v[1]];
	}

	public int getSize(int axis) {
		switch (axis) {
			case org.cobweb.cobweb2.core.Environment.AXIS_X:
				return width;
			case org.cobweb.cobweb2.core.Environment.AXIS_Y:
				return height;
			default:
				return 0;
		}
	}

	public void setLocationBits(org.cobweb.cobweb2.core.Location l, int bits) {
		locationBits[l.v[0]][l.v[1]] = bits;
	}
}
