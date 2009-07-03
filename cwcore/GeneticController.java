/**
 *
 */
package cwcore;

import cobweb.Controller;
import cwcore.ComplexAgent.lookPair;

public class GeneticController implements cobweb.Controller {
	BehaviorArray ga;
	int memorySize;
	int commSize;

	public void addClientAgent(cobweb.Agent a) {
	}

	public void removeClientAgent(cobweb.Agent a) {
	}

	// for asexual reproduction
	public GeneticController(Controller parent, float mutationRate) {
		if (!(parent instanceof GeneticController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}
		GeneticController p = (GeneticController) parent;
		ga = p.ga.copy(mutationRate);
		memorySize = p.memorySize;
	}

	// sexual reproduction
	public GeneticController(Controller parent, Controller parent2, float mutationRate) {
		if (!(parent instanceof GeneticController) || !(parent2 instanceof GeneticController)) {
			throw new RuntimeException("Parent's controller type must match the child's");
		}
		GeneticController p = (GeneticController) parent;
		GeneticController p2 = (GeneticController) parent2;
		ga = p.ga.splice(p2.ga).copy(mutationRate);
		memorySize = p.memorySize;
	}

	// return the measure of similiarity between this agent and the 'other'
	// ranging from 0.0 to 1.0 (identical)
	public double similarity(GeneticController other) {
		return ga.similarity(other.ga);
	}

	public double similarity(int other) {
		return ga.similarity(other);
	}

	// Creating genetic code from scratch
	public GeneticController(int memory, int comm) {
		memorySize = memory;
		commSize = comm;
		int[] outputArray = { OUTPUT_BITS, memorySize, commSize, 1 };
		ga = new BehaviorArray(INPUT_BITS + memorySize + commSize,
				outputArray);
		ga.init();
	}

	// initialize behaviour array and memory
	public GeneticController(BehaviorArray g, int memory) {
		memorySize = memory;
		ga = g;
	}

	public static final int INPUT_BITS = 8;
	public static final int OUTPUT_BITS = 2;
	public static final int ENERGY_THRESHOLD = 160;

	public void controlAgent(cobweb.Agent baseAgent) {
		ComplexAgent theAgent = (ComplexAgent) baseAgent;

		BitField inputCode = new BitField();

		if (theAgent.getEnergy() > ENERGY_THRESHOLD)
			inputCode.add(3, 2);
		else
			inputCode.add((int) ((double) theAgent.getEnergy()
					/ (ENERGY_THRESHOLD) * 4.0), 2);

		inputCode.add(theAgent.getIntFacing(), 2);

		lookPair get = theAgent.distanceLook();
		int type = get.getType();
		int dist = get.getDist();
		inputCode.add(type, 2);
		inputCode.add(dist, 2);

		inputCode.add(theAgent.getMemoryBuffer(), memorySize);
		inputCode.add(theAgent.getCommInbox(), commSize);

		int[] outputArray = ga.getOutput(inputCode.intValue());

		int actionCode = outputArray[0];
		theAgent.setMemoryBuffer(outputArray[1]);
		theAgent.setCommOutbox(outputArray[2]);
		theAgent.setAsexFlag(outputArray[3] != 0);

		theAgent.setCommInbox(0);

		switch (actionCode) {
			case 0:
				theAgent.turnLeft();
				break;
			case 1:
				theAgent.turnRight();
				break;
			case 2:
			case 3:
				theAgent.step();
		}

	}

	public GeneticController splice(GeneticController other) {
		return new GeneticController(ga.splice(other.ga), memorySize);
	}

}