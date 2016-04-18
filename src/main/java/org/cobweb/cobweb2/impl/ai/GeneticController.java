package org.cobweb.cobweb2.impl.ai;

import java.util.Map.Entry;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.ControllerInput;
import org.cobweb.cobweb2.core.ControllerListener;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.vision.SeeInfo;
import org.cobweb.cobweb2.plugins.vision.VisionState;
import org.cobweb.util.BitField;

/**
 * This class contains methods that set up the parameters for agents
 * that are used to influence the actions of the agents.
 *
 * @author ???
 *
 */
public class GeneticController implements Controller {

	private final static int TURN_LEFT = 0;
	private final static int TURN_RIGHT = 1;
	private final static int MOVE_STRAIGHT = 2;
	private final static int NONE = 3;

	public BehaviorArray ga;

	private static final int INPUT_BITS = 8;

	private static final int OUTPUT_BITS = 2;

	private static final int ENERGY_THRESHOLD = 160;

	private final GeneticStateAgentParams params;

	private SimulationInternals simulation;

	public GeneticController(SimulationInternals sim, GeneticStateAgentParams params) {
		this.simulation = sim;
		this.params = params;
		int[] outputArray = { OUTPUT_BITS, params.memoryBits, params.communicationBits, 1 };

		int inputSize = INPUT_BITS + params.memoryBits + params.communicationBits;
		for (int ss : this.params.stateSizes.values()) {
			inputSize += ss;
		}

		ga = new BehaviorArray(inputSize, outputArray);
		ga.randomInit(this.params.randomSeed);
	}

	protected GeneticController(GeneticController parent) {
		simulation = parent.simulation;
		params = parent.params;
		ga = parent.ga
				.copy(params.mutationRate, simulation.getRandom());
	}

	protected GeneticController(GeneticController parent1, GeneticController parent2) {
		simulation = parent1.simulation;
		params = parent1.params;
		ga = BehaviorArray
				.splice(parent1.ga, parent2.ga, simulation.getRandom())
				.copy(params.mutationRate, simulation.getRandom());
	}

	/**
	 * Given the agent's energy, get the amount of energy to add to the array.
	 * @param energy The agent's energy.
	 */
	private static int getEnergy(int energy) {
		final int maxEnergy = 3;

		if(energy > ENERGY_THRESHOLD) {
			return maxEnergy;
		} else {
			return (int) ((double) energy / (ENERGY_THRESHOLD) * 4.0);
		}
	}

	public static class GCInput implements ControllerInput {
		public GCInput(BitField inputCode) {
			this.inputCode = inputCode;
		}
		public BitField inputCode;
	}

	/**
	 * Converts the parameters of the agent into a behavior (turn left or right,
	 * step).
	 *
	 * @see BehaviorArray
	 * @see ComplexAgent#turnLeft()
	 * @see ComplexAgent#turnRight()
	 * @see ComplexAgent#step()
	 */
	@Override
	public void controlAgent(Agent baseAgent, ControllerListener inputCallback) {
		ComplexAgent theAgent = (ComplexAgent) baseAgent;

		BitField inputCode = getInputArray(theAgent);

		inputCallback.beforeControl(theAgent, new GCInput(inputCode));

		int[] outputArray = ga.getOutput(inputCode.intValue());

		int actionCode = outputArray[0];
		theAgent.setMemoryBuffer(dequantize(outputArray[1], params.memoryBits));
		theAgent.setCommOutbox(dequantize(outputArray[2], params.communicationBits));
		//whether to breed
		theAgent.setShouldReproduceAsex(outputArray[3] != 0);

		switch (actionCode) {
			case TURN_LEFT:
				theAgent.turnLeft();
				break;
			case TURN_RIGHT:
				theAgent.turnRight();
				break;
			case MOVE_STRAIGHT:
			case NONE:
				theAgent.step();
				break;
		}
	}

	/**
	 * Given an agent, read all information for it and compound it into an array.
	 * @param theAgent The agent.
	 * @return The input array.
	 */
	private BitField getInputArray(ComplexAgent theAgent) {
		BitField inputCode = new BitField();

		//add the energy info to the array
		inputCode.add(getEnergy(theAgent.getEnergy()), 2);

		//add the direction the agent is facing to the array
		inputCode.add(simulation.getTopology()
				.getRotationBetween(Topology.NORTH, theAgent.getPosition().direction)
				.ordinal(), 2);

		//add the viewing info to the array

		SeeInfo get = theAgent.getState(VisionState.class).distanceLook();
		inputCode.add(get.getType(), 2);
		inputCode.add(get.getDist(), 2);

		//add the memory buffer to the array
		inputCode.add(
				quantize(theAgent.getMemoryBuffer(), params.memoryBits),
				params.memoryBits);

		//add the communications to the array
		inputCode.add(
				quantize(theAgent.getCommInbox(), params.communicationBits),
				params.communicationBits);

		for (Entry<String, Integer> ss : params.stateSizes.entrySet()) {
			StateParameter sp = simulation.getStateParameter(ss.getKey());
			double value = sp.getValue(theAgent);
			int size = ss.getValue();
			inputCode.add(quantize(value, size), size);
		}

		return inputCode;
	}

	private static int quantize(double val, int bits) {
		int max = (1 << bits) - 1;
		double doubleVal = val * max;
		int intVal = (int) Math.round(doubleVal);
		return intVal;
	}

	private static double dequantize(int val, int bits) {
		int max = (1<< bits) -1;
		double doubleVal = (double) val / max;
		return doubleVal;
	}

	@Override
	public GeneticController createChildAsexual() {
		GeneticController child = new GeneticController(this);
		return child;
	}

	@Override
	public GeneticController createChildSexual(Controller parent2) {
		if (!(parent2 instanceof GeneticController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}
		GeneticController p2 = (GeneticController) parent2;

		GeneticController child = new GeneticController(this, p2);
		return child;
	}

	/** return the measure of similiarity between this agent and the 'other'
	 ranging from 0.0 to 1.0 (identical)

	 */
	public double similarity(GeneticController other) {
		return ga.similarity(other.ga);
	}

}