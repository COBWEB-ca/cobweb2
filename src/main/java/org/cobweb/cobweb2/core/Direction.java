package org.cobweb.cobweb2.core;

import java.io.Serializable;


/**
 * 2D direction represented as X and Y deltas
 * -1 < x < 1
 * -1 < y < 1
 */
public class Direction implements Serializable {

	public final int x, y;

	public Direction(int x, int y) {
		this.x = Integer.signum(x);
		this.y = Integer.signum(y);
	}
	/**
	 * Return the angle (between -pi and pi) between the x and y coordinates
	 * of this direction vector. The angle is in standard position.
	 * @return An angle between -pi and pi.
	 */
	public final double heading() {
		return Math.atan2(y, x);
	}

	public boolean equals(Direction other) {
		return x == other.x && y == other.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Direction) {
			return equals((Direction) obj);
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
		return String.format("(%d, %d)", x, y);
	}

	private static final long serialVersionUID = 2L;
}
