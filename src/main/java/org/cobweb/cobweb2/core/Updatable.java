package org.cobweb.cobweb2.core;


/**
 * Component that changes with time
 */
public interface Updatable {

	/**
	 * Updates the component simulation state
	 * @param time Current simulation time
	 */
	void update(long time);
}
