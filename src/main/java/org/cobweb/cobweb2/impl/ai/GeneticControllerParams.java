/**
 *
 */
package org.cobweb.cobweb2.impl.ai;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.impl.ControllerParams;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.plugins.PerAgentParams;

/**
 * Parameters for GeneticController
 */
public class GeneticControllerParams extends PerAgentParams<GeneticStateAgentParams> implements ControllerParams {

	private final transient SimulationParams simParam;

	public GeneticControllerParams(SimulationParams simParams) {
		super(GeneticStateAgentParams.class);
		this.simParam = simParams;
		resize(simParams.getCounts());
	}

	@Override
	protected GeneticStateAgentParams newAgentParam() {
		return new GeneticStateAgentParams(simParam);
	}

	@Override
	public Controller createController(SimulationInternals sim, int type) {
		GeneticController controller = new GeneticController(sim, agentParams[type]);
		return controller;
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);
		for(GeneticStateAgentParams i : agentParams) {
			i.resize(simParam);
		}
	}

	private static final long serialVersionUID = 2L;
}
