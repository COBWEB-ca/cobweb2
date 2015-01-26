package org.cobweb.cobweb2.impl;

/**
 * Retrieves basic simulation parameters.
 */
public interface AgentFoodCountable {

	/**
	 * Number of agent types.
	 * @return number of agent types in the simulation.
	 */
	public int getAgentTypes();

	/**
	 * Height of the simulation grid.
	 * @return height of the simulation grid.
	 */
	public int getHeight();

	/**
	 * Width of the simulation grid.
	 * @return width of the simulation grid.
	 */
	public int getWidth();

}