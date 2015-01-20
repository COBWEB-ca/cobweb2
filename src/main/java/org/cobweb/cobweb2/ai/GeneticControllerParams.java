/**
 *
 */
package org.cobweb.cobweb2.ai;

import java.util.Arrays;

import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
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

	@ConfXMLTag("AgentParams")
	@ConfList(indexName = "Agent", startAtOne = true)
	public GeneticStateAgentParams[] agentParams = new GeneticStateAgentParams[0];

	private final transient SimulationParams simParam;

	public GeneticControllerParams(SimulationParams simParams) {
		this.simParam = simParams;
		resize(simParams.getCounts());
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		int agentTypes = envParams.getAgentTypes();
		GeneticStateAgentParams[] n = Arrays.copyOf(agentParams, agentTypes);

		for (int i = agentParams.length; i < agentTypes; i++) {
			n[i] = new GeneticStateAgentParams(simParam);
		}
		agentParams = n;
	}

	@Override
	public Controller createController(SimulationInternals sim, int type) {
		GeneticController controller = new GeneticController(sim, this, type);
		return controller;
	}
}
