package cobweb;

/**
 * The Scheduler interface is the UIInterface's concept of what a scheduler is.
 * In a nutshell, the Scheduler is the conductor of the simulation; the
 * scheduler is expected to inform all its clients about the passage of time, so
 * that frame data can be updated in a timely manner.
 * 
 * Note that there is no Scheduler.Client class; the interface between a
 * Scheduler and its clients is entirely up to scheduler implementations. This
 * implies that the environment and the agents are tied to a specific scheduler
 * class; this is a concious design decision. It is unreasonable to expect a
 * tick-based agent to operate in a event-based simulation and vice-versa. If a
 * Scheduler client can be used with multiple schedulers, then all that need be
 * done is to have that client implement the client interfaces for each of the
 * supported Schedulers.
 * 
 * Constructors for Scheduler implementations must have this signature: public
 * SchedulerImplementation (UIInterface ui, java.io.Reader dataFile); This is
 * because the Scheduler is created using reflection, and only this signature is
 * checked for.
 * 
 */
public interface Scheduler {
	/**
	 * Start the scheduler. The scheduler is initially in a paused state, but
	 * this method finishes the initialization process for the scheduler, and
	 * requests the frame data representing the initial state of the simulation.
	 * 
	 * The strange method name is chosen so as not to conflict with the
	 * Thread.start method, as the Scheduler may be implemented as a Thread, but
	 * start may not correspond to Thread.start. Other method names in this
	 * class follow the strange example of startScheduler, for consistency.
	 */
	void startScheduler();

	/**
	 * Kill the scheduler. This method should immediately and irrevocably halt
	 * the simulation.
	 */
	void killScheduler();

	/**
	 * Is the simulation paused? Since the Scheduler is the conductor of the
	 * simulation, the Scheduler is the only class that is aware of the activity
	 * state of the simulation. As such, the Scheduler is responsible for
	 * implementing pausing and resuming.
	 * 
	 * @return the paused state flag of the simulation
	 */
	boolean isSchedulerPaused();

	/** Pause the simulation. */
	void pauseScheduler();

	/** Resume the simulation from a paused state. */
	void resumeScheduler();

	/**
	 * Add a client to recieve time notification. Note that no policy is put on
	 * the clients, it is up to implementations of Scheduler to check that
	 * objects added as clients are truly valid clients.
	 */
	void addSchedulerClient(Object theClient);

	/** Remove a client. */
	void removeSchedulerClient(Object theClient);

	/**
	 * Set the refresh timeout on the simulation.
	 * 
	 * @see cobweb.UIInterface#setRefreshTimeout
	 */
	void setSchedulerRefreshTimeout(long timeOut);

	/**
	 * Set the frame skip setting on the simulation.
	 * 
	 * @see cobweb.UIInterface#setFrameSkip
	 */
	void setSchedulerFrameSkip(long frameSkip);

	/**
	 * Save the scheduler to the specified stream.
	 */
	void loadScheduler(UIInterface ui, driver.Parser p/* java.io.Reader r */);

	void saveScheduler(java.io.Writer w);

	/**
	 * Get the current time as a long int.
	 */
	long getTime();

	void resetTime();

	/* slow down scheduler */
	void setSleep(long time);
}
