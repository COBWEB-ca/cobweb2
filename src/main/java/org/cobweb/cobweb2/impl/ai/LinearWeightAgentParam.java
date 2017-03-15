package org.cobweb.cobweb2.impl.ai;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.plugins.ResizableParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.ArrayUtilities;

/**
 * LinearWeightsController per-agent-type parameters
 */
public class LinearWeightAgentParam implements ResizableParam {

	@ConfXMLTag("WeightMatrix")
	@ConfList(indexName = {"inp", "outp"}, startAtOne = false)
	public double[][] dataInitial = new double[0][0];

	@ConfDisplayName("Mutation Rate")
	@ConfXMLTag("MutationRate")
	public float mutationRate = 0.05f;

	private SimulationParams simParam;

	public LinearWeightAgentParam(SimulationParams simParam) {
		this.simParam = simParam;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void resize(AgentFoodCountable envParams) {
		double[][] n = ArrayUtilities.resizeArray(dataInitial,
				LinearWeightsControllerParams.INPUT_COUNT + this.simParam.getPluginParameters().size(),
				LinearWeightsControllerParams.OUTPUT_COUNT);
		dataInitial = n;
	}
}
