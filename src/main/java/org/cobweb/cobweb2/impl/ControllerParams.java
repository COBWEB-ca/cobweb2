package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.io.ParameterSerializable;

/**
 * Configuration for a Controller
 */
public interface ControllerParams extends ParameterSerializable {

	/**
	 * Updates configuration for the new number of agent/resource types
	 * @param envParams used to retrieve agent/resource type count
	 */
	public void resize(AgentFoodCountable envParams);

	/**
	 * Creates Controller for given agent type
	 * @param sim simulation the agent is in
	 * @param type agent type
	 * @return Controller for agent
	 */
	public Controller createController(SimulationInternals sim, int type);
}
