package cobweb;

/**
 * The UIInterface is the "pipe" between the user interface and the simulation classes; it is also the top-level
 * interface to the simulation.
 *
 * The Driver (main in an application, or the applet) should create a derivative of UIInterface to initialize the system
 * as appropriate for the context; a LocalUIInterface exists for a single application simulation, and a ClientInterface
 * could be implemented to connect to a ServerInterface over TCP/IP.
 *
 * By forcing all communication between the UI and the simulation into the pipe formed by this class, the seperation
 * between the two modules is made manifest in the code, and practically speaking, the UIInterface can be implemented so
 * as to allow for a transparent TCP/IP layer, thereby allowing a client/server variant of Cobweb to be developed
 * trivially.
 *
 * Note that UIInterface is only an Interface; all UI and simulation code should be written in terms of this interface,
 * and not a specific derivative. However, the driver will need to create a subclass of UIInterface. The driver should
 * immediately forget the specific type of UIInterface after creation for good form; a code snippit to do this is;
 * UIInterface theUI = new SpecificUIType(param, param, param); Thus the SpecificUIType is only used in the creation of
 * the UIInterface, and is immediately forgotten, treated simply as a UIInterface from then on.
 *
 * @see cobweb.LocalUIInterface
 */

import java.util.List;

import javax.swing.JTextField;

import cobweb.Environment.Location;
import cobweb.LocalUIInterface.TickEventListener;
import driver.Parser;

public interface UIInterface {
	/**
	 * This is the minimal interface that a client of the UIInterface must implement. This allows the UIInterface to
	 * notify the client of new frame data.
	 */
	public static interface UIClient {
		/**
		 * Notification that the UIInterface has new frame data.
		 *
		 * @param theInterface the UIInterface with new frame data
		 */
		public void refresh(UIInterface theInterface, boolean wait);
	}

	public void AddTickEventListener(TickEventListener listener);

	/* returns the number of agents */
	public int countAgentTypes();

	/**
	 * Display the most recent frame data.
	 *
	 * @param g Graphics to render to
	 * @param tileWidth width, in pixels, of a single tile
	 * @param tileHeight height, in pixels, of a single tile
	 */
	public void draw(java.awt.Graphics g, int tileWidth, int tileHeight);

	/* get current time */
	public long getCurrentTime();

	/**
	 * Get the height, in tiles, of the simulation environment.
	 */
	public int getHeight();

	public driver.PauseButton getPauseButton();

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
	 * Query the pause state of the simulation.
	 */
	public boolean isPaused();

	public boolean isRunnable();

	/**
	 * Dispose of the simulation. Call this before disposing of the UI.
	 */
	public void killScheduler();

	public void load(UIInterface.UIClient client, Parser p);

	/**
	 * Request that the log information from the simulation be stored in the file specified.
	 */
	public void log(String filePath) throws java.io.IOException;

	public void newAgent(java.awt.Color agentColor, java.awt.Color typeColor, java.awt.Color strategyColor,
			Point2D position, Point2D facing);

	/**
	 * Inform the UI of the visual state of an agent.
	 *
	 * @param agentColor the color of the agent.
	 * @param position the tile position of the agent.
	 * @param facing a direction vector for the facing direction of the agent. A facing of (0,0) means the agent has no
	 *            facing direction.
	 */
	public void newAgent(java.awt.Color agentColor, java.awt.Color strategyColor, Point2D position,
			Point2D facing);

	public void newPath(List<Location> path);

	/**
	 * Inform the UI of a new tile color array.
	 *
	 * @param tileColors a width * height array of tile colors.
	 */
	public void newTileColors(int width, int height, java.awt.Color[] tileColors);

	/**
	 * Pause the simulation.
	 */
	public void pause();

	/**
	 * Inform the UI that new frame data is available. Calls getDrawInfo on the environment to refresh the frame info
	 * for the UI.
	 *
	 * @param wait Wait for refresh?
	 */
	public void refresh(boolean wait);

	/*
	 * removeComponents: mode 0 : remove all mode -1: remove stones mode -2: remove food mode -3: remove agents
	 */
	public void removeComponents(int mode);

	public void RemoveTickEventListener(TickEventListener listener);

	public void report(String filePath) throws java.io.IOException;

	public void reset();

	/**
	 * Resume the simulation from a paused state.
	 */
	public void resume();

	/**
	 * Request that the state of the simulation be saved. Dumps the state of the simulation into the file specified. The
	 * timing of this is a little tricky; the call blocks until such time as the simulation can safely be saved.
	 */
	public void save(String filePath) throws java.io.IOException;

	/**
	 * Set the number of frames between frame update notifications. The frame skip is the number of frames "dropped" by
	 * the UIInterface between refresh calls on the client. Setting this to 0 disables the frameskip feature. A high
	 * value is useful in a simulation with little activity in a single frame, or to speed up a simulation that is
	 * stalled on graphics performance.
	 *
	 * @param frameSkip the number of frames to drop between refresh calls
	 */
	public void setFrameSkip(long frameSkip);

	public void setPauseButton(driver.PauseButton pb);

	public void setRunnable(boolean ready);

	/*
	 * set the tick number where the application will pause. tickfield obtained from the user. See CobwebApplication
	 */
	public void setTimeStopField(JTextField tickField);

	public void slowDown(long time);

	/**
	 * Begin the simulation. Call this once all the setup tasks are completed in the driver, and the simulation should
	 * be started. Note that the simulation begins in a paused state; the start call simply requests the frame
	 * information associated with the initial state.
	 */
	public void start();

	/**
	 * Request that the trackAgent information from the simulation be stored in the file specified.
	 */
	public void trackAgent(String filePath) throws java.io.IOException;

	/*
	 * for interactive component selection : (int x, int y) location of the component int mode : 1 - stones 2-food
	 * 3-agents int type : applies to type of food or agents, selected by the user.
	 */
	public int updateclick(int x, int y, int mode, int type);

	/**
	 * Called by the Scheduler when it is appropriate to update the log
	 */
	public void writeLogEntry();

	public void writeOutput(String s);
}
