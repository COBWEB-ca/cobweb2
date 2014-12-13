/**
 *
 */
package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.SeeInfo;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.interconnect.StateParameter;
import org.cobweb.cobweb2.io.CobwebParam;

public class LinearWeightsController implements Controller {

	public static final int ENERGY_THRESHOLD = 160;

	public static final int INPUT_COUNT = 10;
	public static final int OUTPUT_COUNT = 6;

	@Deprecated //FIXME static!
	private static double[] runningOutputMean = new double[OUTPUT_COUNT];

	public static final String[] inputNames = { "Constant", "Energy", "Distance to agent", "Distance to food",
		"Distance to obstacle", "Direction", "Memory", "Communication", "Age", "Random" };

	public static final String[] outputNames = { "Memory", "Communication", "Left", "Right", "Forward", "Asexual Breed" };

	private static final double UPDATE_RATE = 0.001;

	@Deprecated //FIXME static!
	public static double[] getRunningOutputMean() {
		return runningOutputMean;
	}

	private int memSize;

	private int commSize;

	private LinearWeightsControllerParams params;

	private SimulationInternals simulator;

	public LinearWeightsController(SimulationInternals sim) {
		simulator = sim;
	}

	@Override
	public void controlAgent(Agent theAgent) {
		ComplexAgent agent;
		if (theAgent instanceof ComplexAgent) {
			agent = (ComplexAgent) theAgent;
		} else {
			return;
		}
		SeeInfo get = agent.distanceLook();
		int type = get.getType();
		int dist = get.getDist();

		/* careful with this block, eclipse likes to screw up the tabs!
		 * if it breaks upon saving, undo and save again, this should save it without breaking
		 */
		double variables[] = new double[INPUT_COUNT + LinearWeightsControllerParams.pluginNames.size()];
		variables[0] = 1.0;
		variables[1] = ((double) agent.getEnergy() / (ENERGY_THRESHOLD));
		variables[2] = type == ComplexEnvironment.FLAG_AGENT ?	(ComplexAgent.LOOK_DISTANCE - dist) / (double) ComplexAgent.LOOK_DISTANCE : 0;
		variables[3] = type == ComplexEnvironment.FLAG_FOOD ? (ComplexAgent.LOOK_DISTANCE - dist) / (double) ComplexAgent.LOOK_DISTANCE : 0;
		variables[4] = type == ComplexEnvironment.FLAG_STONE || type == ComplexEnvironment.FLAG_DROP ? ((double) ComplexAgent.LOOK_DISTANCE - dist) / 4 : 0;
		variables[5] = (double) agent.getIntFacing() / 2;
		variables[6] = memSize == 0 ? 0 : (double) agent.getMemoryBuffer() / ((1 << memSize) - 1);
		variables[7] = commSize == 0 ? 0 : (double) agent.getCommInbox() / ((1 << commSize) - 1);
		variables[8] = Math.max(agent.getAge() / 100.0, 2);
		variables[9] = simulator.getRandom().nextGaussian();
		{
			int i = 10;
			for (String plugin : LinearWeightsControllerParams.pluginNames) {
				StateParameter sp = simulator.getStateParameter(plugin);
				variables[i] = sp.getValue(agent);
				i++;
			}
		}


		double memout = 0.0;
		double commout = 0.0;
		double left = 0.0;
		double right = 0.0;
		double step = 0.0;
		double asexflag = 0.0;
		for (int eq = 0; eq < OUTPUT_COUNT; eq++) {
			double res = 0.0;
			variables[9] = simulator.getRandom().nextGaussian();
			for (int v = 0; v < variables.length; v++) {
				res += params.data[v][eq] * variables[v];
			}

			runningOutputMean[eq] *= (1 - UPDATE_RATE);
			runningOutputMean[eq] += UPDATE_RATE * res;

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
		}

		agent.setMemoryBuffer((int) memout);
		agent.setCommOutbox((int) commout);
		agent.setShouldReproduceAsex(asexflag > 0.50);

		if (right > left && right > step)
			agent.turnRight();
		else if (left > right && left > step)
			agent.turnLeft();
		else if (step > 0)
			agent.step();
	}

	@Override
	public CobwebParam getParams() {
		return params;
	}

	private void mutate(float mutation) {
		double mutationCounter = params.data.length * params.data[0].length * mutation;
		while (mutationCounter > 1) {
			int i = simulator.getRandom().nextInt(params.data.length);
			int j = simulator.getRandom().nextInt(params.data[i].length);
			params.data[i][j] += simulator.getRandom().nextGaussian() * 0.5;
			mutationCounter -= 1;
		}
	}

	public void setParams(CobwebParam params) {
		this.params = (LinearWeightsControllerParams) params;
	}

	@Override
	public void setupFromEnvironment(int memSize, int commSize, CobwebParam params, int type) {
		this.params = (LinearWeightsControllerParams) params;
		this.memSize = memSize;
		this.commSize = commSize;
	}

	@Override
	public void setupFromParent(Controller p, float mutation) {
		if (!(p instanceof LinearWeightsController))
			throw new RuntimeException("Parent's controller type must match the child's");

		LinearWeightsController pa = (LinearWeightsController) p;
		this.params = pa.params.copy();

		mutate(mutation);
	}

	@Override
	public void setupFromParents(Controller p1, Controller p2, float mutation) {
		if (!(p1 instanceof LinearWeightsController) || !(p2 instanceof LinearWeightsController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}
		LinearWeightsController pa1 = (LinearWeightsController) p1;
		LinearWeightsController pa2 = (LinearWeightsController) p2;

		params = pa1.params.copy();

		for (int i = 0; i < params.data.length; i++) {
			for (int j = 0; j < params.data[i].length; j++) {
				if (simulator.getRandom().nextBoolean()) {
					params.data[i][j] = pa2.params.data[i][j];
				}
			}
		}

		mutate(mutation);
	}

	public double similarity(LinearWeightsController other) {
		int diff = 0;
		for (int i = 0; i < params.data.length; i++) {
			for (int j = 0; j < params.data[i].length; j++) {
				diff += Math.abs(this.params.data[i][j] * this.params.data[i][j] - other.params.data[i][j]
						* other.params.data[i][j]);
			}
		}
		return Math.max(0, (100.0 - diff) / 100.0);
	}

}