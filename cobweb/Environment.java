package cobweb;

import java.util.ArrayList;
import java.util.List;
import driver.Parser;

/**
 * The Environment class represents the simulation world; a collection of
 * locations with state, each of which may contain an agent.
 * 
 * The Environment class is designed to handle an arbitrary number of
 * dimensions, although the UIInterface is somewhat tied to two dimensions for
 * display purposes.
 * 
 * All access to the internal data of the Environment is done through an
 * accessor class, Environment.Location. The practical upshot of this is that
 * the Environment internals may be implemented in C or C++ using JNI, while the
 * Java code still has a nice java flavoured interface to the data.
 * 
 * Another advantage of the accessor model is that the internal data need not be
 * in a format that is reasonable for external access. An array of longs where
 * bitfields represent the location states makes sense in this context, because
 * the accessors allow friendly access to this state information.
 * 
 * Furthermore, the accessor is designed to be quite general; there should be no
 * need to subclass Environment.Location for a specific Environment
 * implementation. A number of constants should be defined in an Environment
 * implementation to allow agents to interpret the state information of a
 * location, so agents will need to be somewhat aware of the specific
 * environment they are operating in, but all access should be through this
 * interface, using implementation specific access constants.
 */
public abstract class Environment {
	/** Axis constants, to make dimensionality make sense */
	public static final int AXIS_X = 0;

	public static final int AXIS_Y = 1;

	public static final int AXIS_Z = 2;

	// public static final List currentPackets; //[]SK // remove

	/** Dimensionality independant notion of a direction */
	public static class Direction implements Comparable<Direction> {
		 
		public int[] v;
 
		public Direction(int[] initV) {
			v = initV;
		}
 
		public Direction(int dim) {
			v = new int[dim];
		}

		/**
		 * Compares two Directions
		 * In 1 dimension this just compares the values of the single dimension
		 * In 2 dimensions, it arranges vectors clock-wise
		 */
 
		public int compareTo(Direction other) {
			if (this.v.length != other.v.length){
				throw new IllegalArgumentException("Other Direction of unequal dimention");
			}
			if (this.v.length == 1) {
				return this.v[0] - other.v[0];
			}
			else if (this.v.length == 2) {
				int determinant = this.v[0] * other.v[1] - this.v[1] - other.v[0];
				// If the vectors are negatives of each other, can't use determinant
				if (determinant == 0 && !this.equals(other))
					determinant = 360;
				return determinant;
			}
			else { 
			throw new IllegalArgumentException("Can't compare Directions with dimention greater than 2"); 
			} 
		}
 		 
		/** 
		 * Directions are equal when all their elements of v vector equal 
		 * @param other Direction to compare to 
		 * @return true when directions equivalent 
		 */ 
		public boolean equals(Direction other ) { 
			if (this.v.length != other.v.length){ 
				throw new IllegalArgumentException("Other Direction of unequal dimention");
			}			 
			for (int x = 0; x < v.length; x++) { 
				if (this.v[x] != other.v[x]) 
                    return false; 
			}
			return true; 
		} 	
		/** Required for Comparable, just XOR all vector elements and shift
		the result at each XOR */ 
		public int hashCode() {
			int hash = 0;
			for (int i : this.v) 
				hash = (hash << 13 + hash >> 19) ^ i;
			return hash;
		}
	}
	// Some pre-defined directions for 2D
	public static final Direction DIRECTION_NORTH = new Direction(new int[] {
			0, -1 });

	public static final Direction DIRECTION_SOUTH = new Direction(new int[] {
			0, +1 });

	public static final Direction DIRECTION_WEST = new Direction(new int[] {
			-1, 0 });

	public static final Direction DIRECTION_EAST = new Direction(new int[] {
			+1, 0 });

	public static final Direction DIRECTION_NORTHEAST = new Direction(
			new int[] { +1, -1 });

	public static final Direction DIRECTION_SOUTHEAST = new Direction(
			new int[] { +1, +1 });

	public static final Direction DIRECTION_NORTHWEST = new Direction(
			new int[] { -1, -1 });

	public static final Direction DIRECTION_SOUTHWEST = new Direction(
			new int[] { -1, +1 });

	/**
	 * Public accessor to Location state information. Note that this class is
	 * simply a pass-through to the private getField/setField and
	 * getFlag/setFlag calls in the environment. This design allows for a
	 * logical and clear code style in agents and agent controllers, as the
	 * notion of position in the environment allows immediate access to state
	 * information.
	 */
	public class Location {
		/**
		 * Sometimes it's essential to get the coordinates; iteration is an
		 * example of this. To make this as fast as possible, no accessors are
		 * provided for the coordinate array, instead they're public. Bad form,
		 * but as fast as possible.
		 */
		public int[] v;

		/** Private constructor, as only the environment should create locations. */
		private Location(int[] axisPos) {
			v = new int[Environment.this.getAxisCount()];
			for (int i = 0; i < Environment.this.getAxisCount(); ++i)
				v[i] = axisPos[i];
		}

		/** @return the environment which contains this location */
		public Environment getEnvironment() {
			return Environment.this;
		}

		/**
		 * Through direct manipulation of the coordinate array, arbitrary
		 * locations can be created from a valid location. This checks if the
		 * location is still a valid location in the environment by assuring the
		 * coordinates are positive, but less than the size of the environment.
		 */
		public boolean isValid() {
			for (int i = 0; i < Environment.this.getAxisCount(); ++i) {
				if (v[i] < 0)
					return false;
				if (v[i] >= Environment.this.getSize(i))
					return false;
			}
			return true;
		}

		/** Make this location valid by wrapping, if allowed. */
		public boolean makeValid() {
			for (int i = 0; i < v.length; ++i) {
				if (v[i] < 0 && Environment.this.getAxisWrap(i))
					v[i] += Environment.this.getSize(i);
				else if (v[i] >= Environment.this.getSize(i)
						&& Environment.this.getAxisWrap(i))
					v[i] -= Environment.this.getSize(i);
			}
			return isValid();
		}

		/**
		 * @return the location adjacent to this one, along direction d. Null
		 *         return value means this location is on the edge of the
		 *         environment.
		 */
		public Location getAdjacent(Direction d) {
			// If the direction has too many dimensions, we have a problem
			// Too few is ok; 2D directions should work in 3D
			if (d.v.length > v.length)
				return null;
			int[] newPos = new int[v.length];
			int i = 0;
			for (; i < d.v.length; ++i)
				newPos[i] = v[i] + d.v[i];
			for (; i < newPos.length; ++i)
				newPos[i] = v[i];
			Location retVal = new Location(newPos);
			if (retVal.makeValid())
				return retVal;
			else
				return null;
		}

		/**
		 * Similar to the other getAdjacent call, but this is a single-axis
		 * version
		 */
		public Location getAdjacent(int axis, int delta) {
			if (v.length <= axis)
				return null;
			int[] deltaV = new int[v.length];
			deltaV[axis] = delta;
			return getAdjacent(new Direction(deltaV));
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
		 * "City-block" distance method; number of single axis steps between
		 * this location and the parameter location.
		 */
		public int cityBlockDistance(Location l) {
			int dist = 0;
			for (int i = 0; i < v.length; ++i)
				dist += Math.abs(v[i] - l.v[i]);
			return dist;
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

		/** True distance measure */
		public double distance(Location l) {
			return Math.sqrt(distanceSquare(l));
		}

		/**
		 * Test the flag associated with the constant flag in this location. The
		 * valid values for flag are implementation defined.
		 */
		public boolean testFlag(int flag) {
			return Environment.this.testFlag(this, flag);
		}

		/**
		 * Set the flag associated with the constant flag in this location. The
		 * valid values for flag are implementation defined.
		 */
		public void setFlag(int flag, boolean state) {
			Environment.this.setFlag(this, flag, state);
		}

		/**
		 * Get the value of the field associated with the constant field in this
		 * location. The valid values for field are implementation defined.
		 */
		public int getField(int field) {
			return Environment.this.getField(this, field);
		}

		/**
		 * Set the value of the field associated with the constant field in this
		 * location. The valid values for field are implementation defined.
		 */
		public void setField(int field, int value) {
			Environment.this.setField(this, field, value);
		}

		/**
		 * Get the agent at this location. A location may only contain a single
		 * agent.
		 */
		public Agent getAgent() {
			return Environment.this.getAgent(this);
		}

		/**
		 * Set the agent at this location. A location may only contain a single
		 * agent.
		 */
		public void setAgent(Agent a) {
			Environment.this.setAgent(this, a);
		}

		// Support for containers...
		public int hashCode() {
			int code = 0;
			for (int i = 0; i < v.length; ++i)
				code ^= v[i];
			return code;
		}

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
	}

	/** Constructs an Environment associated with a Scheduler. */
	protected Environment(Scheduler s) {
		/*
		 * this is gone for a reason!!! if (s != null) { theScheduler = s;
		 * s.addSchedulerClient(this); }
		 */
	}

	/** @return the dimensionality of this Environment. */
	public abstract int getAxisCount();

	/** @return the number of unique locations along a specific axis. */
	public abstract int getSize(int axis);

	/** @return true if the axis specified wraps. */
	public abstract boolean getAxisWrap(int axis);

	/** @return the location at coordinates specified by axisPos */
	public Location getLocation(int[] axisPos) {
		return new Location(axisPos);
	}

	protected Scheduler theScheduler;

	/** @return the Scheduler responsible for this Environment. */
	public Scheduler getScheduler() {
		return theScheduler;
	}

	/** Called by the UIInterface to get the frame data for the Environment. */
	void getDrawInfo(UIInterface theUI) {
		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X)
				* getSize(AXIS_Y)];
		fillTileColors(tileColors);
		theUI.newTileColors(getSize(AXIS_X), getSize(AXIS_Y), tileColors);

		for (java.util.Enumeration e = agentTable.elements(); e
				.hasMoreElements();)
			((Agent) e.nextElement()).getDrawInfo(theUI);
	}

	/**
	 * Called from getDrawInfo to allow implementations to fill the array of
	 * tile colors.
	 */
	protected abstract void fillTileColors(java.awt.Color[] tileColors);

	// Syntactic sugar for common cases
	public Location getLocation(int x, int y) {
		return getLocation(new int[] { x, y });
	}

	public Location getLocation(int x, int y, int z) {
		return getLocation(new int[] { x, y, z });
	}

	/** Returns a random location. */
	public Location getRandomLocation() {
		Location l;
		do {
			l = getLocation(cobweb.globals.random.nextInt(getSize(AXIS_X)),
					cobweb.globals.random.nextInt(getSize(AXIS_Y)));
		} while (!l.isValid());
		return l;
	}

	public Location getUserDefinedLocation(int x, int y) {
		Location l;
		do {
			l = getLocation(x, y);
		} while (!l.isValid());
		return l;
	}

	/** Returns an Enumeration of Agents */
	public java.util.Enumeration agents() {
		return agentTable.elements();
	}

	/** Save to a stream */
	public abstract void save(java.io.Writer w);

	/** Load from a stream <== new addition! */
	public abstract void load(cobweb.Scheduler s, Parser p/* java.io.Reader r */)
			throws java.io.IOException;

	/** Log to a stream */
	public abstract void log(java.io.Writer w);

	/** Report to a stream */
	public static void trackAgent(java.io.Writer w) {
		System.err
				.println("Your environment is supposed to override the agent tracking.\n");
		System.exit(0);
	}

	/** Report to a stream */
	public abstract void report(java.io.Writer w);

	/** Update the log; called from the UIInterface */
	protected abstract void writeLogEntry();

	/** Core implementation of testFlag; this is what could be accelerated in C++ */
	protected abstract boolean testFlag(Location l, int flag);

	/** Core implementation of setFlag; this is what could be accelerated in C++ */
	protected abstract void setFlag(Location l, int flag, boolean state);

	/** Core implementation of getField; this is what could be accelerated in C++ */
	protected abstract int getField(Location l, int field);

	/** Core implementation of setField; this is what could be accelerated in C++ */
	protected abstract void setField(Location l, int field, int value);

	public abstract void selectStones(int x, int y, UIInterface theUI);

	public abstract void selectFood(int x, int y, int type,
			cobweb.UIInterface theUI);

	public abstract void selectAgent(int x, int y, int type,
			cobweb.UIInterface theUI);

	public abstract void remove(int mode, cobweb.UIInterface ui);

	// The implementation uses a hashtable to store agents, as we assume there
	// are many
	// more locations than agents.
	private final Agent getAgent(Location l) {
		return (Agent) agentTable.get(l);
	}

	private final void setAgent(Location l, Agent a) {
		if (a != null)
			agentTable.put(l, a);
		else
			agentTable.remove(l);
	}

	protected void clearAgents() {
		agentTable.clear();
		agentTable = new java.util.Hashtable();
	}

	public abstract int getTypeCount();

	public int countAgents() {
		return agentTable.size();
	}

	public java.util.Enumeration getAgents() {
		return agentTable.elements();
	}

	public java.util.Collection getAgentCollection() {
		return agentTable.values();
	}

	private java.util.Hashtable agentTable = new java.util.Hashtable();

	public abstract void setclick(int count);

	public abstract void observe(int x, int y, cobweb.UIInterface ui);

	private static cobweb.UIInterface myUI;

	public static void setUIPipe(cobweb.UIInterface ui) {
		myUI = ui;
	}

	public static cobweb.UIInterface getUIPipe() {
		return myUI;
	}
}
