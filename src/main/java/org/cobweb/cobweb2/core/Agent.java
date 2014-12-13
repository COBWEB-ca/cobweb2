package org.cobweb.cobweb2.core;

import org.cobweb.cobweb2.ai.Controller;





/**
 * The Agent class represents the physical notion of an Agent in a simulation
 * (living or not, location, colour).  Instances of the Agent class are not
 * responsible for implementing the intelligence of the Agent, this is deferred
 * to the Agent.Controller class.
 */
public abstract class Agent {

	private boolean alive = true;

	protected SimulationInterface simulation;

	protected Agent(SimulationInterface sim) {
		id = makeID();
		simulation = sim;
	}

	protected long id;

	private static long nextID = 1;

	private static long makeID() {
		if ((nextID + 1) == Long.MAX_VALUE) {
			nextID = Long.MIN_VALUE;
		}
		return nextID++;
	}

	public long getID() {
		return id;
	}

	protected LocationDirection position;

	protected Controller controller;

	protected void init(Controller ai) {
		controller = ai;
		// nothing for both simple and
		// complex implementations of
		// controller
	}

	/**
	 * Removes this Agent from the Environment.
	 */
	public void die() {
		assert (isAlive());
		if (!isAlive())
			return;

		alive = false;
	}

	public Controller getController() {
		return controller;
	}

	public int getEnergy() {
		return -1;
	}

	/**
	 * @return the location this Agent occupies.
	 */
	public LocationDirection getPosition() {
		return position;
	}

	public boolean isAlive() {
		return alive;
	}


	public int type() {
		return -1;
	}
}
