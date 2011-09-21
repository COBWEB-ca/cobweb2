package cobweb;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import cwcore.ComplexEnvironment.CommManager;
import driver.SimulationConfig;

/**
 * TickScheduler is an implementation of Scheduler that sends uniform ticks to
 * clients.
 */
public class TickScheduler implements Scheduler {

	/**
	 * The TickScheduler client interface is quite trivial; tickNotification is
	 * called on each client each tick.  A client is any object that needs to be 
	 * modified during a tick (examples are agents, and environments).
	 */
	public static interface Client {

		/** Notification of a tick. */
		public void tickNotification(long time);

		public void tickZero();
	}

	/**
	 * Contains the run method used for the simulation thread.
	 * 
	 * @author
	 *
	 */
	private class SchedulerRunnable implements Runnable {

		/**
		 * Contains the main loop to control the simulation.
		 */
		public void run() {
			doZeroTick();
			long frameCount = 0;

			// Main loop
			while (!done) {
				if (!done && !running) {
					myWait(100);
					theUI.refresh(false);
				} else {
					doTick();

					if (theUI.getStopTime() != 0 && getTime() == theUI.getStopTime()) {
						pauseScheduler();
					}
					if (frameCount >= frameSkip) {
						theUI.refresh(slowdown > 0);
						frameCount = 0;
					}
				}

				// start doing something
				if (running && slowdown != 0) {
					myWait(slowdown);
				}
			}
		}
	}

	private volatile boolean running = false;

	private volatile boolean done = false;

	private long frameSkip = 0;

	private long tickCount = 0;

	private long slowdown = 1;

	private UIInterface theUI;

	private final Set<Client> clientV = new LinkedHashSet<Client>();

	private Thread myThread;

	/**
	 * Constructor for TickScheduler. This should NEVER be called directly;
	 * instead reflection should be used inside the implementation of
	 * UIInterface; look at the LocalUIInterface for an implementation example.
	 */
	public TickScheduler(UIInterface ui, SimulationConfig p) {
		myThread = new Thread(new SchedulerRunnable());
		myThread.setName("cobweb.TickScheduler");
		loadScheduler(ui, p);
	}

	public synchronized void addSchedulerClient(Object theClient) {
		clientV.add((Client) theClient);
	}

	/**
	 * Calls tick notifications for each client (agents, 
	 * environment, etc.) within the simulation.
	 * 
	 * @see Client#tickNotification(long)
	 */
	private synchronized void doTick() {

		CommManager commManager = new CommManager();
		commManager.decrementPersistence();
		commManager.unblockBroadcast();
		++tickCount;

		for (Client client : new Vector<Client>(clientV)) {
			client.tickNotification(tickCount);
		}

		theUI.writeLogEntry();
	}

	private void doZeroTick() {
		for (Client client : new Vector<Client>(clientV)) {
			client.tickZero();
		}
	}

	public long getTime() {
		return tickCount;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized void killScheduler() {
		done = true;
		notifyAll();
	}

	public synchronized void loadScheduler(UIInterface ui, SimulationConfig p) {
		theUI = ui;
	}

	private synchronized void myWait(long time) {
		try {
			wait(time);
		} catch (InterruptedException ex) {
			// Should not happen
			Logger.getLogger("COBWEB2").log(Level.INFO, "myWait broke", ex);
		}
	}

	public synchronized void pauseScheduler() {
		running = false;
		theUI.refresh(true);
	}

	public synchronized void removeSchedulerClient(Object theClient) {
		clientV.remove(theClient);
	}

	public void resetTime() {
		tickCount = 0;
	}

	/**
	 * Sets running to true, and notifies the simulation thread 
	 * of this.
	 * 
	 * @see java.lang.Object#notifyAll()
	 */
	public synchronized void resumeScheduler() {
		running = true;
		notifyAll();
	}

	// Saving. // $$$$$ Used to be invoked from this chain: the now silenced
	// method CobwebApplication.saveFile =>
	// LocalUIInterface.save
	public synchronized void saveScheduler(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w);
		pw.println(this.getClass().getName());

		pw.println("TickCount " + tickCount);

		pw.println(this.getClass().getName() + ".End");
		pw.flush();
	}

	public void setSchedulerFrameSkip(long fs) {
		frameSkip = fs;
	}

	public void setSleep(long time) {
		slowdown = time;
	}

	/**
	 * Causes the simulation thread to begin execution.
	 */
	public synchronized void startScheduler() {
		if (!myThread.isAlive()) {
			myThread.start();
		}
		notifyAll();
	}
}
