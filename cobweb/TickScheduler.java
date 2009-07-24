package cobweb;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import cwcore.ComplexEnvironment.CommManager;
import driver.Parser;

/**
 * TickScheduler is an implementation of Scheduler that sends uniform ticks to clients.
 */
public class TickScheduler extends Thread implements Scheduler {

	/**
	 * The TickScheduler client interface is quite trivial; tickNotification is called on each client each tick.
	 */
	public static interface Client {
		/** Notification of a tick. */
		public void tickNotification(long time);
	}

	private volatile boolean bPaused = true;

	private volatile boolean bDone = true; // Thread.stop() is unsafe

	private long frameSkip = 0;

	private long logCount = 0;

	private final long logTicks = 0;

	private long tickCount = 0;

	private long slowdown = 1;

	private UIInterface theUI;

	private final Set<Client> clientV = new LinkedHashSet<Client>();

	/**
	 * Constructor for TickScheduler. This should NEVER be called directly; instead reflection should be used inside the
	 * implementation of UIInterface; look at the LocalUIInterface for an implementation example.
	 */
	public TickScheduler(UIInterface ui, Parser p) {
		this.setName("cobweb.TickScheduler");
		bDone = false;
		loadScheduler(ui, p);
	}

	public synchronized void addSchedulerClient(Object theClient) {
		clientV.add((Client) theClient);
	}

	private synchronized void doTick() {

		CommManager commManager = new CommManager();
		commManager.decrementPersistence();
		commManager.unblockBroadcast();
		++tickCount;

		for (Client client : new Vector<Client>(clientV)) {
			client.tickNotification(tickCount);
		}

		++logCount;
		if (logCount > logTicks) {
			logCount = 0;
			theUI.writeLogEntry();
		}

		// carry on doing something
	}

	public long getTime() {
		return tickCount;
	}

	public boolean isSchedulerPaused() {
		return bPaused;
	}

	// Client management

	public synchronized void killScheduler() {
		bDone = true;
		notifyAll();
	}

	public synchronized void loadScheduler(UIInterface ui, Parser p) {
		theUI = ui;
	}

	private synchronized void myWait(long time) {
		try {
			wait(time);
		} catch (InterruptedException ex) {
			// Should not happen
			Logger.getAnonymousLogger().log(Level.INFO, "myWait broke", ex);
		}
	}

	public synchronized void pauseScheduler() {
		bPaused = true;
		theUI.getPauseButton().updateLabel(); // $$$$$$ Mar 20
	}

	public synchronized void removeSchedulerClient(Object theClient) {
		clientV.remove(theClient);
	}

	public void resetTime() {
		tickCount = 0;
	}

	public synchronized void resumeScheduler() {
		bPaused = false;
		notifyAll();
	}

	@Override
	public void run() {
		theUI.refresh(true);
		long frameCount = 0;
		// Forever...

		while (!bDone) {
			cwcore.ComplexAgent.dumpData(tickCount);
			cwcore.ComplexAgent.clearData();

			if (bPaused && !bDone) {
				myWait(1000);
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
			if (!bPaused && slowdown != 0) {
				myWait(slowdown);
			}
		}
	}

	// Saving. // $$$$$ Used to be invoked from this chain: the now silenced method CobwebApplication.saveFile =>
	// LocalUIInterface.save
	public synchronized void saveScheduler(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w);
		pw.println(this.getClass().getName());

		pw.println("LogTicks " + logTicks);
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

	public synchronized void startScheduler() {
		if (!isAlive()) {
			start();
		}
		notifyAll();
	}
}
