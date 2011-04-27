/**
 *
 */
package cwcore;

import java.io.Serializable;

import cobweb.Controller;
import cobweb.params.CobwebParam;
import cwcore.ComplexAgent.SeeInfo;

public class GeneticController implements cobweb.Controller, Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 8777222590971142868L;

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

	public void controlAgent(cobweb.Agent baseAgent) {
		ComplexAgent theAgent = (ComplexAgent) baseAgent;

		BitField inputCode = new BitField();

		if (theAgent.getEnergy() > ENERGY_THRESHOLD)
			inputCode.add(3, 2);
		else
			inputCode.add((int) ((double) theAgent.getEnergy()
					/ (ENERGY_THRESHOLD) * 4.0), 2);

		inputCode.add(theAgent.getIntFacing(), 2);

		SeeInfo get = theAgent.distanceLook();
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
				//Impulse to turn left; may or may not do so based on its memories
				theAgent.queue(new ComplexAgent.SmartAction(theAgent, "turnLeft") {
					@Override
					void desiredAction(ComplexAgent agent) {
						agent.turnLeft();
					}

					@Override
					public boolean eventIsRelated(ComplexAgent.MemorableEvent event) {
						return event.getDescription().substring(0, 4).equalsIgnoreCase("turn");
					}

					@Override
					public float getMagnitudeFromEvent(ComplexAgent.MemorableEvent event) {
						//If memory has to do with turning right, the opposite sign of the magnitude of that
						//event applies (if it is good to turn LEFT, it is bad to turn RIGHT sorta logic)
						if (event.getDescription().equals("turnRight")) {
							return event.getMagnitude() * -0.5f;
						}
						return super.getMagnitudeFromEvent(event);
					}					

				});
				//theAgent.turnLeft();
				break;
			case 1:
				//Impulse to turn right; may or may not do so based on its memories
				theAgent.queue(new ComplexAgent.SmartAction(theAgent, "turnRight") {
					@Override
					void desiredAction(ComplexAgent agent) {
						agent.turnRight();

					}

					@Override
					public boolean eventIsRelated(ComplexAgent.MemorableEvent event) {
						return event.getDescription().substring(0, 4).equalsIgnoreCase("turn");
					}

					@Override
					public float getMagnitudeFromEvent(ComplexAgent.MemorableEvent event) {
						//If memory has to do with turning left, the opposite sign of the magnitude of that
						//event applies (if it is good to turn RIGHT, it is bad to turn LEFT.)						
						if (event.getDescription().equals("turnLeft")) {
							return event.getMagnitude() * -0.5f;
						}
						return super.getMagnitudeFromEvent(event);
					}
				});
				//theAgent.turnRight();
				break;
			case 2:
			case 3:
				theAgent.step();
		}

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