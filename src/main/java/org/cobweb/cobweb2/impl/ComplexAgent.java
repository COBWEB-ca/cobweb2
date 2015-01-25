package org.cobweb.cobweb2.impl;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentListener;
import org.cobweb.cobweb2.core.AgentStatistics;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SeeInfo;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.broadcast.BroadcastPacket;
import org.cobweb.cobweb2.plugins.broadcast.CheaterBroadcast;
import org.cobweb.cobweb2.plugins.broadcast.FoodBroadcast;
import org.cobweb.util.RandomNoGenerator;

/**
 * TODO better comments
 *
 * <p>During each tick of a simulation, each ComplexAgent instance will
 * be used to call the tickNotification method.  This is done in the
 * TickScheduler.doTick private method.
 *
 * @see Agent
 * @see java.io.Serializable
 *
 */
public class ComplexAgent extends Agent implements Serializable {

	public ComplexAgentParams params;

	/** Prisoner's Dilemma */
	public boolean pdCheater; // The agent's action; 1 == cheater, else cooperator
	private boolean lastPDcheated; // Remember the opponent's move in the last game

	private double commInbox;

	private double commOutbox;

	/** IDs of bad agents. Cheaters, etc */
	private Collection<Integer> badAgentMemory;

	private double memoryBuffer;


	protected ComplexAgent breedPartner;

	// FIXME: AI should call asexBreed() instead of setting flag and agent doing so.
	private boolean shouldReproduceAsex;

	public AgentStatistics stats;

	// pregnancyPeriod is set value while pregPeriod constantly changes
	protected int pregPeriod;

	protected boolean pregnant = false;

	public static final int LOOK_DISTANCE = 4;

	public transient ComplexEnvironment environment;

	protected transient SimulationInternals simulation;

	public ComplexAgent(SimulationInternals sim) {
		this.simulation = sim;
	}

	private Controller controller;

	protected AgentListener getAgentListener() {
		return simulation.getAgentListener();
	}

	protected long getTime() {
		return simulation.getTime();
	}

	protected RandomNoGenerator getRandom() {
		return simulation.getRandom();
	}

	protected float calculateSimilarity(ComplexAgent other) {
		return simulation.getSimilarityCalculator().similarity(this, other);
	}

	@Override
	protected ComplexAgent createChildAsexual(LocationDirection location) {
		ComplexAgent child = new ComplexAgent(simulation);
		child.init(environment, location, this);
		return child;
	}

	private ComplexAgent createChildSexual(LocationDirection location, ComplexAgent otherParent) {
		ComplexAgent child = new ComplexAgent(simulation);
		child.init(environment, location, this, otherParent);
		return child;
	}

	/**
	 * Constructor with two parents
	 *
	 * @param pos spawn position
	 * @param parent1 first parent
	 * @param parent2 second parent
	 */
	protected void init(ComplexEnvironment env, LocationDirection pos, ComplexAgent parent1, ComplexAgent parent2) {
		environment = env;
		copyParams(parent1);
		controller =
				parent1.controller.createChildSexual(
						parent2.controller);

		// child's strategy is determined by its parents, it has a
		// 50% chance to get either parent's strategy
		if (getRandom().nextBoolean()) {
			params.pdCoopProb = parent2.params.pdCoopProb;
			params.pdTitForTat = parent2.params.pdTitForTat;
			params.pdSimilarityNeutral = parent2.params.pdSimilarityNeutral;
			params.pdSimilaritySlope = parent2.params.pdSimilaritySlope;
		} // else keep parent 1's PD config

		stats = environment.addAgentInfo(params.type, parent1.stats, parent2.stats);

		initPosition(pos);

		getAgentListener().onSpawn(this, parent1, parent2);

	}


	/**
	 * Constructor with a parent; standard asexual copy
	 *
	 * @param pos spawn position
	 * @param parent parent
	 */
	protected void init(ComplexEnvironment env, LocationDirection pos, ComplexAgent parent) {
		environment = (env);
		copyParams(parent);
		controller = parent.controller.createChildAsexual();

		stats = environment.addAgentInfo(params.type, parent.stats);

		initPosition(pos);

		getAgentListener().onSpawn(this, parent);
	}

	/**
	 * Constructor with no parent agent; creates an agent using "immaculate conception" technique
	 * @param pos spawn position
	 * @param agentData agent parameters
	 */
	public void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentParams agentData) {
		environment = (env);
		setParams(agentData);

		stats = environment.addAgentInfo(params.type);

		initPosition(pos);

		getAgentListener().onSpawn(this);
	}

	public void setController(Controller c) {
		this.controller = c;
	}

	private void afterTurnAction() {
		changeEnergy(-energyPenalty());
		if (!pregnant)
			tryAsexBreed();
		if (pregnant) {
			pregPeriod--;
		}
	}

	protected void broadcastCheating(ComplexAgent cheater) {
		environment.commManager.addPacketToList(new CheaterBroadcast(cheater, this));
		// new CommPacket sent
		changeEnergy(-params.broadcastEnergyCost); // Deduct broadcasting cost from energy
	}

	/**
	 * Creates a new communication packet.  The energy to broadcast is
	 * deducted here.
	 *
	 * @param loc The location of food.
	 */
	protected void broadcastFood(Location loc) {
		environment.commManager.addPacketToList(new FoodBroadcast(loc, this));
		// new CommPacket sent
		changeEnergy(-params.broadcastEnergyCost); // Deduct broadcasting cost from energy
	}

	/**
	 * @return True if agent has enough energy to broadcast
	 */
	protected boolean canBroadcast() {
		return enoughEnergy(params.broadcastEnergyMin);
	}

	/**
	 * @param destPos The location of the agents next position.
	 * @return True if agent can eat this type of food.
	 */
	public boolean canEat(Location destPos) {
		return params.foodweb.canEatFood[environment.getFoodType(destPos)];
	}

	/**
	 * @param adjacentAgent The agent attempting to eat.
	 * @return True if the agent can eat this type of agent.
	 */
	protected boolean canEat(ComplexAgent adjacentAgent) {
		boolean caneat = false;
		caneat = params.foodweb.canEatAgent[adjacentAgent.getType()];
		if (enoughEnergy(params.breedEnergy))
			caneat = false;

		return caneat;
	}

	/**
	 * @param destPos The location of the agents next position.
	 * @return True if location exists and is not occupied by anything
	 */
	protected boolean canStep(Location destPos) {
		// The position must be valid...
		if (destPos == null)
			return false;
		// and the destination must be clear of stones
		if (environment.hasStone(destPos))
			return false;
		// and clear of wastes
		if (environment.hasDrop(destPos))
			return environment.getDrop(destPos).canStep();
		// as well as other agents...
		if (environment.hasAgent(destPos))
			return false;
		return true;
	}

	protected boolean checkCredibility(ComplexAgent other) {
		return !badAgentMemory.contains(other.stats.id);
	}

	protected void communicate(ComplexAgent target) {
		target.setCommInbox(getCommOutbox());
	}

	private void copyParams(ComplexAgent p) {
		// Copies default constants for this agent type, not directly from agent
		setParams(environment.agentData[p.getType()]);
		pdCheater = p.pdCheater;
	}

	@Override
	public void die() {
		super.die();

		environment.setAgent(position, null);

		getAgentListener().onDeath(this);

		stats.setDeath(getTime());
	}

	/**
	 * This method allows the agent to see what is in front of it.
	 *
	 * @return What the agent sees and at what distance.
	 */
	public SeeInfo distanceLook() {
		LocationDirection destPos = environment.topology.getAdjacent(getPosition());

		for (int dist = 1; dist <= LOOK_DISTANCE; ++dist) {

			// We are looking at the wall
			if (destPos == null)
				return new SeeInfo(dist, Environment.FLAG_STONE);

			// Check for stone...
			if (environment.hasStone(destPos))
				return new SeeInfo(dist, Environment.FLAG_STONE);

			// If there's another agent there, then return that it's a stone...
			if (environment.hasAgent(destPos) && environment.getAgent(destPos) != this)
				return new SeeInfo(dist, Environment.FLAG_AGENT);

			// If there's food there, return the food...
			if (environment.hasFood(destPos))
				return new SeeInfo(dist, Environment.FLAG_FOOD);

			if (environment.hasDrop(destPos))
				return new SeeInfo(dist, Environment.FLAG_DROP);

			destPos = environment.topology.getAdjacent(destPos);
		}
		return new SeeInfo(LOOK_DISTANCE, 0);
	}

	/**
	 * The agent eats the food (food flag is set to false), and
	 * gains energy and waste according to the food type.
	 *
	 * @param destPos Location of food.
	 */
	public void eat(Location destPos) {
		// TODO: CHECK if setting flag before determining type is ok
		// Eat first before we can produce waste, of course.
		environment.removeFood(destPos);
		// Gain Energy according to the food type.
		if (environment.getFoodType(destPos) == params.type) {
			changeEnergy(+params.foodEnergy);
			stats.addFoodEnergy(params.foodEnergy);
		} else {
			changeEnergy(+params.otherFoodEnergy);
			stats.addOthers(params.otherFoodEnergy);
		}
	}

	/**
	 * The agent eats the adjacent agent by killing it and gaining
	 * energy from it.
	 *
	 * @param adjacentAgent The agent being eaten.
	 */
	protected void eat(ComplexAgent adjacentAgent) {
		int gain = (int) (adjacentAgent.getEnergy() * params.agentFoodEnergy);
		changeEnergy(+gain);
		stats.addCannibalism(gain);
		adjacentAgent.die();
	}

	public int energyPenalty() {
		if (!params.agingMode)
			return 0;
		double tempAge = getAge();
		int penaltyValue = Math.min(Math.max(0, getEnergy()), (int)(params.agingRate
				* (Math.tan(((tempAge / params.agingLimit) * 89.99) * Math.PI / 180))));

		return penaltyValue;
	}

	protected Agent getAdjacentAgent() {
		Location destPos = environment.topology.getAdjacent(getPosition());
		if (destPos == null) {
			return null;
		}
		return environment.getAgent(destPos);
	}

	public long getAge() {
		return getTime() - stats.birthTick;
	}

	public boolean getAgentPDActionCheat() {
		return pdCheater;
	}

	public double getCommInbox() {
		return commInbox;
	}

	public double getCommOutbox() {
		return commOutbox;
	}

	public AgentStatistics getInfo() {
		return stats;
	}

	public double getMemoryBuffer() {
		return memoryBuffer;
	}

	private void initPosition(LocationDirection pos) {
		if (pos.direction.equals(Topology.NONE))
			pos = new LocationDirection(pos, simulation.getTopology().getRandomDirection());
		move(pos);
		simulation.addAgent(this);
	}

	/**
	 * The agent will remember the last variable number of agents that
	 * cheated it.  How many cheaters it remembers is determined by its
	 * PD memory size.
	 */
	protected void iveBeenCheated(ComplexAgent cheater) {

		rememberCheater(cheater);

		broadcastCheating(cheater);
	}

	public void rememberCheater(ComplexAgent cheater) {
		if (cheater.equals(this)) // heh
			return;

		int othersID = cheater.stats.id;
		if (badAgentMemory.contains(othersID))
			return;

		badAgentMemory.add(othersID);
	}

	public void move(LocationDirection newPos) {
		environment.setAgent(newPos, this);
		if (position != null)
			environment.setAgent(position, null);
		position = newPos;

		stats.addPathStep(newPos);
	}

	/**
	 * This method initializes the agents actions in an iterated prisoner's
	 * dilemma game.  The agent can use the following strategies described
	 * by the agentPDStrategy integer:
	 *
	 * <p>0. Default
	 *
	 * <p>The agents decision to defect or cooperate is chosen randomly.
	 * The probability of choosing either is determined by the agents
	 * pdCoopProb parameter.
	 *
	 * <p>1. Tit for Tat
	 *
	 * <p>The agent will initially begin with a cooperate, but will then choose
	 * whatever the opposing agent chose last.  For example, the agent begins
	 * with a cooperate, but if the opposing agent has chosen to defect, then
	 * the agent will choose to defect next round.
	 *
	 */
	public void playPD(ComplexAgent other) {

		double coopProb = params.pdCoopProb / 100.0d;

		float similarity = calculateSimilarity(other);

		coopProb += (similarity - params.pdSimilarityNeutral) * params.pdSimilaritySlope;

		if (params.pdTitForTat) { // if true then agent is playing TitForTat
			pdCheater = lastPDcheated;
		} else {
			pdCheater = false; // agent is assumed to cooperate
			float rnd = getRandom().nextFloat();
			if (rnd > coopProb)
				pdCheater = true; // agent defects depending on
			// probability
		}

		return;
	}

	/**
	 *Prisoner's dilemma is played between the two agents using the strategies
	 *assigned in playPD().  The agent will use its PD memory to remember agents
	 *that cheat it, which will affect whether an agent will want to meet another,
	 *and its credibility.
	 *
	 *<p>How Prisoner's Dilemma is played:
	 *
	 *<p>Prisoner's dilemma is a game between two agents when they come in to
	 *contact with each other.  The game determines how much energy each agent
	 *receives after contact.  Each agent has two options: cooperate or defect.
	 *The agents choice to cooperate or defect is determined by the strategy the
	 *agent is using (see playPD() method).  The agents choices can lead to
	 *one of four outcomes:
	 *
	 *<p> 1. REWARD for mutual cooperation (Both agents cooperate)
	 *
	 *<p> 2. SUCKER's payoff (Opposing agent defects; this agent cooperates)
	 *
	 *<p> 3. TEMPTATION to defect (Opposing agent cooperates; this agent defects)
	 *
	 *<p> 4. PUNISHMENT for mutual defection (Both agents defect)
	 *
	 *<p>The best strategy for both agents is to cooperate.  However, if an agent
	 *chooses to defect when the other cooperates, the defecting agent will have
	 *a greater advantage.  For a true game of PD, the energy scores for each
	 *outcome should follow this rule: TEMPTATION > REWARD > PUNISHMENT > SUCKER
	 *
	 *<p>Here is an example of how much energy an agent could receive:
	 *<br> REWARD     =>     5
	 *<br> SUCKER     =>     2
	 *<br> TEMPTATION =>     8
	 *<br> PUNISHMENT =>     3
	 *
	 * @param adjacentAgent Agent playing PD with
	 * @param othersID ID of the adjacent agent.
	 * @see ComplexAgent#playPD(ComplexAgent)
	 * @see <a href="http://en.wikipedia.org/wiki/Prisoner's_dilemma">Prisoner's Dilemma</a>
	 */
	@SuppressWarnings("javadoc")
	public void playPDonStep(ComplexAgent adjacentAgent) {
		if (!environment.isPDenabled())
			return;

		playPD(adjacentAgent);
		adjacentAgent.playPD(this);

		// Save result for future strategy (tit-for-tat, learning, etc.)
		lastPDcheated = adjacentAgent.pdCheater;
		adjacentAgent.lastPDcheated = pdCheater;

		/*
		 * TODO LOW: The ability for the PD game to contend for the Get the food tiles immediately around each agents
		 */

		if (!pdCheater && !adjacentAgent.pdCheater) {
			/* Both cooperate */
			changeEnergy(+environment.data.pdParams.reward);
			adjacentAgent.changeEnergy(+environment.data.pdParams.reward);
			stats.addPDReward();

		} else if (!pdCheater && adjacentAgent.pdCheater) {
			/* Only other agent cheats */
			changeEnergy(+environment.data.pdParams.sucker);
			adjacentAgent.changeEnergy(+environment.data.pdParams.temptation);
			stats.addPDTemptation();

		} else if (pdCheater && !adjacentAgent.pdCheater) {
			/* Only this agent cheats */
			changeEnergy(+environment.data.pdParams.temptation);
			adjacentAgent.changeEnergy(+environment.data.pdParams.sucker);
			stats.addPDSucker();

		} else if (pdCheater && adjacentAgent.pdCheater) {
			/* Both cheat */
			changeEnergy(+environment.data.pdParams.punishment);
			adjacentAgent.changeEnergy(+environment.data.pdParams.punishment);
			stats.addPDPunishment();

		}

		if (adjacentAgent.pdCheater)
			iveBeenCheated(adjacentAgent);
	}

	protected void receiveBroadcast() {
		BroadcastPacket commPacket = environment.commManager.findPacket(getPosition(), this);

		if (commPacket == null)
			return;

		if (checkCredibility(commPacket.sender)) {
			commPacket.process(this);
		}
	}

	public void setShouldReproduceAsex(boolean asexFlag) {
		this.shouldReproduceAsex = asexFlag;
	}

	public void setCommInbox(double commInbox) {
		this.commInbox = commInbox;
	}

	protected void clearCommInbox() {
		commInbox = 0;
	}

	public void setCommOutbox(double commOutbox) {
		this.commOutbox = commOutbox;
	}

	/**
	 * Sets the complex agents parameters.
	 *
	 * @param agentData The ComplexAgentParams used for this complex agent.
	 */
	public void setParams(ComplexAgentParams agentData) {

		this.params = agentData.clone();

		setEnergy(agentData.initEnergy);

		badAgentMemory = new CircularFifoQueue<Integer>(params.pdMemory);

	}

	public void setMemoryBuffer(double memoryBuffer) {
		this.memoryBuffer = memoryBuffer;
	}

	/**
	 * During a step, the agent can encounter four different circumstances:
	 * 1. Nothing is in its way.
	 * 2. Contact with another agent.
	 * 3. Run into waste.
	 * 4. Run into a rock.
	 *
	 * <p> 1. Nothing in its way:
	 *
	 * <p>If the agent can move into the next position, the first thing it will do
	 * is check for food.  If it finds food, then the agent may
	 * broadcast a message containing the location of the food.  The agent may
	 * then eat the food.  If after eating the food the agent was pregnant, a check
	 * will be made to see if the child can be produced now.  If the agent was not
	 * pregnant, then a-sexual breeding will be attempted.
	 *
	 * <p>This method will then iterate through all  mutators used in the simulation
	 * and call onStep for each step mutator.  The agent will then move.  If it
	 * was found that the agent was ready to produce a child, then a new agent
	 * is created.
	 *
	 * <p> 2. Contact with another agent:
	 *
	 * <p> Contact mutators are iterated through and the onContact method is called
	 * for each used within the simulation.  The agent will eat the agent if it can.
	 *
	 * <p> If prisoner's dilemma is being used for this simulation, then a check is
	 * made to see if both agents want to meet each other (True if no bad memories of
	 * adjacent agent).  If the adjacent agent was not eaten and both agents want to
	 * meet each other, then the possibility of breeding will be looked in to.  If
	 * breeding is not possible, then prisoner's dilemma will be played.  If prisoner's
	 * dilemma is not used, then only breeding is checked for.
	 *
	 * <p> An energy penalty is deducted for bumping into another agent.
	 *
	 * <p> 3 and 4. Run into waste/rock:
	 *
	 * <p> Energy penalties are deducted from the agent.
	 */
	public void step() {
		Agent adjAgent;
		LocationDirection destPos = environment.topology.getAdjacent(getPosition());

		if (canStep(destPos)) {

			onstepFreeTile(destPos);

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgent
				&& ((ComplexAgent) adjAgent).stats != null) {
			// two agents meet

			ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;


			onstepAgentBump(adjacentAgent);

		} // end of two agents meet
		else {
			// Non-free tile (rock/waste/etc) bump
			changeEnergy(-params.stepRockEnergy);
			stats.useRockBumpEnergy(params.stepRockEnergy);
		}
		changeEnergy(-energyPenalty());

		if (destPos != null && environment.hasDrop(destPos)) {
			// Bumps into drop
			Drop d = environment.getDrop(destPos);

			if (d.canStep()) {
				d.onStep(this);
			}
			else {
				// can't step, treat as obstacle
				stats.useRockBumpEnergy(params.stepRockEnergy);
			}
		}

		if (getEnergy() <= 0)
			die();

		if (!enoughEnergy(params.breedEnergy)) {
			pregnant = false;
			breedPartner = null;
		}

		if (pregnant) {
			pregPeriod--;
		}
	}

	protected void onstepFreeTile(LocationDirection destPos) {
		// Check for food...
		if (environment.hasFood(destPos)) {
			if (params.broadcastMode && canBroadcast()) {
				broadcastFood(destPos);
			}
			if (canEat(destPos)) {
				eat(destPos);
			}
		}

		LocationDirection breedPos = null;
		if (pregnant && enoughEnergy(params.breedEnergy) && pregPeriod <= 0) {
			breedPos = new LocationDirection(getPosition());
		} else if (!pregnant) {
			tryAsexBreed();
		}

		getAgentListener().onStep(this, getPosition(), destPos);

		move(destPos);

		if (breedPos != null) {
			changeEnergy(-params.initEnergy);
			changeEnergy(-energyPenalty());
			stats.useReproductionEnergy(params.initEnergy);
			stats.addDirectChild();

			if (breedPartner == null) {
				createChildAsexual(breedPos);
			} else {
				createChildSexual(breedPos, breedPartner);
			}
			breedPartner = null;
			pregnant = false;
		}
		changeEnergy(-params.stepEnergy);
		stats.useStepEnergy(params.stepEnergy);
	}

	protected void onstepAgentBump(ComplexAgent adjacentAgent) {
		getAgentListener().onContact(this, adjacentAgent);

		if (canEat(adjacentAgent)) {
			eat(adjacentAgent);
		}

		// if the agents are of the same type, check if they have enough
		// resources to breed
		if (adjacentAgent.params.type == params.type) {

			double sim = 0.0;
			boolean canBreed = !pregnant && enoughEnergy(params.breedEnergy) && params.sexualBreedChance != 0.0
					&& getRandom().nextFloat() < params.sexualBreedChance;

			// Generate genetic similarity number
			sim = calculateSimilarity(adjacentAgent);

			if (sim >= params.commSimMin) {
				communicate(adjacentAgent);
			}

			if (canBreed && sim >= params.breedSimMin
					&& checkCredibility(adjacentAgent) && adjacentAgent.checkCredibility(this)) {
				pregnant = true;
				pregPeriod = params.sexualPregnancyPeriod;
				breedPartner = adjacentAgent;
			}
		}

		if (!pregnant && checkCredibility(adjacentAgent) && adjacentAgent.checkCredibility(this)) {

			playPDonStep(adjacentAgent);
		}
		changeEnergy(-params.stepAgentEnergy);
		stats.useAgentBumpEnergy(params.stepAgentEnergy);
	}

	/**
	 * Controls what happens to the agent on this tick.  If the
	 * agent is still alive, what happens to the agent is determined
	 * by the controller.
	 */
	@Override
	public void update() {
		if (!isAlive())
			return;

		/* Time to die, Agent (mister) Bond */
		if (params.agingMode) {
			if ((getAge()) >= params.agingLimit) {
				die();
				return;
			}
		}

		/* Check if broadcasting is enabled */
		if (params.broadcastMode)
			receiveBroadcast();

		controller.controlAgent(this);

		clearCommInbox();
	}

	/**
	 * If the agent has enough energy to breed, is randomly chosen to breed,
	 * and its shouldReproduceAsex is true, then the agent will be pregnant and set to
	 * produce a child agent after the agent's asexPregnancyPeriod is up.
	 */
	protected void tryAsexBreed() {
		if (shouldReproduceAsex && enoughEnergy(params.breedEnergy) && params.asexualBreedChance != 0.0
				&& getRandom().nextFloat() < params.asexualBreedChance) {
			pregPeriod = params.asexPregnancyPeriod;
			pregnant = true;
		}
	}

	/**
	 * This method makes the agent turn left.  It does this by updating
	 * the direction of the agent and subtracts the amount of
	 * energy it took to turn.
	 */
	public void turnLeft() {
		position = environment.topology.getTurnLeftPosition(position);
		changeEnergy(-params.turnLeftEnergy);
		stats.addTurn(params.turnLeftEnergy);
		afterTurnAction();
	}

	/**
	 * This method makes the agent turn right.  It does this by updating
	 * the direction of the agent subtracts the amount of energy it took
	 * to turn.
	 */
	public void turnRight() {
		position = environment.topology.getTurnRightPosition(position);
		changeEnergy(-params.turnRightEnergy);
		stats.addTurn(params.turnRightEnergy);
		afterTurnAction();
	}


	@Override
	public int getType() {
		return params.type;
	}

	@Override
	public void changeEnergy(int delta) {
		super.changeEnergy(delta);
		getAgentListener().onEnergyChange(this, delta);
	}

	private static final long serialVersionUID = 2L;
}
