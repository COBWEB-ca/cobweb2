package cobweb;

import ga.GATracker;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import cwcore.ComplexEnvironment.CommManager;
import driver.Parser;

/**
 * TickScheduler is an implementation of Scheduler that sends uniform ticks to
 * clients.
 */
public class TickScheduler extends Thread implements Scheduler {

	private volatile boolean bPaused = true;

	private volatile boolean bDone = true; // Thread.stop() is unsafe

	private long refreshTimeout = 0;

	private long frameSkip = 0;

	private long logCount = 0;

	private final long logTicks = 0;

	private long tickCount = 0;

	private long slowdown = 0;

	private UIInterface theUI;

	private final Set<Client> clientV = new LinkedHashSet<Client>();

	public boolean isSchedulerPaused() {
		return bPaused;
	}

	public synchronized void pauseScheduler() {
		bPaused = true;
		theUI.getPauseButton().updateLabel();  // $$$$$$ Mar 20
	}

	public synchronized void resumeScheduler() {
		bPaused = false;
		notifyAll();
	}

	public synchronized void startScheduler() {
		if (!isAlive()) {
			start();
		}
		notifyAll();
	}

	public synchronized void killScheduler() {
		bDone = true;
		notifyAll();
	}

	/**
	 * The TickScheduler client interface is quite trivial; tickNotification is
	 * called on each client each tick.
	 */
	public interface Client {
		/** Notification of a tick. */
		public void tickNotification(long tick);
	}

	// Client management

	public synchronized void addSchedulerClient(Object theClient) {
		clientV.add((Client)theClient);
	}

	public synchronized void removeSchedulerClient(Object theClient) {
		clientV.remove(theClient);
	}

	// Parameters
	public void setSchedulerRefreshTimeout(long tm) {
		refreshTimeout = tm;
	}

	public void setSchedulerFrameSkip(long fs) {
		frameSkip = fs;
	}

	// Saving. // $$$$$ Used to be invoked from this chain: the now silenced method CobwebApplication.saveFile => LocalUIInterface.save
	public synchronized void saveScheduler(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w);
		pw.println(this.getClass().getName());

		pw.println("LogTicks " + logTicks);
		pw.println("TickCount " + tickCount);

		pw.println(this.getClass().getName() + ".End");
		pw.flush();
	}

	public synchronized void loadScheduler(UIInterface ui, Parser p) {
		theUI = ui;
	}

	/**
	 * Constructor for TickScheduler. This should NEVER be called directly;
	 * instead reflection should be used inside the implementation of
	 * UIInterface; look at the LocalUIInterface for an implementation example.
	 */
	public TickScheduler(UIInterface ui, Parser p) {
		this.setName("TickScheduler");
		bDone = false;
		loadScheduler(ui, p);
	}

	public void setSleep(long time) {
		slowdown = time;
	}

	private synchronized void doTick() {

		CommManager commManager = new CommManager();
		commManager.decrementPersistence();
		commManager.unblockBroadcast();
		++tickCount;

		for (Client client : new Vector<Client>(clientV)) {
			client.tickNotification(tickCount);
		}

		/* If program is set to track GA info, then print them. */
		if (GATracker.getTrackGeneStatusDistribution() || GATracker.getTrackGeneValueDistribution()) {
			GATracker.printGAInfo(tickCount);
		}

		++logCount;
		if (logCount > logTicks) {
			logCount = 0;
			theUI.writeLogEntry();
		}


		// carry on doing something
	}

	@Override
	public void run() {
		theUI.refresh(refreshTimeout);
		long frameCount = 0;
		// Forever...

		while (!bDone) {
			cwcore.ComplexAgent.dumpData(tickCount);
			cwcore.ComplexAgent.clearData();

			if (bPaused && !bDone) {
				myWait(100);
			} else {
				doTick();

				if (theUI.getTick() != 0 && getTime() == theUI.getTick()) {
					pauseScheduler();
				}
				if (frameCount >= frameSkip) {
					theUI.refresh(refreshTimeout);
					frameCount = 0;
				}
			}



			// start doing something
			if (!bPaused && slowdown > 0) {
				myWait(slowdown);
			}
		}
	}

	private synchronized void myWait(long time) {
		try {
			wait(time);
		} catch (InterruptedException ex) {
			Logger.getAnonymousLogger().log(Level.INFO, "that's weird", ex);
		}
	}

	public long getTime() {
		return tickCount;
	}

	public void resetTime() {
		tickCount = 0;
	}
}
