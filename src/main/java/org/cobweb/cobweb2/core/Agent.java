package org.cobweb.cobweb2.core;

/**
 * Basic properties of an Agent
 */
public abstract class Agent {

	private boolean alive = true;

	protected LocationDirection position;

	/**
	 * Energy the Agent can use to do things and can gain doing other things
	 */
	protected int energy;

	public void die() {
		assert (isAlive());
		if (!isAlive())
			return;

		alive = false;
	}

	public int getEnergy() {
		return energy;
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

	public abstract int getType();

	protected abstract Agent createChildAsexual(LocationDirection location);
}
