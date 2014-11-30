package org.cobweb.cobweb2.eventlearning;

import org.cobweb.cobweb2.core.ComplexAgentLearning;

public class EnergyChangeOccurrence extends Occurrence {

	private int amountChanged;

	public EnergyChangeOccurrence(ComplexAgentLearning target, int amountChanged, String desc) {
		this(target, amountChanged, desc, 0);
	}

	public EnergyChangeOccurrence(ComplexAgentLearning target, float detectableDistance, String desc, int amountChanged) {
		super(target, detectableDistance, desc);
		this.amountChanged = amountChanged;
	}

	@Override
	public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
		int originalEnergy = concernedAgent.getEnergy();
		concernedAgent.changeEnergy(amountChanged);
		float magnitude = (float) amountChanged / (float) originalEnergy;
		return new MemorableEvent(time, magnitude, "energyChange");
	}

	public int getAmountChanged() {
		return amountChanged;
	}
}