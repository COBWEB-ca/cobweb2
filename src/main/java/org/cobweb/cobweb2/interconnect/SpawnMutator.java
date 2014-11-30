package org.cobweb.cobweb2.interconnect;

import org.cobweb.cobweb2.core.ComplexAgent;

/**
 * Modifies agents when they are born.
 */
public interface SpawnMutator extends AgentMutator {

	/**
	 * Agent died.
	 * @param agent Agent that died.
	 */
	public void onDeath(ComplexAgent agent);

	/**
	 * Agent spawned by user.
	 * @param agent Agent spawned.
	 */
	public void onSpawn(ComplexAgent agent);

	/**
	 * Agent produced asexually 
	 * @param agent Agent produced.
	 * @param parent Asexual parent.
	 */
	public void onSpawn(ComplexAgent agent, ComplexAgent parent);

	/**
	 * Agent produced sexually.
	 * @param agent Agent produced.
	 * @param parent1 First parent.
	 * @param parent2 Second parent.
	 */
	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2);
}
