package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

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

	public void load(int width, int height, boolean wrap) {
		topology = new Topology(simulation, width, height, wrap);
	}


	/**
	 * The implementation uses a hash table to store agents, as we assume there
	 * are many more locations than agents.
	 */
	protected java.util.Hashtable<Location, Agent> agentTable = new Hashtable<Location, Agent>();


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

	/** @return the dimensionality of this Environment. */
	public abstract int getAxisCount();

	/** @return true if the axis specified wraps. */
	public abstract boolean getAxisWrap(int axis);

	public int getAgentCount() {
		return agentTable.keySet().size();

	}

	public abstract EnvironmentStats getStatistics();

	public Location getUserDefinedLocation(int x, int y) {
		Location l = new Location(x, y);
		if (!topology.isValidLocation(l))
			throw new IllegalArgumentException("Location not inside environment");

		return l;
	}

	public final void setAgent(Location l, Agent a) {
		if (a != null)
			agentTable.put(l, a);
		else
			agentTable.remove(l);
	}

	/** Core implementation of setFlag; this is what could be accelerated in C++ */
	protected abstract void setFlag(Location l, int flag, boolean state);

	/**
	 * Core implementation of testFlag; this is what could be accelerated in C++
	 */
	protected abstract boolean testFlag(Location l, int flag);

}
