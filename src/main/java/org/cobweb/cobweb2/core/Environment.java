package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

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
 * in a format that is reasonable for external access. An flagArray of longs where
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
public class Environment implements Updatable {

	protected SimulationInternals simulation;

	public Topology topology;

	public Environment(SimulationInternals simulation) {
		this.simulation = simulation;
	}

	public void load(int width, int height, boolean wrap, boolean keepOldArray) {
		topology = new Topology(simulation, width, height, wrap);

		if (keepOldArray) {
			flagArray = ArrayUtilities.resizeArray(flagArray, topology.width, topology.height);
			foodTypeArray = ArrayUtilities.resizeArray(foodTypeArray, topology.width, topology.height);
		} else {
			flagArray = new int[topology.width][topology.height];
			foodTypeArray = new int[topology.width][topology.height];
		}
		dropArray = ArrayUtilities.resizeArray(dropArray, topology.width, topology.height);
	}


	/**
	 * The implementation uses a hash table to store agents, as we assume there
	 * are many more locations than agents.
	 */
	protected Map<Location, Agent> agentTable = new Hashtable<Location, Agent>();

	private int[][] flagArray = new int[0][0];

	private int[][] foodTypeArray = new int[0][0];

	protected Drop[][] dropArray = new Drop[0][0];

	public static final int FLAG_STONE = 1;

	public static final int FLAG_FOOD = 2;

	public static final int FLAG_AGENT = 3;

	public static final int FLAG_DROP = 4;

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

	public final void setAgent(Location l, Agent a) {
		if (a != null)
			agentTable.put(l, a);
		else
			agentTable.remove(l);
	}

	private int getLocationBits(Location l) {
		return flagArray[l.x][l.y];
	}

	private void setLocationBits(Location l, int bits) {
		flagArray[l.x][l.y] = bits;
	}

	/**
	 * Flags locations as a food/stone/waste location. It does nothing if
	 * the square is already occupied (for example, setFlag((0,0),FOOD,true)
	 * does nothing when (0,0) is a stone
	 */
	protected void setFlag(Location l, int flag, boolean state) {
		int flagBits = 1 << (flag - 1);

		assert (!(state && getLocationBits(l) != 0)) : "Attempted to set flag when location flags non-zero: " + getLocationBits(l);
		assert (!(!state && (getLocationBits(l) & flagBits) == 0)) : "Attempting to unset an unset flag" + flagBits;

		int newValue = getLocationBits(l);

		if (state)
			newValue |= flagBits;
		else
			newValue &= ~flagBits;

		setLocationBits(l, newValue);
	}


	protected boolean testFlag(Location l, int flag) {
		int flagBits = 1 << (flag - 1);
		return (getLocationBits(l) & flagBits) != 0;
	}

	public int getFoodType(Location l) {
		return foodTypeArray[l.x][l.y];
	}

	public synchronized void addFood(Location l, int type) {
		if (hasStone(l)) {
			throw new IllegalArgumentException("stone here already");
		}
		setFlag(l, Environment.FLAG_FOOD, true);
		foodTypeArray[l.x][l.y] = type;
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
		return foodTypeArray[l.x][l.y];
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
		if (hasAgent(l)) {
			return;
		}

		if (hasFood(l))
			removeFood(l);
		if (testFlag(l, Environment.FLAG_DROP))
			setFlag(l, Environment.FLAG_DROP, false);

		setFlag(l, Environment.FLAG_STONE, true);
	}

	public synchronized void clearStones() {
		clearFlag(Environment.FLAG_STONE);
	}

	public boolean hasAnythingAt(Location l) {
		return getLocationBits(l) != 0 || hasAgent(l);
	}

	public synchronized void removeStone(Location l) {
		setFlag(l, Environment.FLAG_STONE, false);
	}

	public void addDrop(Location loc, Drop d) {
		if (hasFood(loc)) {
			removeFood(loc);
		}

		setFlag(loc, Environment.FLAG_DROP, true);

		dropArray[loc.x][loc.y] = d;
	}

	public void removeDrop(Location loc) {
		setFlag(loc, FLAG_DROP, false);
		dropArray[loc.x][loc.y] = null;
	}

	public Drop getDrop(Location loc) {
		return dropArray[loc.x][loc.y];
	}

	public boolean hasDrop(Location loc) {
		return testFlag(loc, FLAG_DROP);
	}

	public boolean hasStone(Location l) {
		return testFlag(l, Environment.FLAG_STONE);
	}

	public boolean hasAgent(Location l) {
		return getAgent(l) != null;
	}

	public synchronized void clearWaste() {
		clearFlag(Environment.FLAG_DROP);
	}

	/**
	 * Removes old agents that are off the new environment.
	 */
	protected void removeOffgridAgents() {
		for (Agent a : new ArrayList<Agent>(getAgents())) {
			Location l = a.getPosition();
			if (l.x >= topology.width || l.y >= topology.height) {
				a.die();
			}
		}

	}

	public synchronized void removeAgent(Location l) {
		Agent a = getAgent(l);
		if (a != null)
			a.die();
	}

	@Override
	public synchronized void update() {
		// nothing
	}

}
