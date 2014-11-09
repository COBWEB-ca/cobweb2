package cobweb;

/**
 * The UIInterface is the "pipe" between the user interface and the simulation
 * classes; it is also the top-level interface to the simulation.
 * 
 * The Driver (main in an application, or the applet) should create a derivative
 * of UIInterface to initialize the system as appropriate for the context; a
 * LocalUIInterface exists for a single application simulation, and a
 * ClientInterface could be implemented to connect to a ServerInterface over
 * TCP/IP.
 * 
 * By forcing all communication between the UI and the simulation into the pipe
 * formed by this class, the separation between the two modules is made manifest
 * in the code, and practically speaking, the UIInterface can be implemented so
 * as to allow for a transparent TCP/IP layer, thereby allowing a client/server
 * variant of Cobweb to be developed trivially.
 * 
 * Note that UIInterface is only an Interface; all UI and simulation code should
 * be written in terms of this interface, and not a specific derivative.
 * However, the driver will need to create a subclass of UIInterface. The driver
 * should immediately forget the specific type of UIInterface after creation for
 * good form; a code snippet to do this is;
 * <code>UIInterface theUI = new SpecificUIType(); </code> Thus the
 * SpecificUIType is only used in the creation of the UIInterface, and is
 * immediately forgotten, treated simply as a UIInterface from then on.
 * 
 * @see cobweb.LocalUIInterface
 */

import java.awt.Graphics;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JTextField;

import cobweb.Environment.EnvironmentStats;
import cobweb.LocalUIInterface.TickEventListener;
import driver.SimulationConfig;

public interface UIInterface {

	public static enum MouseMode {
		Observe, AddStone, AddFood, AddAgent
	}

	/**
	 * This is the minimal interface that a client of the UIInterface must
	 * implement. This allows the UIInterface to notify the client of new frame
	 * data.
	 */
	public static interface UIClient {

		public boolean isReadyToRefresh();

		/**
		 * Notification that the UIInterface has new frame data.
		 */
		public void refresh(boolean wait);

		public void setCurrentFile(String input);

		/**
		 * Makes the user interface show which simulation configuration file 
		 * is currently being used.
		 * 
		 * @param conf Contains all parameters used for the simulation.
		 */
		public void fileOpened(SimulationConfig conf);

		public void setSimulation(UIInterface simulation);

		public UIInterface getUIPipe();
	}

	/**
	 * Add agent at location (x, y) of type, type.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param type Agent type.
	 */
	public void addAgent(int x, int y, int type);

	/**
	 * Add food at location (x, y) of type, type.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param type Agent type.
	 */
	public void addFood(int x, int y, int type);

	/**
	 * Add a stone at location (x, y)
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public void addStone(int x, int y);

	public void AddTickEventListener(TickEventListener listener);

	public void clearAgents();

	public void clearFood();

	public void clearStones();

	public void clearWaste();

	/* returns the number of agents */
	public int countAgentTypes();

	/**
	 * Display the most recent frame data.
	 * 
	 * @param g Graphics to render to
	 * @param tileWidth width, in pixels, of a single tile
	 * @param tileHeight height, in pixels, of a single tile
	 */
	public void draw(Graphics g, int tileWidth, int tileHeight);

	public int getCurrentPopulationNum();

	/* get current time */
	public long getCurrentTime();

	/**
	 * Get the height, in tiles, of the simulation environment.
	 */
	public int getHeight();

	public JButton getPauseButton();

	/**
	 * Returns the tick count being displayed.
	 */
	public long getStopTime();

	/*
	 * get the text field that holds the tick value
	 */
	public JTextField getTimeStopField();

	/**
	 * Get the width, in tiles, of the simulation environment.
	 */
	public int getWidth();

	/**
	 * This is currently being overwritten by the LocalUIInterface.
	 * 
	 * @see LocalUIInterface#insertPopulation(String, boolean)
	 */
	public void insertPopulation(String popName, boolean replace);

	public boolean isRunnable();

	/**
	 * Query the pause state of the simulation.
	 */
	public boolean isRunning();

	/**
	 * Dispose of the simulation. Call this before disposing of the UI.
	 */
	public void killScheduler();

	public void load(SimulationConfig p);

	/**
	 * Request that the log information from the simulation be stored in the
	 * file specified.
	 */
	public void log(String filePath) throws java.io.IOException;

	/*
	 * for interactive component selection : (int x, int y) location of the
	 * component int mode : 1 - stones 2-food 3-agents int type : applies to
	 * type of food or agents, selected by the user.
	 */
	public void observe(int x, int y);

	/**
	 * Pause the simulation.
	 */
	public void pause();

	/**
	 * Inform the UI that new frame data is available. Calls getDrawInfo on the
	 * environment to refresh the frame info for the UI.
	 * 
	 * @param wait Wait for refresh?
	 */
	public void refresh(boolean wait);

	public void removeAgent(int x, int y);

	public void removeFood(int x, int y);

	public void removeStone(int x, int y);

	public void RemoveTickEventListener(TickEventListener listener);

	public void report(String filePath) throws IOException;

	public void reset();

	/**
	 * Resume the simulation from a paused state.
	 */
	public void resume();

	/**
	 * Request that the state of the simulation be saved. Dumps the state of the
	 * simulation into the file specified. The timing of this is a little
	 * tricky; the call blocks until such time as the simulation can safely be
	 * saved.
	 */
	public void save(String filePath) throws IOException;

	public boolean saveCurrentPopulation(String popName, String option, int amount);

	/**
	 * Set the number of frames between frame update notifications. The frame
	 * skip is the number of frames "dropped" by the UIInterface between refresh
	 * calls on the client. Setting this to 0 disables the frameskip feature. A
	 * high value is useful in a simulation with little activity in a single
	 * frame, or to speed up a simulation that is stalled on graphics
	 * performance.
	 * 
	 * @param frameSkip the number of frames to drop between refresh calls
	 */
	public void setFrameSkip(long frameSkip);

	public void setPauseButton(JButton pb);

	public void setRunnable(boolean ready);

	/*
	 * set the tick number where the application will pause. tickfield obtained
	 * from the user. See CobwebApplication
	 */
	public void setTimeStopField(JTextField tickField);

	public void slowDown(long time);

	/**
	 * Begin the simulation. Call this once all the setup tasks are completed in
	 * the driver, and the simulation should be started. Note that the
	 * simulation begins in a paused state; the start call simply requests the
	 * frame information associated with the initial state.
	 */
	public void start();

	public void unObserve();

	/**
	 * Called by the Scheduler when it is appropriate to update the log
	 */
	public void writeLogEntry();

	public void writeOutput(String s);

	public EnvironmentStats getStatistics();

	public long getTime();

	public boolean hasAgent(int x, int y);

	public abstract Agent getAgent(int x, int y);

	public abstract boolean hasFood(int x, int y);

	public abstract int getFood(int x, int y);

	public abstract boolean hasStone(int x, int y);

	public Collection<ViewerPlugin> getViewers();
}
