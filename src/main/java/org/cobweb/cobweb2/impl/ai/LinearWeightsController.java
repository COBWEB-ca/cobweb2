package org.cobweb.cobweb2.impl.ai;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.ControllerInput;
import org.cobweb.cobweb2.core.ControllerListener;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.vision.SeeInfo;
import org.cobweb.cobweb2.plugins.vision.VisionState;
import org.cobweb.util.ArrayUtilities;

public class LinearWeightsController implements Controller {

	private final LinearWeightAgentParam params;

	private final LinearWeightsControllerParams stats;

	private final SimulationInternals simulator;

	private final int agentType;

	public double[][] data = new double[0][0];

	public LinearWeightsController(SimulationInternals sim, LinearWeightsControllerParams params, int agentType) {
		this.simulator = sim;
		this.stats = params;
		this.params = params.agentParams[agentType];
		this.agentType = agentType;

		this.data = ArrayUtilities.clone(this.params.dataInitial);
	}

	protected LinearWeightsController(LinearWeightsController parent) {
		this.simulator = parent.simulator;
		this.stats = parent.stats;
		this.params = parent.params;
		this.agentType = parent.agentType;

		this.data = ArrayUtilities.clone(parent.data);
		mutate(params.mutationRate);
	}

	protected LinearWeightsController(LinearWeightsController parent1, LinearWeightsController parent2) {
		this.simulator = parent1.simulator;
		this.stats = parent1.stats;
		this.params = parent1.params;
		this.agentType = parent1.agentType;

		this.data = ArrayUtilities.clone(parent1.data);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (simulator.getRandom().nextBoolean()) {
					data[i][j] = parent2.data[i][j];
				}
			}
		}
		mutate(params.mutationRate);
	}

	private static int ENERGY_THRESHOLD = 160;

	public class LWInput implements ControllerInput {
		public double[] inputs;

		public LWInput(double[] inputs) {
			this.inputs = inputs;
		}

		@Override
		public void mutate(float adjustmentStrength) {
			for (int eq = 0; eq < data[0].length; eq++) {
				for (int v = 0; v < inputs.length; v++) {
					// variables with stronger inputs are adjusted more
					double strength = adjustmentStrength * Math.abs(inputs[v]);
					data[v][eq] += simulator.getRandom().nextGaussian() * strength;
				}
			}
		}
	}


	@Override
	public void controlAgent(Agent theAgent, ControllerListener inputCallback) {
		ComplexAgent agent = (ComplexAgent) theAgent;
		SeeInfo get = agent.getState(VisionState.class).distanceLook();
		int type = get.getType();
		int dist = get.getDist();

		/* careful with this block, eclipse likes to screw up the tabs!
		 * if it breaks upon saving, undo and save again, this should save it without breaking
		 */
		double variables[] = new double[LinearWeightsControllerParams.INPUT_COUNT + simulator.getStatePluginKeys().size()];
		variables[0] = 1.0;
		variables[1] = ((double) agent.getEnergy() / (ENERGY_THRESHOLD));
		variables[2] = type == Environment.FLAG_AGENT ?	(get.getMaxDistance() - dist) / (double) get.getMaxDistance() : 0;
		variables[3] = type == Environment.FLAG_FOOD ? (get.getMaxDistance() - dist) / (double) get.getMaxDistance() : 0;
		variables[4] = type == Environment.FLAG_STONE || type == Environment.FLAG_DROP ? ((double) get.getMaxDistance() - dist) / 4 : 0;
		variables[5] = simulator.getTopology()
				.getRotationBetween(Topology.NORTH, agent.getPosition().direction)
				.ordinal() / 2.0;
		variables[6] = agent.getMemoryBuffer();
		variables[7] = agent.getCommInbox();
		variables[8] = Math.max(agent.getAge() / 100.0, 2);
		variables[9] = simulator.getRandom().nextGaussian();
		{
			int i = 10;
			for (String plugin : simulator.getStatePluginKeys()) {
				StateParameter sp = simulator.getStateParameter(plugin);
				variables[i] = sp.getValue(agent);
				i++;
			}
		}

		inputCallback.beforeControl(agent, new LWInput(variables));

		double memout = 0.0;
		double commout = 0.0;
		double left = 0.0;
		double right = 0.0;
		double step = 0.0;
		double asexflag = 0.0;
		for (int eq = 0; eq < LinearWeightsControllerParams.OUTPUT_COUNT; eq++) {
			double res = 0.0;
			for (int v = 0; v < variables.length; v++) {
				res += data[v][eq] * variables[v];
			}

			if (eq == 0)
				memout = res;
			else if (eq == 1)
				commout = res;
			else if (eq == 2)
				left = res;
			else if (eq == 3)
				right = res;
			else if (eq == 4)
				step = res;
			else if (eq == 5)
				asexflag = res;

			stats.updateStats(eq, res);
		}

		agent.setMemoryBuffer(memout);
		agent.setCommOutbox(commout);
		agent.setShouldReproduceAsex(asexflag > 0.50);

		if (right > left && right > step)
			agent.turnRight();
		else if (left > right && left > step)
			agent.turnLeft();
		else if (step > 0)
			agent.step();
	}

	private void mutate(float mutation) {
		double mutationCounter = data.length * data[0].length * mutation;
		while (mutationCounter > 1) {
			int i = simulator.getRandom().nextInt(data.length);
			int j = simulator.getRandom().nextInt(data[i].length);
			data[i][j] += simulator.getRandom().nextGaussian() * 0.5;
			mutationCounter -= 1;
		}
	}

	@Override
	public LinearWeightsController createChildAsexual() {
		LinearWeightsController child = new LinearWeightsController(this);
		return child;
	}

	@Override
	public LinearWeightsController createChildSexual(Controller p2) {
		if (!(p2 instanceof LinearWeightsController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}
		LinearWeightsController pa2 = (LinearWeightsController) p2;

		LinearWeightsController child = new LinearWeightsController(this, pa2);
		return child;
	}

	public double similarity(LinearWeightsController other) {
		int diff = 0;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				diff += Math.abs(data[i][j] * data[i][j] - other.data[i][j]
						* other.data[i][j]);
			}
		}
		return Math.max(0, (100.0 - diff) / 100.0);
	}

}