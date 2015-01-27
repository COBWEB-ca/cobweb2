/**
 *
 */
package org.cobweb.cobweb2.impl;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerTypeParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfSquishParent;
import org.cobweb.io.ParameterSerializable;

/**
 * Food web parameters for an agent.
 */
public class FoodwebParams implements ParameterSerializable, PerTypeParam {

	private static final long serialVersionUID = 1380425322335531943L;

	/**
	 * If this agent can eat Agent type 1 canEatAgents[0] is true.
	 */
	@ConfDisplayName("Agent")
	@ConfSquishParent
	@ConfList(indexName = "agent", startAtOne = true)
	public boolean[] canEatAgent = new boolean[0];

	/**
	 * If this agent can eat Food type 1 canEatFood[0] is true.
	 */
	@ConfDisplayName("Food")
	@ConfSquishParent
	@ConfList(indexName = "food", startAtOne = true)
	public boolean[] canEatFood = new boolean[0];

	public FoodwebParams(AgentFoodCountable env) {
		resize(env);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		canEatAgent = Arrays.copyOf(canEatAgent, envParams.getAgentTypes());

		int oldSize = canEatFood.length;
		canEatFood  = Arrays.copyOf(canEatFood, envParams.getAgentTypes());
		// agents can eat all food by default
		for (int i = oldSize; i < canEatFood.length; i++) {
			canEatFood [i] = true;
		}
	}

}