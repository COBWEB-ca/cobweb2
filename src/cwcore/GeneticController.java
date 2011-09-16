/**
 *
 */
package cwcore;

import java.io.Serializable;

import cobweb.Controller;
import cobweb.params.CobwebParam;
import cwcore.ComplexAgent.SeeInfo;

public class GeneticController implements cobweb.Controller, Serializable{
	private static final long serialVersionUID = 8777222590971142868L;

	//true, false, file not found
	protected final static int TURN_LEFT = 0;
	protected final static int TURN_RIGHT = 1;
	protected final static int MOVE_STRAIGHT = 2;
	protected final static int NONE = 3;

	BehaviorArray ga;
	int memorySize;
	int commSize;

	public static final int INPUT_BITS = 8;

	public static final int OUTPUT_BITS = 2;

	public static final int ENERGY_THRESHOLD = 160;

	private GeneticControllerParams params;

	public GeneticController() {
		// Nothing
	}

	private GeneticController(BehaviorArray g, int memory) {
		memorySize = memory;
		ga = g;
	}

	public void addClientAgent(cobweb.Agent a) {
		// Nothing
	}

	/**
	 * Given the agent's energy, get the amount of energy to add to the array.
	 * @param energy The agent's energy.
	 */
	protected int getEnergy(int energy) {
		final int maxEnergy = 3;

		if(energy > ENERGY_THRESHOLD) {
			return maxEnergy;
		} else {
			return (int) ((double) energy / (ENERGY_THRESHOLD) * 4.0);
		}
	}

	/**
	 * Given an agent, control the agent.
	 * @param baseAgent The agent.
	 */
	public void controlAgent(cobweb.Agent baseAgent) {
		ComplexAgent theAgent = (ComplexAgent) baseAgent;

		BitField inputCode = getInputArray(theAgent);

		int[] outputArray = ga.getOutput(inputCode.intValue());

		int actionCode = outputArray[0];
		theAgent.setMemoryBuffer(outputArray[1]);
		theAgent.setCommOutbox(outputArray[2]);
		//whether to breed 
		theAgent.setAsexFlag(outputArray[3] != 0);

		theAgent.setCommInbox(0);

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
		inputCode.add(theAgent.getIntFacing(), 2);

		//add the viewing info to the array
		SeeInfo get = theAgent.distanceLook();
		inputCode.add(get.getType(), 2);
		inputCode.add(get.getDist(), 2);

		//add the memory buffer to the array
		inputCode.add(theAgent.getMemoryBuffer(), memorySize);

		//add the communications to the array
		inputCode.add(theAgent.getCommInbox(), commSize);

		return inputCode;
	}

	public CobwebParam getParams() {
		return params;
	}

	public void removeClientAgent(cobweb.Agent a) {
		// Nothing
	}
	public void setupFromEnvironment(int memory, int comm, CobwebParam params) {
		memorySize = memory;
		commSize = comm;
		this.params = (GeneticControllerParams) params;
		int[] outputArray = { OUTPUT_BITS, memorySize, commSize, 1 };
		ga = new BehaviorArray(INPUT_BITS + memorySize + commSize,
				outputArray);
		ga.randomInit(this.params.randomSeed);
	}
	public void setupFromParent(Controller parent, float mutationRate) {
		if (!(parent instanceof GeneticController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}

		GeneticController p = (GeneticController) parent;
		ga = p.ga.copy(mutationRate);
		memorySize = p.memorySize;
	}

	/** sexual reproduction
	 * @param parent1 first parent
	 * @param parent2 second parent
	 * @param mutationRate mutation rate
	 */
	public void setupFromParents(Controller parent1, Controller parent2, float mutationRate) {
		if (!(parent1 instanceof GeneticController) || !(parent2 instanceof GeneticController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}

		GeneticController p = (GeneticController) parent1;
		GeneticController p2 = (GeneticController) parent2;
		ga = p.ga.splice(p2.ga).copy(mutationRate);
		memorySize = p.memorySize;
	}

	/** return the measure of similiarity between this agent and the 'other'
	 ranging from 0.0 to 1.0 (identical)

	 */
	public double similarity(GeneticController other) {
		return ga.similarity(other.ga);
	}

	public double similarity(int other) {
		return ga.similarity(other);
	}

	public GeneticController splice(GeneticController other) {
		return new GeneticController(ga.splice(other.ga), memorySize);
	}

}