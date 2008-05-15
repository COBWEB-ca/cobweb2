package cobweb;

import cwcore.ComplexEnvironment;
import cwcore.ComplexEnvironment.CommManager;
import driver.Parser;
import ga.GATracker;

/**
 * TickScheduler is an implementation of Scheduler that sends uniform ticks to
 * clients.
 */
public class TickScheduler extends Thread implements Scheduler {

	private volatile boolean bPaused = true;

	private volatile boolean bDone = true; // Thread.stop() is unsafe

	// Thread code; implementation details
	public java.util.Timer timer;

	private long refreshTimeout = 0;

	private long frameSkip = 0;

	private long logCount = 0;

	private long logTicks = 0;

	private long tickCount = 0;

	private long slowdown = 0;

	private UIInterface theUI;

	private java.util.Vector clientV = new java.util.Vector();

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
		if (!isAlive())
			start();
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
		clientV.addElement(theClient);
	}

	public synchronized void removeSchedulerClient(Object theClient) {
		clientV.removeElement(theClient);
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
		bDone = false;
		loadScheduler(ui, p);
		timer = new java.util.Timer();
	}

	public void setSleep(long time) {
		slowdown = time;
	}

	private synchronized void doTick() {
		while (bPaused && !bDone) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		CommManager commManager = new CommManager();
		commManager.decrementPersistence();
		commManager.unblockBroadcast();
		++tickCount;
		
		for (java.util.Enumeration e = clientV.elements(); e.hasMoreElements();)
			((Client) e.nextElement()).tickNotification(tickCount);
		
		/* If program is set to track GA info, then print them. */
		if (GATracker.getTrackGeneStatusDistribution() || GATracker.getTrackGeneValueDistribution()) {
			GATracker.printGAInfo(tickCount);
		}
		
		++logCount;
		if (logCount > logTicks) {
			logCount = 0;
			theUI.writeLogEntry();
		}		
		
		
		
		// start doing something
		try {
			Thread.sleep(slowdown);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// carry on doing something				
	}

	public void run() {
		theUI.refresh(refreshTimeout);
		long frameCount = 0;
		// Forever...
		
		
		while (!bDone) {					
			cwcore.ComplexAgent.dumpData(tickCount);
			cwcore.ComplexAgent.clearData();
			
			if (tickCount == 0) { 
				GATracker.initializeGAInfoOutput();
			}
			
			doTick();

			if (frameCount >= frameSkip) {
				frameCount = 0;
				theUI.refresh(refreshTimeout);
			} else
				++frameCount;
			yield();
			if (theUI.getTick() != 0 && getTime() == theUI.getTick()) {
				pauseScheduler();
				theUI.refresh(refreshTimeout);
				// System.out.println("Value is: "+bPaused);
			}
		}
	}

	public long getTime() {
		return tickCount;
	}

	public void resetTime() {
		tickCount = 0;
	}
}
