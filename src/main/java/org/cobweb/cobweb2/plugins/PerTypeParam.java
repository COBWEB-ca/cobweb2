package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.AgentFoodCountable;

/**
 * Parameter that depends on the number of agent types in the simulation
 */
public interface PerTypeParam {

	/**
	 * Updates configuration for the new number of agent types
	 * @param envParams used to retrieve agent type count
	 */
	public void resize(AgentFoodCountable envParams);
}
