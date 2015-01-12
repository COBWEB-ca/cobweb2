package org.cobweb.cobweb2.core;

import org.cobweb.cobweb2.RandomSource;

/**
 * Methods that only simulation components need access to.
 * UI and other external components should only use SimulationInterface!
 */
public interface SimulationInternals extends RandomSource, StatePluginSource {

	public long getTime();

	public Topology getTopology();

	public Agent newAgent();

	public void addAgent(ComplexAgent agent);

	public StateParameter getStateParameter(String name);

	public AgentSimilarityCalculator getSimilarityCalculator();

	public AgentListener getAgentListener();
}
