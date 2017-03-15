package org.cobweb.cobweb2.impl.ai;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.impl.ControllerParams;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.plugins.PerAgentParams;

public class LinearWeightsControllerParams extends PerAgentParams<LinearWeightAgentParam> implements ControllerParams {

	private final transient SimulationParams simParam;

	public LinearWeightsControllerParams(SimulationParams simParam) {
		super(LinearWeightAgentParam.class);
		this.simParam = simParam;
		resize(simParam);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);

		for (LinearWeightAgentParam a : agentParams) {
			a.resize(this.simParam);
		}
	}

	@Override
	protected LinearWeightAgentParam newAgentParam() {
		return new LinearWeightAgentParam(simParam);
	}

	@Override
	public Controller createController(SimulationInternals sim, int type) {
		LinearWeightsController controller = new LinearWeightsController(sim, this, type);
		return controller;
	}

	public static final int INPUT_COUNT = 10;
	public static final int OUTPUT_COUNT = 6;

	public static final String[] inputNames = { "Constant", "Energy", "Distance to agent", "Distance to food",
		"Distance to obstacle", "Direction", "Memory", "Communication", "Age", "Random" };

	public static final String[] outputNames = { "Memory", "Communication", "Left", "Right", "Forward", "Asexual Breed" };

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
