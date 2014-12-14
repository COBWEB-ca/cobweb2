package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.RandomSource;


public class Topology {

	private RandomSource randomSource;
	public final int width;
	public final int height;
	public final boolean wrap;

	// Some predefined directions for 2D
	public static final Direction DIRECTION_NONE =  new Direction(0, 0);
	public static final Direction DIRECTION_NORTH = new Direction(0, -1);
	public static final Direction DIRECTION_EAST =  new Direction(+1, 0);
	public static final Direction DIRECTION_SOUTH = new Direction(0, +1);
	public static final Direction DIRECTION_WEST =  new Direction(-1, 0);
	public static final Direction DIRECTION_NORTHEAST = new Direction(+1, -1);
	public static final Direction DIRECTION_SOUTHEAST = new Direction(+1, +1);
	public static final Direction DIRECTION_SOUTHWEST = new Direction(-1, +1);
	public static final Direction DIRECTION_NORTHWEST = new Direction(-1, -1);

	public Topology(RandomSource randomSource, int width, int height, boolean wrap) {
		this.randomSource = randomSource;
		this.width = width;
		this.height = height;
		this.wrap = wrap;
	}

	public Location getAdjacent(Location location, Direction direction) {
		return getAdjacent(new LocationDirection(location, direction));
	}

	public double getDistance(Location from, Location to) {
		return Math.sqrt(getDistanceSquared(from, to));
	}

	/**
	 * @return Random location.
	 */
	public Location getRandomLocation() {
		Location l;
		do {
			l = new Location(
					randomSource.getRandom().nextInt(width),
					randomSource.getRandom().nextInt(height));
		} while (!isValidLocation(l));
		return l;
	}

	public boolean isValidLocation(Location l) {
		return l.x >= 0 && l.x < width
				&& l.y >= 0 && l.y < height;
	}


	public LocationDirection getAdjacent(LocationDirection location) {
		Direction direction = location.direction;
		int x = location.x + direction.x;
		int y = location.y + direction.y;

		if (wrap) {
			x = (x + width) % width;
			boolean flip = false;
			if (y < 0) {
				y = -y - 1;
				flip = true;
			} else if (y >= height) {
				y = height * 2 - y - 1;
				flip = true;
			}
			if (flip) {
				x = (x + width / 2) % width;
				direction = new Direction(-direction.x, -direction.y);
			}
		} else {
			if ( x < 0 || x >= width || y < 0 || y >= height)
				return null;
		}
		return new LocationDirection(new Location(x, y), direction);
	}

	public double getDistanceSquared(Location from, Location to) {
		double distance = Double.MAX_VALUE;

		for(Location virtual : getWrapVirtualLocations(to)) {
			double d = simpleDistanceSquared(from, virtual);
			if (d < distance)
				distance = d;
		}

		return distance;
	}

	private static double simpleDistanceSquared(Location from, Location to) {
		int deltaX = to.x - from.x;
		int deltaY = to.y - from.y;
		return deltaX * deltaX + deltaY * deltaY;
	}

	private List<Location> getWrapVirtualLocations(Location l) {
		List<Location> result = new ArrayList<Location>(7);
		result.add(l);

		if (wrap) {
			// wrap left
			result.add(new Location(l.x - width, l.y));
			// wrap right
			result.add(new Location(l.x + width, l.y));

			// wrap down left
			result.add(new Location(l.x - width + width / 2, 2 * height - l.y - 1));
			// wrap down right
			result.add(new Location(l.x + width / 2,         2 * height - l.y - 1));

			// wrap up left
			result.add(new Location(l.x - width + width / 2, - l.y - 1));
			// wrap up right
			result.add(new Location(l.x + width / 2,         - l.y - 1));
		}

		return result;
	}

	public LocationDirection getTurnRightPosition(LocationDirection location) {
		return new LocationDirection(location, new Direction(-location.direction.y, location.direction.x));
	}

	public LocationDirection getTurnLeftPosition(LocationDirection location) {
		return new LocationDirection(location, new Direction(location.direction.y, -location.direction.x));
	}
}
