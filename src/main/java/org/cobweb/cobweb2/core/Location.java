package org.cobweb.cobweb2.core;

import java.io.Serializable;

/**
 * Location on a 2D grid.
 */
public class Location implements Serializable {

	public final int x, y;

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Return the angle in radians from this location to the given location.
	 * The angle is between -pi and pi. Angle 0 starts in the east vector direction.
	 * @param location The target location.
	 * @return The angle to the target location in radians.
	 */
	public final double angleTo(Location location) {
		double deltaX = (location.x - this.x);
		double deltaY = (location.y - this.y);
		return Math.atan2(deltaY, deltaX);
	}

	/**
	 * "City-block" distance method; number of single axis steps between
	 * this location and the parameter location.
	 */
	public int cityBlockDistance(Location l) {
		int dist = Math.abs(l.x - x) + Math.abs(l.y - y);
		return dist;
	}

	/** True distance measure */
	public double distance(Location l) {
		return Math.sqrt(distanceSquared(l));
	}

	/**
	 * Distance squared; useful because sometimes the sqrt is irrelevant, as
	 * in isAdjacent.
	 */
	public int distanceSquared(Location l) {
		int deltaX = (l.x - this.x);
		int deltaY = (l.y - this.y);
		return deltaX * deltaX + deltaY * deltaY;
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
		return String.format("(%d,%d)", x, y);
	}

	private static final long serialVersionUID = 2L;
}