package cwcore.complexParams;

import cobweb.Environment.Location;
import cwcore.ComplexAgent;

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
