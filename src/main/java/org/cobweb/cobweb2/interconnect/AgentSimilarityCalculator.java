package org.cobweb.cobweb2.interconnect;

import org.cobweb.cobweb2.core.ComplexAgent;

/**
 * Calculates similarity between two agents.
 * This is used in combination with minimum communication similarity and minimum breeding similarity.
 */
public interface AgentSimilarityCalculator {

	/**
	 * Calculates similarity between given agents.
	 * @param a1 First agent.
	 * @param a2 Second agent.
	 * @return similarity value.
	 */
	public float similarity(ComplexAgent a1, ComplexAgent a2);
}
