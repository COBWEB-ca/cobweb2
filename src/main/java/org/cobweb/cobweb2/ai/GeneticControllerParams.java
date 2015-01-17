/**
 *
 */
package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;

/**
 * Parameters for GeneticController
 */
public class GeneticControllerParams implements ControllerParams {

	private static final long serialVersionUID = -1252142643022378114L;

	/**
	 * Random seed used to initialize the behaviour array.
	 */
	@ConfDisplayName("Array initialization random seed")
	@ConfXMLTag("RandomSeed")
	public long randomSeed = 42;

	@ConfDisplayName("Parameter Plugins")
	@ConfXMLTag("PluginParams")
	public GeneticStateParams agentParams;

	public GeneticControllerParams(SimulationParams simParams) {
		agentParams = new GeneticStateParams(simParams);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		agentParams.resize(envParams.getAgentTypes());
	}

	@Override
	public Controller createController(SimulationInternals sim, int memoryBits, int communicationBits, int type) {
		GeneticController controller = new GeneticController(sim, this, memoryBits, communicationBits, type);
		return controller;
	}
}
