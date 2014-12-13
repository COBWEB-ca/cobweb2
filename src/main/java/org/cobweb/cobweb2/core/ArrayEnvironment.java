package org.cobweb.cobweb2.core;

import org.cobweb.util.ArrayUtilities;


public class ArrayEnvironment {

	private int[][] locationBits;

	public ArrayEnvironment(int w, int h) {
		locationBits = new int[w][h];
	}

	public ArrayEnvironment(int w, int h, ArrayEnvironment a) {
		locationBits = ArrayUtilities.resizeArray(a.getBitArray(), w, h);
	}

	public int getAxisCount() {
		return 2;
	}

	public int[][] getBitArray() {
		return locationBits;
	}

	public int getLocationBits(Location l) {
		return locationBits[l.x][l.y];
	}

	public void setLocationBits(Location l, int bits) {
		locationBits[l.x][l.y] = bits;
	}
}
