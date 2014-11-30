package org.cobweb.cobweb2.interconnect;

import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Location;

/**
 * Modifies agents when they move forward.
 */
public interface StepMutator extends AgentMutator {

	/**
	 * Agent moved.
	 * @param agent Agent in question.
	 * @param to New location.
	 * @param from Old Location.
	 */
	public void onStep(ComplexAgent agent, Location to, Location from);
}
