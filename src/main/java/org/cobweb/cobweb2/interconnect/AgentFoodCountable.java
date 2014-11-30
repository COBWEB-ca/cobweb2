package org.cobweb.cobweb2.interconnect;

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
	 * Number of food types.
	 * @return number of food types in the simulation.
	 */
	public int getFoodTypes();

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