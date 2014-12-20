package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.cobweb.util.ArrayUtilities;

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

	protected SimulationInternals simulation;

	public Topology topology;

	public Environment(SimulationInternals simulation) {
		this.simulation = simulation;
	}

	public void load(int width, int height, boolean wrap, boolean keepOldArray) {
		topology = new Topology(simulation, width, height, wrap);

		if (keepOldArray) {
			int[] boardIndices = { width, height };
			array = new ArrayEnvironment(width, height, array);
			foodarray = ArrayUtilities.resizeArray(foodarray, boardIndices);
		} else {
			array = new ArrayEnvironment(width, height);
			foodarray = new int[width][height];
		}
	}


	/**
	 * The implementation uses a hash table to store agents, as we assume there
	 * are many more locations than agents.
	 */
	protected java.util.Hashtable<Location, Agent> agentTable = new Hashtable<Location, Agent>();

	public ArrayEnvironment array;

	public int[][] foodarray = new int[0][];

	public static final int FLAG_STONE = 1;

	public static final int FLAG_FOOD = 2;

	public static final int FLAG_AGENT = 3;

	public static final int FLAG_DROP = 4;

	private static final int STONE_CODE = 1 << FLAG_STONE;

	public static final int FOOD_CODE = 1 << FLAG_FOOD;

	private static final int WASTE_CODE = 1 << FLAG_DROP;

	// Bitmasks for boolean states
	private static final int MASK_TYPE = STONE_CODE | FOOD_CODE | WASTE_CODE;

	public void clearAgents() {
		for (Agent a : new ArrayList<Agent>(getAgents())) {
			a.die();
		}
		agentTable.clear();
	}

	public Agent getAgent(Location l) {
		return agentTable.get(l);
	}

	public synchronized Collection<Agent> getAgents() {
		return agentTable.values();
	}

	public int getAgentCount() {
		return agentTable.keySet().size();

	}

	public abstract EnvironmentStats getStatistics();

	public final void setAgent(Location l, Agent a) {
		if (a != null)
			agentTable.put(l, a);
		else
			agentTable.remove(l);
	}

	/**
	 * Flags locations as a food/stone/waste location. It does nothing if
	 * the square is already occupied (for example, setFlag((0,0),FOOD,true)
	 * does nothing when (0,0) is a stone
	 */
	public void setFlag(Location l, int flag, boolean state) {
		switch (flag) {
			case Environment.FLAG_STONE:
				// Sanity check
				if ((getLocationBits(l) & (Environment.FOOD_CODE | Environment.WASTE_CODE)) != 0)
					break;
				if (state) {
					setLocationBits(l, (getLocationBits(l) & ~Environment.MASK_TYPE) | Environment.STONE_CODE);
				} else {
					setLocationBits(l, getLocationBits(l) & ~Environment.MASK_TYPE);
				}
				break;
			case Environment.FLAG_FOOD:
				// Sanity check
				if ((getLocationBits(l) & (Environment.STONE_CODE | Environment.WASTE_CODE)) != 0)
					break;
				if (state) {
					setLocationBits(l, (getLocationBits(l) & ~Environment.MASK_TYPE) | Environment.FOOD_CODE);
				} else
					setLocationBits(l, getLocationBits(l) & ~Environment.MASK_TYPE);
				break;
			case Environment.FLAG_DROP:
				// Sanity check
				if ((getLocationBits(l) & (Environment.FOOD_CODE | Environment.STONE_CODE)) != 0)
					break;
				if (state) {
					setLocationBits(l, (getLocationBits(l) & ~Environment.MASK_TYPE) | Environment.WASTE_CODE);
				} else {
					setLocationBits(l, getLocationBits(l) & ~Environment.MASK_TYPE);
				}
				break;
			default:
		}
	}


	public boolean testFlag(Location l, int flag) {
		switch (flag) {
			case Environment.FLAG_STONE:
				return ((getLocationBits(l) & Environment.MASK_TYPE) == Environment.STONE_CODE);
			case Environment.FLAG_FOOD:
				return ((getLocationBits(l) & Environment.MASK_TYPE) == Environment.FOOD_CODE);
			case Environment.FLAG_DROP:
				return ((getLocationBits(l) & Environment.MASK_TYPE) == Environment.WASTE_CODE);
			default:
				return false;
		}
	}

	public int getFoodType(Location l) {
		return foodarray[l.x][l.y];
	}

	public synchronized void addFood(Location l, int type) {
		if (testFlag(l, Environment.FLAG_STONE)) {
			throw new IllegalArgumentException("stone here already");
		}
		setFlag(l, Environment.FLAG_FOOD, true);
		foodarray[l.x][l.y] = type;
	}

	public synchronized void clearFood() {
		clearFlag(Environment.FLAG_FOOD);
	}

	public synchronized void removeFood(Location l) {
		setFlag(l, Environment.FLAG_FOOD, false);
	}

	public boolean hasFood(Location l) {
		return testFlag(l, Environment.FLAG_FOOD);
	}

	public int getFood(Location l) {
		return foodarray[l.x][l.y];
	}

	protected void clearFlag(int flag) {
		for (int x = 0; x < topology.width; ++x) {
			for (int y = 0; y < topology.height; ++y) {
				Location currentPos = new Location(x, y);

				if (testFlag(currentPos, flag)) {
					setFlag(currentPos, flag, false);
				}
			}
		}
	}

	public synchronized void addStone(Location l) {
		if (getAgent(l) != null) {
			return;
		}

		if (testFlag(l, Environment.FLAG_FOOD))
			setFlag(l, Environment.FLAG_FOOD, false);
		if (testFlag(l, Environment.FLAG_DROP))
			setFlag(l, Environment.FLAG_DROP, false);

		setFlag(l, Environment.FLAG_STONE, true);
	}

	public synchronized void clearStones() {
		clearFlag(Environment.FLAG_STONE);
	}

	protected int getLocationBits(Location l) {
		return array.getLocationBits(l);
	}

	public boolean hasAnythingAt(Location l) {
		return (getLocationBits(l) & MASK_TYPE) != 0;
	}

	public synchronized void removeStone(Location l) {
		setFlag(l, Environment.FLAG_STONE, false);
	}

	protected void setLocationBits(Location l, int bits) {
		array.setLocationBits(l, bits);
	}

	public boolean hasStone(Location l) {
		return testFlag(l, Environment.FLAG_STONE);
	}

	public boolean hasAgent(Location l) {
		return getAgent(l) != null;
	}

}
