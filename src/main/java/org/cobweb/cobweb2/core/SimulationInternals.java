package org.cobweb.cobweb2.core;



/**
 * Methods that only simulation components need access to.
 * UI and other external components should only use SimulationInterface!
 */
public interface SimulationInternals extends RandomSource, StatePluginSource {

	public long getTime();

	public Topology getTopology();

	public Agent newAgent(int type);

	public void addAgent(Agent agent);

	public StateParameter getStateParameter(String name);

	public AgentSimilarityCalculator getSimilarityCalculator();

	public AgentListener getAgentListener();
}
