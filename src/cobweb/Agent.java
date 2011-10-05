package cobweb;

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
	public final boolean canPlaceOnTop(CellObject other) {
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
	 * The unique identifier for this agent.
	 */
	private static long nextID = 1;

	protected long id;

	/**
	 * Return this agent's unique identifier.
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
	 * Reset the id of this agent.
	 */
	public static void resetIDSequence() {
		nextID = 1;
	}

	/**
	 * True if agent is alive.
	 */
	private boolean alive = true;

	/**
	 * TODO move to complex agent???
	 */
	protected Controller controller;

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
	 * Return the direction the agent is currently facing.
	 * @return The direction the agent is currently facing.
	 */
	public final Direction getFacing() {
		return this.facing;
	}

	/**
	 * Return this agent's remaining energy.
	 * Not final, so subclasses can overwrite this.
	 * @return The agent's remaining energy.
	 */
	public int getEnergy() {
		return this.energy;
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

	protected void init(Controller ai) {
		controller = ai;
		controller.addClientAgent(this); // this currently does absolutely
		// nothing for both simple and
		// complex implementations of
		// controller
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
		controller.removeClientAgent(this);
		position.getEnvironment().getScheduler().removeSchedulerClient(this);
	}

	/**
	 * TODO move to ComplexAgent
	 * @return int AgentPDAction
	 */
	public abstract int getAgentPDAction();

	/**
	 * TODO move to ComplexAgent
	 * @return int AgentPDStrategy
	 */
	public abstract int getAgentPDStrategy();

	public abstract java.awt.Color getColor();

	/**
	 * TODO what is this?
	 * @return
	 */
	public Controller getController() {
		return controller;
	}

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

	public abstract void setColor(java.awt.Color c);

	public abstract double similarity(Agent other);


	public abstract double similarity(int other);


	/**
	 * Return this agent's type.
	 * @return The type for this agent.
	 */
	public final int type() {
		return this.agentType;
	}
}
