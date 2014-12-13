package org.cobweb.cobweb2.core;

import java.util.Collection;

import org.cobweb.cobweb2.interconnect.StateParameter;
import org.cobweb.util.RandomNoGenerator;

/**
 * Methods that only simulation components need access to.
 * UI and other external components should only use SimulationInterface!
 */
public interface SimulationInternals extends SimulationInterface {

	public RandomNoGenerator getRandom();

	public ComplexAgent newAgent();

	public void addAgent(ComplexAgent agent);

	public StateParameter getStateParameter(String name);

	public Collection<String> getStatePlugins();
}
