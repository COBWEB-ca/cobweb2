package eventlearning;

import cwcore.ComplexAgent;

public class EnergyChangeOccurrence extends Occurrence {

	private int amountChanged;

	public EnergyChangeOccurrence(ComplexAgent target, int amountChanged, String desc) {
		this(target, amountChanged, desc, 0);
	}

	public EnergyChangeOccurrence(ComplexAgent target, float detectableDistance, String desc, int amountChanged) {
		super(target, detectableDistance, desc);
		this.amountChanged = amountChanged;
	}

	@Override
	public MemorableEvent effect(ComplexAgent concernedAgent) {
		int originalEnergy = concernedAgent.getEnergy();
		concernedAgent.changeEnergy(amountChanged);
		float magnitude = (float) amountChanged / (float) originalEnergy;
		return new MemorableEvent(time, magnitude, "energyChange");
	}

	public int getAmountChanged() {
		return amountChanged;
	}
}