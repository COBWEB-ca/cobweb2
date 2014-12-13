package org.cobweb.cobweb2.core;

import org.cobweb.util.RandomNoGenerator;

/**
 * Methods that only simulation components need access to.
 * UI and other external components should only use SimulationInterface!
 */
public interface SimulationInternals extends SimulationInterface {

	public RandomNoGenerator getRandom();

	public ComplexAgent newAgent();

	public void addAgent(ComplexAgent agent);
}
