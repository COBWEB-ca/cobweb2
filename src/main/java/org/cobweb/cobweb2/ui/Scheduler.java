package org.cobweb.cobweb2.ui;

import org.cobweb.cobweb2.core.SimulationInterface;


/**
 * The Scheduler interface is the UIInterface's concept of what a scheduler is.
 * In a nutshell, the Scheduler is the conductor of the simulation; the
 * scheduler is expected to inform all its clients about the passage of time, so
 * that frame data can be updated in a timely manner.
 * 
 * Note that there is no Scheduler.Client class; the interface between a
 * Scheduler and its clients is entirely up to scheduler implementations. This
 * implies that the environment and the agents are tied to a specific scheduler
 * class; this is a conscious design decision. It is unreasonable to expect a
 * tick-based agent to operate in a event-based simulation and vice-versa. If a
 * Scheduler client can be used with multiple schedulers, then all that need be
 * done is to have that client implement the client interfaces for each of the
 * supported Schedulers.
 * 
 * Constructors for Scheduler implementations must have this signature:
 * <code>public
 * SchedulerImplementation (UIInterface UI, java.io.Reader dataFile);</code>
 * This is because the Scheduler is created using reflection, and only this
 * signature is checked for.
 * 
 */
public interface Scheduler {

	/**
	 * Get the current time as a long.
	 */
	long getTime();

	/**
	 * Is the simulation paused? Since the Scheduler is the conductor of the
	 * simulation, the Scheduler is the only class that is aware of the activity
	 * state of the simulation. As such, the Scheduler is responsible for
	 * implementing pausing and resuming.
	 * 
	 * @return the paused state flag of the simulation
	 */
	boolean isRunning();

	/**
	 * Kill the scheduler. This method should immediately and irrevocably halt
	 * the simulation.
	 */
	void dispose();

	/** Pause the simulation. */
	void pause();

	/** Resume the simulation from a paused state. */
	void resume();

	/* slow down scheduler */
	void setDelay(long time);

	/**
	 * Start the scheduler. The scheduler is initially in a paused state, but
	 * this method finishes the initialisation process for the scheduler, and
	 * requests the frame data representing the initial state of the simulation.
	 * 
	 * The strange method name is chosen so as not to conflict with the
	 * Thread.start method, as the Scheduler may be implemented as a Thread, but
	 * start may not correspond to Thread.start. Other method names in this
	 * class follow the strange example of startScheduler, for consistency.
	 */
	void startIdle();

	public abstract SimulationInterface getSimulation();

	public abstract void removeUIComponent(UpdatableUI ui);

	public abstract void addUIComponent(UpdatableUI ui);
}
