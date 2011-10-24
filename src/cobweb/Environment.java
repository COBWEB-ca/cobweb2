package cobweb;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cwcore.ComplexAgent;
import cwcore.ComplexEnvironment;
import cwcore.FoodSource;
import driver.SimulationConfig;

/**
 * The Environment class represents the simulation world; a collection of
 * locations with state, each of which may contain an agent.
 * 
 * The Environment class is designed to handle an arbitrary number of
 * dimensions, although the UIInterface is somewhat tied to two dimensions for
 * display purposes.
 * 
 * All access to the internal data of the Environment is done through an
 * accessor class, Environment.Location. The practical upshot of this is that
 * the Environment internals may be implemented in C or C++ using JNI, while the
 * Java code still has a nice java flavoured interface to the data.
 * 
 * Another advantage of the accessor model is that the internal data need not be
 * in a format that is reasonable for external access. An array of longs where
 * bitfields represent the location states makes sense in this context, because
 * the accessors allow friendly access to this state information.
 * 
 * Furthermore, the accessor is designed to be quite general; there should be no
 * need to subclass Environment.Location for a specific Environment
 * implementation. A number of constants should be defined in an Environment
 * implementation to allow agents to interpret the state information of a
 * location, so agents will need to be somewhat aware of the specific
 * environment they are operating in, but all access should be through this
 * interface, using implementation specific access constants.
 */
public abstract class Environment {

	public abstract void addWater(int x, int y);

	public static class EnvironmentStats {

		public long[] agentCounts;
		public long[] foodCounts;
		public long timestep;

	}

	/**
	 * All locations within simulation.
	 */
	private Location[][] locationCache;

	/** Axis constants, to make dimensionality make sense */
	public static final int AXIS_X = 0;

	public static final int AXIS_Y = 1;

	public static final int AXIS_Z = 2;

	// Some predefined directions for 2D
	public static final Direction DIRECTION_NORTH = new Direction(new int[] { 0, -1 });

	public static final Direction DIRECTION_SOUTH = new Direction(new int[] { 0, +1 });

	public static final Direction DIRECTION_WEST = new Direction(new int[] { -1, 0 });

	public static final Direction DIRECTION_EAST = new Direction(new int[] { +1, 0 });

	public static final Direction DIRECTION_NORTHEAST = new Direction(new int[] { +1, -1 });

	public static final Direction DIRECTION_SOUTHEAST = new Direction(new int[] { +1, +1 });

	public static final Direction DIRECTION_NORTHWEST = new Direction(new int[] { -1, -1 });

	public static final Direction DIRECTION_SOUTHWEST = new Direction(new int[] { -1, +1 });

	protected Scheduler theScheduler;

	/**
	 * The implementation uses a hash table to store agents, as we assume there
	 * are many more locations than agents.
	 */
	protected java.util.Hashtable<Location, Agent> agentTable = new Hashtable<Location, Agent>();
	protected java.util.Hashtable<Location, Agent> samplePop = new Hashtable<Location, Agent>();

	/**
	 * The implementation uses a hash table to store food sources, as we assume there
	 * are many more locations than food sources.
	 */
	protected java.util.Hashtable<Location, FoodSource> foodSourceTable = new Hashtable<Location, FoodSource>();
	private static DrawingHandler myUI;

	public static DrawingHandler getUIPipe() {
		return myUI;
	}

	public static void setUIPipe(DrawingHandler ui) {
		myUI = ui;
	}

	private Color[] tileColors;

	TempLocationFactory factory;

	/**
	 * Adds agent at given position
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param type agent type
	 */
	public void addAgent(int x, int y, int type) {
		// Nothing
	}

	/**
	 * Adds food at given position
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param type agent type
	 */
	public void addFoodSource(int x, int y, int type) {
		// Nothing
	}

	/**
	 * Adds stone at given position
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void addStone(int x, int y) {
		// Nothing
	}

	/** Returns an Enumeration of Agents */
	public java.util.Enumeration<Agent> agents() {
		return agentTable.elements();
	}

	public void clearAgents() {
		for (Agent a : new LinkedList<Agent>(agentTable.values())) {
			a.die();
		}
		agentTable.clear();
	}

	public void clearFoodSources() {
		for (FoodSource f : new LinkedList<FoodSource>(foodSourceTable.values())) {
			f.getLocation().removeFoodSource();
		}
		foodSourceTable.clear();
	}

	public void clearStones() {
		// Nothing
	}

	public void clearWaste() {
		// Nothing
	}

	public int countAgents() {
		return agentTable.size();
	}

	/**
	 * Called from getDrawInfo to allow implementations to fill the array of
	 * tile colors.
	 */
	protected abstract void fillTileColors(java.awt.Color[] tiles);

	Agent getAgent(Location l) {
		return agentTable.get(l);
	}

	FoodSource getFoodSource(Location l) {
		return foodSourceTable.get(l);
	}

	public java.util.Collection<Agent> getAgentCollection() {
		return agentTable.values();
	}

	public java.util.Enumeration<Agent> getAgents() {
		return agentTable.elements();
	}

	/** @return the dimensionality of this Environment. */
	public abstract int getAxisCount();

	/** @return true if the axis specified wraps. */
	public abstract boolean getAxisWrap(int axis);

	public int getCurrentPopulation() {
		return agentTable.keySet().size();

	}

	/** Called by the UIInterface to get the frame data for the Environment. */
	protected void getDrawInfo(DrawingHandler theUI) {
		fillTileColors(tileColors);
		theUI.newTileColors(getSize(AXIS_X), getSize(AXIS_Y), tileColors);

	}

	/**
	 * Core implementation of getField; this is what could be accelerated in C++
	 */
	protected abstract int getField(Location l, int field);

	// Syntactic sugar for common cases

	/**
	 * Scale down the coordinate until it makes sense to put it on the grid.
	 * @param coord The value of the coordinate.
	 * @param axis The axis along which the coordinate is set.
	 * @return The scaled down coordinate.
	 * TODO wrap properly
	 */
	private int scaleCoord(int coord, int axis) {
		int max = this.getSize(axis);

		while(coord < 0) {
			coord += max;
		}

		while(coord >= max) {
			coord -= max;
		}

		return coord;
	}

	/**
	 * Return the location if it is valid.
	 * Create the location if it is valid but is not yet created.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return The location.
	 */
	public Location getLocation(int x, int y) {
		x = this.scaleCoord(x, AXIS_X);
		y = this.scaleCoord(y, AXIS_Y);

		if (locationCache[x][y] == null)
			locationCache[x][y] = new Location(this, new int[] { x, y });
		return locationCache[x][y];

	}

	void removeFoodSource(Location l) {
		l.setFlag(ComplexEnvironment.FLAG_FOOD, false);
		foodSourceTable.remove(l);
	}

	public int getPopByPercentage(double amount) {

		return (int) (getCurrentPopulation() * (amount / 100));

	}

	/**
	 * @return Random location.
	 */
	public Location getRandomLocation() {
		Location l;
		do {
			l = getLocation(cobweb.globals.random.nextInt(getSize(AXIS_X)),
					cobweb.globals.random.nextInt(getSize(AXIS_Y)));
		} while (!l.isValid());
		return l;
	}

	/** @return the Scheduler responsible for this Environment. */
	public Scheduler getScheduler() {
		return theScheduler;
	}

	/** @return the number of unique locations along a specific axis. */
	public abstract int getSize(int axis);

	public abstract EnvironmentStats getStatistics();

	public abstract int getTypeCount();

	public Location getUserDefinedLocation(int x, int y) {
		Location l;
		l = getLocation(x, y);
		if (!l.isValid())
			throw new IllegalArgumentException("Location not inside environment");

		return l;
	}

	/**
	 * This is currently being overwritten by the ComplexEnvironment class.
	 * 
	 * @see cwcore.ComplexEnvironment#insertPopulation(String, String)
	 */
	public abstract boolean insertPopulation(String fileName, String option);

	/**
	 * Load environment from parameters
	 * 
	 * <p>
	 * This is currently being overwritten by the ComplexEnvironment class.
	 * 
	 * @param scheduler the Scheduler to use
	 * @param parameters the parameters
	 * @see cwcore.ComplexEnvironment#load(Scheduler, SimulationConfig)
	 **/
	public void load(Scheduler scheduler, SimulationConfig parameters) throws IllegalArgumentException {
		tileColors = new Color[parameters.getEnvParams().getWidth() * parameters.getEnvParams().getHeight()];

		factory = new TempLocationFactory(parameters.getEnvParams().getWidth(), parameters.getEnvParams().getHeight());
	}

	/** Log to a stream */
	public abstract void log(java.io.Writer w);

	public abstract void observe(int x, int y);

	/**
	 * Removes agent at given position
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void removeAgent(int x, int y) {
		// Nothing
	}

	/**
	 * Removes food at given position
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void removeFoodSource(int x, int y) {
		Location l = getUserDefinedLocation(x, y);
		l.removeFoodSource();
	}

	/**
	 * Removes stone at given position
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void removeStone(int x, int y) {
		// Nothing
	}

	/** Report to a stream */
	public abstract void report(java.io.Writer w);

	/** Save to a stream */
	public abstract void save(java.io.Writer w);

	/** Save a sample population as an XML file */
	public boolean savePopulation(String popName, String option, int amount) {

		int totalPop;

		if (option.equals("percentage")) {
			totalPop = getPopByPercentage(amount);
		} else {
			int currPop = getCurrentPopulation();
			if (amount > currPop) {

				totalPop = currPop;
			} else {

				totalPop = amount;
			}
		}

		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Node root = d.createElement("Agents");

		int currentPopCount = 1;

		for (Location l : agentTable.keySet()) {
			if (currentPopCount > totalPop)
				break;
			Node node = ((ComplexAgent) agentTable.get(l)).makeNode(d);

			Element locationElement = d.createElement("location");

			l.saveAsANode(locationElement, d);
			node.appendChild(locationElement);

			root.appendChild(node);
			currentPopCount++;

		}

		d.appendChild(root);
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(popName);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("Couldn't open output file", ex);
		}

		Source s = new DOMSource(d);

		Transformer t;
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			t = tf.newTransformer();

		} catch (TransformerConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setParameter(OutputKeys.STANDALONE, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		Result r = new StreamResult(stream);
		try {
			t.transform(s, r);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
		return true;
	}

	final void setAgent(Location l, Agent a) {
		if (a != null)
			agentTable.put(l, a);
		else
			agentTable.remove(l);
	}

	/**
	 * Core implementation of setField; this is what could be accelerated in C++
	 */
	protected abstract void setField(Location l, int field, int value);

	protected void setupLocationCache() {
		locationCache = new Location[getSize(AXIS_X)][getSize(AXIS_Y)];
	}

	public abstract void unObserve();

	/** Update the log; called from the UIInterface */
	protected abstract void writeLogEntry();

	public abstract boolean hasAgent(int x, int y);

	public abstract Agent getAgent(int x, int y);

	public abstract boolean hasFood(int x, int y);

	public abstract int getFood(int x, int y);

	public abstract boolean hasStone(int x, int y);


	/**
	 * Get a random location with no objects on it.
	 * @return A location.
	 */
	public final Location getRandomFreeLocation() {
		Location l;

		do {
			l = this.getRandomLocation();
		} while (!l.isEmpty());

		return l;
	}
}
