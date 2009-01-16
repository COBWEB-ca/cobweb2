package cobweb;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextField;

import cobweb.Environment.EnvironmentStats;
import cwcore.ComplexEnvironment;
import driver.Parser;

public class LocalUIInterface implements UIInterface,
		cobweb.TickScheduler.Client {
	private java.awt.TextArea textArea;

	public void addTextArea(java.awt.TextArea ta) {
		textArea = ta;
	}

	public void writeToTextWindow(String s) {
		if (cobweb.globals.usingTextWindow == true) {textArea.append(s);}		/*** $$$$$$ Cancel textWindow  Apr 22*/
	}

	/**
	 * Start the simulation. Calls startScheduler on the scheduler.
	 */
	public void start() {
		theScheduler.startScheduler();
	}

	/**
	 * Kill the simulation. Calls killScheduler on the scheduler.
	 */
	public void killScheduler() {
		theScheduler.killScheduler();
	}

	/**
	 * Is the simulation paused? Calls isSchedulerPaused on the scheduler.
	 *
	 * @return the paused state flag of the simulation
	 */
	public boolean isPaused() {
		return theScheduler.isSchedulerPaused();
	}

	/**
	 * Pause the simulation. Calls pauseScheduler on the scheduler.
	 */
	public void pause() {
		theScheduler.pauseScheduler();
	}

	/**
	 * Resume the simulation from a paused state. Calls resumeScheduler on the
	 * scheduler.
	 */
	public void resume() {
		theScheduler.resumeScheduler();
	}

	/**
	 * Sets the maximum time the simulation will wait for new frame data to be
	 * processed. Calls setSchedulerRefreshTimout on the scheduler.
	 */
	public void setRefreshTimeout(long tm) {
		theScheduler.setSchedulerRefreshTimeout(tm);
	}

	/**
	 * Sets the number of frames "dropped" between updates of the frame data.
	 * Calls setSchedulerFrameSkip on the scheduler.
	 */
	public void setFrameSkip(long frameSkip) {
		theScheduler.setSchedulerFrameSkip(frameSkip);
	}

	public void slowDown(long time) {
		theScheduler.setSleep(time);
	}

	/**
	 * Returns the width, in tiles, of the environment.
	 *
	 * @return 0 if there is no valid frame data, otherwise returns the width of
	 *         the frame data.
	 */

	public int getWidth() {
		if (theDrawingInfo == null) {
			return 0;
		} else {
			return theDrawingInfo.width;
		}
	}

	/**
	 * Returns the height, in tiles, of the environment.
	 *
	 * @return 0 if there is no valid frame data, otherwise returns the height
	 *         of the frame data.
	 */
	public int getHeight() {
		if (theDrawingInfo == null) {
			return 0;
		} else {
			return theDrawingInfo.height;
		}
	}

	/**
	 * Inform the UI that new frame data is available. Calls getDrawInfo on the
	 * environment, then informs the client that the frame data has been
	 * updated.
	 *
	 * @param timeout
	 *            the number of milliseconds to wait for a draw call to
	 *            complete. Negative values mean don't wait, and a value of 0
	 *            means wait indefinately.
	 */
	public void refresh(long timeout) {
		theEnvironment.getDrawInfo(this);
		theDrawingInfo = newDrawingInfo;
		newDrawingInfo = null;
		if (theClient != null) {
			// Wait for refresh calls theClient.refresh... if it didn't we'd
			// have
			// a race condition between the UI thread that refreshes and this
			// thread
			// which waits for the refresh.
			if (timeout >= 0) {
				waitForRefresh(timeout);
			} else {
				theClient.refresh(this);
			}
		}
	}

	/**
	 * Display the most recent frame data.
	 *
	 * @param g
	 *            Graphics to render to
	 * @param tileWidth
	 *            width, in pixels, of a single tile
	 * @param tileHeight
	 *            height, in pixels, of a single tile
	 */
	public void draw(java.awt.Graphics g, int tileWidth, int tileHeight) {
		if (theDrawingInfo != null) {
			theDrawingInfo.draw(g, tileWidth, tileHeight);
		}
		// Don't synchronize the whole method; drawing could take a while,
		// and the wait in refresh might want to acquire the monitor on a
		// timeout.
		doRefreshNotification();
	}

	/**
	 * Returns the tick count for the most recent frame data.
	 */
	public long getTime() {
		if (theDrawingInfo == null) {
			return 0;
		}
		return theDrawingInfo.getTime();
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

	/**
	 * Write an entry into the log
	 */
	public void writeLogEntry() {
		theEnvironment.writeLogEntry();
	}

	public void setTickField(JTextField tickField) {
		field = tickField;
	}

	/**
	 * @return TextField TickField
	 */
	public JTextField getTickField() {
		if (field != null) {
			return field; // return the TickField instance
		} else {
			return new JTextField(); // return a new "blank" instance
												// of TextField
		}
	}

	public int getTick() {
		RequestedTick = 0;
		if (field != null && !("").equals(field.getText())) {
			try {
				String text = field.getText();
				RequestedTick = Integer.parseInt(text);
			} catch (NumberFormatException e) {
			}

		}
		return RequestedTick;
	}

	public int updateclick(int x, int y, int mode, int type) {
		switch (mode) {
		case 0: // no mode selected. i.e., we want to see this specific tile
			if (cobweb.globals.usingTextWindow == true) {theEnvironment.observe(x, y, this);} 	/*** $$$$$$ Cancel textWindow  Apr 22*/
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
		refresh(1);
		return 1;
	}

	public void removeComponents(int mode) {
		theEnvironment.remove(mode, this);
		refresh(1);

	}

	/* return number of TYPES of agents in the environment */
	public int countAgentTypes() {
		return theEnvironment.getTypeCount();
	}

	public void trackAgent(String filePath) throws java.io.IOException {
		// Open the writer...
		java.io.FileWriter outStream = new java.io.FileWriter(filePath);
		// Fire it off to the environment
		ComplexEnvironment.trackAgent(outStream);
	}

	/**
	 * DrawInfo stores the frame data for the draw method to display. DrawInfo's
	 * are built by calling newTileColors and newAgent in response to a
	 * getDrawInfo call on the Environment or on an Agent. Note that DrawInfo is
	 * a private class, and only LocalUIInterface knows of it's existance. For
	 * this reason, DrawInfo is local to LocalUIInterface, and LocalUIInterface
	 * reads DrawInfo members directly.
	 */
	private static class DrawInfo {
		/** The tick we're drawing */
		long tickCount;

		/** Width of the frame info, in tiles */
		int width;

		/** Height of the frame info, in tiles */
		int height;

		/**
		 * width * height array of colors for the tiles; The color for a
		 * specific tile at (x,y) is tileColors[y * width + x]
		 */
		java.awt.Color[] tileColors;

		/** Linked list of AgentDrawInfo for the display of agents. */
		AgentDrawInfo agents;

		/**
		 * Construct a DrawInfo width specific width, height and tile colors.
		 * The tiles array is not copied; the caller is assumed to "give" the
		 * array to the drawing info, and not keep any local references around.
		 */
		DrawInfo(int w, int h, long initTickCount, java.awt.Color[] tiles) {
			width = w;
			height = h;
			tickCount = initTickCount;
			tileColors = tiles;
			agents = null;
		}

		/** Get the tick count. */
		long getTime() {
			return tickCount;
		}

		/** Draw the tiles and the agents. */
		void draw(java.awt.Graphics g, int tileWidth, int tileHeight) {
			int tileIndex = 0;
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					g.setColor(tileColors[tileIndex++]);
					g.fillRect(x * tileWidth, y * tileHeight, tileWidth,
							tileHeight);

					g.setColor(java.awt.Color.black);
					g.drawRect(x * tileWidth, y * tileHeight, tileWidth,
							tileHeight);
				}
			}
			if (agents != null) {
				agents.draw(g, tileWidth, tileHeight);
			}
		}
	}

	/**
	 * AgentDrawInfo stores the drawable state of a single agent. AgentDrawInfo
	 * exists to make the data passed to newAgent calls persist for subsequent
	 * draw calls. Note that this class is private to LocalUIInterface; no other
	 * class (other than LocalUIInterface.DrawInfo) knows of the existance of
	 * this class.
	 */
	private static class AgentDrawInfo {
		/** Solid color of the agent. */
		java.awt.Color color;

		java.awt.Color type;

		java.awt.Color action;

		/** Position in tile coordinates */
		java.awt.Point position;

		/**
		 * Facing vector; not normalized, but only the sign of each component is
		 * considered.
		 */
		java.awt.Point facing;

		/** Next element in the linked list */
		AgentDrawInfo next;

		/** Construct an AgentDrawInfo, linked to nxt, with specified properties. */
		AgentDrawInfo(AgentDrawInfo nxt, java.awt.Color c, java.awt.Color t,
				java.awt.Color strat, java.awt.Point p, java.awt.Point f) {
			next = nxt;
			color = c;
			type = t;
			action = strat;
			position = p;
			facing = f;
		}

		void draw(java.awt.Graphics g, int tileWidth, int tileHeight) {
			g.setColor(color);
			int topLeftX = position.x * tileWidth;
			int topLeftY = position.y * tileHeight;

			if (facing.x == 0 && facing.y == 0) {
				// No facing; draw an oval
				g.fillOval(topLeftX, topLeftY, tileWidth, tileHeight);
			} else {
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
					xPts = new int[] { centerX + facing.x * deltaX,
							centerX - facing.x * deltaX,
							centerX - facing.x * deltaX };
					yPts = new int[] { centerY, centerY + deltaY,
							centerY - deltaY };
				} else {
					// Vertical facing...
					xPts = new int[] { centerX, centerX + deltaX,
							centerX - deltaX };
					yPts = new int[] { centerY + facing.y * deltaY,
							centerY - facing.y * deltaY,
							centerY - facing.y * deltaY };
				}
				g.fillPolygon(xPts, yPts, 3);
				g.setColor(type);
				g.fillOval(topLeftX + tileWidth / 3 + 1, topLeftY + tileHeight
						/ 3 + 1, tileWidth / 3, tileHeight / 3);
				g.setColor(action);
				g.drawPolygon(xPts, yPts, 3);
			}
			if (next != null) {
				next.draw(g, tileWidth, tileHeight);
			}
		}
	}

	/**
	 * Notify the UI of new tile colors.
	 */
	public void newTileColors(int width, int height, java.awt.Color[] tileColors) {
		newDrawingInfo = new DrawInfo(width, height, theScheduler.getTime(),
				tileColors);

	}

	/**
	 * Notify the UI of agent drawing information.
	 */
	public void newAgent(java.awt.Color agentColor,
			java.awt.Color strategyColor, java.awt.Point position,
			java.awt.Point facing) {
		newDrawingInfo.agents = new AgentDrawInfo(newDrawingInfo.agents,
				agentColor, agentColor, strategyColor, position, facing);
	}

	public void newAgent(java.awt.Color agentColor, java.awt.Color typeColor,
			java.awt.Color strategyColor, java.awt.Point position,
			java.awt.Point facing) {
		newDrawingInfo.agents = new AgentDrawInfo(newDrawingInfo.agents,
				agentColor, typeColor, strategyColor, position, facing);
	}

	/**
	 * Construct a LocalUIInterface from a specified state file URL.
	 *
	 * Any failure to load the state file, either from file/network input
	 * errors, a badly formed state file, or a class specified in the data file
	 * being unknown will result in a InstantiationError being thrown with a
	 * descriptive and appropriate message string.
	 *
	 * @param client
	 *            the UIClient to notify of new frame data.
	 * @param fileIn
	 *            a Reader connected to the file to load as a state file.
	 */

	public LocalUIInterface(UIClient client, Parser p) {
		load(client, p);
		currentParser = p;
		theScheduler.addSchedulerClient(this);
	}

	public LocalUIInterface(UIInterface.UIClient client, Parser p[],
			int time[], int numfiles) {
		theClient = client;
		totalfilenum = numfiles;
		pauseAt = time;
		parsedfiles = p;
		loadNewDataFile(0);
		theScheduler.addSchedulerClient(this);

	}

	private void loadNewDataFile(int n) {
		//System.out.println("Loading new file " + n);  // $$$$$$ silenced on Apr 22
		this.load(theClient, parsedfiles[n]);
		currentParser = parsedfiles[n];
	}

	// return current Parser being dealt with
	public Parser getcurrentParser() {
		return currentParser;
	}

	/**
	 * Initialize the specified environment class with state data read from the
	 * Reader. This is a private helper to the LocalUIInterface constructor.
	 */

	public void load(UIInterface.UIClient client, Parser p) {
		theClient = client;
		InitScheduler(p.TickScheduler, p);
		InitEnvironment("cwcore.ComplexEnvironment", p);
	}

	private void InitEnvironment(String environmentName, Parser p) {
		try {
			if (theEnvironment != null
					&& theEnvironment.getClass().getName().equals(
							environmentName)) {
				//System.out.println("Environment matched\n");  // $$$$$$ silenced on Apr 22
				theEnvironment.load(theScheduler, p);
				return;
			}
			Class<?> environmentClass = Class.forName(environmentName);
			// Use reflection to find a constructor taking a Scheduler parameter
			// and a Reader
			Constructor<?> environmentCtor = null;
			{
				Constructor<?>[] environmentCtors = environmentClass
						.getConstructors();

				for (int i = 0; i < environmentCtors.length; ++i) {
					Class<?>[] params = environmentCtors[i].getParameterTypes();

					if (params != null && params.length == 2
							&& params[0].getName().equals("cobweb.Scheduler")
							&& params[1].getName().equals("driver.Parser")) {
						environmentCtor = environmentCtors[i];
						break;
					}
				}
			}
			if (environmentCtor == null) {
				throw new InstantiationError(
						"No valid constructor found on environment class.");
			} else {
				theEnvironment = (Environment) environmentCtor
						.newInstance(new Object[] { theScheduler, p });
			}
		} catch (SecurityException e) {
			throw new InstantiationError(e.toString());
		} catch (InstantiationException e) {
			throw new InstantiationError(e.toString());
		} catch (IllegalAccessException e) {
			throw new InstantiationError(e.toString());
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new InstantiationError(e.getTargetException().toString());
		} catch (ClassNotFoundException e) {
			throw new InstantiationError(e.toString());
		} catch (java.io.IOException e) {
			throw new InstantiationError(e.toString());
		}
		Environment.setUIPipe(this);
	}

	/** ********************************************************************** */
	public void tickNotification(long tickCount) {
		if (files < totalfilenum
				&& (getTime() + 1) == (pauseAt[files] + tickcounter)) {
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

	private final Set<TickEventListener> tickListeners = new HashSet<TickEventListener>();

	public interface TickEventListener {
		public void TickPerformed(long currentTick);
	}

	public void AddTickEventListener(TickEventListener listener) {
		tickListeners.add(listener);
		listener.TickPerformed(tickcounter);
	}

	public void RemoveTickEventListener(TickEventListener listener) {
		tickListeners.remove(listener);
	}

	/**
	 * Initialize the specified Scheduler class with state data read from the
	 * Reader. This is a private helper to the LocalUIInterface constructor.
	 */
	private void InitScheduler(String schedulerName, Parser p/*
																 * java.io.Reader
																 * fileIn
																 */) {
		//System.out.println("Scheduler name = " + theScheduler);  // $$$$$$ silenced on Apr 22
		try {
			if (theScheduler != null
					&& theScheduler.getClass().getName().equals(schedulerName)) {
				//System.out.println("Scheduler matched\n");   // $$$$$$ silenced on Apr 22
				theScheduler.loadScheduler(this, p /* fileIn */);
				return;
			}

			Class<?> schedulerClass = Class.forName(schedulerName);
			// Use reflection to find a contructor taking a UIInterface
			// parameter and a Reader
			Constructor<?> theCtor = null;
			{
				Constructor<?>[] schedulerCtors = schedulerClass.getConstructors();
				for (int i = 0; i < schedulerCtors.length; ++i) {
					Class<?>[] params = schedulerCtors[i].getParameterTypes();
					if (params != null
							&& params.length == 2
							&& params[0].getName().equals("cobweb.UIInterface")
							&& params[1]
									.getName()
									.equals("driver.Parser"/* "java.io.Reader" */)) {
						theCtor = schedulerCtors[i];
						break;
					}
				}
			}
			if (theCtor == null) {
				throw new InstantiationError(
						"Correct constructor not found in " + schedulerName);
			} else {
				theScheduler = (Scheduler) theCtor.newInstance(new Object[] {
						this, p /* fileIn */});
			}
		} catch (SecurityException e) {
			throw new InstantiationError(e.toString());
		} catch (InstantiationException e) {
			throw new InstantiationError(e.toString());
		} catch (IllegalAccessException e) {
			throw new InstantiationError(e.toString());
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new InstantiationError(e.getTargetException().toString());
		} catch (ClassNotFoundException e) {
			throw new InstantiationError(e.toString());
		}
	}

	/**
	 * Notify the simulation of a completed draw call. Private helper to draw.
	 *
	 * @see cobweb.LocalUIInterface#draw
	 */
	private synchronized void doRefreshNotification() {
		notifyAll();
	}

	/**
	 * Wait at most timeout ms for a refresh notification. Private helper to
	 * refresh.
	 *
	 * @see cobweb.LocalUIInterface#refresh
	 */
	private synchronized void waitForRefresh(long timeout) {
		theClient.refresh(this);
		try {
			if (timeout == 0) {
				wait();
			} else {
				wait(timeout);
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * This is the frame data under construction by setAgent calls. Constructed
	 * in a seperate member from the finished frame data so a refresh can occur
	 * while a draw is happening.
	 */
	private volatile DrawInfo newDrawingInfo;

	/** This is the most recent completed drawing info */
	private volatile DrawInfo theDrawingInfo;

	private UIInterface.UIClient theClient;

	private Environment theEnvironment;

	private volatile Scheduler theScheduler;

	private int RequestedTick = 0;

	private JTextField field;

	public int count = 0;

	public void reset() {
		theScheduler.resetTime();
	}

	private Parser[] parsedfiles;

	private Parser currentParser;

	private int[] pauseAt;

	private int files = 0;

	private int totalfilenum = 0;

	private long tickcounter = 0;


	private driver.PauseButton pauseButton;  // $$$$$$ Mar 20

	// $$$$$$ Get/Set Pause Button.  Mar 20
	public driver.PauseButton getPauseButton() {
		return pauseButton;
	}
	public void setPauseButton(driver.PauseButton pb) {
		pauseButton = pb;
	}

	public EnvironmentStats getStatistics() {
		return theEnvironment.getStatistics();
	}
}
