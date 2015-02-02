package org.cobweb.cobweb2.core;

/**
 * Component of the simulation that changes with time.
 * update() will be called at every simulation step.
 */
public interface Updatable {

	/**
	 * Updates the state of the simulation component for the current time step
	 */
	public void update();
}
