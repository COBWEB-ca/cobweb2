package cobweb;

import java.awt.Color;
import java.util.Hashtable;

import driver.Parser;

/**
 * The Environment class represents the simulation world; a collection of locations with state, each of which may
 * contain an agent.
 *
 * The Environment class is designed to handle an arbitrary number of dimensions, although the UIInterface is somewhat
 * tied to two dimensions for display purposes.
 *
 * All access to the internal data of the Environment is done through an accessor class, Environment.Location. The
 * practical upshot of this is that the Environment internals may be implemented in C or C++ using JNI, while the Java
 * code still has a nice java flavoured interface to the data.
 *
 * Another advantage of the accessor model is that the internal data need not be in a format that is reasonable for
 * external access. An array of longs where bitfields represent the location states makes sense in this context, because
 * the accessors allow friendly access to this state information.
 *
 * Furthermore, the accessor is designed to be quite general; there should be no need to subclass Environment.Location
 * for a specific Environment implementation. A number of constants should be defined in an Environment implementation
 * to allow agents to interpret the state information of a location, so agents will need to be somewhat aware of the
 * specific environment they are operating in, but all access should be through this interface, using implementation
 * specific access constants.
 */
public abstract class Environment {
	public static class EnvironmentStats {
		public long[] agentCounts;
		public long[] foodCounts;
		public long timestep;

	}

	/**
	 * Public accessor to Location state information. Note that this class is simply a pass-through to the private
	 * getField/setField and getFlag/setFlag calls in the environment. This design allows for a logical and clear code
	 * style in agents and agent controllers, as the notion of position in the environment allows immediate access to
	 * state information.
	 */
	public class Location {
		/**
		 * Sometimes it's essential to get the coordinates; iteration is an example of this. To make this as fast as
		 * possible, no accessors are provided for the coordinate array, instead they're public. Bad form, but as fast
		 * as possible.
		 */
		public int[] v;

		/** Private constructor, as only the environment should create locations. */
		private Location(int[] axisPos) {
			v = new int[Environment.this.getAxisCount()];
			for (int i = 0; i < Environment.this.getAxisCount(); ++i)
				v[i] = axisPos[i];
		}

		/**
		 * "City-block" distance method; number of single axis steps between this location and the parameter location.
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
		 * Distance squared; useful because sometimes the sqrt is irrelevant, as in isAdjacent.
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
		 * @return the location adjacent to this one, along direction d. Null return value means this location is on the
		 *         edge of the environment.
		 */
		public Location getAdjacent(Direction d) {
			// If the direction has too many dimensions, we have a problem
			// Too few is OK; 2D directions should work in 3D
			if (d.v.length > v.length)
				return null;

			int x = v[AXIS_X] + d.v[AXIS_X];
			int y = v[AXIS_Y] + d.v[AXIS_Y];

			if (x < 0 || x >= getSize(AXIS_X)) {
				if (getAxisWrap(AXIS_X))
					x = (x + getSize(AXIS_X)) % getSize(AXIS_X);
				else
					return null;
			}

			if (y < 0 || y >= getSize(AXIS_Y)) {
				if (getAxisWrap(AXIS_Y)) {
					if (y < 0) {
						y = 0;
						x = (x + getSize(AXIS_X) / 2) % getSize(AXIS_X);
					} else if (y >= getSize(AXIS_Y)) {
						y = getSize(AXIS_Y) - 1;
						x = (x + getSize(AXIS_X) / 2) % getSize(AXIS_X);
					}
				} else
					return null;
			}
			Location retVal = getLocation(x, y);
			return retVal;
		}

		/**
		 * Similar to the other getAdjacent call, but this is a single-axis version
		 */
		public Location getAdjacent(int axis, int delta) {
			if (v.length <= axis)
				return null;
			int[] deltaV = new int[v.length];
			deltaV[axis] = delta;
			return getAdjacent(new Direction(deltaV));
		}

		/**
		 * Get the agent at this location. A location may only contain a single agent.
		 */
		public Agent getAgent() {
			return Environment.this.getAgent(this);
		}

		/** @return the environment which contains this location */
		public Environment getEnvironment() {
			return Environment.this;
		}

		/**
		 * Get the value of the field associated with the constant field in this location. The valid values for field
		 * are implementation defined.
		 */
		public int getField(int field) {
			return Environment.this.getField(this, field);
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
		 * Is the location adjacent in a true distance measure sense (diagonal is adjacent)? Different from city block
		 * distance, as a city block distance of 2 Can be 2 steps on the same axis (not adjacent), or 1 step on 2 axes
		 * (adjacent)
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
		 * Through direct manipulation of the coordinate array, arbitrary locations can be created from a valid
		 * location. This checks if the location is still a valid location in the environment by assuring the
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

		/**
		 * Set the agent at this location. A location may only contain a single agent.
		 */
		public void setAgent(Agent a) {
			Environment.this.setAgent(this, a);
		}

		/**
		 * Set the value of the field associated with the constant field in this location. The valid values for field
		 * are implementation defined.
		 */
		public void setField(int field, int value) {
			Environment.this.setField(this, field, value);
		}

		/**
		 * Set the flag associated with the constant flag in this location. The valid values for flag are implementation
		 * defined.
		 */
		public void setFlag(int flag, boolean state) {
			Environment.this.setFlag(this, flag, state);
		}

		/**
		 * Test the flag associated with the constant flag in this location. The valid values for flag are
		 * implementation defined.
		 */
		public boolean testFlag(int flag) {
			return Environment.this.testFlag(this, flag);
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

		public boolean checkFlip(Direction dir) {
			int y = v[1] + dir.v[1];
			return (y < 0 || y >= getSize(AXIS_Y)) && getAxisWrap(AXIS_Y);
		}

	}


	private Location[][] locationCache;

	/** Axis constants, to make dimensionality make sense */
	public static final int AXIS_X = 0;

	public static final int AXIS_Y = 1;

	public static final int AXIS_Z = 2;

	// Some predefined directions for 2D
	public static final Direction DIRECTION_NORTH = new Direction(new int[] { 0, -1 });

	public static final Direction DIRECTION_SOUTH = new Direction(new int[] { 0, +1 });

	public static final Direction DIRECTION_WEST = new Direction(new int[] { -1, 0 });

	public static final Direction DIRECTION_EAST = new Direction(new int[] { +1, 0 });

	public static final Direction DIRECTION_NORTHEAST = new Direction(new int[] { +1, -1 });

	public static final Direction DIRECTION_SOUTHEAST = new Direction(new int[] { +1, +1 });

	public static final Direction DIRECTION_NORTHWEST = new Direction(new int[] { -1, -1 });

	public static final Direction DIRECTION_SOUTHWEST = new Direction(new int[] { -1, +1 });

	/** Report to a stream */
	public static void trackAgent(java.io.Writer w) {
		System.err.println("Your environment is supposed to override the agent tracking.\n");
		System.exit(0);
	}

	protected Scheduler theScheduler;

	protected java.util.Hashtable<Location, Agent> agentTable = new Hashtable<Location, Agent>();

	private static DrawingHandler myUI;

	public static DrawingHandler getUIPipe() {
		return myUI;
	}

	public static void setUIPipe(DrawingHandler ui) {
		myUI = ui;
	}

	/** Returns an Enumeration of Agents */
	public java.util.Enumeration<Agent> agents() {
		return agentTable.elements();
	}

	protected void clearAgents() {
		agentTable.clear();
		agentTable = new java.util.Hashtable<Location, Agent>();
	}

	public int countAgents() {
		return agentTable.size();
	}

	/**
	 * Called from getDrawInfo to allow implementations to fill the array of tile colors.
	 */
	protected abstract void fillTileColors(java.awt.Color[] tiles);

	// The implementation uses a hashtable to store agents, as we assume there
	// are many
	// more locations than agents.
	private Agent getAgent(Location l) {
		return agentTable.get(l);
	}

	public java.util.Collection<Agent> getAgentCollection() {
		return agentTable.values();
	}

	public java.util.Enumeration<Agent> getAgents() {
		return agentTable.elements();
	}

	/** @return the dimensionality of this Environment. */
	public abstract int getAxisCount();

	/** @return true if the axis specified wraps. */
	public abstract boolean getAxisWrap(int axis);

	/** Called by the UIInterface to get the frame data for the Environment. */
	protected void getDrawInfo(DrawingHandler theUI) {
		fillTileColors(tileColors);
		theUI.newTileColors(getSize(AXIS_X), getSize(AXIS_Y), tileColors);

	}

	private Color[] tileColors;

	/** Core implementation of getField; this is what could be accelerated in C++ */
	protected abstract int getField(Location l, int field);

	protected void setupLocationCache() {
		locationCache = new Location[getSize(AXIS_X)][getSize(AXIS_Y)];
	}

	// Syntactic sugar for common cases
	public Location getLocation(int x, int y) {

		if (locationCache[x][y] == null)
			locationCache[x][y] = new Location(new int[] { x, y });
		return locationCache[x][y];
	}

	/** Returns a random location. */
	public Location getRandomLocation() {
		Location l;
		do {
			l = getLocation(cobweb.globals.random.nextInt(getSize(AXIS_X)), cobweb.globals.random
					.nextInt(getSize(AXIS_Y)));
		} while (!l.isValid());
		return l;
	}

	/** @return the Scheduler responsible for this Environment. */
	public Scheduler getScheduler() {
		return theScheduler;
	}

	/** @return the number of unique locations along a specific axis. */
	public abstract int getSize(int axis);

	public abstract EnvironmentStats getStatistics();

	public abstract int getTypeCount();

	public Location getUserDefinedLocation(int x, int y) {
		Location l;
		do {
			l = getLocation(x, y);
		} while (!l.isValid());
		return l;
	}

	/** Load from a stream <== new addition! */
	public void load(Scheduler s, Parser p) throws IllegalArgumentException {
		tileColors = new Color[p.getEnvParams().getWidth() * p.getEnvParams().getHeight()];
	}

	/** Log to a stream */
	public abstract void log(java.io.Writer w);

	public abstract void observe(int x, int y);

	public abstract void clearFood();

	public abstract void clearStones();

	/** Report to a stream */
	public abstract void report(java.io.Writer w);

	/** Save to a stream */
	public abstract void save(java.io.Writer w);

	public abstract void addAgent(int x, int y, int type);

	public abstract void removeAgent(int x, int y);

	public abstract void addFood(int x, int y, int type);

	public abstract void removeFood(int x, int y);

	public abstract void addStone(int x, int y);

	public abstract void removeStone(int x, int y);

	private final void setAgent(Location l, Agent a) {
		if (a != null)
			agentTable.put(l, a);
		else
			agentTable.remove(l);
	}

	/** Core implementation of setField; this is what could be accelerated in C++ */
	protected abstract void setField(Location l, int field, int value);

	/** Core implementation of setFlag; this is what could be accelerated in C++ */
	protected abstract void setFlag(Location l, int flag, boolean state);

	/** Core implementation of testFlag; this is what could be accelerated in C++ */
	protected abstract boolean testFlag(Location l, int flag);

	/** Update the log; called from the UIInterface */
	protected abstract void writeLogEntry();

	public abstract void unObserve();

	public abstract void clearWaste();
}
