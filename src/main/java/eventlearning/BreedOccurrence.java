package eventlearning;

import cwcore.ComplexAgentLearning;

public class BreedOccurrence extends Occurrence {

	public BreedOccurrence(ComplexAgentLearning target, float detectableDistance, String desc) {
		super(target, detectableDistance, desc);
	}

	@Override
	public MemorableEvent effect(ComplexAgentLearning concernedAgent) {

		return null;
	}
}