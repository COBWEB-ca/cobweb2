package org.cobweb.cobweb2.core;

import java.io.Serializable;

/**
 * Location on a 2D grid.
 */
public class Location implements Serializable {

	public int x, y;

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equals(Location other) {
		return x == other.x && y == other.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			return equals((Location) obj);
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return ((y & 0xffff) << 16) | (x & 0xffff);
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	private static final long serialVersionUID = 2L;
}