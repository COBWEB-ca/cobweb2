package cobweb;

import java.awt.Color;

import cobweb.Environment.Location;
import cwcore.ComplexAgentInfo;
import cwcore.ComplexEnvironment;
import cwcore.Food;
import cwcore.complexParams.ComplexAgentParams;

/**
 * The Agent class represents the physical notion of an Agent in a simulation 
 * (living or not, location, colour).  Instances of the Agent class are not 
 * responsible for implementing the intelligence of the Agent, this is deferred 
 * to the Agent.Controller class.
 */
public abstract class Agent extends CellObject {

	/**
	 * Cannot place anything on top of an agent.
	 * @param other Another cell object.
	 * @return False.
	 */
	@Override
	public final boolean canCoverWith(CellObject other) {
		return false;
	}

	/**
	 * The possible actions for this agent.
	 */
	protected enum Action {
		TURN_RIGHT,
		TURN_LEFT,
		MOVE_STRAIGHT
	}

	/**
	 * The unique identifier for the next agent.
	 */
	private static long nextID = 1;

	/**
	 * The unique identifier for this agent
	 */
	protected long id;

	/**
	 * @return The unique identifier for this agent.
	 */
	public long getID() {
		return id;
	}

	/**
	 * Create a unique identifier for an agent and return it.
	 * @return The unique identifier for the agent.
	 */
	private static long makeID() {
		if ((nextID + 1) == Long.MAX_VALUE) {
			nextID = Long.MIN_VALUE;
		}
		return nextID++;
	}

	/** 
	 * The current tick we are in (or the last tick this agent was notified 
	 */
	protected long currTick = 0;

	/**
	 * Reset the id of this agent.
	 */
	public static void resetIDSequence() {
		nextID = 1;
	}

	/**
	 * True if asexual reproduction is an option.
	 */
	protected boolean asexFlag;

	/**
	 * pregnancyPeriod is set value while pregPeriod constantly changes
	 */
	protected int pregPeriod;

	/**
	 * Colour of the agent.
	 */
	private Color color = Color.lightGray;

	/**
	 * True if agent is alive.
	 */
	private boolean alive = true;

	/**
	 * Environment agent is located in.
	 */
	public ComplexEnvironment environment;

	/**
	 * Agent parameters.
	 */
	public ComplexAgentParams params;

	/**
	 * default parameters.
	 */
	private static ComplexAgentParams defaultParams [];

	/**
	 * Agent info
	 */
	protected ComplexAgentInfo info;

	/**
	 * The tick in which this agent was born.
	 */
	protected long birthTick;

	/**
	 * The number of ticks this agent has lived.
	 */
	protected long age;

	/**
	 * The agent's type.
	 */
	protected int agentType = 0;

	/**
	 * The agent's remaining energy.
	 */
	protected int energy;

	/**
	 * Accumulated waste gain.
	 */
	protected int wasteCounterGain;

	/**
	 * Accumulated waste loss.
	 */
	protected int wasteCounterLoss;

	/**
	 * Whether the agent is pregnant or not.
	 */
	protected boolean pregnant = false;

	/**
	 * The square of the maximum seeing distance.
	 */
	public static final int MAX_SEE_SQUARE_DIST = 16;

	/**
	 * True if the agent is currently in motion.
	 */
	protected boolean isMoving = false;

	/**
	 * The direction in which the agent is facing.
	 */
	protected Direction facing;

	/**
	 * Set to true when an agent has eaten in that tick, false otherwise.
	 * Reset every tick.
	 */
	protected boolean hasEaten = false;

	/**
	 * Return the direction the agent is currently facing.
	 * @return The direction the agent is currently facing.
	 */
	public final Direction getFacing() {
		return this.facing;
	}

	/**
	 * Age this agent by 1 tick.
	 */
	public final void age() {
		this.age++;
	}

	/**
	 * Return the agent's age.
	 * @return The agent's age.
	 */
	public final long getAge() {
		return this.age;
	}

	/**
	 * Create a new agent.
	 */
	protected Agent() {
		id = makeID();
	}

	/** Sets the default mutable parameters of each agent type. */
	public static void setDefaultMutableParams(ComplexAgentParams[] params) {
		defaultParams = params.clone();
		for (int i = 0; i < params.length; i++) {
			defaultParams[i] = (ComplexAgentParams) params[i].clone();
		}
	}

	/**
	 * Sets the agents parameters.
	 * 
	 * @param agentData The ComplexAgentParams used for this complex agent.
	 */
	public void setConstants(ComplexAgentParams agentData) {

		this.params = agentData;

		this.agentType = agentData.type;

		energy = agentData.initEnergy;
		wasteCounterGain = params.wasteLimitGain;
		setWasteCounterLoss(params.wasteLimitLoss);

	}

	/**
	 * @param wasteCounterLoss waste counter loss.
	 */
	public void setWasteCounterLoss(int wasteCounterLoss) {
		this.wasteCounterLoss = wasteCounterLoss;
	}

	/**
	 * Copies the parameters from an Agent to be used for this
	 * agent
	 * 
	 * @param p Agent parameters copied.
	 */
	public void copyConstants(Agent p) {
		setConstants((ComplexAgentParams) defaultParams[p.getAgentType()].clone());
	}

	/**
	 * @param destPos The location of the agents next position.
	 * @return True if agent can eat this type of food.
	 */
	public boolean canEat(cobweb.Environment.Location destPos) {
		return params.foodweb.canEatFood[environment.getFoodType(destPos)];
	}

	/**
	 * @param adjacentAgent The agent attempting to eat.
	 * @return True if the agent can eat this type of agent.
	 */
	protected boolean canEat(Agent adjacentAgent) {
		boolean caneat = false;
		caneat = params.foodweb.canEatAgent[adjacentAgent.getAgentType()];
		if (this.energy > params.breedEnergy)
			caneat = false;

		return caneat;
	}

	/**
	 * The agent eats the adjacent agent by killing it and gaining 
	 * energy from it.
	 * 
	 * @param adjacentAgent The agent being eaten.
	 */
	protected void eat(Agent adjacentAgent) {
		int gain = (int) (adjacentAgent.energy * params.agentFoodEnergy);
		energy += gain;
		wasteCounterGain -= gain;
		info.addCannibalism(gain);
		adjacentAgent.die();
	}

	/**
	 * The agent will eat and, as a result, will gain energy from 
	 * the food if it has not eaten in this tick already.
	 * 
	 * @param food Food object being eaten.
	 */
	protected void eat(Food food) {
		//agent can only eat once per turn
		if(!this.hasEaten) 
			// Eat first before we can produce waste, of course.

			// Gain Energy according to the food type.
			if (food.getType() == agentType) {
				energy += params.foodEnergy;
				wasteCounterGain -= params.foodEnergy;
				info.addFoodEnergy(params.foodEnergy);
			} else {
				energy += params.otherFoodEnergy;
				wasteCounterGain -= params.otherFoodEnergy;
				info.addOthers(params.otherFoodEnergy);
			}

		//set eaten flag
		this.hasEaten = true;
	}

	/**
	 * If the agent changed directions this tick.  The 
	 * agent will perform these actions.
	 */
	private void afterTurnAction() {
		energy -= energyPenalty();
		if (energy <= 0)
			die();
		if (!pregnant)
			tryAsexBreed();
		if (pregnant) {
			pregPeriod--;
		}
	}

	/**
	 * If the agent has enough energy to breed, is randomly chosen to breed, 
	 * and its asexFlag is true, then the agent will be pregnant and set to 
	 * produce a child agent after the agent's asexPregnancyPeriod is up.
	 */
	protected void tryAsexBreed() {
		if (asexFlag && energy >= params.breedEnergy && params.asexualBreedChance != 0.0
				&& cobweb.globals.random.nextFloat() < params.asexualBreedChance) {
			pregPeriod = params.asexPregnancyPeriod;
			pregnant = true;
		}
	}

	/**
	 * As the agent ages, it will lose more energy.
	 * 
	 * @return Energy penalty
	 */
	public double energyPenalty() {
		if (!params.agingMode)
			return 0.0;
		double tempAge = currTick - birthTick;
		assert(tempAge == age);
		int penaltyValue = Math.min(Math.max(0, energy), (int)(params.agingRate
				* (Math.tan(((tempAge / params.agingLimit) * 89.99) * Math.PI / 180))));

		return penaltyValue;
	}

	/**
	 * Return the agent's tick of birth.
	 * @return The agent's tick of birth.
	 */
	public final long birthday() {
		return this.birthTick;
	}

	/**
	 * Create the same agent as this one and return it.
	 * @return A copy of this agent.
	 */
	@Override
	public Object clone() {
		try {
			return super.clone(); }
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Alias of agent.die().
	 */
	public void kill() {
		this.die();
	}

	/**
	 * Removes this Agent from the Environment.
	 */
	public void die() {
		assert (isAlive());
		if (!isAlive())
			return;

		position.setAgent(null);
		alive = false;
		position.getEnvironment().getScheduler().removeSchedulerClient(this);
	}

	/**
	 * Extracts pertinent information about the agent so the user interface 
	 * used can draw it.
	 * 
	 * @param theUI The user interface responsible for drawing the agent.
	 */
	public abstract void getDrawInfo(DrawingHandler theUI);

	/**
	 * Return the agent's current position.
	 * @return The location this Agent occupies.
	 */
	public Environment.Location getPosition() {
		return position;
	}

	/**
	 * Return true if this agent is alive, false otherwise.
	 * @return True if the agent is alive, false otherwise.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Sets the position of the Agent.
	 * @param newPos The new position of the agent.
	 */
	public void move(Environment.Location newPos) {
		newPos.setAgent(this);
		if (position != null)
			position.setAgent(null);
		position = newPos;
	}

	/**
	 * Given one of the possible actions for this agent, perform that action.
	 * @param action The action to be performed.
	 */
	public final void move(Action action) {
		switch(action) {
			case MOVE_STRAIGHT:
				this.move(this.position.add(1, this.facing));
				break;
			case TURN_LEFT:
				break;
			case TURN_RIGHT:
				break;
		}
	}

	/**
	 * This method makes the agent turn left.  It does this by updating 
	 * the direction of the agent and subtracts the amount of 
	 * energy it took to turn.
	 */
	public void turnLeft() {
		cobweb.Direction newFacing = new cobweb.Direction(2);
		newFacing.v[0] = facing.v[1];
		newFacing.v[1] = -facing.v[0];
		facing = newFacing;
		energy -= params.turnLeftEnergy;
		setWasteCounterLoss(getWasteCounterLoss() - params.turnLeftEnergy);
		info.useTurning(params.turnLeftEnergy);
		info.addTurn();
		afterTurnAction();
	}

	/**
	 * This method makes the agent turn right.  It does this by updating 
	 * the direction of the agent subtracts the amount of energy it took 
	 * to turn.
	 */
	public void turnRight() {
		cobweb.Direction newFacing = new cobweb.Direction(2);
		newFacing.v[0] = -facing.v[1];
		newFacing.v[1] = facing.v[0];
		facing = newFacing;
		energy -= params.turnRightEnergy;
		setWasteCounterLoss(getWasteCounterLoss() - params.turnRightEnergy);
		info.useTurning(params.turnRightEnergy);
		info.addTurn();
		afterTurnAction();
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
		if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
			return false;
		// and clear of wastes
		if (destPos.testFlag(ComplexEnvironment.FLAG_DROP))
			return environment.dropArray[destPos.v[0]][destPos.v[1]].canStep();
		// as well as other agents...
		if (destPos.getAgent() != null)
			return false;
		return true;
	}

	/**
	 * @see Agent#color
	 */
	public void setColor(Color c) {
		this.color = c;
	}

	/**
	 * @return The colour of the agent
	 */
	public Color getColor() {
		return this.color;
	}

	public abstract double similarity(Agent other);


	public abstract double similarity(int other);


	/**
	 * Return this agent's type.
	 * @return The type for this agent.
	 */
	public final int type() {
		return this.agentType;
	}

	/**
	 * @see ComplexAgentParams#type
	 */
	public int getAgentType() {
		return this.params.type;
	}

	/**
	 * @see Agent#wasteCounterLoss
	 */
	public int getWasteCounterLoss() {
		return this.wasteCounterLoss;
	}

	/**
	 * @return Agent's energy
	 */
	public int getEnergy() {
		return this.energy;
	}

	/**
	 * @return Adjacent facing agent.
	 */
	public Agent getAdjacentAgent() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		if (destPos == null) {
			return null;
		}
		return destPos.getAgent();
	}
}
