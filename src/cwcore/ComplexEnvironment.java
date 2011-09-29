package cwcore;

import ga.GATracker;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import production.Product;
import production.ProductionMapper;
import cobweb.Agent;
import cobweb.ArrayEnvironment;
import cobweb.ColorLookup;
import cobweb.Direction;
import cobweb.DrawingHandler;
import cobweb.Environment;
import cobweb.Point2D;
import cobweb.RandomNoGenerator;
import cobweb.Scheduler;
import cobweb.TickScheduler;
import cobweb.TypeColorEnumeration;
import cwcore.broadcast.PacketConduit;
import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ComplexEnvironmentParams;
import cwcore.complexParams.ComplexFoodParams;
import cwcore.complexParams.ProductionParams;
import driver.ControllerFactory;
import driver.SimulationConfig;

/**
 * This class contains an implementation of the TickScheduler.Client class.
 */
public class ComplexEnvironment extends Environment implements TickScheduler.Client {

	/**
	 * Contains methods
	 *  
	 */
	public abstract static class Drop {
		//Gold colored drops
		final static Color DROP_COLOR = new Color(238, 201, 0);

		public Drop() {

		}

		public abstract boolean isActive(long val);

		public abstract void reset(long time, int weight, float rate);

		public Color getColor() {
			return DROP_COLOR;
		}

		public boolean canStep() {
			return true;
		}
	}

	public static class Waste extends Drop {

		private int initialWeight;

		private long birthTick;

		private float rate;

		private double threshold;

		private boolean valid;

		public Waste() {
			this(0, 0, 0);
		}

		public Waste(long birthTick, int weight, float rate) {
			super();
			initialWeight = weight;
			this.birthTick = birthTick;
			this.rate = rate;
			// to avoid recalculating every tick
			threshold = 0.001 * initialWeight; // changed from 0.5 to 0.001 by
			// skinawy
			this.valid = true;
		}

		/* Call me sparsely, especially when the age is really old */
		public double getAmount(long tick) {
			return initialWeight * Math.pow(Math.E, -rate * (tick - birthTick));
		}

		@Override
		public boolean isActive(long tick) {
			if (!valid)
				return false;
			if (getAmount(tick) < threshold) {
				valid = false;
				return false;
			}
			return true;
		}

		@Override
		public void reset(long newBirthTick, int weight, float newRate) {
			initialWeight = weight;
			this.birthTick = newBirthTick;
			this.rate = newRate;
			// to avoid recalculating every tick
			threshold = 0.001 * initialWeight; // changed from 0.5 to 0.001 by
			// skinawy
			valid = true;
		}

		@Override
		public Color getColor() {
			return wasteColor;
		}

		@Override
		public boolean canStep() {
			return false;
		}
	}

	private static final int DROP_ATTEMPTS_MAX = 5;

	public static final int FLAG_STONE = 1;

	public static final int FLAG_FOOD = 2;

	public static final int FLAG_AGENT = 3;

	public static final int FLAG_DROP = 4;

	public static final int INIT_LAST_MOVE = 0; // Initial last move set to
	// cooperate

	public int PD_PAYOFF_REWARD;

	public int PD_PAYOFF_SUCKER;

	public int PD_PAYOFF_TEMPTATION;

	public int PD_PAYOFF_PUNISHMENT;

	/*
	 * All variables that will by referenced by the parser must be declared as
	 * 1-sized arrays as all of the following are.
	 */

	// Info for logfile
	private long tickCount = 0;

	private static RandomNoGenerator environmentRandom;

	private ComplexFoodParams foodData[];

	protected ComplexAgentParams agentData[];

	protected ProductionParams prodData[];

	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();

	private static java.awt.Color wasteColor = new java.awt.Color(204, 102, 0);

	// Bitmasks for boolean states
	private static final int MASK_TYPE = 15;

	private static final int STONE_CODE = 1;

	private static final int FOOD_CODE = 2;

	private static final int WASTE_CODE = 4;

	public void setDrop(Location loc, Drop d) {
		dropArray[loc.v[0]][loc.v[1]] = d;
	}

	public void addProduct(Location loc, Product prod) {
		prodMapper.addProduct(prod, loc);
		setDrop(loc, prod);
	}


	// Returns current location's food type
	public int getFoodType(cobweb.Environment.Location l) {
		return foodarray[l.v[0]][l.v[1]];
	}

	GATracker gaTracker;

	private java.io.PrintWriter logStream;

	private Vector<ComplexAgentInfo> agentInfoVector = new Vector<ComplexAgentInfo>();

	private cobweb.ArrayEnvironment array;

	private ComplexEnvironmentParams data = new ComplexEnvironmentParams();

	// Food array contains type and their locations
	private static int[][] foodarray = new int[0][];

	/*
	 * Waste tile array to store the data per waste tile. Needed to allow
	 * depletion of waste
	 */
	public Drop[][] dropArray;	

	private Agent observedAgent = null;

	private int draughtdays[];

	int[][] backFoodArray;

	ArrayEnvironment backArray;

	int mostFood[];

	public PacketConduit commManager;

	public ComplexEnvironment() {
		super();
		commManager = new PacketConduit();
	}

	public ProductionMapper prodMapper;

	@Override
	public synchronized void addAgent(int x, int y, int type) {
		super.addAgent(x, y, type);
		int action = -1; // $$$$$ -1: not play Prisoner's Dilemma
		cobweb.Environment.Location l;
		l = getUserDefinedLocation(x, y);
		if ((l.getAgent() == null) && !l.testFlag(ComplexEnvironment.FLAG_STONE)
				&& !l.testFlag(ComplexEnvironment.FLAG_DROP)) {
			int agentType = type;
			if (data.prisDilemma) {

				action = 0;

				double coopProb = agentData[agentType].pdCoopProb / 100.0d; // static

				float rnd = cobweb.globals.random.nextFloat();

				if (rnd > coopProb)
					action = 1; // agent defects depending on probability
			}

			spawnAgent(action, l, agentType);

		}
	}

	protected void spawnAgent(int action, cobweb.Environment.Location location, int agentType) {
		ComplexAgent child = (ComplexAgent)AgentSpawner.spawn();
		child.init(agentType, location, action, (ComplexAgentParams) agentData[agentType].clone(),
				(ProductionParams) prodData[agentType].clone()); // Default
	}

	ComplexAgentInfo addAgentInfo(ComplexAgentInfo info) {
		agentInfoVector.add(info);
		return info;
	}

	ComplexAgentInfo addAgentInfo(int agentT, ComplexAgentInfo p1, ComplexAgentInfo p2, int action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT, tickCount, p1, p2, action));
	}

	ComplexAgentInfo addAgentInfo(int agentT, ComplexAgentInfo p1, int action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT, tickCount, p1, action));
	}

	ComplexAgentInfo addAgentInfo(int agentT, int action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT, tickCount, action));
	}

	@Override
	public synchronized void addFood(int x, int y, int type) {

		cobweb.Environment.Location l;
		l = getUserDefinedLocation(x, y);
		if (l.testFlag(ComplexEnvironment.FLAG_STONE)) {
			throw new IllegalArgumentException("stone here already");
		}
		l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
		setFoodType(l, type);

		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X) * getSize(AXIS_Y)];
		fillTileColors(tileColors);
	}

	@Override
	public synchronized void addStone(int x, int y) {
		cobweb.Environment.Location l;
		l = getUserDefinedLocation(x, y);
		if (l.getAgent() != null) {
			return;
		}

		if (l.testFlag(FLAG_FOOD))
			l.setFlag(FLAG_FOOD, false);
		if (l.testFlag(FLAG_DROP))
			l.setFlag(FLAG_DROP, false);

		l.setFlag(ComplexEnvironment.FLAG_STONE, true);

		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X) * getSize(AXIS_Y)];
		fillTileColors(tileColors);
	}

	@Override
	public synchronized void clearAgents() {
		super.clearAgents();
	}

	private void clearFlag(int flag) {
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);

				if (currentPos.testFlag(flag)) {
					currentPos.setFlag(flag, false);
				}
			}
		}
	}

	/*
	 * Remove components from the environment mode 0 : remove all the components
	 * mode -1: remove stones mode -2: remove food mode -3: remove agents mode
	 * -4: remove waste
	 */
	@Override
	public synchronized void clearFood() {
		super.clearFood();
		clearFlag(FLAG_FOOD);
	}

	@Override
	public synchronized void clearStones() {
		super.clearStones();
		clearFlag(FLAG_STONE);
	}

	@Override
	public synchronized void clearWaste() {
		super.clearWaste();
		clearFlag(FLAG_DROP);
	}

	/**
	 * Stores the following types of parameters into ComplexEnvironment 
	 * member variables as well as Prisoner's Dilemma payoffs:
	 * 
	 * <p>ComplexEnvironmentParams
	 * <br>ComplexFoodParams
	 * <br>ComplexAgentParams
	 *  
	 * @param p The current simulation configuration file.
	 */
	protected void copyParamsFromParser(SimulationConfig p) {
		data = p.getEnvParams();

		foodData = p.getFoodParams();

		agentData = p.getAgentParams();

		prodData = p.getProdParams();

		PD_PAYOFF_REWARD = data.pdParams.reward;
		PD_PAYOFF_TEMPTATION = data.pdParams.temptation;
		PD_PAYOFF_SUCKER = data.pdParams.sucker;
		PD_PAYOFF_PUNISHMENT = data.pdParams.punishment;
	}

	/* Return totalEnergy of all agents */
	private long countAgentEnergy() {
		long totalEnergy = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	/* Return totalEnergy (long) for a certain agentType (int) */
	private long countAgentEnergy(int agentType) {
		long totalEnergy = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentType() == agentType)
				totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	/* Return the number of agents (int) for a certain agentType (int) */
	private int countAgents(int agentType) {
		int agentCount = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentType() == agentType)
				agentCount++;
		}
		return agentCount;
	}

	/* Return foodCount (long) of all types of food */
	private long countFoodTiles() {
		long foodCount = 0;

		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD))
					++foodCount;
			}
		}
		return foodCount;
	}

	/* Return foodCount (long) for a specific foodType (int) */
	private int countFoodTiles(int foodType) {
		int foodCount = 0;
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD))
					if (getFoodType(currentPos) == foodType)
						++foodCount;
			}
		}
		return foodCount;
	}

	private void depleteFood(int type) {
		// the algorithm for randomly selecting the food cells to delete
		// is as follows:
		// We iterate through all of the cells and the location of each
		// one containing food type i is added to
		// a random position in our vector. We then calculate exactly
		// how many food items we need to destroy, say N,
		// and we destroy the food at the positions occupying the last N
		// spots in our vector
		LinkedList<Location> locations = new LinkedList<Location>();
		for (int x = 0; x < getSize(AXIS_X); ++x)
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD) && getFoodType(currentPos) == type)
					locations.add(environmentRandom.nextInt(locations.size() + 1), currentPos);
			}

		int foodToDeplete = (int) (locations.size() * foodData[type].depleteRate);

		for (int j = 0; j < foodToDeplete; ++j) {
			Location loc = locations.removeLast();

			loc.setFlag(FLAG_FOOD, false);
		}
		draughtdays[type] = foodData[type].draughtPeriod;
	}

	private void dropFood(int type) {
		float foodDrop = foodData[type].dropRate;
		while (environmentRandom.nextFloat() < foodDrop) {
			--foodDrop;
			cobweb.Environment.Location l;
			int j = 0;
			do {
				++j;
				l = getRandomLocation();

			} while (j < DROP_ATTEMPTS_MAX
					&& (l.testFlag(ComplexEnvironment.FLAG_STONE) || l.testFlag(ComplexEnvironment.FLAG_FOOD)
							|| l.testFlag(ComplexEnvironment.FLAG_DROP) || l.getAgent() != null));

			if (j < DROP_ATTEMPTS_MAX) {
				l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
				setFoodType(l, type);
			}
		}
	}

	@Override
	public void fillTileColors(java.awt.Color[] tileColors) {
		int tileIndex = 0;
		for (int y = 0; y < getSize(AXIS_Y); ++y) {
			for (int x = 0; x < getSize(AXIS_X); ++x) {
				Location currentPos = getLocation(x, y);

				if (currentPos.testFlag(FLAG_STONE))
					tileColors[tileIndex++] = java.awt.Color.darkGray;

				else if (currentPos.testFlag(FLAG_FOOD))
					tileColors[tileIndex++] = colorMap.getColor(getFoodType(currentPos), 0 /* agentTypeCount */);

				else
					tileColors[tileIndex++] = java.awt.Color.white;
			}
		}
	}

	@Override
	protected void finalize() {
		if (logStream != null)
			logStream.close();
	}

	public int getAgentTypes() {
		return data.getAgentTypes();
	}

	@Override
	public int getAxisCount() {
		return 2;
	}

	@Override
	public boolean getAxisWrap(int axis) {
		return data.wrapMap;
	}

	@Override
	protected synchronized void getDrawInfo(DrawingHandler theUI) {
		super.getDrawInfo(theUI);
		for (Agent a : agentTable.values()) {
			a.getDrawInfo(theUI);
		}

		if (observedAgent != null) {
			List<Location> path = ((ComplexAgent) observedAgent).getInfo().getPathHistory();
			if (path != null)
				theUI.newPath(path);
		}

		getDropInfo(theUI);

	}

	// Hidden implementation stuff...

	private void getDropInfo(DrawingHandler theUI) {
		for (int y = 0; y < getSize(AXIS_Y); ++y) {
			for (int x = 0; x < getSize(AXIS_X); ++x) {
				Location currentPos = getLocation(x, y);
				if (currentPos.testFlag(FLAG_DROP)){
					Color c = dropArray[x][y].getColor();
					theUI.newDrop(new Point2D(x, y), c);
				}
			}
		}
	}

	// Ignored; this model has no fields
	@Override
	protected int getField(cobweb.Environment.Location l, int field) {
		return 0;
	}

	public int getInfoNum() {
		return agentInfoVector.size();
	}

	protected int getLocationBits(cobweb.Environment.Location l) {
		return array.getLocationBits(l);
	}

	@Override
	public int getSize(int axis) {
		return array.getSize(axis);
	}

	@Override
	public EnvironmentStats getStatistics() {
		EnvironmentStats stats = new EnvironmentStats();
		stats.agentCounts = new long[getAgentTypes()];
		stats.foodCounts = new long[getAgentTypes()];
		for (int i = 0; i < getAgentTypes(); i++) {
			stats.agentCounts[i] = countAgents(i);
			stats.foodCounts[i] = countFoodTiles(i);
		}
		stats.timestep = this.getTickCount();
		return stats;
	}

	/* Return the current tick number, tickCount (long) */
	public long getTickCount() {
		return tickCount;
	}

	public int getWidth() {
		return data.width;
	}

	public int getHeight() {
		return data.height;
	}

	@Override
	public int getTypeCount() {

		return data.getAgentTypes();
	}

	private void growFood() {

		for (int y = 0; y < getSize(AXIS_Y); ++y) {
			for (int x = 0; x < getSize(AXIS_X); ++x) {
				Location currentPos = getLocation(x, y);
				// if there's a stone or already food, we simply copy the
				// information from the old arrays to the new ones
				backArray.setLocationBits(currentPos, array.getLocationBits(currentPos));
				backFoodArray[currentPos.v[0]][currentPos.v[1]] = foodarray[currentPos.v[0]][currentPos.v[1]];
			}
		}

		// create a new ArrayEnvironment and a new food type array
		// loop through all positions
		for (int y = 0; y < getSize(AXIS_Y); ++y) {
			for (int x = 0; x < getSize(AXIS_X); ++x) {
				Location currentPos = getLocation(x, y);
				// if there's a stone or already food, we simply copy the
				// information from the old arrays to the new ones
				if ((array.getLocationBits(currentPos) & MASK_TYPE) == 0) {
					// otherwise, we want to see if we should grow food here
					// the following code block tests all adjacent squares
					// to this one and counts how many have food
					// as well how many of each food type exist

					double foodCount = 0;
					Arrays.fill(mostFood, 0);

					Location checkPos = currentPos.getAdjacent(DIRECTION_NORTH);
					if (checkPos != null && checkPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}
					checkPos = currentPos.getAdjacent(DIRECTION_SOUTH);
					if (checkPos != null && checkPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}
					checkPos = currentPos.getAdjacent(DIRECTION_EAST);
					if (checkPos != null && checkPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}
					checkPos = currentPos.getAdjacent(DIRECTION_WEST);
					if (checkPos != null && checkPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}

					// and if we have found any adjacent food, theres a
					// chance we want to grow food here
					if (foodCount > 0) {

						int max = 0;
						int growingType;

						// find the food that exists in the largest quantity
						for (int i = 1; i < mostFood.length; ++i)
							if (mostFood[i] > mostFood[max])
								max = i;

						// give the max food an extra chance to be chosen

						if (data.likeFoodProb >= cobweb.globals.random.nextFloat()) {
							growingType = max;
						} else {
							growingType = environmentRandom.nextInt(data.getFoodTypes());
						}

						// finally, we grow food according to a certain
						// amount of random chance
						if (foodCount * foodData[growingType].growRate > 100 * environmentRandom.nextFloat()) {
							backArray.setLocationBits(currentPos, FOOD_CODE);
							// setFoodType (currentPos, growMe);
							backFoodArray[currentPos.v[0]][currentPos.v[1]] = growingType;
						} else {
							backArray.setLocationBits(currentPos, 0);
							backFoodArray[currentPos.v[0]][currentPos.v[1]] = -123154534;
						}
					} else {
						backArray.setLocationBits(currentPos, 0);
						backFoodArray[currentPos.v[0]][currentPos.v[1]] = -123154534;
					}
				}
			}
		}

		// The tile array we've just computed becomes the current tile array
		ArrayEnvironment swapArray = array;
		array = backArray;
		backArray = swapArray;

		int[][] swapFoodArray = foodarray;
		foodarray = backFoodArray;
		backFoodArray = swapFoodArray;

	}

	/**
	 * 
	 * @param fileName 
	 * @param option
	 * @return 
	 */
	@Override
	public boolean insertPopulation(String fileName, String option) throws FileNotFoundException {


		if (option.equals("replace")) {
			clearAgents();
		}

		//Load XML file
		FileInputStream file = new FileInputStream(fileName);

		// DOM initialization
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		// factory.setValidating(true);

		Document document;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file);
		} catch (SAXException ex) {
			throw new IllegalArgumentException("Can't open config file", ex);
		} catch (ParserConfigurationException ex) {
			throw new IllegalArgumentException("Can't open config file", ex);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Can't open config file", ex);
		}


		NodeList agents = document.getElementsByTagName("Agent");


		for (int i = 0 ; i < agents.getLength(); i++){
			ComplexAgentParams params = new ComplexAgentParams(data);
			ProductionParams prodParams = new ProductionParams();

			Node agent = agents.item(i);
			Element element = (Element) agent;

			NodeList paramsElement = element.getElementsByTagName("params");
			Element paramNode = (Element) paramsElement.item(0);			


			//NodeList prodParamsElement = element.getElementsByTagName("prodParams");
			//Element prodParamNode = (Element) prodParamsElement.item(0);

			NodeList agentTypeElement = element.getElementsByTagName("agentType");
			NodeList pdCheaterElement = element.getElementsByTagName("doCheat");

			NodeList directionElement = element.getElementsByTagName("direction");
			Element direction = (Element) directionElement.item(0);
			NodeList coordinates = direction.getElementsByTagName("coordinate");

			NodeList locationElement = element.getElementsByTagName("location");
			Element location = (Element) locationElement.item(0);
			NodeList axisPos = location.getElementsByTagName("axisPos");

			// location
			int [] axis = new int [axisPos.getLength()];
			for (int j = 0 ; j < axisPos.getLength(); j++) {
				axis[j] = Integer.parseInt(axisPos.item(j).getChildNodes().item(0).getNodeValue());
			}

			Location loc = getLocation(axis[0], axis[1]);

			// direction
			int [] coords = new int [coordinates.getLength()];
			for (int j = 0 ; j < coordinates.getLength(); j++) {
				coords[j] = Integer.parseInt(coordinates.item(j).getChildNodes().item(0).getNodeValue());
			}
			Direction facing = new Direction(coords);

			// parameters
			params.loadConfig(paramNode);
			//prodParams.loadConfig(prodParamNode);

			// agentType
			int agentType = Integer.parseInt(agentTypeElement.item(0).getChildNodes().item(0).getNodeValue());

			// doCheat
			int pdCheater = Integer.parseInt(pdCheaterElement.item(0).getChildNodes().item(0).getNodeValue());


			ComplexAgent cAgent = (ComplexAgent)AgentSpawner.spawn();
			cAgent.init(agentType, pdCheater, params, prodParams, facing, loc);
			agentTable.put(loc, cAgent);
		}


		return true;
	}

	/**
	 * Removes all old agents.
	 */
	private void killOldAgents() {
		// if keepOldAgents is false then we want to kill all of the agents
		for (Agent a : new LinkedList<Agent>(getAgentCollection())) {
			if (a.isAlive()) {
				a.die();
			}
		}
		// This second line may seem redundant, but it fact its not.
		// By reseting the hashtable, we remove a bit of non-determinance
		// due to the way
		// hashtable is implemented. If you remove this line, the coloring
		// will not be entirely consistant
		// between simulation runs based on the same seed and parameters (a
		// bad thing)
		clearAgents();
	}

	/**
	 * Loads a new complex environment using the data held within the simulation 
	 * configuration object, p.  The following actions are performed during a load:
	 * 
	 * <p>1. Attaches the scheduler, s, to the environment.
	 * <br>2. Stores parameters from the file or stream in to ComplexEnvironment.
	 * <br>3. Sets up location cache, and environment food and waste arrays.
	 * <br>4. Keeps or removes old agents.
	 * <br>5. Adds new stones, food, and agents.
	 * 
	 * @param s The simulation scheduler used to control the time.
	 * @param p The simulation configuration settings
	 * @see cobweb.Environment#load(Scheduler, SimulationConfig)
	 */
	@Override
	public void load(Scheduler s, SimulationConfig p) throws IllegalArgumentException {
		super.load(s, p);
		// sFlag stores whether or not we are using a new scheduler
		boolean sFlag = (s != theScheduler);

		if (sFlag) {
			theScheduler = s;
			s.addSchedulerClient(this);
		}

		tickCount = theScheduler.getTime();

		int oldH = data.height;
		int oldW = data.width;

		/**
		 * the first parameter must be the number of agent types as we must
		 * allocate space to store the parameter for each type
		 */

		copyParamsFromParser(p);

		setDefaultMutableAgentParam();

		/**
		 * If the random seed is set to 0 in the data file, it means we use the
		 * system time instead
		 */
		// $$$$$ This means setting Random Seed to zero would get non-repeatable
		// results. Apr 19
		if (data.randomSeed == 0)
			data.randomSeed = System.currentTimeMillis();

		cobweb.globals.random = new RandomNoGenerator(data.randomSeed);
		environmentRandom = cobweb.globals.random;

		if (data.keepOldArray) {
			int[] boardIndices = { data.width, data.height };
			array = new cobweb.ArrayEnvironment(data.width, data.height, array);
			foodarray = cobweb.ArrayUtilities.resizeArray(foodarray, boardIndices);
		} else {
			array = new cobweb.ArrayEnvironment(data.width, data.height);
			foodarray = new int[data.width][data.height];
		}
		backArray = new ArrayEnvironment(data.width, data.height);
		backFoodArray = new int[data.width][data.height];
		mostFood = new int[data.getFoodTypes()];

		setupLocationCache();
		cwcore.ComplexAgentInfo.initialize(data.getAgentTypes());

		if (dropArray == null || !data.keepOldWaste) {
			loadNewWaste();
		} else {
			loadOldWaste();
		}

		loadFoodMode();

		if (data.keepOldAgents) {
			loadOldAgents(sFlag, oldH, oldW);
		} else {
			killOldAgents();
		}

		if (!data.keepOldPackets) {
			commManager.clearPackets();
		}

		// add stones in to random locations
		for (int i = 0; i < data.initialStones; ++i) {
			cobweb.Environment.Location l;
			int tries = 0;
			do {
				l = getRandomLocation();
			} while ((tries++ < 100)
					&& ((l.testFlag(ComplexEnvironment.FLAG_STONE) || l.testFlag(ComplexEnvironment.FLAG_DROP)) && l
							.getAgent() == null));
			if (tries < 100)
				l.setFlag(ComplexEnvironment.FLAG_STONE, true);
		}

		// add food to random locations
		if (data.dropNewFood) {
			loadNewFood();
		}

		try {
			ControllerFactory.Init(data.controllerName, p.getControllerParams());
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		// spawn new random agents for each type
		if (data.spawnNewAgents) {
			loadNewAgents();
		}
	}

	/**
	 * Initializes drought days for each food type to zero.  Also checks to see if 
	 * food deplete rates and times are valid for each food type.  Valid random food 
	 * deplete rates and times will be generated using the environments random number 
	 * generator for each invalid entry.
	 */
	private void loadFoodMode() {
		draughtdays = new int[data.getFoodTypes()];
		for (int i = 0; i < data.getFoodTypes(); ++i) {
			draughtdays[i] = 0;
			if (foodData[i].depleteRate < 0.0f || foodData[i].depleteRate > 1.0f)
				foodData[i].depleteRate = environmentRandom.nextFloat();
			if (foodData[i].depleteTime <= 0)
				foodData[i].depleteTime = environmentRandom.nextInt(100) + 1;
		}
	}

	/**
	 * Places agents in random locations.  If prisoner's dilemma is being used, 
	 * a number of cheaters that is dependent on the probability of being a cheater
	 * are randomly assigned to agents.
	 */
	private void loadNewAgents() {
		int doCheat = -1; // $$$$$ -1: not play Prisoner's Dilemma
		for (int i = 0; i < data.getAgentTypes(); ++i) {
			double coopProb = agentData[i].pdCoopProb / 100.0d; // static value
			// for now
			// $$$$$$ added
			// for the below
			// else
			// block.
			// Apr 18

			for (int j = 0; j < agentData[i].initialAgents; ++j) {
				if (data.prisDilemma) {
					if (agentData[i].pdTitForTat) {
						doCheat = 0; // spawn cooperating agent
					} else {
						// $$$$$$ change from the first silent line to the
						// following three. Apr 18
						// action = 1;
						doCheat = 0;
						float rnd = cobweb.globals.random.nextFloat();
						// System.out.println("rnd: " + rnd);
						// System.out.println("coopProb: " + coopProb);
						if (rnd > coopProb)
							doCheat = 1; // agent defects depending on
						// probability
					}

				}

				cobweb.Environment.Location location;
				int tries = 0;
				do {
					location = getRandomLocation();
				} while ((tries++ < 100) && ((location.getAgent() != null) // don't
						// spawn
						// on
						// top
						// of
						// agents
						|| location.testFlag(ComplexEnvironment.FLAG_STONE) // nor
						// on
						// stone
						// tiles
						|| location.testFlag(ComplexEnvironment.FLAG_DROP))); // nor
				// on
				// waste
				// tiles
				if (tries < 100) {
					int agentType = i;
					spawnAgent(doCheat, location, agentType);
				}
			}
		}
	}

	/**
	 * Randomly places food in the environment.
	 */
	private void loadNewFood() {
		for (int i = 0; i < data.getFoodTypes(); ++i) {
			for (int j = 0; j < foodData[i].initial; ++j) {
				cobweb.Environment.Location l;
				int tries = 0;
				do {
					l = getRandomLocation();
				} while ((tries++ < 100)
						&& (l.testFlag(ComplexEnvironment.FLAG_STONE) || l.testFlag(ComplexEnvironment.FLAG_DROP)));
				if (tries < 100)
					l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
				setFoodType(l, i);
			}
		}
	}

	/**
	 * Creates a new waste array and initializes each location as having 
	 * no waste.
	 */
	private void loadNewWaste() {
		dropArray = new Drop[data.width][data.height];
		prodMapper = new ProductionMapper(data.width * data.height, this);
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				getLocation(x, y).setFlag(FLAG_DROP, false);
			}
		}
	}

	/**
	 * Searches through each location to find every old agent.  Each agent that is found 
	 * is added to the scheduler if the scheduler is new.  Agents that are off the new 
	 * environment are removed from the environment.
	 * 
	 * @param sFlag True if using a new scheduler
	 * @param oldH Height of old environment
	 * @param oldW Width of old environment
	 */
	private void loadOldAgents(boolean sFlag, int oldH, int oldW) {
		// Add in-bounds old agents to the new scheduler and update new
		// constants
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.getAgent() != null) {
					// we only need to add the agent if the scheduler is
					// new, otherwise we assume it already belongs to the
					// scheduler
					if (sFlag) {
						getScheduler().addSchedulerClient(currentPos.getAgent());
					}
					int theType = ((ComplexAgent) currentPos.getAgent()).getAgentType();
					((ComplexAgent) currentPos.getAgent()).setConstants(((ComplexAgent) currentPos.getAgent())
							.getAgentPDAction(), agentData[theType], prodData[theType]); // Default
					// genetic
					// sequence of
					// agent type
				}
			}
		}

		removeOffgridAgents(oldH, oldW);
	}

	/**
	 * Removes old agents that are off the new environment.
	 * 
	 * @param oldH Old environment height
	 * @param oldW Old environment width
	 */
	private void removeOffgridAgents(int oldH, int oldW) {
		for (Agent a : new LinkedList<Agent>(getAgentCollection())) {
			Location l = a.getPosition();
			if (l.v[0] >= data.width || l.v[1] >= data.height) {
				a.die();
			}
		}

	}

	/**
	 * Resizes old waste array to create a new waste array while keeping waste data 
	 * that was stored in old waste array.
	 */
	private void loadOldWaste() {
		Drop[][] oldWasteArray = dropArray;
		dropArray = new Drop[data.width][data.height];

		int width = Math.min(data.width, oldWasteArray.length);
		int height = Math.min(data.height, oldWasteArray[0].length);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				dropArray[i][j] = oldWasteArray[i][j];

		// Add in-bounds old waste to the new scheduler and update new
		// constants
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (dropArray[currentPos.v[0]][currentPos.v[1]] != null) {
					currentPos.setFlag(ComplexEnvironment.FLAG_FOOD, false);
					currentPos.setFlag(ComplexEnvironment.FLAG_STONE, false);
					currentPos.setFlag(ComplexEnvironment.FLAG_DROP, true);
				}
			}
		}
	}

	@Override
	public void log(java.io.Writer w) {
		logStream = new java.io.PrintWriter(w, false);
		writeLogTitles();
	}

	private int[] numAgentsStrat() {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentPDAction() == 0) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentPDAction() == 1) { // $$$$$$ add
				// " if (agent.getAgentPDAction() == 1) ".
				// Apr 19
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	private int[] numAgentsStrat(int agentType) {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentType() == agentType && agent.getAgentPDAction() == 0) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentType() == agentType && agent.getAgentPDAction() == 1) {
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	/*
	 * This gets called when the user clicks on a tile without selecting outside
	 * of edit mode
	 */
	@Override
	public void observe(int x, int y) {
		cobweb.Environment.Location l = getUserDefinedLocation(x, y);
		/* A tile can only consist of: STONE or WASTE or (FOOD|AGENT) */
		observedAgent = l.getAgent();
	}

	/**
	 * Dump header for report file to path
	 * 
	 * @param pw writer to print into
	 */
	public void printAgentHeader(java.io.PrintWriter pw) {
		StringBuffer buffer = new StringBuffer();

		String agentInfoHeader = "Agent Number";
		agentInfoHeader += "\tAgent Type";
		agentInfoHeader += "\tBirth Tick";
		agentInfoHeader += "\tBirth Type";
		agentInfoHeader += "\tDeath Tick";
		agentInfoHeader += "\tGenetic Code";
		agentInfoHeader += "\tDirect Descendants";
		agentInfoHeader += "\tTotal Descendants";
		agentInfoHeader += "\tSexual Pregnancies";
		agentInfoHeader += "\tSteps";
		agentInfoHeader += "\tTurns";
		agentInfoHeader += "\tAgent Bumps";
		agentInfoHeader += "\tRock Bumps";
		agentInfoHeader += "\tStrategy";
		buffer.append(agentInfoHeader);
		buffer.append("\n");
		pw.write(buffer.toString());
	}

	public void printAgentInfo(java.io.PrintWriter pw) {

		ComplexAgentInfo.initStaticAgentInfo(this.getAgentTypes());

		ComplexAgentInfo.printAgentHeaders(pw);


		for (ComplexAgentInfo info : agentInfoVector) {
			info.printInfo(pw);
		}


	}

	@Override
	public synchronized void removeAgent(int x, int y) {
		super.removeAgent(x, y);

		Location l = getLocation(x, y);
		Agent a = l.getAgent();
		if (a != null)
			a.die();
	}

	@Override
	public synchronized void removeFood(int x, int y) {
		super.removeFood(x, y);
		Location l = getLocation(x, y);
		l.setFlag(FLAG_FOOD, false);
	}

	@Override
	public synchronized void removeStone(int x, int y) {
		super.removeStone(x, y);
		Location l = getLocation(x, y);
		l.setFlag(FLAG_STONE, false);
	}

	@Override
	public void report(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w, false);

		printAgentInfo(pw);
	}

	public void resetAgentInfo() {
		agentInfoVector = new Vector<ComplexAgentInfo>();
	}

	/*
	 * save copies all current parsable parameters, and their values, to the
	 * specified writer
	 */
	@Override
	public void save(java.io.Writer w) {
		/*
		 * java.io.PrintWriter pw = new java.io.PrintWriter(w);
		 * pw.println(this.getClass().getName() + " " + data.agentTypeCount);
		 * pw.println(""); int[] indices = new int[512]; int length =
		 * parseData.size(); String[] names = new String[length]; Object[]
		 * saveArray = new Object[length]; java.util.Enumeration<String>
		 * nameList = parseData.keys(); java.util.Enumeration<Object> saveList =
		 * parseData.elements(); for (int i = 0; saveList.hasMoreElements() &&
		 * nameList.hasMoreElements(); ++i) { names[i] = nameList.nextElement();
		 * saveArray[i] = java.lang.reflect.Array.get(saveList.nextElement(),
		 * 0); } try { cobweb.parseClass.parseSave(pw, saveArray, names,
		 * indices, 0); } catch (java.io.IOException e) { // throw new
		 * java.io.IOException(); } pw.println(this.getClass().getName() +
		 * ".End"); pw.println(""); pw.flush();
		 */
	}

	/** Sets the default mutable variables of each agent type. */
	public void setDefaultMutableAgentParam() {
		ComplexAgent.setDefaultMutableParams(agentData, prodData);
	}

	@Override
	protected void setField(cobweb.Environment.Location l, int field, int value) {
		// Nothing
	}

	/**
	 * Flags locations as a food/stone/waste location. It does nothing if
	 * the square is already occupied (for example, setFlag((0,0),FOOD,true)
	 * does nothing when (0,0) is a stone
	 */
	@Override
	protected void setFlag(cobweb.Environment.Location l, int flag, boolean state) {
		switch (flag) {
			case FLAG_STONE:
				// Sanity check
				if ((getLocationBits(l) & (FOOD_CODE | WASTE_CODE)) != 0)
					break;
				if (state) {
					setLocationBits(l, (getLocationBits(l) & ~MASK_TYPE) | STONE_CODE);
				} else {
					setLocationBits(l, getLocationBits(l) & ~MASK_TYPE);
				}
				break;
			case FLAG_FOOD:
				// Sanity check
				if ((getLocationBits(l) & (STONE_CODE | WASTE_CODE)) != 0)
					break;
				if (state) {
					setLocationBits(l, (getLocationBits(l) & ~MASK_TYPE) | FOOD_CODE);
				} else
					setLocationBits(l, getLocationBits(l) & ~MASK_TYPE);
				break;
			case FLAG_DROP:
				// Sanity check
				if ((getLocationBits(l) & (FOOD_CODE | STONE_CODE)) != 0)
					break;
				if (state) {
					setLocationBits(l, (getLocationBits(l) & ~MASK_TYPE) | WASTE_CODE);
				} else {
					setLocationBits(l, getLocationBits(l) & ~MASK_TYPE);
				}
				break;
			default:
		}
	}

	// Sets Food Type in foodarray [];
	public void setFoodType(cobweb.Environment.Location l, int i) {
		foodarray[l.v[0]][l.v[1]] = i;
	}

	protected void setLocationBits(cobweb.Environment.Location l, int bits) {
		array.setLocationBits(l, bits);
	}

	/* $$$$$$ Set the current tick number, tickCount (long). Apr 19 */
	public void setTickCount(long tick) {
		tickCount = tick;
	}

	@Override
	protected boolean testFlag(cobweb.Environment.Location l, int flag) {
		switch (flag) {
			case FLAG_STONE:
				return ((getLocationBits(l) & MASK_TYPE) == STONE_CODE);
			case FLAG_FOOD:
				return ((getLocationBits(l) & MASK_TYPE) == FOOD_CODE);
			case FLAG_DROP:
				return ((getLocationBits(l) & MASK_TYPE) == WASTE_CODE);
			default:
				return false;
		}
	}

	/**
	 * tickNotification is the method called by the scheduler for each of its
	 * clients for every tick of the simulation. For environment,
	 * tickNotification performs all of the per-tick tasks necessary for the
	 * environment to function properly. These tasks include managing food
	 * depletion, food growth, and random food-"dropping".
	 */
	public void tickNotification(long tick) {
		++tickCount;

		commManager.decrementPersistence();
		commManager.unblockBroadcast();

		updateWaste();

		// for each agent type, we test to see if its deplete time step has
		// come, and if so deplete the food random
		// by the appropriate percentage

		for (int i = 0; i < data.getFoodTypes(); ++i) {
			if (foodData[i].depleteRate != 0.0f && foodData[i].growRate > 0
					&& (tickCount % foodData[i].depleteTime) == 0) {
				depleteFood(i);
			}
		}

		boolean shouldGrow = false;
		for (int i = 0; i < data.getAgentTypes(); ++i) {
			if (foodData[i].growRate > 0) {
				shouldGrow = true;
				break;
			}
		}

		// if no food is growing (total == 0) this loop is not nessesary
		if (shouldGrow) {
			growFood();
		}

		// Air-drop food into the environment
		for (int i = 0; i < data.foodTypeCount; ++i) {
			if (draughtdays[i] == 0) {
				dropFood(i);
			} else {
				draughtdays[i]--;
			}
		}

		if (observedAgent != null && !observedAgent.isAlive()) {
			observedAgent = null;
		}
	}

	public void tickZero() {
		// Nothing
	}

	@Override
	public void unObserve() {
		// Nothing
	}

	/**
	 * 
	 */
	private void updateWaste() {
		for (int i = 0; i < dropArray.length; i++) {
			for (int j = 0; j < dropArray[i].length; j++) {
				Location l = getLocation(i, j);
				if (l.testFlag(ComplexEnvironment.FLAG_DROP) == false)
					continue;
				Drop d = dropArray[i][j];
				if (!d.isActive(getTickCount())) {
					l.setFlag(ComplexEnvironment.FLAG_DROP, false);
					if (d instanceof Product) {
						prodMapper.remProduct((Product)d, getLocation(i, j));
					}
					dropArray[i][j] = null; // consider deactivating
					// and not deleting
				}
			}
		}
	}

	/**
	 * Write to Log file: FoodCount, AgentCount, Average Agent Energy and Agent
	 * Energy at the most recent ticks ( by tick and by Agent/Food preference)
	 */
	@Override
	public void writeLogEntry() {
		if (logStream == null) {
			return;
		}

		java.text.DecimalFormat z = new DecimalFormat("#,##0.000");

		setTickCount(theScheduler.getTime()); // $$$$$$ get the current tick.
		// Apr 19
		// For this tick: print FoodCount, AgentCount, Average Agent Energy
		// and Agent Energy for EACH AGENT TYPE
		for (int i = 0; i < data.getAgentTypes(); i++) {

			long agentCount = countAgents(i);
			long agentEnergy = countAgentEnergy(i);
			/*
			 * System.out
			 * .println("************* Near Agent Count *************");
			 */

			int cheaters = (numAgentsStrat(i))[1];
			int coops = (numAgentsStrat(i))[0];
			logStream.print(tickCount);
			logStream.print('\t');
			logStream.print(countFoodTiles(i));
			logStream.print('\t');
			logStream.print(agentCount);
			logStream.print('\t');
			// Format Average agentEnergy to 3 decimal places
			if (agentCount != 0)
				logStream.print(z.format(((float) agentEnergy) / agentCount));
			else
				logStream.print("00.000");
			logStream.print('\t');
			logStream.print(agentEnergy);
			logStream.print('\t');
			logStream.print(cheaters);
			logStream.print('\t');
			logStream.print(coops);
			logStream.print('\t');

			for (String s : ComplexAgent.logDataAgent(i)) {
				logStream.print(s);
				logStream.print('\t');
			}
		}
		// print the TOTAL of FoodCount, AgentCount, Average Agent Energy
		// and Agent Energy at a certain tick
		/*
		 * System.out
		 * .println("************* Before Agent Count Call *************");
		 */
		long agentCountAll = countAgents();
		/*
		 * System.out
		 * .println("************* After Agent Count Call *************");
		 */
		long agentEnergyAll = countAgentEnergy();
		int total_cheaters = (numAgentsStrat())[1];
		int total_coops = (numAgentsStrat())[0];
		logStream.print(tickCount);
		logStream.print('\t');
		logStream.print(countFoodTiles());
		logStream.print('\t');
		logStream.print(agentCountAll);
		logStream.print('\t');
		logStream.print(z.format(((float) agentEnergyAll) / agentCountAll));
		logStream.print('\t');
		logStream.print(agentEnergyAll);
		logStream.print('\t');
		logStream.print(total_cheaters);
		logStream.print('\t');
		logStream.print(total_coops);
		logStream.print('\t');

		for (String s : ComplexAgent.logDataTotal()) {
			logStream.print(s);
			logStream.print('\t');
		}
		logStream.println();
		// logStream.flush();
	}

	/* Write the Log titles to the file,(called by log (java.io.Writer w)) */
	public void writeLogTitles() {
		if (logStream != null) {
			for (int i = 1; i <= data.getAgentTypes(); i++) {

				logStream.print("Tick\t");
				logStream.print("FoodCount " + i + "\t");
				logStream.print("AgentCount " + i + "\t");
				logStream.print("AveAgentEnergy " + i + "\t");
				logStream.print("AgentEnergy " + i + "\t");
				logStream.print("Cheat " + i + "\t");
				logStream.print("Coop " + i + "\t");
				for (String s : ComplexAgent.logHederAgent()) {
					logStream.print(s);
					logStream.print(" " + i);
					logStream.print('\t');
				}
			}
			// One final round of output for total
			logStream.print("Tick\t");
			logStream.print("FoodCount T\t");
			logStream.print("AgentCount T\t");
			logStream.print("AveAgentEnergy T\t");
			logStream.print("AgentEnergy T\t");
			logStream.print("Num. Cheat T\t");
			logStream.print("Num. Coop T\t");
			for (String s : ComplexAgent.logHederTotal()) {
				logStream.print(s);
				logStream.print(" T");
				logStream.print('\t');
			}
			logStream.println();
		}
	}

	@Override
	public boolean hasAgent(int x, int y) {
		return getUserDefinedLocation(x, y).getAgent() != null;
	}

	@Override
	public Agent getAgent(int x, int y) {
		return getUserDefinedLocation(x, y).getAgent();
	}

	@Override
	public boolean hasFood(int x, int y) {
		return testFlag(getUserDefinedLocation(x, y), FLAG_FOOD);
	}

	@Override
	public int getFood(int x, int y) {
		return foodarray[x][y];
	}

	@Override
	public boolean hasStone(int x, int y) {
		return testFlag(getUserDefinedLocation(x, y), FLAG_STONE);
	}


}
