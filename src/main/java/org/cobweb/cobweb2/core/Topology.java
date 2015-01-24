package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.List;


public class Topology {

	private RandomSource randomSource;
	public final int width;
	public final int height;
	private final boolean wrap;

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
		return simpleDistanceSquared(from, getClosestWrapLocation(from, to));
	}

	protected double simpleDistanceSquared(Location from, Location to) {
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

	private Location getClosestWrapLocation(Location zero, Location target) {
		if (!wrap)
			return target;

		double distance = Double.MAX_VALUE;
		Location best = target;
		for(Location virtual : getWrapVirtualLocations(target)) {
			double d = simpleDistanceSquared(zero, virtual);
			if (d < distance) {
				distance = d;
				if (best != virtual)
					best = virtual;
			}
		}
		return best;
	}

	public LocationDirection getTurnRightPosition(LocationDirection location) {
		return new LocationDirection(location, turnRight(location.direction));
	}

	public LocationDirection getTurnLeftPosition(LocationDirection location) {
		return new LocationDirection(location, turnLeft(location.direction));
	}

	protected Direction turnRight(Direction dir) {
		return new Direction(-dir.y, +dir.x);
	}

	protected Direction turnLeft(Direction dir) {
		return new Direction(+dir.y, -dir.x);
	}

	public Rotation getRotationBetween(Direction from, Direction to) {
		if (from.equals(to))
			return Rotation.None;
		else if (turnRight(from).equals(to))
			return Rotation.Right;
		else if (turnLeft(from).equals(to))
			return Rotation.Left;
		else
			return Rotation.UTurn;
	}

	public Direction getDirectionBetween4way(Location from, Location to) {
		to = getClosestWrapLocation(from, to);

		int deltaX = to.x - from.x;
		int deltaY = to.y - from.y;
		if (deltaX == 0 && deltaY == 0)
			return NONE;

		// Split circle into 8 sections
		// -3 -2 -1
		//   \ | /
		//  ------- 0
		//   / | \
		//  3  2  1
		double division = Math.atan2(deltaY, deltaX) / Math.PI * 4;

		if      (division >= -3 && division < -1)
			return NORTH;
		else if (division >= -1 && division <  1)
			return EAST;
		else if (division >=  1 && division <  3)
			return SOUTH;
		else
			return WEST;
	}

	public Direction getDirectionBetween8way(Location from, Location to) {
		to = getClosestWrapLocation(from, to);

		int deltaX = to.x - from.x;
		int deltaY = to.y - from.y;
		if (deltaX == 0 && deltaY == 0)
			return NONE;

		double division = Math.atan2(deltaY, deltaX) / Math.PI * 8;

		if      (division >= -7 && division < -5)
			return NORTHWEST;
		if      (division >= -5 && division < -3)
			return NORTH;
		if      (division >= -3 && division < -1)
			return NORTHEAST;
		else if (division >= -1 && division <  1)
			return EAST;
		else if (division >=  1 && division <  3)
			return SOUTHEAST;
		else if (division >=  3 && division <  5)
			return SOUTH;
		else if (division >=  5 && division <  7)
			return SOUTHWEST;
		else
			return WEST;
	}

	// Some predefined directions for 2D
	public static final Direction NONE =  new Direction(0, 0);
	public static final Direction NORTH = new Direction(0, -1);
	public static final Direction EAST =  new Direction(+1, 0);
	public static final Direction SOUTH = new Direction(0, +1);
	public static final Direction WEST =  new Direction(-1, 0);
	public static final Direction NORTHEAST = new Direction(+1, -1);
	public static final Direction SOUTHEAST = new Direction(+1, +1);
	public static final Direction SOUTHWEST = new Direction(-1, +1);
	public static final Direction NORTHWEST = new Direction(-1, -1);

	public final Direction[] ALL_4_WAY = {
			NORTH, EAST,
			SOUTH, WEST
	};

	public final Direction[] ALL_8_WAY = {
			NORTH, EAST,
			SOUTH, WEST,
			NORTHEAST, SOUTHEAST,
			SOUTHWEST, NORTHWEST
	};

	public Direction getRandomDirection() {
		int i = randomSource.getRandom().nextInt(ALL_4_WAY.length);
		return ALL_4_WAY[i];
	}

}
