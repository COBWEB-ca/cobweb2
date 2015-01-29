package org.cobweb.cobweb2.impl;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentListener;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.broadcast.BroadcastPacket;
import org.cobweb.cobweb2.plugins.broadcast.CheaterBroadcast;
import org.cobweb.cobweb2.plugins.broadcast.FoodBroadcast;
import org.cobweb.cobweb2.plugins.broadcast.PacketConduit;
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
	private Collection<Agent> badAgentMemory;

	private double memoryBuffer;


	protected ComplexAgent breedPartner;

	// FIXME: AI should call asexBreed() instead of setting flag and agent doing so.
	private boolean shouldReproduceAsex;

	// pregnancyPeriod is set value while pregPeriod constantly changes
	protected int pregPeriod;

	protected boolean pregnant = false;

	public transient ComplexEnvironment environment;

	protected transient SimulationInternals simulation;

	long birthTick;

	public ComplexAgent(SimulationInternals sim, int type) {
		super(type);
		this.simulation = sim;
		this.birthTick = getTime();
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
		ComplexAgent child = new ComplexAgent(simulation, getType());
		child.init(environment, location, this);
		return child;
	}

	private ComplexAgent createChildSexual(LocationDirection location, ComplexAgent otherParent) {
		ComplexAgent child = new ComplexAgent(simulation, getType());
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

		getAgentListener().onSpawn(this, parent1, parent2);

		initPosition(pos);
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

		getAgentListener().onSpawn(this, parent);

		initPosition(pos);
	}

	/**
	 * Constructor with no parent agent; creates an agent using "immaculate conception" technique
	 * @param pos spawn position
	 * @param agentData agent parameters
	 */
	public void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentParams agentData) {
		environment = (env);
		setParams(agentData);

		getAgentListener().onSpawn(this);

		initPosition(pos);
	}

	public void setController(Controller c) {
		this.controller = c;
	}

	/**
	 * Sends out given broadcast
	 */
	public void broadcast(BroadcastPacket packet) {
		//TODO move to plugin?
		environment.commManager.addPacketToList(packet);

		changeEnergy(-params.broadcastEnergyCost, new PacketConduit.BroadcastCause());
	}

	/**
	 * @return True if agent has enough energy to broadcast
	 */
	protected boolean canBroadcast() {
		return params.broadcastMode && enoughEnergy(params.broadcastEnergyMin);
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

	protected boolean isAgentGood(ComplexAgent other) {
		if (!badAgentMemory.contains(other))
			return true;

		// Refresh memory
		rememberCheater(other);
		return false;
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
		move(null);

		super.die();

		getAgentListener().onDeath(this);

		// Release references to other agents
		badAgentMemory.clear();
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
		if (environment.getFoodType(destPos) == getType()) {
			changeEnergy(+params.foodEnergy, new EatFavoriteFoodCause());
		} else {
			changeEnergy(+params.otherFoodEnergy, new EatFoodCause());
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
		changeEnergy(+gain, new EatAgentCause());
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

	public static class AgingPenaltyCause implements Cause {@Override
		public String getName() { return "Aging Penalty"; }
	}

	protected Agent getAdjacentAgent() {
		Location destPos = environment.topology.getAdjacent(getPosition());
		if (destPos == null) {
			return null;
		}
		return environment.getAgent(destPos);
	}

	public long getAge() {
		return getTime() - birthTick;
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

		broadcast(new CheaterBroadcast(cheater, this));
	}

	public void rememberCheater(ComplexAgent cheater) {
		if (cheater.equals(this)) // heh
			return;

		// Moves cheater to the more recent end of memory
		badAgentMemory.remove(cheater);
		badAgentMemory.add(cheater);
	}

	public void move(LocationDirection newPos) {
		LocationDirection oldPos = getPosition();

		if (oldPos != null)
			environment.setAgent(oldPos, null);

		if (newPos != null)
			environment.setAgent(newPos, this);

		position = newPos;

		getAgentListener().onStep(this, oldPos, newPos);
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
			changeEnergy(+environment.data.pdParams.reward, new PDRewardCause());
			adjacentAgent.changeEnergy(+environment.data.pdParams.reward, new PDRewardCause());

		} else if (!pdCheater && adjacentAgent.pdCheater) {
			/* Only other agent cheats */
			changeEnergy(+environment.data.pdParams.sucker, new PDSuckerCause());
			adjacentAgent.changeEnergy(+environment.data.pdParams.temptation, new PDTemptationCause());

		} else if (pdCheater && !adjacentAgent.pdCheater) {
			/* Only this agent cheats */
			changeEnergy(+environment.data.pdParams.temptation, new PDTemptationCause());
			adjacentAgent.changeEnergy(+environment.data.pdParams.sucker, new PDSuckerCause());

		} else if (pdCheater && adjacentAgent.pdCheater) {
			/* Both cheat */
			changeEnergy(+environment.data.pdParams.punishment, new PDPunishmentCause());
			adjacentAgent.changeEnergy(+environment.data.pdParams.punishment, new PDPunishmentCause());

		}

		if (adjacentAgent.pdCheater)
			iveBeenCheated(adjacentAgent);
	}

	public static abstract class PDCause implements Cause {
		@Override
		public String getName() { return "PD"; }
	}
	public static class PDRewardCause extends PDCause {
		@Override
		public String getName() { return "PD Reward"; }
	}
	public static class PDTemptationCause extends PDCause {
		@Override
		public String getName() { return "PD Temptation"; }
	}
	public static class PDSuckerCause extends PDCause {
		@Override
		public String getName() { return "PD Sucker"; }
	}
	public static class PDPunishmentCause extends PDCause {
		@Override
		public String getName() { return "PD Punishment"; }
	}

	protected void receiveBroadcast() {
		BroadcastPacket commPacket = environment.commManager.findPacket(getPosition(), this);

		if (commPacket == null)
			return;

		if (isAgentGood(commPacket.sender)) {
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

		badAgentMemory = new CircularFifoQueue<Agent>(params.pdMemory);

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

		// FIXME: clean this up, split into step-wall, step-agent, step-food functions
		if (canStep(destPos)) {

			onstepFreeTile(destPos);

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgent) {
			// two agents meet

			ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;


			onstepAgentBump(adjacentAgent);

		} // end of two agents meet
		else {
			// Non-free tile (rock/waste/etc) bump
			changeEnergy(-params.stepRockEnergy, new BumpWallCause());
		}
		applyAgePenalty();

		if (destPos != null && environment.hasDrop(destPos)) {
			// Bumps into drop
			Drop d = environment.getDrop(destPos);

			if (d.canStep()) {
				d.onStep(this);
			}
			else {
				// can't step, treat as obstacle
				changeEnergy(-params.stepRockEnergy, new BumpWallCause());
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
			if (canBroadcast()) {
				broadcast(new FoodBroadcast(destPos, this));
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

		move(destPos);

		if (breedPos != null) {

			ReproductionCause cause = null;
			if (breedPartner == null) {
				createChildAsexual(breedPos);
				cause = new AsexualReproductionCause();
			} else {
				createChildSexual(breedPos, breedPartner);
				cause = new SexualReproductionCause();
			}
			changeEnergy(-params.initEnergy, cause);
			applyAgePenalty();
			breedPartner = null;
			pregnant = false;
		}
		changeEnergy(-params.stepEnergy, new StepForwardCause());
	}

	protected void onstepAgentBump(ComplexAgent adjacentAgent) {
		getAgentListener().onContact(this, adjacentAgent);
		changeEnergy(-params.stepAgentEnergy, new BumpAgentCause());

		if (canEat(adjacentAgent)) {
			eat(adjacentAgent);
		}

		if (!adjacentAgent.isAlive())
			return;

		// if the agents are of the same type, check if they have enough
		// resources to breed
		if (adjacentAgent.getType() == getType()) {

			double sim = 0.0;
			boolean canBreed = !pregnant && enoughEnergy(params.breedEnergy) && params.sexualBreedChance != 0.0
					&& getRandom().nextFloat() < params.sexualBreedChance;

			// Generate genetic similarity number
			sim = calculateSimilarity(adjacentAgent);

			if (sim >= params.commSimMin) {
				communicate(adjacentAgent);
			}

			if (canBreed && sim >= params.breedSimMin
					&& isAgentGood(adjacentAgent) && adjacentAgent.isAgentGood(this)) {
				pregnant = true;
				pregPeriod = params.sexualPregnancyPeriod;
				breedPartner = adjacentAgent;
			}
		}

		if (!pregnant && isAgentGood(adjacentAgent) && adjacentAgent.isAgentGood(this)) {

			playPDonStep(adjacentAgent);
		}
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
		changeEnergy(-params.turnLeftEnergy, new TurnLeftCause());
		afterTurnAction();
	}

	/**
	 * This method makes the agent turn right.  It does this by updating
	 * the direction of the agent subtracts the amount of energy it took
	 * to turn.
	 */
	public void turnRight() {
		position = environment.topology.getTurnRightPosition(position);
		changeEnergy(-params.turnRightEnergy, new TurnRightCause());
		afterTurnAction();
	}

	private void afterTurnAction() {
		applyAgePenalty();
		if (!pregnant)
			tryAsexBreed();
		if (pregnant) {
			pregPeriod--;
		}
	}

	protected void applyAgePenalty() {
		int penalty = energyPenalty();
		if (penalty > 0)
			changeEnergy(-penalty, new AgingPenaltyCause());
	}

	@Override
	public void changeEnergy(int delta, Cause cause) {
		super.changeEnergy(delta, cause);
		getAgentListener().onEnergyChange(this, delta, cause);
	}

	public static abstract class MovementCause implements Cause {
		@Override
		public String getName() { return "Movement"; }
	}

	public static class StepForwardCause extends MovementCause {
		@Override
		public String getName() { return "Step Forward"; }
	}

	public static abstract class TurnCause extends MovementCause {
		@Override
		public String getName() { return "Turn"; }
	}

	public static class TurnLeftCause extends TurnCause {
		@Override
		public String getName() { return "Turn Left"; }
	}

	public static class TurnRightCause extends TurnCause {
		@Override
		public String getName() { return "Turn Right"; }
	}

	public static abstract class EatCause implements Cause {
		@Override
		public String getName() { return "Eat"; }
	}

	public static class EatFoodCause extends EatCause {
		@Override
		public String getName() { return "Eat Food"; }
	}

	public static class EatFavoriteFoodCause extends EatCause {
		@Override
		public String getName() { return "Eat Favorite Food"; }
	}

	public static class EatAgentCause extends EatCause {
		@Override
		public String getName() { return "Eat Agent"; }
	}

	public static abstract class BumpCause implements Cause {
		@Override
		public String getName() { return "Bump"; }
	}

	public static class BumpWallCause extends BumpCause {
		@Override
		public String getName() { return "Bump Wall"; }
	}

	public static class BumpAgentCause extends BumpCause {
		@Override
		public String getName() { return "Bump Agent"; }
	}

	public static abstract class ReproductionCause implements Cause {
		@Override
		public String getName() { return "Reproduction"; }
	}

	public static class SexualReproductionCause extends ReproductionCause {
		@Override
		public String getName() { return "Sexual Reproduction"; }
	}

	public static class AsexualReproductionCause extends ReproductionCause {
		@Override
		public String getName() { return "Asexual Reproduction"; }
	}

	private static final long serialVersionUID = 2L;
}
