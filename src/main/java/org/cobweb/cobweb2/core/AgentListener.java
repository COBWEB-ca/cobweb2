package org.cobweb.cobweb2.core;


public interface AgentListener extends ControllerListener {

	public abstract void onContact(Agent bumper, Agent bumpee);

	public abstract void onStep(Agent agent, LocationDirection from, LocationDirection to);

	public abstract void onSpawn(Agent agent, Agent parent1, Agent parent2);

	public abstract void onSpawn(Agent agent, Agent parent);

	public abstract void onSpawn(Agent agent);

	public abstract void onDeath(Agent agent);

	public abstract void onConsumeFood(Agent agent, int foodType);

	public abstract void onConsumeAgent(Agent agent, Agent food);

	public abstract void onEnergyChange(Agent agent, int delta, Cause cause);

	public abstract void onUpdate(Agent agent);

	public abstract boolean onNextMove(Agent agent);

}
