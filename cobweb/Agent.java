package cobweb;

import ga.GeneticCode;

/**
 * The Agent class represents the physical notion of an Agent in a simulation.
 * Instances of the Agent class are not responsible for implementing the
 * intelligence of the Agent, this is deferred to the Agent.Controller class.
 */
public abstract class Agent {
	/**
	 * @return the location this Agent occupies.
	 */
	public Environment.Location getPosition() {
		return position;
	}

	/**
	 * Sets the position of the Agent.
	 */
	public void move(Environment.Location newPos) {
		newPos.setAgent(this);
		position.setAgent(null);
		position = newPos;
	}

	StackTraceElement[] firstDeath;

	/**
	 * Removes this Agent from the Environment.
	 */
	public void die() {
		if (!alive) {
			throw new RuntimeException("Already dead!");
		}
		firstDeath = Thread.currentThread().getStackTrace();
		position.setAgent(null);
		alive = false;
		controller.removeClientAgent(this);
		position.getEnvironment().getScheduler().removeSchedulerClient(this);
	}

	private boolean alive = true;

	public boolean isAlive() {
		return alive;
	}

	/**
	 * @return a valid drawing information structure for the current state of
	 *         the Agent.
	 */
	public abstract void getDrawInfo(UIInterface theUI);

	public abstract double similarity(Agent other);

	public abstract double similarity(int other);

	public abstract java.awt.Color getColor();

	public abstract void setColor(java.awt.Color c);

	public abstract GeneticCode getGeneticCode();

	public abstract String getGeneticSequence();

	/**
	 * @return int AgentPDStrategy
	 */
	public abstract int getAgentPDStrategy();

	/**
	 * @return int AgentPDAction
	 */
	public abstract int getAgentPDAction();

	public Controller getController() {
		return controller;
	}

	protected Agent(Environment.Location pos, Controller ai) {
		position = pos;
		controller = ai;
		controller.addClientAgent(this); // this currently does absolutely
											// nothing for both simple and
											// complex implementations of
											// controller
		position.setAgent(this);
		position.getEnvironment().getScheduler().addSchedulerClient(this);
		id =  globals.random.nextLong();
	}

	protected Environment.Location position;

	protected Controller controller;

	protected long id;

	public long age() {
		return 0L;
	}

	public long birthday() {
		return 0L;
	}

	public int type() {
		return -1;
	}

	public int getEnergy() {
		return -1;
	}

	public String getID() {
		return "-1";
	}
}
