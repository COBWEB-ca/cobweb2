package eventlearning;

import cwcore.ComplexAgent;

public class BreedOccurrence extends Occurrence {

	public BreedOccurrence(ComplexAgent target, float detectableDistance, String desc) {
		super(target, detectableDistance, desc);
	}

	@Override
	public MemorableEvent effect(ComplexAgent concernedAgent) {

		return null;
	}
}