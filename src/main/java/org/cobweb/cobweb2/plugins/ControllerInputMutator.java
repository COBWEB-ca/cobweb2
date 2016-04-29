package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ControllerInput;


public interface ControllerInputMutator extends AgentMutator {

	/**
	 * Controller has controlled an agent.
	 * @param agent Agent in question.
	 * @param cInput controller input state
	 */
	public void onControl(Agent agent, ControllerInput cInput);
}
