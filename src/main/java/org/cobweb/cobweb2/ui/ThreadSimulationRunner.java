package org.cobweb.cobweb2.ui;

import org.cobweb.cobweb2.Simulation;


/**
 * TickScheduler is an implementation of Scheduler that sends uniform ticks to
 * clients.
 */
public class ThreadSimulationRunner extends SimulationRunnerBase {

	public ThreadSimulationRunner(Simulation simulation) {
		super(simulation);
	}

	/**
	 * Simulation thread loop
	 */
	private class SchedulerRunnable implements Runnable {

		/**
		 * Contains the main loop to control the simulation.
		 */
		@Override
		public void run() {
			long frameCount = 0;

			// while ThreadSimulationRunner is in control of the simulation
			while (isRunning()) {
				// Core of the simulation
				simulation.step();

				// Stop at target time
				if (getAutoStopTime() != 0 && simulation.getTime() >= getAutoStopTime()) {
					stop();
				}

				// Update UI only every frameSkip frames
				if (frameCount++ >= frameSkip) {
					updateUI(delay > 0);
					frameCount = 0;
				}

				// Delay between frames
				// The delay can be aborted early through myThreadMonitor
				synchronized(myThreadMonitor) {
					if (isRunning() && delay != 0) {
						try {
							myThreadMonitor.wait(delay);
						}
						catch (InterruptedException ex) {
							throw new RuntimeException(ex);
						}
					}
				}
			}
		}

	}

	private long frameSkip = 0;

	private long delay = 1;

	private Thread myThread;
	private Object myThreadMonitor = new Object();

	@Override
	public synchronized void stop() {
		if (!isRunning())
			return;

		// Break thread loop
		// Wake up thread if it's in delay wait()
		synchronized(myThreadMonitor) {
			super.stop();

			myThreadMonitor.notifyAll();
		}

		// In theory, there should be a myThread.join() in here, but
		// if stop() is called from the UI event thread while myThread
		// is calling functions that require the UI event thread,
		// there will be a deadlock!
		myThread = null;
	}

	@Override
	public synchronized void run() {
		if (isRunning())
			throw new IllegalStateException("Already running");

		running = true;

		notifyStarted();

		myThread = new Thread(new SchedulerRunnable());
		myThread.setName("cobweb.TickScheduler");
		myThread.start();
	}

	public void setFrameSkip(long fs) {
		frameSkip = fs;
	}

	public void setDelay(long time) {
		delay = time;
	}
}
