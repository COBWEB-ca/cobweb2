/**
 *
 */
package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.SeeInfo;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.Topology;

public class LinearWeightsController implements Controller {

	private int memSize;

	private int commSize;

	private LinearWeightsControllerParams params;

	private SimulationInternals simulator;

	public LinearWeightsController(SimulationInternals sim, LinearWeightsControllerParams params, int memSize, int commSize) {
		this.simulator = sim;
		this.params = params;
		this.memSize = memSize;
		this.commSize = commSize;
	}

	protected LinearWeightsController(LinearWeightsController parent, float mutation) {
		this.simulator = parent.simulator;
		this.params = parent.params.copy();
		mutate(mutation);
	}

	protected LinearWeightsController(LinearWeightsController parent1, LinearWeightsController parent2, float mutation) {
		this.simulator = parent1.simulator;
		this.params = parent1.params.copy();

		for (int i = 0; i < params.data.length; i++) {
			for (int j = 0; j < params.data[i].length; j++) {
				if (simulator.getRandom().nextBoolean()) {
					this.params.data[i][j] = parent2.params.data[i][j];
				}
			}
		}

		mutate(mutation);
	}

	private static int ENERGY_THRESHOLD = 160;

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
		double variables[] = new double[params.INPUT_COUNT + simulator.getStatePluginKeys().size()];
		variables[0] = 1.0;
		variables[1] = ((double) agent.getEnergy() / (ENERGY_THRESHOLD));
		variables[2] = type == Environment.FLAG_AGENT ?	(ComplexAgent.LOOK_DISTANCE - dist) / (double) ComplexAgent.LOOK_DISTANCE : 0;
		variables[3] = type == Environment.FLAG_FOOD ? (ComplexAgent.LOOK_DISTANCE - dist) / (double) ComplexAgent.LOOK_DISTANCE : 0;
		variables[4] = type == Environment.FLAG_STONE || type == Environment.FLAG_DROP ? ((double) ComplexAgent.LOOK_DISTANCE - dist) / 4 : 0;
		variables[5] = simulator.getTopology()
				.getRotationBetween(Topology.NORTH, agent.getPosition().direction)
				.ordinal() / 2.0;
		variables[6] = memSize == 0 ? 0 : (double) agent.getMemoryBuffer() / ((1 << memSize) - 1);
		variables[7] = commSize == 0 ? 0 : (double) agent.getCommInbox() / ((1 << commSize) - 1);
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


		double memout = 0.0;
		double commout = 0.0;
		double left = 0.0;
		double right = 0.0;
		double step = 0.0;
		double asexflag = 0.0;
		for (int eq = 0; eq < params.OUTPUT_COUNT; eq++) {
			double res = 0.0;
			variables[9] = simulator.getRandom().nextGaussian();
			for (int v = 0; v < variables.length; v++) {
				res += params.data[v][eq] * variables[v];
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

			params.updateStats(eq, res);
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

	private void mutate(float mutation) {
		double mutationCounter = params.data.length * params.data[0].length * mutation;
		while (mutationCounter > 1) {
			int i = simulator.getRandom().nextInt(params.data.length);
			int j = simulator.getRandom().nextInt(params.data[i].length);
			params.data[i][j] += simulator.getRandom().nextGaussian() * 0.5;
			mutationCounter -= 1;
		}
	}

	@Override
	public LinearWeightsController createChildAsexual(float mutation) {
		LinearWeightsController child = new LinearWeightsController(this, mutation);
		return child;
	}

	@Override
	public LinearWeightsController createChildSexual(Controller p2, float mutation) {
		if (!(p2 instanceof LinearWeightsController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}
		LinearWeightsController pa2 = (LinearWeightsController) p2;

		LinearWeightsController child = new LinearWeightsController(this, pa2, mutation);
		return child;
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