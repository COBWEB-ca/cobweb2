package org.cobweb.cobweb2.core;

public interface AgentListener {

	public abstract void onContact(ComplexAgent bumper, ComplexAgent bumpee);

	public abstract void onStep(ComplexAgent agent, LocationDirection from, LocationDirection to);

	public abstract void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2);

	public abstract void onSpawn(ComplexAgent agent, ComplexAgent parent);

	public abstract void onSpawn(ComplexAgent agent);

	public abstract void onDeath(ComplexAgent agent);

}
