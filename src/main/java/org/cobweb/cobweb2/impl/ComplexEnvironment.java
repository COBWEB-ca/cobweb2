package org.cobweb.cobweb2.impl;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentStatistics;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.plugins.broadcast.PacketConduit;
import org.cobweb.cobweb2.plugins.food.FoodGrowth;

/**
 * 2D grid where agents and food live
 */
public class ComplexEnvironment extends Environment {

	protected ComplexAgentParams agentData[];

	public final List<AgentStatistics> agentInfoVector = new ArrayList<AgentStatistics>();

	public ComplexEnvironmentParams data = new ComplexEnvironmentParams();

	public PacketConduit commManager;

	private FoodGrowth foodManager;

	public ComplexEnvironment(SimulationInternals simulation) {
		super(simulation);
		commManager = new PacketConduit();
		foodManager = new FoodGrowth(simulation);
	}

	public synchronized void addAgent(Location l, int type) {
		if (!hasAgent(l) && !hasStone(l) && !hasDrop(l)) {
			int agentType = type;

			spawnAgent(new LocationDirection(l), agentType);

		}
	}

	protected Agent spawnAgent(LocationDirection location, int agentType) {
		ComplexAgent child = (ComplexAgent) simulation.newAgent(agentType);
		ComplexAgentParams params = agentData[agentType];
		child.init(this, location, params);
		return child;
	}

	private AgentStatistics addAgentInfo(AgentStatistics info) {
		agentInfoVector.add(info);
		return info;
	}

	private int makeNextAgentID() {
		return agentInfoVector.size();
	}

	AgentStatistics addAgentInfo(int agentT, AgentStatistics p1, AgentStatistics p2) {
		return addAgentInfo(new AgentStatistics(makeNextAgentID(), agentT, simulation.getTime(), p1, p2));
	}

	AgentStatistics addAgentInfo(int agentT, AgentStatistics p1) {
		return addAgentInfo(new AgentStatistics(makeNextAgentID(), agentT, simulation.getTime(), p1));
	}

	AgentStatistics addAgentInfo(int agentT) {
		return addAgentInfo(new AgentStatistics(makeNextAgentID(), agentT, simulation.getTime()));
	}

	@Override
	public synchronized void clearAgents() {
		super.clearAgents();
		agentInfoVector.clear();
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
		data = config.getEnvParams();
		agentData = config.getAgentParams();

		super.load(data.width, data.height, data.wrapMap, data.keepOldArray);

		// Remove old components
		loadExisting();

		loadNew(config);
	}

	protected void loadExisting() {
		removeOffgridAgents();

		if (data.keepOldAgents) {
			loadOldAgents();
		} else {
			clearAgents();
		}

		if (!data.keepOldPackets) {
			commManager.clearPackets();
		}
	}

	protected void loadNew(SimulationConfig config) {
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

		foodManager.load(this, data.dropNewFood, data.likeFoodProb, config.getFoodParams());

		if (!data.keepOldWaste) {
			loadNewWaste();
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
	 */
	private void loadOldAgents() {
		// Add in-bounds old agents to the new scheduler and update new
		// constants
		// TODO: a way to keep old parameters for old agents?
		for (int x = 0; x < topology.width; ++x) {
			for (int y = 0; y < topology.height; ++y) {
				Location currentPos = new Location(x, y);
				ComplexAgent agent = (ComplexAgent) getAgent(currentPos);
				if (agent != null) {
					int theType = agent.getType();
					agent.setParams(agentData[theType]);
				}
			}
		}

		removeOffgridAgents();
	}

	/**
	 * tickNotification is the method called by the scheduler for each of its
	 * clients for every tick of the simulation. For environment,
	 * tickNotification performs all of the per-tick tasks necessary for the
	 * environment to function properly. These tasks include managing food
	 * depletion, food growth, and random food-"dropping".
	 */
	@Override
	public synchronized void update() {
		super.update();

		commManager.update();

		updateWaste();

		foodManager.update();
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
