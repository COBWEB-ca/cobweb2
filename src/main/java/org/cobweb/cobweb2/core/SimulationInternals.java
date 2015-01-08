package org.cobweb.cobweb2.core;

import java.util.Collection;

import org.cobweb.cobweb2.RandomSource;
import org.cobweb.cobweb2.interconnect.StateParameter;

/**
 * Methods that only simulation components need access to.
 * UI and other external components should only use SimulationInterface!
 */
public interface SimulationInternals extends RandomSource {

	public long getTime();

	public Topology getTopology();

	public ComplexAgent newAgent();

	public void addAgent(ComplexAgent agent);

	public StateParameter getStateParameter(String name);

	public Collection<String> getStatePlugins();

	public AgentSimilarityCalculator getSimilarityCalculator();

	public AgentListener getAgentListener();
}
