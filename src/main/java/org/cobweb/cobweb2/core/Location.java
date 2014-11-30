package org.cobweb.cobweb2.core;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Public accessor to Location state information. Note that this class is
 * simply a pass-through to the private getField/setField and
 * getFlag/setFlag calls in the environment. This design allows for a
 * logical and clear code style in agents and agent controllers, as the
 * notion of position in the environment allows immediate access to state
 * information.
 */
public class Location implements Serializable {

	/**
	 * 
	 */
	private final Environment environment;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3224450412135996690L;
	/**
	 * Sometimes it's essential to get the coordinates; iteration is an
	 * example of this. To make this as fast as possible, no accessors are
	 * provided for the coordinate array, instead they're public. Bad form,
	 * but as fast as possible.
	 */
	public int[] v;

	/**
	 * Private constructor, as only the environment should create locations.
	 */
	Location(Environment environment, int[] axisPos) {

		this.environment = environment;
		v = new int[this.environment.getAxisCount()];
		for (int i = 0; i < this.environment.getAxisCount(); ++i)
			v[i] = axisPos[i];
	}

	/**
	 * Return the angle in radians from this position to the given position.
	 * The angle is between -pi and pi. Angle 0 starts in the east vector direction.
	 * @param location The target location.
	 * @return The angle to the target location in radians.
	 */
	public final double angleTo(Location location) {
		double deltaX = (location.v[0] - this.v[0]);
		double deltaY = (location.v[1] - this.v[1]);
		return Math.atan2(deltaY, deltaX);
	}

	/**
	 * @param dir The direction of the agent
	 * @return True if axis wraps and the next location is off the map
	 */
	public boolean checkFlip(Direction dir) {
		int y = v[1] + dir.v[1];
		return (y < 0 || y >= this.environment.getSize(Environment.AXIS_Y)) && this.environment.getAxisWrap(Environment.AXIS_Y);
	}

	/**
	 * "City-block" distance method; number of single axis steps between
	 * this location and the parameter location.
	 */
	public int cityBlockDistance(Location l) {
		int dist = 0;
		for (int i = 0; i < v.length; ++i)
			dist += Math.abs(v[i] - l.v[i]);
		return dist;
	}

	/** True distance measure */
	public double distance(Location l) {
		return Math.sqrt(distanceSquare(l));
	}

	/**
	 * Distance squared; useful because sometimes the sqrt is irrelevant, as
	 * in isAdjacent.
	 */
	public int distanceSquare(Location l) {
		int dist = 0;
		for (int i = 0; i < v.length; ++i) {
			int delta = v[i] - l.v[i];
			dist += delta * delta;
		}
		return dist;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Location))
			return false;

		Location lOther = (Location) other;
		if (lOther.v.length != v.length)
			return false;
		for (int i = 0; i < v.length; ++i)
			if (v[i] != lOther.v[i])
				return false;
		return true;
	}

	/**
	 * @return the location adjacent to this one, along direction d. Null
	 *         return value means this location is on the edge of the
	 *         environment.
	 */
	public Location getAdjacent(Direction d) {
		// If the direction has too many dimensions, we have a problem
		// Too few is OK; 2D directions should work in 3D
		if (d.v.length > v.length)
			return null;

		int x = v[Environment.AXIS_X] + d.v[Environment.AXIS_X];
		int y = v[Environment.AXIS_Y] + d.v[Environment.AXIS_Y];

		if (x < 0 || x >= this.environment.getSize(Environment.AXIS_X)) {
			if (this.environment.getAxisWrap(Environment.AXIS_X))
				x = (x + this.environment.getSize(Environment.AXIS_X)) % this.environment.getSize(Environment.AXIS_X);
			else
				return null;
		}

		if (y < 0 || y >= this.environment.getSize(Environment.AXIS_Y)) {
			if (this.environment.getAxisWrap(Environment.AXIS_Y)) {
				if (y < 0) {
					y = 0;
					x = (x + this.environment.getSize(Environment.AXIS_X) / 2) % this.environment.getSize(Environment.AXIS_X);
				} else if (y >= this.environment.getSize(Environment.AXIS_Y)) {
					y = this.environment.getSize(Environment.AXIS_Y) - 1;
					x = (x + this.environment.getSize(Environment.AXIS_X) / 2) % this.environment.getSize(Environment.AXIS_X);
				}
			} else
				return null;
		}
		Location retVal = this.environment.getLocation(x, y);
		return retVal;
	}

	/**
	 * Return a new location by moving in a certain direction, in a certain distance
	 * from the current location.
	 * @param distance Number of tiles you wish to move.
	 * @param dir The direction you wish to move in.
	 * @return The resultant location.
	 */
	public Location add(int distance, Direction dir) {
		int[] addComponents = dir.v;
		int[] newCoords = new int[addComponents.length];

		for(int i = 0; i < addComponents.length; i++) {
			newCoords[i] += addComponents[i] * distance;
		}

		return this.environment.getLocation(newCoords[0], newCoords[1]);
	}

	/**
	 * Get the agent at this location. A location may only contain a single
	 * agent.
	 */
	public Agent getAgent() {
		return this.environment.getAgent(this);
	}

	/** @return the environment which contains this location */
	public Environment getEnvironment() {
		return this.environment;
	}

	// Support for containers...
	@Override
	public int hashCode() {
		int hash = 0;
		for (int i : this.v)
			hash = (hash << 13 | hash >> 19) ^ i;
		return hash;
	}

	/**
	 * Is the location adjacent in a true distance measure sense (diagonal
	 * is adjacent)? Different from city block distance, as a city block
	 * distance of 2 Can be 2 steps on the same axis (not adjacent), or 1
	 * step on 2 axes (adjacent)
	 */
	public boolean isAdjacent(Location l) {
		int distSquare = distanceSquare(l);
		// Equivalent locations are NOT adjacent!
		if (distSquare == 0)
			return false;
		// Must be within a distance of sqrt(1 * dimensionality)
		if (distSquare > v.length)
			return false;
		return true;
	}

	/**
	 * Through direct manipulation of the coordinate array, arbitrary
	 * locations can be created from a valid location. This checks if the
	 * location is still a valid location in the environment by assuring the
	 * coordinates are positive, but less than the size of the environment.
	 */
	public boolean isValid() {
		for (int i = 0; i < this.environment.getAxisCount(); ++i) {
			if (v[i] < 0)
				return false;
			if (v[i] >= this.environment.getSize(i))
				return false;
		}
		return true;
	}

	public void saveAsANode(Node node, Document doc) {

		for (int i : v) {
			Element locationElement = doc.createElement("axisPos");
			locationElement.appendChild(doc.createTextNode(i + ""));
			node.appendChild(locationElement);
		}

	}

	/**
	 * Set the agent at this location. A location may only contain a single
	 * agent.
	 */
	public void setAgent(Agent a) {
		this.environment.setAgent(this, a);
	}

	/**
	 * Set the flag associated with the constant flag in this location. The
	 * valid values for flag are implementation defined.
	 */
	public void setFlag(int flag, boolean state) {
		this.environment.setFlag(this, flag, state);
	}

	/**
	 * Test the flag associated with the constant flag in this location. The
	 * valid values for flag are implementation defined.
	 */
	public boolean testFlag(int flag) {
		return this.environment.testFlag(this, flag);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder("(");
		for (int i = 0; i < v.length - 1; i++) {
			out.append(v[i]);
			out.append(",");
		}
		out.append(v[v.length - 1]);
		out.append(")");
		return out.toString();
	}

}