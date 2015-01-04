package org.cobweb.cobweb2.core;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ai.ControllerFactory;
import org.cobweb.cobweb2.broadcast.PacketConduit;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.core.params.ComplexEnvironmentParams;
import org.cobweb.cobweb2.food.Food;
import org.cobweb.util.ArrayUtilities;

/**
 * 2D grid where agents and food live
 */
public class ComplexEnvironment extends Environment implements Updatable {

	protected ComplexAgentParams agentData[];

	public void addDrop(Location loc, Drop d) {
		if (hasFood(loc)) {
			removeFood(loc);
		}

		setFlag(loc, Environment.FLAG_DROP, true);

		dropArray[loc.x][loc.y] = d;
	}

	public void removeDrop(Location loc) {
		setFlag(loc, FLAG_DROP, false);
		dropArray[loc.x][loc.y] = null;
	}

	public Drop getDrop(Location loc) {
		return dropArray[loc.x][loc.y];
	}

	public boolean hasDrop(Location loc) {
		return testFlag(loc, FLAG_DROP);
	}

	public final List<ComplexAgentStatistics> agentInfoVector = new ArrayList<ComplexAgentStatistics>();

	public ComplexEnvironmentParams data = new ComplexEnvironmentParams();

	/*
	 * Waste tile array to store the data per waste tile. Needed to allow
	 * depletion of waste
	 */
	public Drop[][] dropArray;

	public PacketConduit commManager;

	public Food foodManager;

	public ComplexEnvironment(SimulationInternals simulation) {
		super(simulation);
		commManager = new PacketConduit();
		foodManager = new Food(simulation);
	}

	public ControllerFactory controllerFactory;

	public synchronized void addAgent(Location l, int type) {
		if (!hasAgent(l) && !hasStone(l) && !hasDrop(l)) {
			int agentType = type;

			spawnAgent(new LocationDirection(l), agentType);

		}
	}

	protected void spawnAgent(LocationDirection location, int agentType) {
		ComplexAgent child = simulation.newAgent();
		child.init(this, location, (ComplexAgentParams) agentData[agentType].clone());
	}

	ComplexAgentStatistics addAgentInfo(ComplexAgentStatistics info) {
		agentInfoVector.add(info);
		return info;
	}

	private int makeNextAgentID() {
		return agentInfoVector.size();
	}

	ComplexAgentStatistics addAgentInfo(int agentT, ComplexAgentStatistics p1, ComplexAgentStatistics p2) {
		return addAgentInfo(new ComplexAgentStatistics(makeNextAgentID(), agentT, simulation.getTime(), p1, p2));
	}

	ComplexAgentStatistics addAgentInfo(int agentT, ComplexAgentStatistics p1) {
		return addAgentInfo(new ComplexAgentStatistics(makeNextAgentID(), agentT, simulation.getTime(), p1));
	}

	ComplexAgentStatistics addAgentInfo(int agentT) {
		return addAgentInfo(new ComplexAgentStatistics(makeNextAgentID(), agentT, simulation.getTime()));
	}

	@Override
	public synchronized void clearAgents() {
		super.clearAgents();
		agentInfoVector.clear();
	}

	public synchronized void clearWaste() {
		clearFlag(Environment.FLAG_DROP);
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

		agentData = p.getAgentParams();
	}

	/* Return the number of agents (int) for a certain agentType (int) */
	public int countAgents(int agentType) {
		int agentCount = 0;
		for(Agent a : getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
			if (agent.getType() == agentType)
				agentCount++;
		}
		return agentCount;
	}

	/* Return foodCount (long) of all types of food */
	public long countFoodTiles() {
		long foodCount = 0;

		for (int x = 0; x < topology.width; ++x) {
			for (int y = 0; y < topology.height; ++y) {
				Location currentPos = new Location(x, y);
				if (hasFood(currentPos))
					++foodCount;
			}
		}
		return foodCount;
	}

	/* Return foodCount (long) for a specific foodType (int) */
	public int countFoodTiles(int foodType) {
		int foodCount = 0;
		for (int x = 0; x < topology.width; ++x) {
			for (int y = 0; y < topology.height; ++y) {
				Location currentPos = new Location(x, y);
				if (hasFood(currentPos))
					if (getFoodType(currentPos) == foodType)
						++foodCount;
			}
		}
		return foodCount;
	}

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
	public synchronized void load(SimulationConfig config) throws IllegalArgumentException {
		int oldH = data.height;
		int oldW = data.width;
		/**
		 * the first parameter must be the number of agent types as we must
		 * allocate space to store the parameter for each type
		 */

		copyParamsFromParser(config);
		super.load(data.width, data.height, data.wrapMap, data.keepOldArray);

		foodManager.load(this, data.dropNewFood, data.likeFoodProb, config.getFoodParams());

		if (dropArray == null || !data.keepOldWaste) {
			loadNewWaste();
		} else {
			loadOldWaste();
		}

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
			Location l;
			int tries = 0;
			do {
				l = topology.getRandomLocation();
			} while (tries++ < 100 && (hasStone(l) || hasDrop(l) || hasAgent(l)));
			if (tries < 100)
				addStone(l);
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

	}

	/**
	 * Places agents in random locations.  If prisoner's dilemma is being used,
	 * a number of cheaters that is dependent on the probability of being a cheater
	 * are randomly assigned to agents.
	 */
	private void loadNewAgents() {
		for (int i = 0; i < data.getAgentTypes(); ++i) {

			for (int j = 0; j < agentData[i].initialAgents; ++j) {

				Location location;
				int tries = 0;
				do {
					location = topology.getRandomLocation();
				} while (tries++ < 100 && (hasAgent(location) || hasStone(location) || hasDrop(location)));
				if (tries < 100) {
					int agentType = i;
					spawnAgent(new LocationDirection(location), agentType);
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
		for (int x = 0; x < topology.width; ++x) {
			for (int y = 0; y < topology.height; ++y) {
				Location loc = new Location(x, y);
				if (hasDrop(loc))
					removeDrop(loc);
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
		for (int x = 0; x < topology.width; ++x) {
			for (int y = 0; y < topology.height; ++y) {
				Location currentPos = new Location(x, y);
				ComplexAgent agent = (ComplexAgent) getAgent(currentPos);
				if (agent != null) {
					int theType = agent.getType();
					agent.setConstants(agentData[theType]);
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
		dropArray = ArrayUtilities.resizeArray(dropArray, topology.width, topology.height);
	}

	public synchronized void removeAgent(Location l) {
		Agent a = getAgent(l);
		if (a != null)
			a.die();
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

		foodManager.update(tick);
	}

	/**
	 *
	 */
	private void updateWaste() {
		for (int x = 0; x < topology.width; x++) {
			for (int y = 0; y < topology.height; y++) {
				Location l = new Location(x, y);
				if (!hasDrop(l))
					continue;
				Drop d = getDrop(l);
				if (!d.isActive(simulation.getTime())) {
					d.expire();
					removeDrop(l);
				}
			}
		}
	}

	public boolean isPDenabled() {
		return data.prisDilemma;
	}

}
