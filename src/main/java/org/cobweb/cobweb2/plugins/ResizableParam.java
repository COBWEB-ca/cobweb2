package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.io.ParameterSerializable;


public interface ResizableParam extends ParameterSerializable {

	/**
	 * Updates configuration for the new number of agent types
	 * @param envParams used to retrieve agent type count
	 */
	public void resize(AgentFoodCountable envParams);
}
