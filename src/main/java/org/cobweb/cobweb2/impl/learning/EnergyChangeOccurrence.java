package org.cobweb.cobweb2.impl.learning;


public class EnergyChangeOccurrence extends Occurrence {

	private int amountChanged;

	public EnergyChangeOccurrence(ComplexAgentLearning target, long time, int amountChanged, String desc) {
		this(target, time, 0, desc, amountChanged);
	}

	public EnergyChangeOccurrence(ComplexAgentLearning target, long time, float detectableDistance, String desc, int amountChanged) {
		super(target, time, detectableDistance, desc);
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