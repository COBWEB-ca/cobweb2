package org.cobweb.cobweb2.impl.ai;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.impl.ControllerParams;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.ArrayUtilities;

public class LinearWeightsControllerParams extends PerAgentParams<LinearWeightAgentParam> implements ControllerParams {

	@ConfXMLTag("WeightMatrix")
	@ConfList(indexName = {"inp", "outp"}, startAtOne = false)
	public double[][] data = new double[0][0];

	private final transient SimulationParams simParam;

	public LinearWeightsControllerParams(SimulationParams simParam) {
		super(LinearWeightAgentParam.class);
		this.simParam = simParam;
		resize(simParam);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);

		double[][] n = ArrayUtilities.resizeArray(data,
				INPUT_COUNT + this.simParam.getPluginParameters().size(),
				OUTPUT_COUNT);
		data = n;
	}

	public LinearWeightsControllerParams copy() {
		LinearWeightsControllerParams p = new LinearWeightsControllerParams(simParam);
		p.data = new double[data.length][data[0].length];
		for (int i = 0; i < p.data.length; i++)
			for (int j = 0; j < p.data[i].length; j++)
				p.data[i][j] = data[i][j];

		p.agentParams = Arrays.copyOf(agentParams, agentParams.length);
		return p;
	}

	@Override
	protected LinearWeightAgentParam newAgentParam() {
		return new LinearWeightAgentParam();
	}

	@Override
	public Controller createController(SimulationInternals sim, int type) {
		LinearWeightsController controller = new LinearWeightsController(sim, this, type);
		return controller;
	}

	public final int INPUT_COUNT = 10;
	public final int OUTPUT_COUNT = 6;

	public final String[] inputNames = { "Constant", "Energy", "Distance to agent", "Distance to food",
			"Distance to obstacle", "Direction", "Memory", "Communication", "Age", "Random" };

	public final String[] outputNames = { "Memory", "Communication", "Left", "Right", "Forward", "Asexual Breed" };

	private final double UPDATE_RATE = 0.001;

	private transient double[] runningOutputMean = new double[OUTPUT_COUNT];

	public void updateStats(int output, double value) {
		runningOutputMean[output] *= (1 - UPDATE_RATE);
		runningOutputMean[output] += UPDATE_RATE * value;
	}

	public double[] getRunningOutputMean() {
		return runningOutputMean;
	}

	private static final long serialVersionUID = 2L;
}
