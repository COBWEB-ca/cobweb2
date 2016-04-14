package org.cobweb.cobweb2.plugins;


/**
 * Modifies agents' parameters during the simulation.
 */
public interface AgentMutator {

	/**
	 * Checks whether this mutator can use given AgentState in the current simulation configuration
	 */
	public <T extends AgentState> boolean acceptsState(Class<T> type, T value);
}
