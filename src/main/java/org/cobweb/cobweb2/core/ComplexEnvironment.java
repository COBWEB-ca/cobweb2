package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ai.ControllerFactory;
import org.cobweb.cobweb2.broadcast.PacketConduit;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.core.params.ComplexEnvironmentParams;
import org.cobweb.cobweb2.core.params.ComplexFoodParams;
import org.cobweb.cobweb2.interconnect.StateParameter;
import org.cobweb.cobweb2.interconnect.StatePlugin;
import org.cobweb.cobweb2.production.ProductionMapper;
import org.cobweb.cobweb2.production.ProductionParams;

/**
 * 2D grid where agents and food live
 */
public class ComplexEnvironment extends Environment implements Updatable {

	private static final int DROP_ATTEMPTS_MAX = 5;

	public static final int FLAG_STONE = 1;

	public static final int FLAG_FOOD = 2;

	public static final int FLAG_AGENT = 3;

	public static final int FLAG_DROP = 4;

	public int PD_PAYOFF_REWARD;

	public int PD_PAYOFF_SUCKER;

	public int PD_PAYOFF_TEMPTATION;

	public int PD_PAYOFF_PUNISHMENT;

	private ComplexFoodParams foodData[];

	protected ComplexAgentParams agentData[];

	protected ProductionParams prodData[];

	// Bitmasks for boolean states
	private static final int MASK_TYPE = 15;

	private static final int STONE_CODE = 1;

	private static final int FOOD_CODE = 2;

	private static final int WASTE_CODE = 4;

	public void setDrop(Location loc, Drop d) {
		dropArray[loc.x][loc.y] = d;
	}

	public Drop getDrop(Location loc) {
		return dropArray[loc.x][loc.y];
	}

	// Returns current location's food type
	public int getFoodType(org.cobweb.cobweb2.core.Location l) {
		return foodarray[l.x][l.y];
	}

	public final List<ComplexAgentStatistics> agentInfoVector = new ArrayList<ComplexAgentStatistics>();

	private org.cobweb.cobweb2.core.ArrayEnvironment array;

	private ComplexEnvironmentParams data = new ComplexEnvironmentParams();

	private int[][] foodarray = new int[0][];

	/*
	 * Waste tile array to store the data per waste tile. Needed to allow
	 * depletion of waste
	 */
	public Drop[][] dropArray;

	private int draughtdays[];

	int[][] backFoodArray;

	ArrayEnvironment backArray;

	int mostFood[];

	public PacketConduit commManager;

	public ComplexEnvironment(SimulationInternals simulation) {
		super(simulation);
		commManager = new PacketConduit();
	}

	public ProductionMapper prodMapper;

	public ControllerFactory controllerFactory;

	@Override
	public synchronized void addAgent(int x, int y, int type) {
		super.addAgent(x, y, type);
		org.cobweb.cobweb2.core.Location l;
		l = getUserDefinedLocation(x, y);
		if ((getAgent(l) == null) && !testFlag(l, ComplexEnvironment.FLAG_STONE)
				&& !testFlag(l, ComplexEnvironment.FLAG_DROP)) {
			int agentType = type;

			spawnAgent(new LocationDirection(l, DIRECTION_NONE), agentType);

		}
	}

	protected void spawnAgent(LocationDirection location, int agentType) {
		ComplexAgent child = simulation.newAgent();
		child.init(this, agentType, location, (ComplexAgentParams) agentData[agentType].clone(),
				(ProductionParams) prodData[agentType].clone()); // Default
	}

	ComplexAgentStatistics addAgentInfo(ComplexAgentStatistics info) {
		agentInfoVector.add(info);
		return info;
	}

	ComplexAgentStatistics addAgentInfo(int agentT, ComplexAgentStatistics p1, ComplexAgentStatistics p2) {
		return addAgentInfo(new ComplexAgentStatistics(getInfoNum(), agentT, simulation.getTime(), p1, p2));
	}

	ComplexAgentStatistics addAgentInfo(int agentT, ComplexAgentStatistics p1) {
		return addAgentInfo(new ComplexAgentStatistics(getInfoNum(), agentT, simulation.getTime(), p1));
	}

	ComplexAgentStatistics addAgentInfo(int agentT) {
		return addAgentInfo(new ComplexAgentStatistics(getInfoNum(), agentT, simulation.getTime()));
	}

	@Override
	public synchronized void addFood(int x, int y, int type) {

		org.cobweb.cobweb2.core.Location l;
		l = getUserDefinedLocation(x, y);
		if (testFlag(l, ComplexEnvironment.FLAG_STONE)) {
			throw new IllegalArgumentException("stone here already");
		}
		setFlag(l, ComplexEnvironment.FLAG_FOOD, true);
		setFoodType(l, type);
	}

	@Override
	public synchronized void addStone(int x, int y) {
		org.cobweb.cobweb2.core.Location l;
		l = getUserDefinedLocation(x, y);
		if (getAgent(l) != null) {
			return;
		}

		if (testFlag(l, FLAG_FOOD))
			setFlag(l, FLAG_FOOD, false);
		if (testFlag(l, FLAG_DROP))
			setFlag(l, FLAG_DROP, false);

		setFlag(l, ComplexEnvironment.FLAG_STONE, true);
	}

	@Override
	public synchronized void clearAgents() {
		super.clearAgents();
		resetAgentInfo();
	}

	private void clearFlag(int flag) {
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				Location currentPos = getLocation(x, y);

				if (testFlag(currentPos, flag)) {
					setFlag(currentPos, flag, false);
				}
			}
		}
	}

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
	public long countAgentEnergy() {
		long totalEnergy = 0;
		for(Agent a : getAgents()){
			ComplexAgent agent = (ComplexAgent) a;
			totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	/* Return totalEnergy (long) for a certain agentType (int) */
	public long countAgentEnergy(int agentType) {
		long totalEnergy = 0;
		for(Agent a : getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
			if (agent.getAgentType() == agentType)
				totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	/* Return the number of agents (int) for a certain agentType (int) */
	public int countAgents(int agentType) {
		int agentCount = 0;
		for(Agent a : getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
			if (agent.getAgentType() == agentType)
				agentCount++;
		}
		return agentCount;
	}

	/* Return foodCount (long) of all types of food */
	public long countFoodTiles() {
		long foodCount = 0;

		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				Location currentPos = getLocation(x, y);
				if (testFlag(currentPos, ComplexEnvironment.FLAG_FOOD))
					++foodCount;
			}
		}
		return foodCount;
	}

	/* Return foodCount (long) for a specific foodType (int) */
	public int countFoodTiles(int foodType) {
		int foodCount = 0;
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				Location currentPos = getLocation(x, y);
				if (testFlag(currentPos, ComplexEnvironment.FLAG_FOOD))
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
		for (int x = 0; x < getWidth(); ++x)
			for (int y = 0; y < getHeight(); ++y) {
				Location currentPos = getLocation(x, y);
				if (testFlag(currentPos, ComplexEnvironment.FLAG_FOOD) && getFoodType(currentPos) == type)
					locations.add(simulation.getRandom().nextInt(locations.size() + 1), currentPos);
			}

		int foodToDeplete = (int) (locations.size() * foodData[type].depleteRate);

		for (int j = 0; j < foodToDeplete; ++j) {
			Location loc = locations.removeLast();

			setFlag(loc, FLAG_FOOD, false);
		}
		draughtdays[type] = foodData[type].draughtPeriod;
	}

	private void dropFood(int type) {
		float foodDrop = foodData[type].dropRate;
		while (simulation.getRandom().nextFloat() < foodDrop) {
			--foodDrop;
			org.cobweb.cobweb2.core.Location l;
			int j = 0;
			do {
				++j;
				l = getRandomLocation();

			} while (j < DROP_ATTEMPTS_MAX
					&& (testFlag(l, ComplexEnvironment.FLAG_STONE) || testFlag(l, ComplexEnvironment.FLAG_FOOD)
							|| testFlag(l, ComplexEnvironment.FLAG_DROP) || getAgent(l) != null));

			if (j < DROP_ATTEMPTS_MAX) {
				setFlag(l, ComplexEnvironment.FLAG_FOOD, true);
				setFoodType(l, type);
			}
		}
	}

	@Override
	public int getAxisCount() {
		return 2;
	}

	@Override
	public boolean getAxisWrap(int axis) {
		return data.wrapMap;
	}

	public int getInfoNum() {
		return agentInfoVector.size();
	}

	protected int getLocationBits(org.cobweb.cobweb2.core.Location l) {
		return array.getLocationBits(l);
	}

	@Override
	public synchronized EnvironmentStats getStatistics() {
		EnvironmentStats stats = new EnvironmentStats();
		stats.agentCounts = new long[data.agentTypeCount];
		stats.foodCounts = new long[data.agentTypeCount];
		for (int i = 0; i < data.agentTypeCount; i++) {
			stats.agentCounts[i] = countAgents(i);
			stats.foodCounts[i] = countFoodTiles(i);
		}
		stats.timestep = simulation.getTime();
		return stats;
	}

	@Override
	public int getWidth() {
		return data.width;
	}

	@Override
	public int getHeight() {
		return data.height;
	}

	private void growFood() {

		for (int y = 0; y < getHeight(); ++y) {
			for (int x = 0; x < getWidth(); ++x) {
				Location currentPos = getLocation(x, y);
				// if there's a stone or already food, we simply copy the
				// information from the old arrays to the new ones
				backArray.setLocationBits(currentPos, array.getLocationBits(currentPos));
				backFoodArray[currentPos.x][currentPos.y] = foodarray[currentPos.x][currentPos.y];
			}
		}

		// create a new ArrayEnvironment and a new food type array
		// loop through all positions
		for (int y = 0; y < getHeight(); ++y) {
			for (int x = 0; x < getWidth(); ++x) {
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

					Location checkPos = getAdjacent(currentPos, DIRECTION_NORTH);
					if (checkPos != null && testFlag(checkPos, ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}
					checkPos = getAdjacent(currentPos, DIRECTION_SOUTH);
					if (checkPos != null && testFlag(checkPos, ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}
					checkPos = getAdjacent(currentPos, DIRECTION_EAST);
					if (checkPos != null && testFlag(checkPos, ComplexEnvironment.FLAG_FOOD)) {
						foodCount++;
						mostFood[getFoodType(checkPos)]++;
					}
					checkPos = getAdjacent(currentPos, DIRECTION_WEST);
					if (checkPos != null && testFlag(checkPos, ComplexEnvironment.FLAG_FOOD)) {
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

						if (data.likeFoodProb >= simulation.getRandom().nextFloat()) {
							growingType = max;
						} else {
							growingType = simulation.getRandom().nextInt(data.getFoodTypes());
						}

						// finally, we grow food according to a certain
						// amount of random chance
						if (foodCount * foodData[growingType].growRate > 100 * simulation.getRandom().nextFloat()) {
							backArray.setLocationBits(currentPos, FOOD_CODE);
							// setFoodType (currentPos, growMe);
							backFoodArray[currentPos.x][currentPos.y] = growingType;
						} else {
							backArray.setLocationBits(currentPos, 0);
							backFoodArray[currentPos.x][currentPos.y] = -123154534;
						}
					} else {
						backArray.setLocationBits(currentPos, 0);
						backFoodArray[currentPos.x][currentPos.y] = -123154534;
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
	 * Loads a new complex environment using the data held within the simulation
	 * configuration object, p.  The following actions are performed during a load:
	 *
	 * <p>1. Attaches the scheduler, s, to the environment.
	 * <br>2. Stores parameters from the file or stream in to ComplexEnvironment.
	 * <br>3. Sets up location cache, and environment food and waste arrays.
	 * <br>4. Keeps or removes old agents.
	 * <br>5. Adds new stones, food, and agents.
	 *
	 * @param config The simulation  settings
	 */
	@Override
	public synchronized void load(SimulationConfig config) throws IllegalArgumentException {
		super.load(config);

		int oldH = data.height;
		int oldW = data.width;

		/**
		 * the first parameter must be the number of agent types as we must
		 * allocate space to store the parameter for each type
		 */

		copyParamsFromParser(config);

		setDefaultMutableAgentParam();

		/**
		 * If the random seed is set to 0 in the data file, it means we use the
		 * system time instead
		 */
		if (data.randomSeed == 0)
			data.randomSeed = System.currentTimeMillis();

		if (data.keepOldArray) {
			int[] boardIndices = { data.width, data.height };
			array = new org.cobweb.cobweb2.core.ArrayEnvironment(data.width, data.height, array);
			foodarray = org.cobweb.util.ArrayUtilities.resizeArray(foodarray, boardIndices);
		} else {
			array = new org.cobweb.cobweb2.core.ArrayEnvironment(data.width, data.height);
			foodarray = new int[data.width][data.height];
		}
		backArray = new ArrayEnvironment(data.width, data.height);
		backFoodArray = new int[data.width][data.height];
		mostFood = new int[data.getFoodTypes()];

		if (dropArray == null || !data.keepOldWaste) {
			loadNewWaste();
		} else {
			loadOldWaste();
		}

		loadFoodMode();

		if (data.keepOldAgents) {
			loadOldAgents(oldH, oldW);
		} else {
			clearAgents();
		}

		if (!data.keepOldPackets) {
			commManager.clearPackets();
		}

		// add stones in to random locations
		for (int i = 0; i < data.initialStones; ++i) {
			org.cobweb.cobweb2.core.Location l;
			int tries = 0;
			do {
				l = getRandomLocation();
			} while ((tries++ < 100)
					&& ((testFlag(l, ComplexEnvironment.FLAG_STONE) || testFlag(l, ComplexEnvironment.FLAG_DROP))
							&& getAgent(l) == null));
			if (tries < 100)
				setFlag(l, ComplexEnvironment.FLAG_STONE, true);
		}

		// add food to random locations
		if (data.dropNewFood) {
			loadNewFood();
		}

		try {
			controllerFactory = new ControllerFactory(data.controllerName, config.getControllerParams(), simulation);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		// spawn new random agents for each type
		if (data.spawnNewAgents) {
			loadNewAgents();
		}

		plugins.add(prodMapper);

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
				foodData[i].depleteRate = simulation.getRandom().nextFloat();
			if (foodData[i].depleteTime <= 0)
				foodData[i].depleteTime = simulation.getRandom().nextInt(100) + 1;
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

				org.cobweb.cobweb2.core.Location location;
				int tries = 0;
				do {
					location = getRandomLocation();
				} while ((tries++ < 100) && ((getAgent(location) != null) // don't spawn on top of agents
						|| testFlag(location, ComplexEnvironment.FLAG_STONE) // nor on stone tiles
						|| testFlag(location, ComplexEnvironment.FLAG_DROP))); // nor on waste tiles
				if (tries < 100) {
					int agentType = i;
					spawnAgent(new LocationDirection(location, DIRECTION_NONE), agentType);
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
				org.cobweb.cobweb2.core.Location l;
				int tries = 0;
				do {
					l = getRandomLocation();
				} while ((tries++ < 100)
						&& (testFlag(l, ComplexEnvironment.FLAG_STONE) || testFlag(l, ComplexEnvironment.FLAG_DROP)));
				if (tries < 100)
					setFlag(l, ComplexEnvironment.FLAG_FOOD, true);
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
		prodMapper = new ProductionMapper(simulation);
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				setFlag(getLocation(x, y), FLAG_DROP, false);
			}
		}
	}

	/**
	 * Searches through each location to find every old agent.  Each agent that is found
	 * is added to the scheduler if the scheduler is new.  Agents that are off the new
	 * environment are removed from the environment.
	 *
	 * @param oldH Height of old environment
	 * @param oldW Width of old environment
	 */
	private void loadOldAgents(int oldH, int oldW) {
		// Add in-bounds old agents to the new scheduler and update new
		// constants
		// TODO: a way to keep old parameters for old agents?
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				Location currentPos = getLocation(x, y);
				ComplexAgent agent = (ComplexAgent) getAgent(currentPos);
				if (agent != null) {
					int theType = agent.getAgentType();
					agent.setConstants(agentData[theType], prodData[theType]);
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
		for (Agent a : new ArrayList<Agent>(getAgents())) {
			Location l = a.getPosition();
			if (l.x >= data.width || l.y >= data.height) {
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
		for (int x = 0; x < getHeight(); ++x) {
			for (int y = 0; y < getWidth(); ++y) {
				Location currentPos = getLocation(x, y);
				if (getDrop(currentPos) != null) {
					setFlag(currentPos, ComplexEnvironment.FLAG_FOOD, false);
					setFlag(currentPos, ComplexEnvironment.FLAG_STONE, false);
					setFlag(currentPos, ComplexEnvironment.FLAG_DROP, true);
				}
			}
		}
	}

	public int[] numAgentsStrat() {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		for(Agent a : getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
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

	public int[] numAgentsStrat(int agentType) {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		for(Agent a : getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
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


	@Override
	public synchronized void removeAgent(int x, int y) {
		super.removeAgent(x, y);

		Location l = getLocation(x, y);
		Agent a = getAgent(l);
		if (a != null)
			a.die();
	}

	@Override
	public synchronized void removeFood(int x, int y) {
		super.removeFood(x, y);
		Location l = getLocation(x, y);
		setFlag(l, FLAG_FOOD, false);
	}

	@Override
	public synchronized void removeStone(int x, int y) {
		super.removeStone(x, y);
		Location l = getLocation(x, y);
		setFlag(l, FLAG_STONE, false);
	}

	public void resetAgentInfo() {
		agentInfoVector.clear();
	}

	/** Sets the default mutable variables of each agent type. */
	public void setDefaultMutableAgentParam() {
		ComplexAgent.setDefaultMutableParams(agentData, prodData);
	}

	/**
	 * Flags locations as a food/stone/waste location. It does nothing if
	 * the square is already occupied (for example, setFlag((0,0),FOOD,true)
	 * does nothing when (0,0) is a stone
	 */
	@Override
	public void setFlag(org.cobweb.cobweb2.core.Location l, int flag, boolean state) {
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
	public void setFoodType(org.cobweb.cobweb2.core.Location l, int i) {
		foodarray[l.x][l.y] = i;
	}

	protected void setLocationBits(org.cobweb.cobweb2.core.Location l, int bits) {
		array.setLocationBits(l, bits);
	}

	@Override
	public boolean testFlag(org.cobweb.cobweb2.core.Location l, int flag) {
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
	@Override
	public synchronized void update(long tick) {

		commManager.decrementPersistence();
		commManager.unblockBroadcast();

		updateWaste();

		// for each agent type, we test to see if its deplete time step has
		// come, and if so deplete the food random
		// by the appropriate percentage

		for (int i = 0; i < data.getFoodTypes(); ++i) {
			if (foodData[i].depleteRate != 0.0f && foodData[i].growRate > 0
					&& (simulation.getTime() % foodData[i].depleteTime) == 0) {
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
	}

	/**
	 *
	 */
	private void updateWaste() {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				Location l = getLocation(x, y);
				if (testFlag(l, ComplexEnvironment.FLAG_DROP) == false)
					continue;
				Drop d = getDrop(l);
				if (!d.isActive(simulation.getTime())) {
					setFlag(l, ComplexEnvironment.FLAG_DROP, false);
					d.expire();
					setDrop(l, null); // consider deactivating
					// and not deleting
				}
			}
		}
	}

	@Override
	public boolean hasAgent(int x, int y) {
		return getAgent(getUserDefinedLocation(x, y)) != null;
	}

	@Override
	public Agent getAgent(int x, int y) {
		return getAgent(getUserDefinedLocation(x, y));
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

	private List<StatePlugin> plugins = new LinkedList<StatePlugin>();

	private Map<String, StateParameter> pluginMap = new HashMap<String, StateParameter>();

	public StateParameter getStateParameter(String name) {
		return pluginMap.get(name);
	}

	public boolean isPDenabled() {
		return data.prisDilemma;
	}

	public LocationDirection getAdjacent(LocationDirection location) {
		Direction direction = location.direction;
		int x = location.x + direction.x;
		int y = location.y + direction.y;

		if (data.wrapMap) {
			x = (x + getWidth()) % getWidth();
			boolean flip = false;
			if (y < 0) {
				y = -y - 1;
				flip = true;
			} else if (y >= getHeight()) {
				y = getHeight() * 2 - y - 1;
				flip = true;
			}
			if (flip) {
				x = (x + getWidth() / 2) % getWidth();
				direction = new Direction(-direction.x, -direction.y);
			}
		} else {
			if ( x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
				return null;
		}
		return new LocationDirection(new Location(x, y), direction);
	}

	public Location getAdjacent(Location location, Direction direction) {
		return getAdjacent(new LocationDirection(location, direction));
	}

	public LocationDirection getTurnRightPosition(LocationDirection location) {
		return new LocationDirection(location, new Direction(-location.direction.y, location.direction.x));
	}

	public LocationDirection getTurnLeftPosition(LocationDirection location) {
		return new LocationDirection(location, new Direction(location.direction.y, -location.direction.x));
	}

}
