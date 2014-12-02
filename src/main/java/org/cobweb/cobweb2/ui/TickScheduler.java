package org.cobweb.cobweb2.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cobweb.cobweb2.core.SimulationInterface;

/**
 * TickScheduler is an implementation of Scheduler that sends uniform ticks to
 * clients.
 */
public class TickScheduler implements Scheduler {

	private List<UpdatableUI> uiComponents = new ArrayList<UpdatableUI>();

	/**
	 * Contains the run method used for the simulation thread.
	 * 
	 */
	private class SchedulerRunnable implements Runnable {

		/**
		 * Contains the main loop to control the simulation.
		 */
		public void run() {
			long frameCount = 0;

			// Main loop
			while (!done) {
				if (!done && !running) {
					myWait(100);
					updateUI(false);
				} else {
					// Core of the simulation
					stepSimulation();

					if (tickAutoStop != 0 && getTime() == tickAutoStop) {
						pause();
					}
					if (frameCount >= frameSkip) {
						updateUI(slowdown > 0);
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

	private void updateUI(boolean synchronous) {
		for (UpdatableUI client : uiComponents) {
			client.update(synchronous);
		}
	}

	@Override
	public void addUIComponent(UpdatableUI ui) {
		uiComponents.add(ui);
	}

	@Override
	public void removeUIComponent(UpdatableUI ui) {
		uiComponents.remove(ui);
	}

	private volatile boolean running = false;

	private volatile boolean done = false;

	private long frameSkip = 0;

	private long tickCount = 0;

	private long tickAutoStop = 0;

	private long slowdown = 1;

	private SimulationInterface simulation;

	private Thread myThread;

	/**
	 * Constructor for TickScheduler. This should NEVER be called directly;
	 * instead reflection should be used inside the implementation of
	 * UIInterface; look at the LocalUIInterface for an implementation example.
	 */
	public TickScheduler(SimulationInterface simulation) { // NO_UCD. Created with reflection
		myThread = new Thread(new SchedulerRunnable());
		myThread.setName("cobweb.TickScheduler");
		this.simulation = simulation;
	}

	@Override
	public SimulationInterface getSimulation() {
		return simulation;
	}

	public long getTime() {
		return tickCount;
	}

	public void setAutoStopTime(long t) {
		tickAutoStop = t;
	}

	public long getAutoStopTime() {
		return tickAutoStop;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized void dispose() {
		done = true;
		notifyAll();
	}

	private synchronized void myWait(long time) {
		try {
			wait(time);
		} catch (InterruptedException ex) {
			// Should not happen
			Logger.getLogger("COBWEB2").log(Level.INFO, "myWait broke", ex);
		}
	}


	private synchronized void stepSimulation() {
		simulation.step();
	}

	public synchronized void pause() {
		running = false;
		updateUI(true);
	}

	/**
	 * Sets running to true, and notifies the simulation thread 
	 * of this.
	 * 
	 * @see java.lang.Object#notifyAll()
	 */
	public synchronized void resume() {
		running = true;
		notifyAll();
	}

	public void setFrameSkip(long fs) {
		frameSkip = fs;
	}

	public void setDelay(long time) {
		slowdown = time;
	}

	/**
	 * Causes the simulation thread to begin execution.
	 */
	public synchronized void startIdle() {
		if (!myThread.isAlive()) {
			myThread.start();
		}
		notifyAll();
	}

	public void step() {
		if (isRunning()) {
			pause();
		}
		stepSimulation();
		updateUI(true);
	}

}
