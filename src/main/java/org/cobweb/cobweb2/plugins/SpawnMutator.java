package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;

/**
 * Modifies agents when they are born.
 */
public interface SpawnMutator extends AgentMutator {

	/**
	 * Agent died.
	 * @param agent Agent that died.
	 */
	public void onDeath(Agent agent);

	/**
	 * Agent spawned by user.
	 * @param agent Agent spawned.
	 */
	public void onSpawn(Agent agent);

	/**
	 * Agent produced asexually
	 * @param agent Agent produced.
	 * @param parent Asexual parent.
	 */
	public void onSpawn(Agent agent, Agent parent);

	/**
	 * Agent produced sexually.
	 * @param agent Agent produced.
	 * @param parent1 First parent.
	 * @param parent2 Second parent.
	 */
	public void onSpawn(Agent agent, Agent parent1, Agent parent2);
}
