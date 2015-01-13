package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.AgentFoodCountable;
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
	 * @param memoryBits number of bits of memory the controller will use // FIXME: this is controller dependent, should not be here
	 * @param communicationBits number of bits the agent uses to communicate // FIXME: this is controller dependent, should not be here
	 * @param type agent type
	 * @return Controller for agent
	 */
	public Controller createController(SimulationInternals sim, int memoryBits, int communicationBits, int type);
}
