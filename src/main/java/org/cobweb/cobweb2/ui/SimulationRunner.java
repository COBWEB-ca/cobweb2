package org.cobweb.cobweb2.ui;

import org.cobweb.cobweb2.core.SimulationInterface;


/**
 * Continuously steps through simulation and updates UpdatableUI clients at every step.
 */
public interface SimulationRunner {

	/** Perform one simulation step */
	public void step();

	/** Start stepping through the simulation */
	public void run();

	/** Is the SimulationRunnerBase running the simulation? */
	public boolean isRunning();

	/** Stop stepping through the simulation */
	public void stop();

	/** Simulation that is being run */
	public SimulationInterface getSimulation();

	public void removeUIComponent(UpdatableUI ui);

	public void addUIComponent(UpdatableUI ui);
}
