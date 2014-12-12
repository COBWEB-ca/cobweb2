package org.cobweb.cobweb2.core;


public class ArrayEnvironment {

	private int[][] locationBits;

	public ArrayEnvironment(int w, int h) {
		locationBits = new int[w][h];
	}

	public ArrayEnvironment(int w, int h, ArrayEnvironment a) {
		locationBits = org.cobweb.util.ArrayUtilities.resizeArray(a.getBitArray(), w, h);
	}

	public int getAxisCount() {
		return 2;
	}

	public int[][] getBitArray() {
		return locationBits;
	}

	public int getLocationBits(org.cobweb.cobweb2.core.Location l) {
		return locationBits[l.x][l.y];
	}

	public void setLocationBits(org.cobweb.cobweb2.core.Location l, int bits) {
		locationBits[l.x][l.y] = bits;
	}
}
