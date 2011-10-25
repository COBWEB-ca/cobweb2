package cwcore;

import ga.GATracker;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cobweb.Agent;
import cobweb.ArrayEnvironment;
import cobweb.ColorLookup;
import cobweb.Direction;
import cobweb.DrawingHandler;
import cobweb.Environment;
import cobweb.Location;
import cobweb.Point2D;
import cobweb.RandomNoGenerator;
import cobweb.Scheduler;
import cobweb.TickScheduler;
import cobweb.TypeColorEnumeration;
import cwcore.broadcast.PacketConduit;
import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ComplexEnvironmentParams;
import cwcore.complexParams.ComplexFoodParams;
import cwcore.state.StateParameter;
import cwcore.state.StatePlugin;
import driver.ControllerFactory;
import driver.SimulationConfig;

/**
 * This class contains an implementation of the TickScheduler.Client class.
 */
public class ComplexEnvironment extends Environment implements TickScheduler.Client {


	private static final int DROP_ATTEMPTS_MAX = 5;

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

	/**
	 * The food params for this environment.
	 * What does this encompass?
	 */
	private ComplexFoodParams foodData[];

	protected ComplexAgentParams agentData[];

	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();

	// Bitmasks for boolean states
	private static final int MASK_TYPE = 15;

	private static final int STONE_CODE = 1;

	private static final int FOOD_CODE = 2;

	private static final int WATER_CODE = 8;

	private static final int WASTE_CODE = 4;

	public void setDrop(Location loc, Drop d) {
		dropArray[loc.v[0]][loc.v[1]] = d;
	}

	// Returns current location's food type
	public int getFoodType(cobweb.Location l) {
		return l.getFoodSource().getType();
	}

	GATracker gaTracker;

	private java.io.PrintWriter logStream;

	private Vector<ComplexAgentInfo> agentInfoVector = new Vector<ComplexAgentInfo>();

	private ComplexEnvironmentParams data = new ComplexEnvironmentParams();

	/*
	 * Waste tile array to store the data per waste tile. Needed to allow
	 * depletion of waste
	 */
	public Drop[][] dropArray;	

	private Agent observedAgent = null;

	private int draughtdays[];

	public PacketConduit commManager;

	public ComplexEnvironment() {
		super();
		commManager = new PacketConduit();
	}

	@Override
	public synchronized void addAgent(int x, int y, int type) {
		super.addAgent(x, y, type);
		cobweb.Location l;
		l = getUserDefinedLocation(x, y);
		if ((l.getAgent() == null) && !l.testFlag(ComplexEnvironment.FLAG_STONE)
				&& !l.testFlag(ComplexEnvironment.FLAG_DROP)) {
			int agentType = type;

			spawnAgent(l, agentType);

		}
	}

	protected void spawnAgent(cobweb.Location location, int agentType) {
		ComplexAgent child = (ComplexAgent)AgentSpawner.spawn();
		child.init(agentType, location, (ComplexAgentParams) agentData[agentType].clone()); // Default
	}

	ComplexAgentInfo addAgentInfo(ComplexAgentInfo info) {
		agentInfoVector.add(info);
		return info;
	}

	ComplexAgentInfo addAgentInfo(int agentT, ComplexAgentInfo p1, ComplexAgentInfo p2, boolean action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT, tickCount, p1, p2, action));
	}

	ComplexAgentInfo addAgentInfo(int agentT, ComplexAgentInfo p1, boolean action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT, tickCount, p1, action));
	}

	ComplexAgentInfo addAgentInfo(int agentT, boolean action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT, tickCount, action));
	}

	@Override
	public synchronized void addFoodSource(int x, int y, int type) {

		cobweb.Location l;
		l = getUserDefinedLocation(x, y);
		if (l.testFlag(ComplexEnvironment.FLAG_STONE)) {
			throw new IllegalArgumentException("stone here already");
		}
		l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
		FoodSource f = new FoodSource (foodData[type].quantity, type, l, 
				foodData[type].depleteRate, foodData[type].growRate);
		foodSourceTable.put(l, f);

		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X) * getSize(AXIS_Y)];
		fillTileColors(tileColors);
	}


	@Override
	public synchronized void addWater(int x, int y) {
		Location l;

		l = getUserDefinedLocation(x, y);

		if(l.getAgent() != null) {
			return;
		}

		if(l.getFoodSource() != null) {
			l.removeFoodSource();
		}

		if(l.testFlag(FLAG_DROP))
			l.setFlag(FLAG_DROP, false);

		l.setFlag(ComplexEnvironment.FLAG_WATER, true);

		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X) * getSize(AXIS_Y)];
		fillTileColors(tileColors);
	}

	@Override
	public synchronized void addStone(int x, int y) {
		Location l;
		l = getUserDefinedLocation(x, y);
		if (l.getAgent() != null) {
			return;
		}

		if (l.getFoodSource() != null)
			l.removeFoodSource();
		if (l.testFlag(FLAG_DROP))
			l.setFlag(FLAG_DROP, false);

		l.setFlag(ComplexEnvironment.FLAG_STONE, true);

		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X) * getSize(AXIS_Y)];
		fillTileColors(tileColors);
	}

	/**
	 * Removes all agents from the environment.
	 */
	@Override
	public synchronized void clearAgents() {
		super.clearAgents();
	}

	private void clearFlag(int flag) {
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);

				if (currentPos.testFlag(flag)) {
					if (flag == ComplexEnvironment.FLAG_FOOD)
						currentPos.removeFoodSource();
					else
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

	/**
	 * Removes all food sources from the environment.
	 */
	@Override
	public synchronized void clearFoodSources() {
		super.clearFoodSources();
		clearFlag(FLAG_FOOD);
	}

	/**
	 * Removes all stones from the environment.
	 */
	@Override
	public synchronized void clearStones() {
		super.clearStones();
		clearFlag(FLAG_STONE);
	}

	/**
	 * Clears all waste from the environment.
	 */
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
				if (currentPos.getFoodSource() != null)
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
				if (currentPos.getFoodSource() != null)
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
		for (int x = 0; x < getSize(AXIS_X); ++x) {
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.getFoodSource() != null && getFoodType(currentPos) == type)
					locations.add(environmentRandom.nextInt(locations.size() + 1), currentPos);
			}
		}

		int foodToDeplete = (int) (locations.size() * foodData[type].depleteRate);

		for (int j = 0; j < foodToDeplete; ++j) {
			Location loc = locations.removeLast();
			loc.removeFoodSource();
		}
		draughtdays[type] = foodData[type].draughtPeriod;
	}

	private void dropFood(int type) {
		float foodDrop = foodData[type].dropRate;
		while (environmentRandom.nextFloat() < foodDrop) {
			--foodDrop;
			cobweb.Location l;
			int j = 0;
			do {
				++j;
				l = getRandomLocation();

			} while (j < DROP_ATTEMPTS_MAX
					&& (l.testFlag(ComplexEnvironment.FLAG_STONE) || l.getFoodSource() != null
							|| l.testFlag(ComplexEnvironment.FLAG_DROP) || l.getAgent() != null));

			if (j < DROP_ATTEMPTS_MAX) {
				addFoodSource(l.v[0], l.v[1], type);
			}
		}
	}

	/**
	 * Fills tile colour array with colours corresponding to whether there is 
	 * a food source, a stone, waste, or nothing.
	 */
	@Override
	public void fillTileColors(java.awt.Color[] tileColors) {
		int tileIndex = 0;
		for (int y = 0; y < getSize(AXIS_Y); ++y) {
			for (int x = 0; x < getSize(AXIS_X); ++x) {
				Location currentPos = getLocation(x, y);

				if (currentPos.testFlag(FLAG_STONE))
					tileColors[tileIndex++] = java.awt.Color.darkGray;

				else if (currentPos.testFlag(FLAG_WATER)) {
					tileColors[tileIndex++] = java.awt.Color.blue;
				}
				else if (currentPos.getFoodSource() != null)
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

	public int getInfoNum() {
		return agentInfoVector.size();
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

	/**
	 * Call on each food source to reproduce. Add the offsprings to the food source hash table.
	 */
	private void growFood() {

		//		long sum = 0;
		//		int numFood = foodSourceTable.size();

		for (FoodSource food : new LinkedList<FoodSource>(foodSourceTable.values())) {
			LinkedList<FoodSource> newFood = food.reproduce();
			FoodSource f;

			//			sum += this.reproductiveSuccess(food, newFood.size());

			while(!newFood.isEmpty()) {
				f = newFood.pop();
				foodSourceTable.put(f.getLocation(), f);
			}
		}

		//		System.out.println("=== New tick ===");
		//		System.out.println("Average reproductive success was " + (sum / numFood) + "%");
	}

	/**
	 * Return the reproductive success rate of the given food source as a percent
	 */
	private int reproductiveSuccess(FoodSource f, int numOffspring) {
		if(f.getNumSeeds() == 0) {
			return 0;
		} else {
			return Math.round(numOffspring * 100 / f.getNumSeeds());
		}
	}

	/**
	 * TODO: Check if this works, possibly rewrite
	 * @param fileName 
	 * @param option
	 * @return 
	 */
	@Override
	public boolean insertPopulation(String fileName, String option) {


		if (option.equals("replace")) {
			clearAgents();
		}

		//Load XML file
		FileInputStream file;
		try {
			file = new FileInputStream(fileName);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}

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
			Node agent = agents.item(i);
			Element element = (Element) agent;

			NodeList paramsElement = element.getElementsByTagName("params");
			Element paramNode = (Element) paramsElement.item(0);			

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

			// agentType
			int agentType = Integer.parseInt(agentTypeElement.item(0).getChildNodes().item(0).getNodeValue());

			// doCheat
			boolean pdCheater = Boolean.parseBoolean(pdCheaterElement.item(0).getChildNodes().item(0).getNodeValue());


			ComplexAgent cAgent = (ComplexAgent)AgentSpawner.spawn();
			cAgent.init(agentType, params, facing, loc);
			cAgent.pdCheater = pdCheater;
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
		} else {
			array = new cobweb.ArrayEnvironment(data.width, data.height);
		}
		backArray = new ArrayEnvironment(data.width, data.height);

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
			cobweb.Location l;
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

		setupPlugins();

	}

	private void setupPlugins() {
		for (StatePlugin plugin : plugins) {
			for (StateParameter param : plugin.getParameters()) {
				pluginMap.put(param.getName(), param);
			}
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
		for (int i = 0; i < data.getAgentTypes(); ++i) {

			for (int j = 0; j < agentData[i].initialAgents; ++j) {

				cobweb.Location location;
				int tries = 0;
				do {
					location = getRandomLocation();
				} while ((tries++ < 100) && ((location.getAgent() != null) // don't spawn on top of agents
						|| location.testFlag(ComplexEnvironment.FLAG_STONE) // nor on stone tiles
						|| location.testFlag(ComplexEnvironment.FLAG_DROP))); // nor on waste tiles
				if (tries < 100) {
					int agentType = i;
					spawnAgent(location, agentType);
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
				cobweb.Location l;
				int tries = 0;
				do {
					l = getRandomLocation();
				} while ((tries++ < 100)
						&& (l.testFlag(ComplexEnvironment.FLAG_STONE) || l.testFlag(ComplexEnvironment.FLAG_DROP)));
				if (tries < 100) {
					addFoodSource(l.v[0], l.v[1], i);
				}
			}
		}
	}

	/**
	 * Creates a new waste array and initializes each location as having 
	 * no waste.
	 */
	private void loadNewWaste() {
		dropArray = new Drop[data.width][data.height];
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
					((ComplexAgent) currentPos.getAgent()).setConstants(agentData[theType]); // Default
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
					currentPos.removeFoodSource();
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
			if (!agent.getAgentPDActionCheat()) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentPDActionCheat()) {
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
			if (agent.getAgentType() == agentType && !agent.getAgentPDActionCheat()) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentType() == agentType && agent.getAgentPDActionCheat()) {
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	/**
	 * This gets called when the user clicks on a tile without selecting outside
	 * of edit mode.  Sets observed agent if agent is clicked on.
	 */
	@Override
	public void observe(int x, int y) {
		cobweb.Location l = getUserDefinedLocation(x, y);
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
	public synchronized void removeFoodSource(int x, int y) {
		super.removeFoodSource(x, y);
	}

	@Override
	public synchronized void removeStone(int x, int y) {
		super.removeStone(x, y);
		Location l = getLocation(x, y);
		l.setFlag(FLAG_STONE, false);
	}

	@Override
	public synchronized void removeWater(int x, int y) {
		super.removeWater(x, y);
		Location l = getLocation(x, y);
		l.setFlag(FLAG_WATER, false);
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
		ComplexAgent.setDefaultMutableParams(agentData);
	}

	/**
	 * @param l Location of food source.
	 * @param type Type of food source. 
	 */
	public void setFoodType(cobweb.Location l, int type) {
		l.getFoodSource().setType(type);
	}

	/* $$$$$$ Set the current tick number, tickCount (long). Apr 19 */
	public void setTickCount(long tick) {
		tickCount = tick;
	}

	/**
	 * tickNotification is the method called by the scheduler for each of its
	 * clients for every tick of the simulation. For environment,
	 * tickNotification performs all of the per-tick tasks necessary for the
	 * environment to function properly. These tasks include managing food
	 * depletion, food growth, and random food-"dropping".
	 */
	public void tickNotification(long tick) {
		//1 tick has passed
		++tickCount;

		//XXX what does this do?
		commManager.decrementPersistence();
		commManager.unblockBroadcast();

		//XXX why is waste evaluated at the beginning of the tick instead of at the end?
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

		//what is foodData?

		//what does this variable refer to?
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

		for (FoodSource f : new LinkedList<FoodSource>(foodSourceTable.values())) {
			if (f.isEmpty())
				foodSourceTable.remove(f.getLocation());
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
					d.expire();
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

	/**
	 * @param x Scrutinized X coordinate.
	 * @param y Scrutinized Y coordinate.
	 * @return True if an agent is at this location.
	 */
	@Override
	public boolean hasAgent(int x, int y) {
		return getUserDefinedLocation(x, y).getAgent() != null;
	}

	/**
	 * @param x X coordinate of agent location.
	 * @param y Y coordinate of agent location.
	 * @return Agent at specified location.
	 */
	@Override
	public Agent getAgent(int x, int y) {
		return getUserDefinedLocation(x, y).getAgent();
	}

	/**
	 * @param x Scrutinized x coordinate.
	 * @param y Scrutinized y coordinate.
	 * @return True if a food source exists at scrutinized location.
	 */
	@Override
	public boolean hasFood(int x, int y) {
		Location l = getUserDefinedLocation(x, y);
		return (l.getFoodSource() != null);
	}

	/**
	 * @param x X coordinate of food source.
	 * @param y Y coordinate of food source.
	 * @return Type of food source at this location.
	 */
	@Override
	public int getFood(int x, int y) {
		Location l = getUserDefinedLocation(x, y);
		return l.getFoodSource().getType();
	}

	/**
	 * @param x Scrutinized X coordinate.
	 * @param y Scrutinized Y coordinate.
	 * @return True if stone exists at this location.
	 */
	@Override
	public boolean hasStone(int x, int y) {
		return testFlag(getUserDefinedLocation(x, y), FLAG_STONE);
	}

	@Override
	public boolean hasWater(int x, int y) {
		return testFlag(getUserDefinedLocation(x, y), FLAG_WATER);
	}

	private List<StatePlugin> plugins = new LinkedList<StatePlugin>();

	private Map<String, StateParameter> pluginMap = new HashMap<String, StateParameter>();

	public StateParameter getStateParameter(String name) {
		return pluginMap.get(name);
	}

	public boolean isPDenabled() {
		return data.prisDilemma;
	}


}
