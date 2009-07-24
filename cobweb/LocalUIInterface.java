package cobweb;

import ga.GeneticsMutator;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JTextField;

import cobweb.Environment.EnvironmentStats;
import cobweb.Environment.Location;
import cwcore.ComplexAgent;
import cwcore.ComplexEnvironment;
import driver.Parser;

public class LocalUIInterface implements UIInterface, cobweb.TickScheduler.Client {
	/**
	 * AgentDrawInfo stores the drawable state of a single agent. AgentDrawInfo exists to make the data passed to
	 * newAgent calls persist for subsequent draw calls. Note that this class is private to LocalUIInterface; no other
	 * class (other than LocalUIInterface.DrawInfo) knows of the existence of this class.
	 */
	private static class AgentDrawInfo {
		/** Solid color of the agent. */
		java.awt.Color agentColor;

		java.awt.Color type;

		java.awt.Color action;

		/** Position in tile coordinates */
		Point2D position;

		/**
		 * Facing vector; not normalized, but only the sign of each component is considered.
		 */
		Point2D facing;

		/** Construct an AgentDrawInfo, linked to nxt, with specified properties. */
		AgentDrawInfo(java.awt.Color c, java.awt.Color t, java.awt.Color strat, Point2D p, Point2D f) {
			agentColor = c;
			type = t;
			action = strat;
			position = p;
			facing = f;
		}

		void draw(Graphics g, int tileWidth, int tileHeight) {
			g.setColor(agentColor);
			int topLeftX = position.x * tileWidth;
			int topLeftY = position.y * tileHeight;

			if (facing.x != 0 || facing.y != 0) {
				int[] xPts = null;
				int[] yPts = null;
				int deltaX = tileWidth / 2;
				int deltaY = tileHeight / 2;
				int centerX = topLeftX + deltaX;
				int centerY = topLeftY + deltaY;
				if (facing.x != 0 && facing.y != 0) {
					// Diagonal; deal with this later
				} else if (facing.x != 0) {
					// Horizontal facing...
					xPts = new int[] { centerX + facing.x * deltaX, centerX - facing.x * deltaX,
							centerX - facing.x * deltaX };
					yPts = new int[] { centerY, centerY + deltaY, centerY - deltaY };
				} else {
					// Vertical facing...
					xPts = new int[] { centerX, centerX + deltaX, centerX - deltaX };
					yPts = new int[] { centerY + facing.y * deltaY, centerY - facing.y * deltaY,
							centerY - facing.y * deltaY };
				}
				g.fillPolygon(xPts, yPts, 3);
				g.setColor(type);
				g.fillOval(topLeftX + tileWidth / 3, topLeftY + tileHeight / 3, tileWidth / 3, tileHeight / 3);
				g.setColor(action);
				g.drawPolygon(xPts, yPts, 3);
			} else {
				g.fillOval(topLeftX, topLeftY, tileWidth, tileHeight);
			}
		}
	}

	/**
	 * DrawInfo stores the frame data for the draw method to display. DrawInfo's are built by calling newTileColors and
	 * newAgent in response to a getDrawInfo call on the Environment or on an Agent. Note that DrawInfo is a private
	 * class, and only LocalUIInterface knows of it's existance. For this reason, DrawInfo is local to LocalUIInterface,
	 * and LocalUIInterface reads DrawInfo members directly.
	 */
	private static class DrawInfo {
		/** The tick we're drawing */
		long tickCount;

		/** Width of the frame info, in tiles */
		int width;

		/** Height of the frame info, in tiles */
		int height;

		/**
		 * width * height array of colors for the tiles; The color for a specific tile at (x,y) is
		 * tileColors[y * width * + x]
		 */
		java.awt.Color[] tileColors;

		/** Linked list of AgentDrawInfo for the display of agents. */
		List<AgentDrawInfo> agents;

		List<PathDrawInfo> paths;

		/**
		 * Construct a DrawInfo width specific width, height and tile colors. The tiles array is not copied; the caller
		 * is assumed to "give" the array to the drawing info, and not keep any local references around.
		 */
		DrawInfo(int w, int h, long initTickCount, java.awt.Color[] tiles) {
			width = w;
			height = h;
			tickCount = initTickCount;
			tileColors = tiles;
			agents = new LinkedList<AgentDrawInfo>();
			paths = new LinkedList<PathDrawInfo>();
		}

		/** Draw the tiles and the agents. */
		void draw(java.awt.Graphics g, int tileWidth, int tileHeight) {
			int tileIndex = 0;
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					g.setColor(tileColors[tileIndex++]);
					g.fillRect(x * tileWidth + 1, y * tileHeight + 1, tileWidth - 1, tileHeight - 1);
				}
			}
			g.setColor(COLOR_GRIDLINES);
			for (int y = 0; y <= height; y++) {
				g.drawLine(0, y * tileHeight, tileWidth * width, y * tileHeight);
			}
			for (int x = 0; x <= width; x++) {
				g.drawLine(x * tileWidth, 0, x * tileWidth, tileHeight * height);
			}

			for (AgentDrawInfo a : agents) {
				a.draw(g, tileWidth, tileHeight);
			}

			for (PathDrawInfo path : paths) {
				path.draw(g, tileWidth, tileHeight);
			}
		}

		/** Get the tick count. */
		long getTime() {
			return tickCount;
		}
	}

	private static class PathDrawInfo {

		public static PathDrawInfo pathDraw;

		private final List<Location> path;

		public PathDrawInfo(List<Location> path) {
			this.path = path;
		}

		public void draw(Graphics g, int tileWidth, int tileHeight) {
			Iterator<Location> itr = path.iterator();
			if (!itr.hasNext()) {
				return;
			}
			Location p1;
			Location p2 = itr.next();
			Color o = g.getColor();

			int alpha = 63;

			int increment = 192 / path.size();

			while (itr.hasNext()) {
				g.setColor(new Color(0, 0, 255, alpha));
				p1 = p2;
				p2 = itr.next();
				int x = Math.min(p1.v[0], p2.v[0]);
				int y = Math.min(p1.v[1], p2.v[1]);
				int w = Math.abs(p1.v[0] - p2.v[0]);
				int h = Math.abs(p1.v[1] - p2.v[1]);

				if (w > 1 || h > 1) {
					continue;
				}

				int stripwidth = (tileWidth - tileWidth * 4 / 5) + 1;
				int stripheight = (tileHeight - tileHeight * 4 / 5) + 1;

				x = x * tileWidth + tileWidth * 2 / 5;
				y = y * tileHeight + tileHeight * 2 / 5;
				w = w * tileWidth + stripwidth;
				h = h * tileHeight + stripheight;

				if (p1.v[0] - p2.v[0] > 0) {
					w -= stripwidth;
					x += stripwidth;
				} else if (p1.v[0] - p2.v[0] < 0) {
					w -= stripwidth;
				} else if (p1.v[1] - p2.v[1] > 0) {
					h -= stripheight;
					y += stripheight;
				} else if (p1.v[1] - p2.v[1] < 0) {
					h -= stripheight;
				}

				g.fillRect(x, y, w, h);
				alpha += increment;
			}
			g.setColor(o);
		}
	}

	public static interface TickEventListener {
		public void TickPerformed(long currentTick);
	}

	long myClock = -1;

	long delay = 0;

	private static final Color COLOR_GRIDLINES = Color.lightGray;

	private final Set<TickEventListener> tickListeners = new LinkedHashSet<TickEventListener>();

	/**
	 * This is the frame data under construction by setAgent calls. Constructed in a seperate member from the finished
	 * frame data so a refresh can occur while a draw is happening.
	 */
	private volatile DrawInfo newDrawingInfo;

	/** This is the most recent completed drawing info */
	private volatile DrawInfo theDrawingInfo;

	private UIInterface.UIClient theClient;

	private Environment theEnvironment;

	private volatile Scheduler theScheduler;

	private JTextField stopTickField;

	public int count = 0;

	private Parser[] parsedfiles;

	private Parser currentParser;

	private int[] pauseAt;

	private int files = 0;

	private int totalfilenum = 0;

	private long tickcounter = 0;

	private driver.PauseButton pauseButton;

	private boolean runnable = false;

	/**
	 * Construct a LocalUIInterface from a specified state file URL.
	 *
	 * Any failure to load the state file, either from file/network input errors, a badly formed state file, or a class
	 * specified in the data file being unknown will result in a InstantiationError being thrown with a descriptive and
	 * appropriate message string.
	 *
	 * @param client the UIClient to notify of new frame data.
	 * @param p configuration
	 */

	public LocalUIInterface(UIClient client, Parser p) {
		load(client, p);
		currentParser = p;
	}

	public LocalUIInterface(UIInterface.UIClient client, Parser p[], int time[], int numfiles) {
		theClient = client;
		totalfilenum = numfiles;
		pauseAt = time;
		parsedfiles = p;
		loadNewDataFile(0);
	}

	public void AddTickEventListener(TickEventListener listener) {
		tickListeners.add(listener);
		listener.TickPerformed(myClock);
	}

	/* return number of TYPES of agents in the environment */
	public int countAgentTypes() {
		return theEnvironment.getTypeCount();
	}

	/**
	 * Display the most recent frame data.
	 *
	 * @param g Graphics to render to
	 * @param tileWidth width, in pixels, of a single tile
	 * @param tileHeight height, in pixels, of a single tile
	 */
	public synchronized void draw(java.awt.Graphics g, int tileWidth, int tileHeight) {
		if (theDrawingInfo != null) {
			theDrawingInfo.draw(g, tileWidth, tileHeight);
		}
		// Don't synchronise the whole method; drawing could take a while,
		// and the wait in refresh might want to acquire the monitor on a
		// timeout.
		//// ^ odd, works fine
		//doRefreshNotification();
	}

	// return current Parser being dealt with
	public Parser getcurrentParser() {
		return currentParser;
	}

	/**
	 * Returns the tick count for the most recent frame data.
	 */
	public long getCurrentTime() {
		return myClock;
	}

	/**
	 * Returns the height, in tiles, of the environment.
	 *
	 * @return 0 if there is no valid frame data, otherwise returns the height of the frame data.
	 */
	public int getHeight() {
		return theEnvironment.getSize(1);
	}

	public driver.PauseButton getPauseButton() {
		return pauseButton;
	}

	public EnvironmentStats getStatistics() {
		return theEnvironment.getStatistics();
	}

	public long getStopTime() {
		int stopTime = 0;
		if (stopTickField != null && !("").equals(stopTickField.getText())) {
			try {
				String text = stopTickField.getText();
				stopTime = Integer.parseInt(text);
			} catch (NumberFormatException e) {
				// broken input, don't care
			}
		}
		return stopTime;
	}

	/**
	 * @return TextField TickField
	 */
	public JTextField getTimeStopField() {
		if (stopTickField != null) {
			return stopTickField; // return the TickField instance
		} else {
			return new JTextField(); // return a new "blank" instance
			// of TextField
		}
	}

	/**
	 * Returns the width, in tiles, of the environment.
	 *
	 * @return 0 if there is no valid frame data, otherwise returns the width of the frame data.
	 */

	public int getWidth() {
		return theEnvironment.getSize(0);
	}

	private void InitEnvironment(String environmentName, Parser p) {
		try {
			if (theEnvironment == null) {
				Class<?> environmentClass = Class.forName(environmentName);
				// Use reflection to find a constructor taking a Scheduler parameter
				// and a Reader
				Constructor<?> environmentCtor = environmentClass.getConstructor();
				if (environmentCtor == null) {
					throw new InstantiationError("No valid constructor found on environment class.");
				} else {
					theEnvironment = (Environment) environmentCtor.newInstance();
				}
			}
			theEnvironment.load(theScheduler, p);
		} catch (Exception ex) {
			throw new RuntimeException("Can't InitEnvironment", ex);
		}
		Environment.setUIPipe(this);
	}

	/**
	 * Initialize the specified Scheduler class with state data read from the Reader. This is a private helper to the
	 * LocalUIInterface constructor.
	 */
	private void InitScheduler(String schedulerName, Parser p) {
		try {
			if (theScheduler != null) {
				theScheduler.killScheduler();
			}

			Class<?> schedulerClass = Class.forName(schedulerName);
			// Use reflection to find a constructor taking a UIInterface
			// parameter and a Reader
			Constructor<?> theCtor = schedulerClass.getConstructor(UIInterface.class, Parser.class);
			if (theCtor == null) {
				throw new InstantiationError("Correct constructor not found in " + schedulerName);
			} else {
				theScheduler = (Scheduler) theCtor.newInstance(this, p);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot initialize scheduler", e);
		}
	}

	/**
	 * Is the simulation paused? Calls isSchedulerPaused on the scheduler.
	 *
	 * @return the paused state flag of the simulation
	 */
	public boolean isPaused() {
		return theScheduler.isSchedulerPaused();
	}

	public boolean isRunnable() {
		return runnable;
	}

	/**
	 * Kill the simulation. Calls killScheduler on the scheduler.
	 */
	public void killScheduler() {
		theScheduler.killScheduler();
	}

	/**
	 * Initialize the specified environment class with state data read from the Reader. This is a private helper to the
	 * LocalUIInterface constructor.
	 */

	public void load(UIInterface.UIClient client, Parser p) {
		theClient = client;
		InitScheduler(p.getEnvParams().schedulerName, p);

		GeneticsMutator gm = new GeneticsMutator(p.getGeneticParams(), p.getEnvParams().agentTypeCount);
		ComplexAgent.addMutator(gm);
		ComplexAgent.setSimularityCalc(gm);

		InitEnvironment("cwcore.ComplexEnvironment", p);


		theScheduler.addSchedulerClient(this);
		theScheduler.addSchedulerClient(gm.getTracker());

		theScheduler.setSleep(delay);

		theScheduler.startScheduler();
		tickNotification(0);
	}

	private void loadNewDataFile(int n) {
		// System.out.println("Loading new file " + n); // $$$$$$ silenced on Apr 22
		this.load(theClient, parsedfiles[n]);
		currentParser = parsedfiles[n];
	}

	/**
	 * Set the log file.
	 *
	 * @see cobweb.UIInterface#log
	 */
	public void log(String filePath) throws java.io.IOException {
		// Open the Writer...
		java.io.FileWriter outStream = new java.io.FileWriter(filePath);
		// Fire it off to the environment
		theEnvironment.log(outStream);
	}

	public synchronized void newAgent(java.awt.Color agentColor, java.awt.Color typeColor,
			java.awt.Color strategyColor, Point2D position, Point2D facing) {
		newDrawingInfo.agents.add(new AgentDrawInfo(agentColor, typeColor, strategyColor, position, facing));
	}

	/**
	 * Notify the UI of agent drawing information.
	 */
	public synchronized void newAgent(java.awt.Color agentColor, java.awt.Color strategyColor, Point2D position,
			Point2D facing) {
		newDrawingInfo.agents.add(new AgentDrawInfo(agentColor, agentColor, strategyColor, position, facing));
	}

	public synchronized void newPath(List<Location> path) {
		newDrawingInfo.paths.add(new PathDrawInfo(path));
	}

	/**
	 * Notify the UI of new tile colors.
	 */
	public void newTileColors(int width, int height, java.awt.Color[] tileColors) {
		newDrawingInfo = new DrawInfo(width, height, theScheduler.getTime(), tileColors);

	}

	/**
	 * Pause the simulation. Calls pauseScheduler on the scheduler.
	 */
	public void pause() {
		theScheduler.pauseScheduler();
	}

	/**
	 * Inform the UI that new frame data is available. Calls getDrawInfo on the environment, then informs the client
	 * that the frame data has been updated.
	 *
	 * @param wait Wait for refresh?
	 */
	public void refresh(boolean wait) {
		theEnvironment.getDrawInfo(this);
		theDrawingInfo = newDrawingInfo;
		newDrawingInfo = null;
		if (theClient != null) {
			// Wait for refresh calls theClient.refresh... if it didn't we'd have a race condition between
			// the UI thread that refreshes and this thread which waits for the refresh.
			theClient.refresh(this, wait);
		}
	}

	public void removeComponents(int mode) {
		theEnvironment.remove(mode, this);
		refresh(true);
	}

	public void RemoveTickEventListener(TickEventListener listener) {
		tickListeners.remove(listener);
	}

	public void report(String filePath) throws java.io.IOException {
		// Open the Writer...
		java.io.FileWriter outStream = new java.io.FileWriter(filePath);

		boolean pauseState = isPaused();

		// Pause, if needed
		if (!pauseState) {
			pause();
		}

		// Save the environment
		theEnvironment.report(outStream);

		// Close the stream
		outStream.close();

		// Resume, if needed
		if (!pauseState) {
			resume();
		}
	}

	public void reset() {
		theScheduler.resetTime();
	}

	/**
	 * Resume the simulation from a paused state. Calls resumeScheduler on the scheduler.
	 */
	public void resume() {
		theScheduler.resumeScheduler();
	}

	/**
	 * Save the state of the simulation.
	 *
	 * @see cobweb.UIInterface#save
	 */
	// $$$$$ Method save used to be invoked by the now silenced method CobwebApplication.saveFile
	public void save(String filePath) throws java.io.IOException {
		// Open the Writer...
		java.io.FileWriter outStream = new java.io.FileWriter(filePath);

		boolean pauseState = isPaused();

		// Pause, if needed
		if (!pauseState) {
			pause();
		}

		// Save the scheduler: blocks for appropriate timing
		theScheduler.saveScheduler(outStream);

		// Save the environment
		theEnvironment.save(outStream);

		// Close the stream
		outStream.close();

		// Resume, if needed
		if (!pauseState) {
			resume();
		}
	}

	/**
	 * Sets the number of frames "dropped" between updates of the frame data. Calls setSchedulerFrameSkip on the
	 * scheduler.
	 */
	public void setFrameSkip(long frameSkip) {
		theScheduler.setSchedulerFrameSkip(frameSkip);
	}

	public void setPauseButton(driver.PauseButton pb) {
		pauseButton = pb;
	}

	public void setRunnable(boolean ready) {
		this.runnable = ready;
	}

	public void setTimeStopField(JTextField tickField) {
		stopTickField = tickField;
	}

	public void slowDown(long time) {
		delay = time;
		theScheduler.setSleep(delay);
	}

	/**
	 * Start the simulation. Calls startScheduler on the scheduler.
	 */
	public void start() {
	}

	/** ********************************************************************** */
	public void tickNotification(long tickCount) {
		myClock = tickCount;
		if (files < totalfilenum && (tickCount + 1) == (pauseAt[files] + tickcounter)) {
			this.pause();
			tickcounter = pauseAt[files];
			files++;
			if (files < totalfilenum) {
				loadNewDataFile(files);
				this.resume();
			}
		}
		if (pauseAt != null && files >= pauseAt.length) {
			this.pause();
		}

		for (TickEventListener listener : tickListeners) {
			listener.TickPerformed(tickCount);
		}
	}

	public void trackAgent(String filePath) throws java.io.IOException {
		// Open the writer...
		java.io.FileWriter outStream = new java.io.FileWriter(filePath);
		// Fire it off to the environment
		ComplexEnvironment.trackAgent(outStream);
	}

	public int updateclick(int x, int y, int mode, int type) {
		switch (mode) {
			case 0: // no mode selected. i.e., we want to see this specific tile
				theEnvironment.observe(x, y, this);
				break;
			case 1:
				theEnvironment.selectStones(x, y, this);
				break;
			case 2:
				theEnvironment.selectFood(x, y, type, this);
				break;
			case 3:
				theEnvironment.selectAgent(x, y, type, this);
				break;
			default:
				break;
		}
		try {
			refresh(true);
		} catch (ConcurrentModificationException ex) {
			// Doesn't do anything bad, but a new agent might not show up if this exception occurs
			// TODO: fix if possible
		}
		return 1;
	}


	/**
	 * Write an entry into the log
	 */
	public void writeLogEntry() {
		theEnvironment.writeLogEntry();
	}

	Logger myLogger = Logger.getLogger("COBWEB2");

	public void writeOutput(String s) {
		myLogger.info(s);
	}
}
