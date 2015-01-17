package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;

public class LinearWeightsControllerParams implements ControllerParams {

	private static final long serialVersionUID = 8856565519749448009L;

	@ConfXMLTag("WeightMatrix")
	@ConfList(indexName = {"inp", "outp"}, startAtOne = false)
	public double[][] data;

	private final transient SimulationParams simParam;

	public LinearWeightsControllerParams(SimulationParams simParam) {
		this.simParam = simParam;
		data = new double[INPUT_COUNT + this.simParam.getPluginParameters().size()][OUTPUT_COUNT];
	}

	public LinearWeightsControllerParams copy() {
		LinearWeightsControllerParams p = new LinearWeightsControllerParams(simParam);
		p.data = new double[data.length][data[0].length];
		for (int i = 0; i < p.data.length; i++)
			for (int j = 0; j < p.data[i].length; j++)
				p.data[i][j] = data[i][j];
		return p;
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		// Doesn't do anything so far
	}

	@Override
	public Controller createController(SimulationInternals sim, int memoryBits, int communicationBits, int type) {
		LinearWeightsController controller = new LinearWeightsController(sim, this, memoryBits, communicationBits);
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

}
