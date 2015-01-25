package org.cobweb.cobweb2.impl.learning;

import org.cobweb.cobweb2.core.Cause;


public class EnergyChangeOccurrence extends Occurrence {

	private int amountChanged;
	private Cause cause;

	public EnergyChangeOccurrence(ComplexAgentLearning target, long time, int amountChanged, Cause cause) {
		this(target, time, 0, cause, amountChanged);
	}

	public EnergyChangeOccurrence(ComplexAgentLearning target, long time, float detectableDistance, Cause cause, int amountChanged) {
		super(target, time, detectableDistance, cause.getName());
		this.cause = cause;
		this.amountChanged = amountChanged;
	}

	@Override
	public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
		int originalEnergy = concernedAgent.getEnergy();
		concernedAgent.changeEnergy(amountChanged, cause);
		float magnitude = (float) amountChanged / (float) originalEnergy;
		return new MemorableEvent(time, magnitude, "energyChange");
	}

	public int getAmountChanged() {
		return amountChanged;
	}
}