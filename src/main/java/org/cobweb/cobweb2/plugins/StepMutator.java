package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;

/**
 * Modifies agents when they move forward.
 */
public interface StepMutator extends AgentMutator {

	/**
	 * Agent moved.
	 * @param agent Agent in question.
	 * @param from Old Location.
	 * @param to New location.
	 */
	public void onStep(Agent agent, Location from, Location to);
}
