package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.io.CobwebParam;

/**
 * The "brain" of an Agent, the controller causes the controlled agent to act by
 * calling methods on it. The Controller is notified by a call to control agent
 * that the Agent is requesting guidance.
 */
public interface Controller {

	/**
	 * Cause the specified agent to act.
	 * 
	 * @param theAgent agent to control
	 */
	public void controlAgent(Agent theAgent);

	/**
	 * Returns current controller configuration
	 * 
	 * @return configuration
	 */
	public CobwebParam getParams();

	/**
	 * Sets controller up based on environment parameters
	 * 
	 * @param memoryBits memory size
	 * @param commBits communication size
	 * @param params other parameters
	 */
	public void setupFromEnvironment(int memoryBits, int commBits, CobwebParam params, int type);

	/**
	 * Sets controller up based on parameters of the asexual breeding parent
	 * 
	 * @param parent parent
	 * @param mutationRate rate at which mutations can occur during breeding
	 */
	public void setupFromParent(Controller parent, float mutationRate);

	/**
	 * Sets controller up based on parameters of the sexual breeding parents
	 * 
	 * @param parent1 first parent
	 * @param parent2 second parent
	 * @param mutationRate rate at which mutations can occur during breeding
	 */
	public void setupFromParents(Controller parent1, Controller parent2, float mutationRate);
}
